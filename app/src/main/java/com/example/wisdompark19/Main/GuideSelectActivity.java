package com.example.wisdompark19.Main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.AutoProject.TimeChange;
import com.example.wisdompark19.R;
import com.mysql.jdbc.Connection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by ROBOSOFT on 2018/6/5.
 */

public class GuideSelectActivity extends AppCompatActivity {

    private EditText select_que;
    private EditText select_ans;
    private int select_id;
    private String guide_que;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_guide_select);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue)); //设置顶部系统栏颜色
        Intent intent = getIntent();
        guide_que = intent.getStringExtra("guide_select_que");
        select_id = intent.getIntExtra("guide_select",0);
        Toolbar toolbar = (Toolbar)findViewById(R.id.guide_select_mainTool); //标题栏
        toolbar.setNavigationIcon(R.mipmap.ic_back_white);
        toolbar.setTitle("咨询中心");
        back(toolbar);
        findView();
    }

    private void findView(){
        Button button = (Button) findViewById(R.id.guide_select_ok);
        select_que = (EditText) findViewById(R.id.guide_select_que);
        select_ans = (EditText) findViewById(R.id.guide_select_ans);
        if(select_id == 0){
            select_ans.setVisibility(View.INVISIBLE);
        }else {
            select_que.setEnabled(false);
            select_que.setText(guide_que);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(select_id == 0){
                    if(select_que.getText().toString().isEmpty()){
                        Toast.makeText(GuideSelectActivity.this,"请输入内容",Toast.LENGTH_LONG).show();
                    }else {
                        //上传
                        upload();
                    }
                }else {
                    if(select_ans.getText().toString().isEmpty()){
                        Toast.makeText(GuideSelectActivity.this,"请输入内容",Toast.LENGTH_LONG).show();
                    }else {
                        //上传
                        update();
                    }
                }
            }
        });
    }

    private void upload(){
        final ProgressDialog progressDialog = ProgressDialog.show(GuideSelectActivity.this,"","正在上传",true);
        new Thread(){
            public void run(){
                try{
                    Looper.prepare();//用于toast
                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
                    if (conn != null) {
                        Log.d("调试", "连接成功");
                        Statement stmt = conn.createStatement();
                        //上传
                        PreparedStatement preparedStatement = null;
                        String newmessage_sql_insert = "insert into guide (guide_phone,guide_area,guide_time," +
                                "guide_que) values(?,?,?,?)";
                        preparedStatement = (java.sql.PreparedStatement)conn.prepareStatement(newmessage_sql_insert,Statement.RETURN_GENERATED_KEYS);
                        preparedStatement.setString(1, SharePreferences.getString(GuideSelectActivity.this, AppConstants.USER_PHONE));
                        preparedStatement.setString(2, SharePreferences.getString(GuideSelectActivity.this, AppConstants.USER_AREA));
                        preparedStatement.setString(3, TimeChange.getBigTime());
                        preparedStatement.setString(4, select_que.getText().toString());
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        JDBCTools.releaseConnection(stmt,conn);
                        Intent intent_broad = new Intent(AppConstants.BROAD_GUIDE);
                        LocalBroadcastManager.getInstance(GuideSelectActivity.this).sendBroadcast(intent_broad);
                        progressDialog.dismiss();
                        finish();
                    }else {
                        Log.d("调试", "连接失败");
                        Toast toast = Toast.makeText(GuideSelectActivity.this, "请检查网络", Toast.LENGTH_SHORT);
                        toast.show();
                        progressDialog.dismiss();
                    }
                }catch (SQLException e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }.start();
    }

    private void update(){
        final ProgressDialog progressDialog = ProgressDialog.show(GuideSelectActivity.this,"","正在修改",true);
        new Thread(){
            public void run(){
                try{
                    Looper.prepare();//用于toast
                    Connection conn_update = JDBCTools.getConnection("shequ","Zz123456");
                    if(conn_update != null){
                        Log.d("调试", "连接成功，修改订单");
                        Statement stmt = conn_update.createStatement();
                        String update_old = "update guide set guide_ans = ? where guide_id = ?";
                        PreparedStatement preparedStatement;
                        preparedStatement = conn_update.prepareStatement(update_old);
                        preparedStatement.setString(1,select_ans.getText().toString());
                        preparedStatement.setInt(2,select_id);
                        preparedStatement.executeUpdate();//执行更新操作
                        preparedStatement.close();
                        JDBCTools.releaseConnection(stmt,conn_update);
                        Intent intent_broad = new Intent(AppConstants.BROAD_GUIDE);
                        LocalBroadcastManager.getInstance(GuideSelectActivity.this).sendBroadcast(intent_broad);
                        progressDialog.dismiss();
                        finish();
                    }
                }catch (SQLException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
                Looper.loop();
            }
        }.start();
    }


    //返回注销事件
    private void back(Toolbar toolbar){
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
