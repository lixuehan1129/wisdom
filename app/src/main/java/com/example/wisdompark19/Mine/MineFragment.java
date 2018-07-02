package com.example.wisdompark19.Mine;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.DealBitmap;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.R;
import com.example.wisdompark19.ViewHelper.BaseFragment;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by 最美人间四月天 on 2018/1/9.
 */

public class MineFragment extends BaseFragment implements View.OnClickListener {
    private LocalBroadcastManager broadcastManager;
    private IntentFilter intentFilter;
    private BroadcastReceiver mReceiver;

    public static MineFragment newInstance(String info) {
        Bundle args = new Bundle();
        args.putString("info", info);
        MineFragment mineFragment = new MineFragment();
        mineFragment.setArguments(args);
        return mineFragment;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        broadcastManager.unregisterReceiver(mReceiver);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.minefragment, null);
        Toolbar mToolbar = (Toolbar)view.findViewById(R.id.minefragment_mainTool);
        mToolbar.setTitle("我的");
      //  findView(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findView(getView());
        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstants.BROAD_CON);
       // intentFilter.addAction(AppConstants.BROAD_LOGIN);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent){
                //收到广播后所作的操作
                findView(getView());
                System.out.println("收广播呀");
            }
        };
        broadcastManager.registerReceiver(mReceiver, intentFilter);
    }

    private void findView(View view){
        CircleImageView minefragment_picture = (CircleImageView) view.findViewById(R.id.minefragment_picture);
        TextView minefragment_name = (TextView) view.findViewById(R.id.minefragment_name);
        TextView minefragment_address = (TextView) view.findViewById(R.id.minefragment_address);
        TextView minefragment_phone = (TextView) view.findViewById(R.id.minefragment_phone);
        TextView minefragment_ziliao = (TextView) view.findViewById(R.id.minefragment_ziliao);
        TextView minefragment_recode = (TextView) view.findViewById(R.id.minefragment_recode);
        TextView minefragment_setting = (TextView) view.findViewById(R.id.minefragment_setting);
        TextView minefragment_fankui = (TextView) view.findViewById(R.id.minefragment_fankui);
        TextView minefragment_back = (TextView) view.findViewById(R.id.minefragment_back);
        String imageBase64 = SharePreferences.getString(getActivity(),AppConstants.USER_PICTURE);
        Bitmap user_bitmap = DealBitmap.StringToBitmap(imageBase64);
        if(user_bitmap != null){
            minefragment_picture.setImageBitmap(user_bitmap);
        }else {
            minefragment_picture.setImageResource(R.mipmap.ic_launcher_round);
        }
        minefragment_name.setText(SharePreferences.getString(getActivity(),AppConstants.USER_NAME));//姓名
        minefragment_address.setText(SharePreferences.getString(getActivity(),AppConstants.USER_ADDRESS));//地址
        minefragment_phone.setOnClickListener(this);
        minefragment_ziliao.setOnClickListener(this);
        minefragment_recode.setOnClickListener(this);
        minefragment_fankui.setOnClickListener(this);
        minefragment_setting.setOnClickListener(this);
        minefragment_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.minefragment_phone:{
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(SharePreferences.getString(getActivity(),AppConstants.USER_PHONE) +
                "\n" + SharePreferences.getString(getActivity(),AppConstants.USER_ADDRESS))
                        .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                break;
            }
            case R.id.minefragment_ziliao:{
                Intent intent = new Intent(getActivity(),MineChangeActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.minefragment_recode:{
                Intent intent = new Intent(getActivity(),MineRecodeActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.minefragment_setting:{
//                Intent intent = new Intent(getActivity(),MineRegistAddActivity.class);
//                intent.putExtra("put_data_regist_add","17888836862");
//                intent.putExtra("put_data_regist_select","regist");
//                startActivity(intent);
                break;
            }
            case R.id.minefragment_fankui:{
                startActivity(new Intent(getActivity(),MineRequireActivity.class));
                break;
            }
            case R.id.minefragment_back:{
                showNormalDialog();
                break;
            }
        }
    }

    private void showNormalDialog(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(getActivity());
        normalDialog.setMessage("确定退出？");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharePreferences.clear(getActivity());
                        getActivity().finish();
                        Intent intent = new Intent(getActivity(),MineLoginActivity.class);
                        intent.putExtra("put_data_login","登录");
                        startActivity(intent);
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        // 显示
        normalDialog.show();
    }
}
