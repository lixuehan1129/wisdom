package com.example.wisdompark19.Mine;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.R;
import com.mysql.jdbc.Connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by ROBOSOFT on 2018/4/18.
 */

public class MineRecodeActivity extends AppCompatActivity{
    private TextInputLayout mine_recode_old_layout;
    private TextInputLayout mine_recode_new1_layout;
    private TextInputLayout mine_recode_new2_layout;
    private TextInputEditText mine_recode_old;
    private TextInputEditText mine_recode_new1;
    private TextInputEditText mine_recode_new2;
    private Button mine_recode_ok;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mine_recode);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue)); //设置顶部系统栏颜色
        Toolbar toolbar = (Toolbar)findViewById(R.id.mine_recode_mainTool); //标题栏
        toolbar.setNavigationIcon(R.mipmap.ic_back_white);
        toolbar.setTitle("修改密码");
        back(toolbar);
        findView();
    }

    private void findView(){
        mine_recode_old_layout = (TextInputLayout)findViewById(R.id.mine_recode_old_layout);
        mine_recode_new1_layout = (TextInputLayout)findViewById(R.id.mine_recode_new1_layout);
        mine_recode_new2_layout = (TextInputLayout)findViewById(R.id.mine_recode_new2_layout);
        mine_recode_old = (TextInputEditText) findViewById(R.id.mine_recode_old);
        mine_recode_new1 = (TextInputEditText) findViewById(R.id.mine_recode_new1);
        mine_recode_new2 = (TextInputEditText) findViewById(R.id.mine_recode_new2);
        mine_recode_ok = (Button)findViewById(R.id.mine_recode_button);

/*
        * 密码输入监听
        * */
        mine_recode_new1.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mine_recode_new1_layout.setErrorEnabled(false);
            }
            @Override
            public void afterTextChanged(Editable s) {
                String password = mine_recode_new1.getText().toString();
                String password_again = mine_recode_new1.getText().toString();
                if(password.length()>5){
                    if(password_again.equals(password)){
                        mine_recode_new1_layout.setErrorEnabled(false);
                    }else {
                        if(password_again.length()>0)
                        {
                            mine_recode_new1_layout.setError("两次密码输入不一致");
                        }
                    }}else {
                    mine_recode_new1_layout.setError("密码错误(不少于6位)");
                }
            }
        });

        /*
        * 再次输入密码监听
        * */
        mine_recode_new2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mine_recode_new2_layout.setErrorEnabled(false);
            }
            @Override
            public void afterTextChanged(Editable s) {
                String password = mine_recode_new2.getText().toString();
                String password_again = mine_recode_new2.getText().toString();
                if(!password_again.equals(password)){
                    mine_recode_new2_layout.setError("两次密码输入不一致");
                }
            }
        });

        mine_recode_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mine_recode_old.getText().toString().length()>5 &&
                        mine_recode_new1.getText().toString().length()>5 &&
                        mine_recode_new2.getText().toString().length()>5){
                    if(mine_recode_new1.getText().toString().equals(mine_recode_new2.getText().toString())){
                        uploadPassword();
                    }else {
                        Toast.makeText(MineRecodeActivity.this,"两次密码不同",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(MineRecodeActivity.this,"密码格式不正确",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadPassword(){
        new  Thread() {
            public void run() {
                try {
                    Looper.prepare();//用于toast
                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
                    if (conn != null) { //判断 如果返回不为空则说明链接成功 如果为null的话则连接失败 请检查你的 mysql服务器地址是否可用 以及数据库名是否正确 并且 用户名跟密码是否正确
                        Log.d("调试", "连接成功");
                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
                        //查找手机号是否已存在
                        String sql_pass = "select user_password from user where user_phone = '" +
                                SharePreferences.getString(MineRecodeActivity.this, AppConstants.USER_PHONE) +
                                "'";
                        ResultSet resultSet_old = stmt.executeQuery(sql_pass);
                        resultSet_old.next();
                        String old_pass = resultSet_old.getString("user_password");
                        resultSet_old.close();
                        if(old_pass.equals(mine_recode_old.getText().toString())){
                            String sql_new = "update user set user_password = '" +
                                    mine_recode_new1.getText().toString() +
                                    "' where user_phone = '" +
                                    SharePreferences.getString(MineRecodeActivity.this, AppConstants.USER_PHONE) +
                                    "'";
                            stmt.execute(sql_new);
                        }else {
                            Toast.makeText(MineRecodeActivity.this, "旧密码不正确", Toast.LENGTH_SHORT).show();
                        }
                        JDBCTools.releaseConnection(stmt,conn);
                        MineRecodeActivity.this.finish();
                    } else {
                        Log.d("调试", "连接失败");
                        Toast toast = Toast.makeText(MineRecodeActivity.this, "请检查网络", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
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
