package com.example.wisdompark19.Mine;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.R;
import com.mysql.jdbc.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 最美人间四月天 on 2018/4/2.
 */

public class MineRegistAddActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText user_regist_add_name;
    private EditText user_regist_add_address;
    private Button user_regist_add_ok;
    private TextView user_regist_add_join;
    private TextView user_regist_add_add;
    private EditText user_regist_add_society;
    private RadioGroup user_sex;
    private RadioButton user_male;
    private RadioButton user_female;
    private String user_sex_select;
    private String user_phone;
    private int user_sort;
    private String select;
    private AlertDialog alertDialog;
    public static final int UPDATE_SPINNER = 1;
    private List<String> society_list = new ArrayList<>();
    private int AREA_SELECT;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_regist_add);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue)); //设置顶部系统栏颜色
        Intent intent = getIntent();
        String intent_data = intent.getStringExtra("put_data_regist_add");
        select = intent.getStringExtra("put_data_regist_select");
        user_phone = intent_data;
     //   user_phone = "17888836863";
        Toolbar toolbar = (Toolbar)findViewById(R.id.user_regist_add_mainTool); //标题栏
        toolbar.setNavigationIcon(R.mipmap.ic_back_white);
        toolbar.setTitle("业主认证");  //标题栏名称
        back(toolbar);    //返回
        findView();
        initSpinner();
        save();
    }

    private void findView(){
        user_regist_add_ok = (Button)findViewById(R.id.user_regist_add_ok);
        user_regist_add_name = (EditText)findViewById(R.id.user_regist_add_name);
        user_sex = (RadioGroup)findViewById(R.id.user_sex);
        user_male = (RadioButton)findViewById(R.id.user_male);
        user_female = (RadioButton)findViewById(R.id.user_female);
        user_regist_add_society = (EditText) findViewById(R.id.user_regist_add_society);
        user_regist_add_address = (EditText)findViewById(R.id.user_regist_add_address);
        user_regist_add_join = (TextView)findViewById(R.id.user_regist_add_join);
        user_regist_add_add = (TextView)findViewById(R.id.user_regist_add_add);
        user_regist_add_ok.setOnClickListener(this);
        user_regist_add_join.setOnClickListener(this);
        user_regist_add_add.setOnClickListener(this);
        user_regist_add_society.setEnabled(false);
        user_sex_select = "女";
        user_sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(R.id.user_male == i){
                    user_sex_select = "男";
                }else{
                    user_sex_select = "女";
                }

            }
        });
    }

    private Handler handler_community = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case UPDATE_SPINNER:{
                    createSociety();
                    break;
                }
                default:
                    break;
            }
            return false;
        }
    });

    public void showSingleAlertDialog(){
        final String[] items = new String[society_list.size()];
        final String[] select_item = new String[1];
        for(int i = 0 ;i < society_list.size(); i++){
            items[i] = society_list.get(i);
        }
        System.out.println(items);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("选择社区");
        alertBuilder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int index) {
                Toast.makeText(MineRegistAddActivity.this, items[index], Toast.LENGTH_SHORT).show();
                select_item[0] = items[index];
            }
        });
        alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //TODO 业务逻辑代码
                if(select_item[0] == null){
                    user_regist_add_society.setText(society_list.get(0));
                }else {
                    user_regist_add_society.setText(select_item[0]);
                }
                // 关闭提示框
                alertDialog.dismiss();
            }
        });
        alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO 业务逻辑代码

                // 关闭提示框
                alertDialog.dismiss();
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    //联网查询小区数据
    private void initSpinner(){
        new Thread(){
            public void run(){
                try {
                    Looper.prepare();
                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
                    if (conn != null) {
                        Statement statement_community = conn.createStatement();
                        String community_sql = "select * from community";
                        ResultSet resultSet_community = statement_community.executeQuery(community_sql);
                        while (resultSet_community.next()){
                            //spinners.add(new RegistAddAdapter.mSpinner(resultSet_community.getString("community_name")));
                           // gridview_list.add(resultSet_community.getString("community_name"));
                            society_list.add(resultSet_community.getString("community_name"));
                        }

                        Message message = new Message();
                        message.what = UPDATE_SPINNER;
                        handler_community.sendMessage(message);

                        JDBCTools.releaseConnection(statement_community,conn);
                    } else {
                        Log.d("调试", "连接失败");
                        Toast toast=Toast.makeText(MineRegistAddActivity.this, "请检查网络", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }.start();
    }

    //按钮点击事件，保存内容
    private void save(){
            user_regist_add_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(TextUtils.isEmpty(user_regist_add_name.getText().toString()) ||
                            TextUtils.isEmpty(user_regist_add_address.getText().toString()) ||
                            TextUtils.isEmpty(user_regist_add_society.getText().toString())){
                        Toast toast=Toast.makeText(MineRegistAddActivity.this, "内容不能为空", Toast.LENGTH_SHORT);
                        toast.show();
                    }else {
                        if(AREA_SELECT == 1){  //说明为创建社区，这里要判断创建的群组是不是已经存在。
                           if(society_list.contains(user_regist_add_society.getText().toString())){
                               Toast.makeText(MineRegistAddActivity.this, "社区已存在", Toast.LENGTH_SHORT).show();
                           }else {
                               update();
                           }
                        }else {
                            update();
                        }
                    }
                }
            });

    }

 //   用户创建和加入社区
    private void createSociety(){
        user_regist_add_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AREA_SELECT = 0; //选择了一个社区。
                user_regist_add_add.setSelected(false);
                user_regist_add_join.setSelected(true);
                user_regist_add_society.setEnabled(false);
                if(society_list.size()>0){
                    showSingleAlertDialog();
                }else {
                    Toast toast=Toast.makeText(MineRegistAddActivity.this, "还没有创建的社区", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
        user_regist_add_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AREA_SELECT = 1; //添加了一个社区
                user_regist_add_join.setSelected(false);
                user_regist_add_add.setSelected(true);
                user_regist_add_society.setText(null);
                user_regist_add_society.setHint("请在这里输入创建的社区");
                user_regist_add_society.setEnabled(true);
            }
        });
    }

    //上传数据
    private void update(){
        final ProgressDialog progressDialog = ProgressDialog.show(MineRegistAddActivity.this,"","正在上传",true);
        new Thread(){
            public void run(){
                try {
                    Looper.prepare();
                    Connection conn_update = JDBCTools.getConnection("shequ","Zz123456");
                    if (conn_update!= null) {
                        if(user_phone != null){
                            Statement statement = conn_update.createStatement();
                            if(AREA_SELECT == 1){
                                user_sort = 0; //管理员,创建了一个社区
                            }else {
                                user_sort = 1; //业主，没有创建社区
                            }
                            String update_sql = "update user set user_sort = ?, user_name = ?, user_address = ?, " +
                                    "user_area = ?, user_sex = ? where user_phone = ?";
                            java.sql.PreparedStatement preparedStatement = null;
                            preparedStatement = conn_update.prepareStatement(update_sql);
                            preparedStatement.setInt(1,user_sort);
                            preparedStatement.setString(2,user_regist_add_name.getText().toString());
                            preparedStatement.setString(3,user_regist_add_address.getText().toString());
                            preparedStatement.setString(4,user_regist_add_society.getText().toString());
                            preparedStatement.setString(5,user_sex_select);
                            preparedStatement.setString(6,user_phone);
                            preparedStatement.executeUpdate();//执行更新操作
                            preparedStatement.close();
                            if(AREA_SELECT == 1){
                                String sql_update_area = "insert into community (community_name,community_phone) values('" +
                                        user_regist_add_society.getText().toString()+
                                        "','" +
                                        user_phone +
                                        "')";
                                statement.executeUpdate(sql_update_area);
                            }
                            JDBCTools.releaseConnection(statement,conn_update);
                            if(select.equals("regist")){
                                progressDialog.dismiss();
                                Intent intent = new Intent(MineRegistAddActivity.this,MineLoginActivity.class);
                                startActivity(intent);
                            }else {
                                Toast toast=Toast.makeText(MineRegistAddActivity.this, "修改完成", Toast.LENGTH_SHORT);
                                toast.show();
                                progressDialog.dismiss();
                            }

                        }
                    } else {
                        Log.d("调试", "连接失败");
                        Toast toast=Toast.makeText(MineRegistAddActivity.this, "请检查网络", Toast.LENGTH_SHORT);
                        toast.show();
                        progressDialog.dismiss();
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

    @Override
    public void onClick(View v) {

    }
}
