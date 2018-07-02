package com.example.wisdompark19.Mine;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.R;
import com.mysql.jdbc.Connection;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by ROBOSOFT on 2018/5/12.
 */

public class MineRequireActivity extends AppCompatActivity {

    private EditText require_content;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mine_require_activity);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue)); //设置顶部系统栏颜色
        Toolbar toolbar = (Toolbar)findViewById(R.id.mine_require_mainTool); //标题栏
        toolbar.setNavigationIcon(R.mipmap.ic_back_white);
        toolbar.setTitle("意见反馈");
        back(toolbar);
        findView();
    }

    private void findView(){
        Button require_ok = (Button) findViewById(R.id.mine_require_ok);
        require_content = (EditText) findViewById(R.id.mine_require_content);
        require_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(require_content.getText().toString().isEmpty()){
                    Toast.makeText(MineRequireActivity.this,"内容不能为空",Toast.LENGTH_LONG).show();
                }else {
                    upload();
                }
            }
        });

    }

    private void upload(){
        final ProgressDialog progressDialog = ProgressDialog.show(MineRequireActivity.this,"","正在上传",true);
        new Thread(){
            public void run(){
                try{
                    Looper.prepare();//用于toast
                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
                    if (conn != null) { //判断 如果返回不为空则说明链接成功 如果为null的话则连接失败 请检查你的 mysql服务器地址是否可用 以及数据库名是否正确 并且 用户名跟密码是否正确
                        Log.d("调试", "连接成功");
                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
                        //上传
                        java.sql.PreparedStatement preparedStatement;
                        String newmessage_sql_insert = "insert into user_require(user_require_content) values(?)";
                        preparedStatement = (java.sql.PreparedStatement)conn.prepareStatement(newmessage_sql_insert,Statement.RETURN_GENERATED_KEYS);
                        preparedStatement.setString(1, require_content.getText().toString().trim());
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        JDBCTools.releaseConnection(stmt,conn);
                        progressDialog.dismiss();
                        finish();
                    }else {
                        Log.d("调试", "连接失败");
                        Toast toast = Toast.makeText(MineRequireActivity.this, "请检查网络", Toast.LENGTH_SHORT);
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
