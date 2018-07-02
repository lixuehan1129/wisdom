package com.example.wisdompark19.Mine;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.DealBitmap;
import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.MainActivity;
import com.example.wisdompark19.R;
import com.example.wisdompark19.ViewHelper.DataBaseHelper;
import com.mysql.jdbc.Connection;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 最美人间四月天 on 2018/3/28.
 */

public class MineLoginActivity extends AppCompatActivity {

    private DataBaseHelper dataBaseHelper;
    private TextInputLayout user_login_name_layout;
    private TextInputLayout user_login_password_layout;
    private TextInputEditText user_login_phone;
    private TextInputEditText user_login_password;
    private Button user_login_forget;
    private Button user_login_regist;
    private Button user_login_login;
    private ArrayList<String> user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue)); //设置顶部系统栏颜色
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //不弹出输入法
        Intent intent = getIntent();
        String intent_data = intent.getStringExtra("put_data_login");
        Toolbar toolbar = (Toolbar)findViewById(R.id.user_login_mainTool); //标题栏
        toolbar.setTitle("用户登录");  //标题栏名称
        setQuanXian();
        findView();
        problem_jiaodian();
    }

    private void findView(){
//        user_login_picture = (CircleImageView)findViewById(R.id.user_login_picture);
        user_login_name_layout = (TextInputLayout)findViewById(R.id.user_login_name_layout);
        user_login_password_layout = (TextInputLayout)findViewById(R.id.user_login_password_layout);
        user_login_phone = (TextInputEditText)findViewById(R.id.user_login_name);
        user_login_password = (TextInputEditText)findViewById(R.id.user_login_password);
        user_login_forget = (Button)findViewById(R.id.user_login_forget);
        user_login_regist = (Button)findViewById(R.id.user_login_regist);
        user_login_login = (Button)findViewById(R.id.user_login_login);
        dataBaseHelper = new DataBaseHelper(MineLoginActivity.this,AppConstants.SQL_VISION);
        getUser();
        initEditText();
        pageDo();
    }

    private void pageDo(){
        user_login_forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast=Toast.makeText(MineLoginActivity.this, "忘记密码", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        user_login_regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast toast=Toast.makeText(MineLoginActivity.this, "注册", Toast.LENGTH_SHORT);
//                toast.show();
                Intent intent = new Intent(MineLoginActivity.this,MineRegistActivity.class);
                intent.putExtra("put_data_regist","注册");
                startActivity(intent);
            }
        });
        user_login_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user_login_phone.getText().toString().length() == 11 &&
                        user_login_password.getText().toString().length() > 5){
                    login();
                }else {
                    Toast toast = Toast.makeText(MineLoginActivity.this, "手机号或密码格式不正确", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }
    private void getUser(){
        user_id = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query("user",null,null,null,null,null,"user_sort");
        if(cursor != null){
            while (cursor.moveToNext()){
                //从本地数据库读取
                user_id.add(cursor.getString(cursor.getColumnIndex("user_phone")));
            }
            cursor.close();
        }
        sqLiteDatabase.close();
    }

    private void login(){
        final ProgressDialog progressDialog = ProgressDialog.show(MineLoginActivity.this,"","正在登陆，请稍后...",true);
        new  Thread() {
            public void run() {
                try {
                    Looper.prepare();
                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
                    if (conn != null) { //判断 如果返回不为空则说明链接成功 如果为null的话则连接失败 请检查你的 mysql服务器地址是否可用 以及数据库名是否正确 并且 用户名跟密码是否正确
                        Log.d("调试", "连接成功");
                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
                        //查找管理员
                        String administrators_sql_number = "select * from user where user_phone = '" +
                                user_login_phone.getText().toString() +
                                "'"; //要执行的sql语句
                        ResultSet resultSet_number = stmt.executeQuery(administrators_sql_number); //使用executeQury方法执行sql语句 返回ResultSet对象 即查询的结果
                        if (resultSet_number.next()) {
                            String login_password_get = resultSet_number.getString("user_password");
                            if(login_password_get.equals(user_login_password.getText().toString())){
                                //登陆后记住用户名,存储本地信息
                                String login_user_picture = null;
                                String user_area = null;
                                Blob user_picture = resultSet_number.getBlob("user_picture");
                                if(user_picture != null){
                                    //图片格式转换，保存为String格式
                                    InputStream inputStream = user_picture.getBinaryStream();
                                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 50,baos);
                                    String imageBase64 = new String (Base64.encode(baos.toByteArray(), 0));
                                    login_user_picture = imageBase64;
                                }
                                int login_user_sort = resultSet_number.getInt("user_sort");
                                if(login_user_sort == 2){
                                    SharePreferences.putString(MineLoginActivity.this, AppConstants.USER_PHONE,user_login_phone.getText().toString());
                                    SharePreferences.putString(MineLoginActivity.this, AppConstants.USER_NAME,"访客");
                                    SharePreferences.putString(MineLoginActivity.this, AppConstants.USER_ADDRESS,"访客");
                                    SharePreferences.putString(MineLoginActivity.this, AppConstants.USER_AREA,"访客");
                                    SharePreferences.putString(MineLoginActivity.this, AppConstants.USER_SEX,"访客");
                                    SharePreferences.putInt(MineLoginActivity.this, AppConstants.USER_SORT,login_user_sort);
                                    SharePreferences.putString(MineLoginActivity.this, AppConstants.USER_PICTURE,login_user_picture);
                                }else {
                                    user_area = resultSet_number.getString("user_area");
                                    SharePreferences.putString(MineLoginActivity.this, AppConstants.USER_PHONE,user_login_phone.getText().toString());
                                    SharePreferences.putString(MineLoginActivity.this, AppConstants.USER_NAME,resultSet_number.getString("user_name"));
                                    SharePreferences.putString(MineLoginActivity.this, AppConstants.USER_ADDRESS,resultSet_number.getString("user_address"));
                                    SharePreferences.putInt(MineLoginActivity.this, AppConstants.USER_SORT,login_user_sort);
                                    SharePreferences.putString(MineLoginActivity.this, AppConstants.USER_SEX,resultSet_number.getString("user_sex"));
                                    SharePreferences.putString(MineLoginActivity.this, AppConstants.USER_AREA,user_area);
                                    SharePreferences.putString(MineLoginActivity.this, AppConstants.USER_PICTURE,login_user_picture);
                                }
                                resultSet_number.close();
                                if(user_area != null){
                                    String sql_user = "select * from user where user_area = '" +
                                             user_area +
                                            "'";
                                    ResultSet rs = stmt.executeQuery(sql_user);
                                    SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();
                                    while (rs.next()){
                                        if(!user_id.contains(rs.getString("user_phone"))){
                                            ContentValues values = new ContentValues();
                                            values.put("user_id",rs.getInt("user_id"));
                                            values.put("user_name",rs.getString("user_name"));
                                            values.put("user_phone",rs.getString("user_phone"));
                                            values.put("user_address",rs.getString("user_address"));
                                            values.put("user_area",rs.getString("user_area"));
                                            values.put("user_sort",rs.getInt("user_sort"));
                                            values.put("user_sex",rs.getString("user_sex"));
                                            Blob picture = rs.getBlob("user_picture");
                                            if(picture != null){
                                                values.put("user_picture", DealBitmap.blobToBytes(picture));
                                            }else {
                                                values.put("user_picture", (byte[]) null);
                                            }
                                            sqLiteDatabase.insert("user",null,values);
                                        }
                                    }
                                    sqLiteDatabase.close();
                                    rs.close();
                                    JDBCTools.releaseConnection(stmt,conn);
                                    Toast toast = Toast.makeText(MineLoginActivity.this, "登录成功", Toast.LENGTH_SHORT);
                                    toast.show();
                                    progressDialog.dismiss();
                                    Intent intent = new Intent(MineLoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }else {
                                    JDBCTools.releaseConnection(stmt,conn);
                                    Toast toast = Toast.makeText(MineLoginActivity.this, "登录成功,当前状态为访客，请完善个人信息。", Toast.LENGTH_SHORT);
                                    toast.show();
                                    progressDialog.dismiss();
                                    Intent intent = new Intent(MineLoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            }
                            else {
                                Toast toast = Toast.makeText(MineLoginActivity.this, "密码错误", Toast.LENGTH_SHORT);
                                toast.show();
                                progressDialog.dismiss();
                            }
                        }else {
                            Toast toast = Toast.makeText(MineLoginActivity.this, "用户名不存在", Toast.LENGTH_SHORT);
                            toast.show();
                            progressDialog.dismiss();
                        }
                        Looper.loop();
                    } else {
                        Log.d("调试", "连接失败");
                        Toast toast = Toast.makeText(MineLoginActivity.this, "请检查网络", Toast.LENGTH_SHORT);
                        toast.show();
                        progressDialog.dismiss();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void initEditText(){
        user_login_name_layout.setCounterEnabled(true);  //设置可以计数
        user_login_name_layout.setCounterMaxLength(11); //计数的最大值

        user_login_phone.setText(getIntent().getStringExtra("put_extra"));

        user_login_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                user_login_name_layout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        user_login_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                user_login_password_layout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    ///Android按返回键，程序进入后台运行，不关闭程序
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
          //  moveTaskToBack(false);
            //方式二：返回手机的主屏幕
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /*
   * 点击空白区域 Edittext失去焦点 关闭输入法
   * */
    @SuppressLint("ClickableViewAccessibility")
    private void problem_jiaodian() {
        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.edit_relativeLayout);
        relativeLayout.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                relativeLayout.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                return false;
            }
        });
    }

    private void setQuanXian(){
        //获取权限
        List<String> permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MineLoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.VIBRATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.VIBRATE);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.CAMERA);
        }
        if(!permissionList.isEmpty()){
            String[] permissions= permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MineLoginActivity.this,permissions,1);
        }
    }
}
