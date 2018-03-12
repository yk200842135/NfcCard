/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.reformer.cardemulate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.reformer.cardemulate.event.HttpReponseEvent;
import com.reformer.cardemulate.event.OpenEvent;
import com.reformer.cardemulate.event.SettingEvent;
import com.reformer.cardemulate.util.ByteUtils;
import com.reformer.nfclibrary.RfNfcKey;
import com.squareup.seismic.ShakeDetector;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;

import cn.com.reformer.rfBleService.BleDevContext;
import cn.com.reformer.rfBleService.BleService;
import cn.com.reformer.rfBleService.OnCompletedListener;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.SENSOR_SERVICE;

/**
 * Generic UI for sample discovery.
 */
public class CardEmulationFragment extends Fragment implements ShakeDetector.Listener{

    public static final String TAG = "CardEmulationFragment";
    private ImageView iv_card;
    private TextView tv_result;
    private ListView lsv_door;
    private ArrayAdapter<String> adapter;
    private boolean isCardA;
    private boolean hasChange;
    private BleService.RfBleKey mRfBleKey;
    private BleService mService;
    private volatile boolean isOpening = false;
    private long openTime;
    private long shakeTime;
    private SensorManager sensorManager;
    private ShakeDetector sd;
    private ArrayList<byte[]> whiteList = new ArrayList<>();
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder rawBinder) {
            if (rawBinder instanceof BleService.LocalBinder) {
                mService = ((BleService.LocalBinder) rawBinder).getService();
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
                        .getDefaultAdapter();
                if (mBluetoothAdapter.isEnabled()) {
                    mRfBleKey = mService.getRfBleKey();
                    mRfBleKey.init(whiteList);
                    mRfBleKey.setOnCompletedListener(onCompletedListener);
                    if (getActivity() != null) {
                        EventBus.getDefault().post(new SettingEvent(SettingEvent.SHAKE, AccountStorage.GetShake(getActivity().getApplication())));
                        EventBus.getDefault().post(new SettingEvent(SettingEvent.FEEL, AccountStorage.GetFeel(getActivity().getApplication())));
                    }
                }else {
                    Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(mIntent, 1);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };

    private OnCompletedListener onCompletedListener = new OnCompletedListener() {
        @Override
        public void OnCompleted(byte[] bytes, int i) {
            isOpening = false;
            switch (i){
                case 0:
                    EventBus.getDefault().post(new OpenEvent("开门成功"));
                    break;
                case 1:
                    EventBus.getDefault().post(new OpenEvent("开门密码错误"));
                    break;
                case 2:
                    EventBus.getDefault().post(new OpenEvent("通讯异常断开"));
                    break;
                case 3:
                    EventBus.getDefault().post(new OpenEvent("开门重试超时"));
                    break;
            }
        }
    };

    /** Called when sample is created. Displays generic UI with welcome text. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent bindIntent = new Intent(getContext().getApplicationContext(), BleService.class);
        whiteList.add(new byte[]{0x32,0x4A,0x34,0x39,0x67,0x54,0x38,0x36,0x59});
        whiteList.add(new byte[]{0x32,0x46,0x52,0x42,0x35,0x32,0x58,0x72,0x48});
        getContext().getApplicationContext().bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        RfNfcKey.getInstance().setCardId(AccountStorage.GetAccount(getActivity()));
        RfNfcKey.getInstance().setPassword(AccountStorage.GetKey(getActivity()));
        RfNfcKey.getInstance().setEnable(!AccountStorage.GetToken(getActivity()).equals(""));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.main_fragment, container, false);
        TextView tv_version = (TextView)v.findViewById(R.id.tv_version);
        iv_card = (ImageView)v.findViewById(R.id.iv_card);
        tv_result = (TextView) v.findViewById(R.id.tv_result);
        lsv_door = (ListView) v.findViewById(R.id.lsv_door);
        tv_version.setText("版本:"+getVersionName(getContext()));
        isCardA = true;
        iv_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnimator1();
            }
        });
        v.findViewById(R.id.btn_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshDoors();
            }
        });
        adapter = new ArrayAdapter<String>(getActivity(),R.layout.lsv_door_item);
        lsv_door.setAdapter(adapter);
        lsv_door.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               opendoor(ByteUtils.stringToBytes(adapter.getItem(position).substring(0,18)));
            }
        });
        return v;
    }

    @Override
    public void onStart(){
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onDestroy(){
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (sd != null) {
            sd.stop();
        }
        if(mRfBleKey != null){
            mRfBleKey.setOnBleDevLisFeelListener(null);
        }
        getActivity().getApplication().unbindService(mServiceConnection);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(OpenEvent event) {
        if (event != null && event.getContent() != null) {
            tv_result.setText(event.getContent());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(HttpReponseEvent event) {
        if (event != null && event.getContent() != null) {
            tv_result.setText(tv_result.getText() + "\r\n"+event.getContent());
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSettingEvent(SettingEvent event) {
        if (event != null && event.getObj() != null) {
            if (event.getObj().equals(SettingEvent.SHAKE)){
                if (event.isChecked()){
                    Toast.makeText(getActivity(),"启用摇一摇开门",Toast.LENGTH_SHORT).show();
                    sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
                    sd = new ShakeDetector(this);
                    sd.start(sensorManager);
                }else{
                    if (sd != null) {
                        Toast.makeText(getActivity(),"停用摇一摇开门",Toast.LENGTH_SHORT).show();
                        sd.stop();
                    }
                }

            }else if (event.getObj().equals(SettingEvent.FEEL)){
                if (event.isChecked()) {
                    startFeelOpen();
                }else{
                    stopFeelOpen();
                }
            }
        }
    }

    @Override
    public void hearShake() {
        if (!isOpening &&( shakeTime == 0 || System.currentTimeMillis() - shakeTime > 2000)) {
            shakeTime = System.currentTimeMillis();
            byte[] mac = getMaxRssiDevice();
            if (mac != null) {
                opendoor(mac);
            }else{
                Toast.makeText(getActivity(),"摇一摇没有找到设备",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                mRfBleKey = mService.getRfBleKey();
                mRfBleKey.init(whiteList);
                mRfBleKey.setOnCompletedListener(onCompletedListener);
            }
        }
    }

    private void startAnimator1(){
        hasChange = false;
        iv_card.setClickable(false);
        final ValueAnimator animator = ObjectAnimator.ofFloat(iv_card,"rotationY",isCardA ? 0 : 180, isCardA ? 180 : 360);
        animator.setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (!hasChange){
                    if (isCardA){
                        if ((Float)animation.getAnimatedValue() >= 90) {
                            iv_card.setImageResource(R.mipmap.rfcard2);
                            hasChange = true;
                            isCardA = !isCardA;
                        }
                    }else{
                        if ((Float)animation.getAnimatedValue() >= 270){
                            iv_card.setImageResource(R.mipmap.rfcard1);
                            hasChange = true;
                            isCardA = !isCardA;
                        }
                    }
                }
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                iv_card.setClickable(true);
            }
        });
        animator.start();
    }

    private void opendoor(byte[] mac){
        if (mRfBleKey == null || isOpening || mac == null)
            return;
        int ret = mRfBleKey.openDoor(mac
                ,"0000"+AccountStorage.GetAccount(getContext().getApplicationContext())
                ,50);
        if (0 == ret) {
            isOpening = true;
            openTime = System.currentTimeMillis();
            tv_result.setText(R.string.openning);
        }else if (1 == ret){
            tv_result.setText(R.string.error_param);
        }else if (2 == ret){

            tv_result.setText(R.string.wait_please);
        }else if (3 == ret){
            tv_result.setText(R.string.device_invalid);
        }
    }


    private void refreshDoors(){
        if (mRfBleKey!=null){
            //Scan dev list
            ArrayList<BleDevContext> lst = mRfBleKey.getDiscoveredDevices();
            adapter.clear();
            for (BleDevContext dev:lst){
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(ByteUtils.bytesToString(dev.mac))
                        .append(" (").append(dev.rssi).append(")");
                adapter.add(stringBuffer.toString().toUpperCase());
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void startFeelOpen(){
        if(mRfBleKey != null){
            Toast.makeText(getActivity(),"启用感应开门",Toast.LENGTH_SHORT).show();
            mRfBleKey.setLiveLife(5000);
            mRfBleKey.setRemoveInterval(3000);
            mRfBleKey.setOnBleDevLisFeelListener(new BleService.OnBleDevListFeelListener() {
                @Override
                public void onFeelDev(final BleDevContext bleDevContext) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),"感应到->"+ByteUtils.bytesToString(bleDevContext.mac)+"_"+bleDevContext.rssi,Toast.LENGTH_SHORT).show();
                            opendoor(bleDevContext.mac);
                        }
                    });
                }
            });
        }
    }

    private void stopFeelOpen(){
        if(mRfBleKey != null){
            Toast.makeText(getActivity(),"停用感应开门",Toast.LENGTH_SHORT).show();
            mRfBleKey.setOnBleDevLisFeelListener(null);
        }
    }

    private byte[] getMaxRssiDevice(){
        if (mRfBleKey!=null) {
            //Scan dev list bsfx
            ArrayList<BleDevContext> lst = mRfBleKey.getDiscoveredDevices();
            Iterator<BleDevContext> it = lst.iterator();
            int maxRssi = -97;
            BleDevContext selectBdc = null;
            while (it.hasNext()){
                BleDevContext bdc = it.next();
                if (bdc.rssi > maxRssi) {
                    maxRssi = bdc.rssi;
                    selectBdc = bdc;
                }
            }
            return selectBdc == null ? null : selectBdc.mac;
        }
        return null;
    }

    /**
     * 获取本地Apk版本名称
     *
     * @param context 上下文
     * @return String
     */
    public static String getVersionName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }
}
