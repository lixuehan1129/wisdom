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
import com.iflytek.cloud.SpeechUtility;
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

        // 应用程序入口处调用，避免手机内存过小，杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
        // 如在Application中调用初始化，需要在Mainifest中注册该Applicaiton
        // 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
        // 参数间使用半角“,”分隔。
        // 设置你申请的应用appid,请勿在'='与appid之间添加空格及空转义符
        // 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误
        SpeechUtility.createUtility(SplashActivity.this, "appid=" + getString(R.string.app_id));
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
