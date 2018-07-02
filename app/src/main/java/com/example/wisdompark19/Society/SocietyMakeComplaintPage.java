package com.example.wisdompark19.Society;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by 最美人间四月天 on 2018/4/7.
 */

public class SocietyMakeComplaintPage extends AppCompatActivity {
    private Button society_tocao_page_ok;
    private EditText society_tocao_page_content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.society_tocao_page);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue)); //设置顶部系统栏颜色
        Intent intent = getIntent();
        String intent_data = intent.getStringExtra("put_data_tucao");
        Toolbar toolbar = (Toolbar) findViewById(R.id.society_tocao_page_mainTool); //标题栏
        toolbar.setNavigationIcon(R.mipmap.ic_back_white);
        toolbar.setTitle(intent_data);
        setSupportActionBar(toolbar);
        back(toolbar);
        findView();
    }

    private void findView(){
        society_tocao_page_ok = (Button)findViewById(R.id.society_tocao_page_ok);
        society_tocao_page_content = (EditText)findViewById(R.id.society_tucao_page_content);

        society_tocao_page_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(society_tocao_page_content.getText().toString().isEmpty()){
                    Toast.makeText(SocietyMakeComplaintPage.this,"内容不能为空",Toast.LENGTH_LONG).show();
                }else {
                    UpdateData();
                }
            }
        });
    }

    private void UpdateData(){
        final ProgressDialog progressDialog = ProgressDialog.show(SocietyMakeComplaintPage.this,"","正在上传",true);
        new Thread(){
            public void run(){
                try{
                    Looper.prepare();//用于toast
                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
                    if (conn != null) { //判断 如果返回不为空则说明链接成功 如果为null的话则连接失败 请检查你的 mysql服务器地址是否可用 以及数据库名是否正确 并且 用户名跟密码是否正确
                        Log.d("调试", "连接成功");
                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
                        //上传
                        java.sql.PreparedStatement preparedStatement = null;
                        String newmessage_sql_insert = "insert into tucao (tucao_name,tucao_phone,tucao_area,tucao_time," +
                                "tucao_content) values(?,?,?,?,?)";
                        preparedStatement = (java.sql.PreparedStatement)conn.prepareStatement(newmessage_sql_insert,Statement.RETURN_GENERATED_KEYS);
                        preparedStatement.setString(1, SharePreferences.getString(SocietyMakeComplaintPage.this, AppConstants.USER_NAME));
                        preparedStatement.setString(2, SharePreferences.getString(SocietyMakeComplaintPage.this, AppConstants.USER_PHONE));
                        preparedStatement.setString(3, SharePreferences.getString(SocietyMakeComplaintPage.this, AppConstants.USER_AREA));
                        preparedStatement.setString(4, getTime());
                        preparedStatement.setString(5, society_tocao_page_content.getText().toString());
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        JDBCTools.releaseConnection(stmt,conn);
                        Intent intent_broad = new Intent(AppConstants.BROAD_COM);
                        LocalBroadcastManager.getInstance(SocietyMakeComplaintPage.this).sendBroadcast(intent_broad);
                        progressDialog.dismiss();
                        finish();
                    }else {
                        Log.d("调试", "连接失败");
                        Toast toast = Toast.makeText(SocietyMakeComplaintPage.this, "请检查网络", Toast.LENGTH_SHORT);
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

    //获取系统时间，并进行格式转换
    private String getTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = simpleDateFormat.format(new Date());
        return dateString;
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
