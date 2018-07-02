package com.example.wisdompark19;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.base.bj.paysdk.utils.TrPay;
import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.Mine.MineLoginActivity;
import com.tencent.bugly.Bugly;

/**
 * Created by ROBOSOFT on 2018/4/18.
 */

public class SplashActivity extends AppCompatActivity {

    private final static String channel = "360";//应用商店渠道名(如：360，小米、华为)
    private final static String appkey = "c45a799b4c2d4b80a361eacd228ef79e";//应用AppKey

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化PaySdk(Context请传入当前Activity对象)
        TrPay.getInstance(SplashActivity.this).initPaySdk(appkey, channel);

       // Bmob.initialize(SplashActivity.this,"17f7847bf93a59bff2a1dfa47c14dc9c");

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if(!SharePreferences.getString(SplashActivity.this, AppConstants.USER_PHONE).isEmpty()){
            IntoMain();
        }else {
            IntoLogin();
        }
    }
    private void IntoMain(){
        Intent intent = new Intent(SplashActivity.this,MainActivity.class);
        startActivity(intent);
      //  finish();
    }
    private void IntoLogin(){
        Intent intent = new Intent(SplashActivity.this,MineLoginActivity.class);
        startActivity(intent);
      //  finish();
    }


}
