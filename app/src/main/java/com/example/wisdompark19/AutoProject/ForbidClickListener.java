package com.example.wisdompark19.AutoProject;

import android.view.View;

import java.util.Calendar;

/**
 * Created by ROBOSOFT on 2018/4/19.
 */

public abstract class ForbidClickListener implements View.OnClickListener{
    private static final int MIN_TIME = 3000;
    private long lastTime = 0;

    @Override
    public void onClick(View v){
        long cTime = Calendar.getInstance().getTimeInMillis();
        if(cTime - lastTime > MIN_TIME){
            lastTime = cTime;
            forbidClick(v);
        }
    }

    protected abstract void forbidClick(View v);
}
