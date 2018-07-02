package com.example.wisdompark19.Main;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.DealBitmap;
import com.example.wisdompark19.AutoProject.QRCodeUtil;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.R;
import com.example.wisdompark19.ViewHelper.DataBaseHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by 最美人间四月天 on 2018/1/18.
 */

public class CodeActivity extends AppCompatActivity {

    private DataBaseHelper dataBaseHelper = new DataBaseHelper(CodeActivity.this,AppConstants.SQL_VISION);
    private ImageView imageView;
    private ImageView imageView_f;
    private TextView code_cancel;
    private TextView code_ok;
    private EditText code_name;
    private String code_sex_select;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.code_activity);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue)); //设置顶部系统栏颜色
        Intent intent = getIntent();
        String intent_data = intent.getStringExtra("put_data_code");
        Toolbar toolbar = (Toolbar)findViewById(R.id.code_activity_mainTool); //标题栏
        toolbar.setNavigationIcon(R.mipmap.ic_back_white);
        toolbar.setTitle(intent_data);  //标题栏名称
        setMenu(toolbar);
        findView();
        setView();
    }


    private void findView(){
        imageView = (ImageView) findViewById(R.id.code_activity_iv);
        LinearLayout code_create = (LinearLayout) findViewById(R.id.code_create); //生成访客
        CircleImageView circleImageView = (CircleImageView) findViewById(R.id.code_picture);
        TextView name = (TextView) findViewById(R.id.code_name);
        TextView address = (TextView) findViewById(R.id.code_add);
        String imageBase64 = SharePreferences.getString(CodeActivity.this,AppConstants.USER_PICTURE);
        Bitmap user_bitmap = DealBitmap.StringToBitmap(imageBase64);
        if(user_bitmap != null){
            circleImageView.setImageBitmap(user_bitmap);
        }else {
            circleImageView.setImageResource(R.mipmap.ic_launcher_round);
        }
        name.setText(SharePreferences.getString(CodeActivity.this,AppConstants.USER_NAME));
        address.setText(SharePreferences.getString(CodeActivity.this,AppConstants.USER_AREA));
        code_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewPage();
            }
        });
    }

    private Bitmap getPicture(){
        Bitmap picture = null;
        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor = sqLiteDatabase.query("user",null,
                "user_phone = ?",new String[]{
                        SharePreferences.getString(CodeActivity.this,AppConstants.USER_PHONE)
                },null,null,null);
        while (cursor.moveToFirst()){
            byte[] bytes = cursor.getBlob(cursor.getColumnIndex("user_picture"));
            if(bytes != null){
                picture = DealBitmap.byteToBit(bytes);
            }
        }
        return picture;
    }



    private void setView(){
        String sex = SharePreferences.getString(CodeActivity.this, AppConstants.USER_SEX);
        String user_phone = SharePreferences.getString(CodeActivity.this, AppConstants.USER_PHONE);
      //  String user_number = user_phone;
        String user_number = "2";
        int user_sort = SharePreferences.getInt(CodeActivity.this, AppConstants.USER_SORT);
        String model = null;
        String user_name = SharePreferences.getString(CodeActivity.this, AppConstants.USER_NAME);
        String user_address = SharePreferences.getString(CodeActivity.this, AppConstants.USER_ADDRESS);
        if(user_sort == 0){
            model = "管理员";
        }else if(user_sort ==1){
            model = "业主";
        }else {
            model = "访客";
            user_name = "访客";
            user_address = "访客";
        }
        String time = getTime();
        if(sex.equals("男")){
            user_number = "1";
        }else if(sex.equals("女")){
            user_number = "0";
        }
            String setContent = user_number + " " + user_name + " " + model + " " + time + " " + user_address;
            String md5Content = md5(setContent);
            String newContent = user_number + "," + user_name + "," + model + "," + time + "," + user_address + ","
                    + md5Content.substring(md5Content.length()-8);
            System.out.println(newContent);
            Bitmap bitmap = QRCodeUtil.createQRCodeBitmap(newContent,640,640);
            imageView.setImageBitmap(bitmap);
    }

    private void setMenu(Toolbar toolbar){
        //返回按钮监听
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void showNewPage(){
        //1.创建一个Dialog对象，如果是AlertDialog对象的话，弹出的自定义布局四周会有一些阴影，效果不好
        Dialog mDialog = new Dialog(CodeActivity.this);
        //去除标题栏
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //2.填充布局
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.code_auto, null);
        //将自定义布局设置进去
        mDialog.setContentView(dialogView);

        //3.设置指定的宽高,如果不设置的话，弹出的对话框可能不会显示全整个布局，当然在布局中写死宽高也可以
        /*WindowManager.LayoutParams lp     = new WindowManager.LayoutParams();
        Window                     window = mDialog.getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;*/
        //注意要在Dialog show之后，再将宽高属性设置进去，才有效果
        mDialog.show();
//        window.setAttributes(lp);
        //设置点击其它地方不让消失弹窗
        mDialog.setCancelable(false);
        initDialogView(dialogView);
        initDialogListener(mDialog);
    }

    private void initDialogView(View view){
        imageView_f = (ImageView)view.findViewById(R.id.code_auto_iv);
        code_cancel = (TextView) view.findViewById(R.id.code_cancel);
        code_ok = (TextView) view.findViewById(R.id.code_ok);
        code_name = (EditText)view.findViewById(R.id.code_auto_name);
        RadioGroup code_sex = (RadioGroup) view.findViewById(R.id.code_sex);
        code_sex_select = "女";
        code_sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(R.id.code_male == i){
                    code_sex_select = "男";
                }else{
                    code_sex_select = "女";
                }

            }
        });
    }

    private void initDialogListener(final Dialog dialog){
        code_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        code_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(code_name.getText().toString().isEmpty()){
                   Toast.makeText(CodeActivity.this,"请输入访客姓名",Toast.LENGTH_LONG).show();
                }else {
                    String code_number = null;
                    if(code_sex_select.equals("男")){
                        code_number = "1";
                    }else if(code_sex_select.equals("女")){
                        code_number = "0";
                    }
                    String setContent = code_number + " " + code_name.getText().toString() + " " + "访客" + " " + getTime() + " " + "访客邀请";
                    String md5Content = md5(setContent);
                    String newContent = code_number + "," + code_name.getText().toString() + "," + "访客" + "," + getTime() + "," + "访客邀请" + ","
                            + md5Content.substring(md5Content.length()-8);
                    System.out.println(newContent);
                    Bitmap bitmap = QRCodeUtil.createQRCodeBitmap(newContent,640,640);
                    imageView_f.setImageBitmap(bitmap);
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm.isActive()){
                        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(),0);

                    }
                }
            }
        });
    }


    //获取系统时间，并进行格式转换
    private String getTime(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(new Date());
    }

    //MD5加密
    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
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
