package com.reformer.cardemulate;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.reformer.cardemulate.event.SettingEvent;
import com.reformer.nfclibrary.RfNfcKey;

import org.greenrobot.eventbus.EventBus;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_preference_fragment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.prefs, new MyFragment()).commit();

    }

    public static void start(Activity activity){
        activity.startActivity(new Intent(activity,SettingsActivity.class));
    }


    public static class MyFragment extends PreferenceFragment {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_setting);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            EditTextPreference text = (EditTextPreference) findPreference("card_uid");
            CharSequence charSequence = AccountStorage.GetAccount(getActivity().getApplication());
            text.setSummary(charSequence);
            text.setText(charSequence.toString());
            text.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String str = newValue.toString().toUpperCase();
                    if (str.length() != 8){
                        Toast.makeText(getActivity(),"卡号长度错误",Toast.LENGTH_SHORT).show();
                        return false;
                    }else if (str.equals("00000000")){
                        Toast.makeText(getActivity(),"卡号不能全0",Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    for (int i = 0;i<str.length();i++){
                        if (str.charAt(i) < '0' || str.charAt(i) > 'F'
                                || (str.charAt(i) > '9' && str.charAt(i) < 'A')){
                            Toast.makeText(getActivity(),"非法卡号",Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                    if (preference.getSummary() != null && !preference.getSummary().equals(newValue.toString())) {
                        Toast.makeText(getActivity(), "修改卡号成功", Toast.LENGTH_SHORT).show();
                        preference.setSummary(newValue.toString());
                        ((EditTextPreference)preference).setText(newValue.toString());
                        AccountStorage.SetAccount(getActivity().getApplication(), newValue.toString());
                        RfNfcKey.getInstance().setCardId(newValue.toString());
                    }
                    return false;
                }
            });
            EditTextPreference text1 = (EditTextPreference) findPreference("card_key");
            CharSequence charSequence1 = AccountStorage.GetKey(getActivity().getApplication());
            text1.setSummary(charSequence1);
            text1.setText(charSequence1.toString());
            text1.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String str = newValue.toString().toUpperCase();
                    if (str.length() != 16){
                        Toast.makeText(getActivity(),"卡密长度错误",Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (preference.getSummary() != null && !preference.getSummary().equals(newValue.toString())) {
                        Toast.makeText(getActivity(), "修改卡密成功", Toast.LENGTH_SHORT).show();
                        preference.setSummary(newValue.toString());
                        ((EditTextPreference)preference).setText(newValue.toString());
                        AccountStorage.SetKey(getActivity().getApplication(), newValue.toString());
                        RfNfcKey.getInstance().setPassword(newValue.toString());
                    }
                    return false;
                }
            });
            SwitchPreference sp_shake = (SwitchPreference) findPreference("sp_shake");
            sp_shake.setChecked(AccountStorage.GetShake(getActivity().getApplication()));
            sp_shake.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ((SwitchPreference)preference).setChecked((boolean)newValue);
                    EventBus.getDefault().post(new SettingEvent(SettingEvent.SHAKE,(boolean)newValue));
                    AccountStorage.SetShake(getActivity().getApplication(), (boolean)newValue);
                    return false;
                }
            });
            SwitchPreference sp_feel = (SwitchPreference) findPreference("sp_feel");
            sp_feel.setChecked(AccountStorage.GetFeel(getActivity().getApplication()));
            sp_feel.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ((SwitchPreference)preference).setChecked((boolean)newValue);
                    EventBus.getDefault().post(new SettingEvent(SettingEvent.FEEL,(boolean)newValue));
                    AccountStorage.SetFeel(getActivity().getApplication(), (boolean)newValue);
                    return false;
                }
            });
        }
    }
}
