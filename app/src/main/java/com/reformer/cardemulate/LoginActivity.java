package com.reformer.cardemulate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.reformer.cardemulate.callback.CallbackBase;
import com.reformer.cardemulate.callback.base.ResponseBase;
import com.reformer.cardemulate.event.HttpReponseEvent;
import com.reformer.cardemulate.util.AbDateUtils;
import com.reformer.cardemulate.util.AccessToken;
import com.zhy.http.okhttp.OkHttpUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import okhttp3.Call;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private AutoCompleteTextView mUserView;
    private EditText mAuthCodeView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mSendAuthCodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUserView = (AutoCompleteTextView) findViewById(R.id.view_username);

        mAuthCodeView = (EditText) findViewById(R.id.view_password);
        mAuthCodeView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mSendAuthCodeButton = (Button) findViewById(R.id.btn_get_identifying_code);
        mSendAuthCodeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mSendAuthCodeButton.setEnabled(false);
                attemptGetAuthCode();
            }
        });
        Button mSignInButton = (Button) findViewById(R.id.btn_email_sign_in);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        if (!AccountStorage.GetToken(getApplication()).equals("")){
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(HttpReponseEvent event) {
        if (event != null && event.getContent() != null){
            showProgress(false);
           if (event.getContent().equals(getString(R.string.get_authcode_success))){
               if (event.getResponseBase() != null){
                   if (event.getResponseBase().getResult() == 200){
                       Toast.makeText(LoginActivity.this,getString(R.string.get_authcode_success),Toast.LENGTH_SHORT).show();
                   }else {
                       mAuthCodeView.setError("获取验证码失败，err:" + event.getResponseBase().getResult());
                       mSendAuthCodeButton.setEnabled(true);
                   }
               }else {
                   mAuthCodeView.setError(getString(R.string.error_json));
               }
               mAuthCodeView.requestFocus();
           }else if (event.getContent().equals(getString(R.string.error_net_connection))){
                mAuthCodeView.setError(getString(R.string.error_net_connection));
                mAuthCodeView.requestFocus();
           }else if (event.getContent().equals(getString(R.string.login_success))){
               if (event.getResponseBase() != null){
                   if (event.getResponseBase().getResult() == 200){
                       Toast.makeText(LoginActivity.this,getString(R.string.login_success),Toast.LENGTH_SHORT).show();
                       AccountStorage.SetToken(getApplicationContext(),event.getResponseBase().getResultInfo());
                       startActivity(new Intent(LoginActivity.this,MainActivity.class));
                       finish();
                   }else {
                       mAuthCodeView.setError("登录失败，err:" + event.getResponseBase().getResult());
                       //                        mSendAuthCodeButton.setEnabled(true);
                   }
               }else {
                   mAuthCodeView.setError(getString(R.string.error_json));
               }
               mAuthCodeView.requestFocus();
           }
        }
    };


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mUserView.setError(null);
        mAuthCodeView.setError(null);

        // Store values at the time of the login attempt.
        String user = mUserView.getText().toString();
        String authcode = mAuthCodeView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(authcode) && !isAuthCodeValid(authcode)) {
            mAuthCodeView.setError(getString(R.string.error_invalid_authcode));
            focusView = mAuthCodeView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(user)) {
            mUserView.setError(getString(R.string.error_phone_empty));
            focusView = mUserView;
            cancel = true;
        }else if (user.length() != 11){
            mUserView.setError(getString(R.string.error_phone_len));
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            login(user,authcode);
        }
    }

    private void attemptGetAuthCode() {
        // Reset errors..
        mUserView.setError(null);

        // Store values at the time of the login attempt.
        String user = mUserView.getText().toString();

        boolean cancel = false;
        View focusView = null;
        // Check for a valid email address.
        if (TextUtils.isEmpty(user)) {
            mUserView.setError(getString(R.string.error_phone_empty));
            focusView = mUserView;
            cancel = true;
        }else if (user.length() != 11){
            mUserView.setError(getString(R.string.error_phone_len));
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            getAuthCode(user);
        }
    }


    private boolean isAuthCodeValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void getAuthCode(String mUser){
        try {
            JSONObject param = new JSONObject();
            param.put("uid",mUser);
            param.put("time", AbDateUtils.getCurrentDate(AbDateUtils.dateFormatYMDHMS));
            OkHttpUtils.post().url("http://test.key1.cn/reformer/member/keyfree/v2/sendMsgCode.shtml")
                    .addParams("param",param.toString())
                    .addParams("accesstoken",AccessToken.generate("1000", 1, 900, mUser))
                    .build().execute(new CallbackBase() {
                @Override
                public void onError(Call call, Exception e) {
                    EventBus.getDefault().post(new HttpReponseEvent(getString(R.string.error_net_connection)));
                }

                @Override
                public void onResponse(ResponseBase response) {
                    EventBus.getDefault().post(new HttpReponseEvent(getString(R.string.get_authcode_success),response));
                }
            });
        } catch (Exception e) {
        }
    }

    private void login(String mUser, String mAuthCode){
        try {
            JSONObject param = new JSONObject();
            param.put("uid",mUser);
            param.put("authCode", mAuthCode);
            param.put("model", android.os.Build.MODEL);
            param.put("osName", "Android");
            param.put("osVersion", android.os.Build.VERSION.RELEASE);
            param.put("version", "DEMO_V1.0.0");
            param.put("build", "DEMO_BUILD");
            OkHttpUtils.post().url("http://test.key1.cn/reformer/member/keyfree/v2/getUserToken.shtml")
                    .addParams("param",param.toString())
                    .addParams("accesstoken",AccessToken.generate("1000", 1, 900, mUser))
                    .build().execute(new CallbackBase() {
                @Override
                public void onError(Call call, Exception e) {
                    EventBus.getDefault().post(new HttpReponseEvent(getString(R.string.error_net_connection)));
                }

                @Override
                public void onResponse(ResponseBase response) {
                    EventBus.getDefault().post(new HttpReponseEvent(getString(R.string.login_success),response));
                }
            });
        } catch (Exception e) {
        }
    }
}

