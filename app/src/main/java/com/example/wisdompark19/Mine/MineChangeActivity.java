package com.example.wisdompark19.Mine;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.DealBitmap;
import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.R;
import com.example.wisdompark19.ViewHelper.DataBaseHelper;
import com.mysql.jdbc.Connection;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

import static com.example.wisdompark19.AutoProject.AbsolutePath.getImageAbsolutePath;

/**
 * Created by ROBOSOFT on 2018/4/18.
 */

public class MineChangeActivity extends AppCompatActivity implements View.OnClickListener{

    //添加设置
    private DataBaseHelper dataBaseHelper = new DataBaseHelper(MineChangeActivity.this,AppConstants.SQL_VISION);
    private CircleImageView mine_change_picture;
    private Button mine_change_ok;
    private EditText mine_change_name;
    private RadioGroup mine_change_sex;
    private RadioButton mine_change_male;
    private RadioButton mine_change_female;
    private EditText mine_change_society;
    private LinearLayout mine_change_linear;
    private TextView mine_change_so;
    private TextView mine_change_join;
    private TextView mine_change_add;
    private TextView mine_change_address;
    private String user_sex_select;
    private List<String> society_list = new ArrayList<>();
    private AlertDialog alertDialog;
    private String old;
    private Uri photoUri;
    String touxiang_path = null;
    private int AREA_SELECT = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mine_change);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue)); //设置顶部系统栏颜色
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //不弹出输入法
        Toolbar toolbar = (Toolbar)findViewById(R.id.mine_change_mainTool); //标题栏
        toolbar.setNavigationIcon(R.mipmap.ic_back_white);
        toolbar.setTitle("详细资料");
        back(toolbar);
        initSpinner();
        findView();
        problem_jiaodian();
    }

    private void findView(){
        mine_change_picture = (CircleImageView)findViewById(R.id.mine_change_picture);
        mine_change_picture.setOnClickListener(this);
        mine_change_ok = (Button) findViewById(R.id.mine_change_ok);
        mine_change_ok.setOnClickListener(this);
        mine_change_name = (EditText) findViewById(R.id.mine_change_name);
        mine_change_sex = (RadioGroup) findViewById(R.id.mine_change_sex);
        mine_change_male = (RadioButton) findViewById(R.id.mine_change_male);
        mine_change_female = (RadioButton) findViewById(R.id.mine_change_female);
        mine_change_so = (TextView) findViewById(R.id.mine_change_so);
        mine_change_so.setOnClickListener(this);
        mine_change_society = (EditText) findViewById(R.id.mine_change_society);
        mine_change_linear = (LinearLayout) findViewById(R.id.mine_change_Linear_3);
        mine_change_linear.setVisibility(View.INVISIBLE);
        mine_change_join = (TextView) findViewById(R.id.mine_change_join);
        mine_change_join.setOnClickListener(this);
        mine_change_add = (TextView) findViewById(R.id.mine_change_add);
        mine_change_add.setOnClickListener(this);
        mine_change_address = (EditText)findViewById(R.id.mine_change_address);

        String image = SharePreferences.getString(MineChangeActivity.this,AppConstants.USER_PICTURE);
        Bitmap user_bitmap = DealBitmap.StringToBitmap(image);
        if(user_bitmap != null){
            mine_change_picture.setImageBitmap(user_bitmap);
        }else {
            mine_change_picture.setImageResource(R.mipmap.ic_launcher_round);
        }
        mine_change_name.setText(SharePreferences.getString(MineChangeActivity.this,AppConstants.USER_NAME));
        mine_change_society.setText(SharePreferences.getString(MineChangeActivity.this,AppConstants.USER_AREA));
        old = mine_change_society.getText().toString();
        mine_change_society.setEnabled(false);
        mine_change_address.setText(SharePreferences.getString(MineChangeActivity.this,AppConstants.USER_ADDRESS));
        if(SharePreferences.getString(MineChangeActivity.this, AppConstants.USER_SEX).equals("男")){
            mine_change_male.performClick();
            user_sex_select = "男";
        }else if(SharePreferences.getString(MineChangeActivity.this, AppConstants.USER_SEX).equals("女")){
            mine_change_female.performClick();
            user_sex_select = "女";
        }
        mine_change_sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(mine_change_male.getId() == i){
                    user_sex_select = "男";
                }else if(mine_change_female.getId() == i){
                    user_sex_select = "女";
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mine_change_picture :{ //选择照片
                final String[] items = new String[] {"选择图片","拍摄图片"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MineChangeActivity.this);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i == 0){
                            select_photo();
                        }else if(i == 1){
                            take_photo();
                        }
                    }
                }).create().show();
                break;
            }
            case R.id.mine_change_so :{ //社区
                AlertDialog.Builder builder = new AlertDialog.Builder(MineChangeActivity.this);
                builder.setMessage("修改社区会改变当前的状态")
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).
                setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(SharePreferences.getInt(MineChangeActivity.this,AppConstants.USER_SORT) == 0){
                            Toast.makeText(MineChangeActivity.this,"您为管理员，无法修改",Toast.LENGTH_SHORT).show();
                        }else {
//                            System.out.println("点击");
//                            mine_change_linear.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT,WRAP_CONTENT));
//                            System.out.println("点击");
                            mine_change_linear.setVisibility(View.VISIBLE);
                        }
                    }
                }).show();
                break;
            }
            case R.id.mine_change_join :{ //加入社区
                mine_change_add.setSelected(false);
                mine_change_join.setSelected(true);
                mine_change_society.setEnabled(false);
                if(society_list.size()>0){
                    showSingleAlertDialog();
                }else {
                    Toast toast=Toast.makeText(MineChangeActivity.this, "还没有创建的社区", Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            }
            case R.id.mine_change_add :{ //创建社区
                mine_change_join.setSelected(false);
                mine_change_add.setSelected(true);
                mine_change_society.setEnabled(true);
                break;
            }
            case R.id.mine_change_ok :{  //保存
                if(AREA_SELECT == 2 && !mine_change_society.getText().toString().equals(old)){
                    AREA_SELECT = 1; //添加了一个社区
                }
                if(TextUtils.isEmpty(mine_change_name.getText().toString()) ||
                        TextUtils.isEmpty(mine_change_address.getText().toString()) ||
                        TextUtils.isEmpty(mine_change_society.getText().toString())){
                    Toast toast=Toast.makeText(MineChangeActivity.this, "内容不能为空", Toast.LENGTH_SHORT);
                    toast.show();
                }else {
                    if(AREA_SELECT == 1){  //说明为创建社区，这里要判断创建的群组是不是已经存在。
                        if(society_list.contains(mine_change_society.getText().toString())){
                            System.out.println("1113");
                            Toast.makeText(MineChangeActivity.this, "社区已存在", Toast.LENGTH_SHORT).show();
                        }else {
                            update();
                        }
                    }else {
                        update();
                    }
                }
                break;
            }
        }
    }

    //上传数据
    private void update(){
        final ProgressDialog progressDialog = ProgressDialog.show(MineChangeActivity.this,"","正在上传",true);
        new Thread(){
            public void run(){
                try {
                    Looper.prepare();
                    Connection conn_update = JDBCTools.getConnection("shequ","Zz123456");
                        if(conn_update != null){
                            int user_sort;
                            Statement statement = conn_update.createStatement();
                            if(AREA_SELECT == 1){
                                user_sort = 0; //管理员,创建了一个社区
                            }else if(AREA_SELECT == 0){
                                user_sort = 1; //业主，没有创建社区
                            }else {
                                user_sort = SharePreferences.getInt(MineChangeActivity.this,AppConstants.USER_SORT);
                            }
                            System.out.println(touxiang_path);
                            if(touxiang_path != null){
                                String update_sql = "update user set user_sort = ?, user_name = ?, user_address = ?, " +
                                        "user_area = ?, user_sex = ?, user_picture = ? where user_phone = ?";
                                java.sql.PreparedStatement preparedStatement = null;
                                preparedStatement = conn_update.prepareStatement(update_sql);
                                preparedStatement.setInt(1,user_sort);
                                preparedStatement.setString(2,mine_change_name.getText().toString());
                                preparedStatement.setString(3,mine_change_address.getText().toString());
                                preparedStatement.setString(4,mine_change_society.getText().toString());
                                preparedStatement.setString(5,user_sex_select);
                                File file = new File(touxiang_path);
                                if(file.exists()){
                                    try {
                                        InputStream inputStream = new FileInputStream(file);
                                        preparedStatement.setBinaryStream(6,inputStream,file.length());
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    preparedStatement.setBinaryStream(6,null, 0);
                                }
                                preparedStatement.setString(7,
                                        SharePreferences.getString(MineChangeActivity.this,AppConstants.USER_PHONE));
                                preparedStatement.executeUpdate();//执行更新操作
                                preparedStatement.close();
                                //图片格式转换，保存为String格式
                                InputStream inputStream = null;
                                try {
                                    inputStream = new FileInputStream(new File(touxiang_path));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                if(inputStream != null){
                                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 50,baos);
                                    String imageBase64 = new String (Base64.encode(baos.toByteArray(), 0));
                                    SharePreferences.remove(MineChangeActivity.this,AppConstants.USER_PICTURE);
                                    SharePreferences.putString(MineChangeActivity.this,AppConstants.USER_PICTURE,imageBase64);
                                }
                            }else {
                                String update_sql = "update user set user_sort = ?, user_name = ?, user_address = ?, " +
                                        "user_area = ?, user_sex = ? where user_phone = ?";
                                java.sql.PreparedStatement preparedStatement = null;
                                preparedStatement = conn_update.prepareStatement(update_sql);
                                preparedStatement.setInt(1,user_sort);
                                preparedStatement.setString(2,mine_change_name.getText().toString());
                                preparedStatement.setString(3,mine_change_address.getText().toString());
                                preparedStatement.setString(4,mine_change_society.getText().toString());
                                preparedStatement.setString(5,user_sex_select);
                                preparedStatement.setString(6,
                                        SharePreferences.getString(MineChangeActivity.this,AppConstants.USER_PHONE));
                                preparedStatement.executeUpdate();//执行更新操作
                                preparedStatement.close();
                            }
                            if(AREA_SELECT == 1){
                                String sql_update_area = "insert into community (community_name,community_phone) values('" +
                                        mine_change_society.getText().toString()+
                                        "','" +
                                        SharePreferences.getString(MineChangeActivity.this,AppConstants.USER_PHONE) +
                                        "')";
                                statement.executeUpdate(sql_update_area);
                            }
                            JDBCTools.releaseConnection(statement,conn_update);
                            Toast toast=Toast.makeText(MineChangeActivity.this, "修改完成", Toast.LENGTH_SHORT);
                            toast.show();

                            SharePreferences.remove(MineChangeActivity.this,AppConstants.USER_NAME);
                            SharePreferences.putString(MineChangeActivity.this,AppConstants.USER_NAME,mine_change_name.getText().toString());

                            SharePreferences.remove(MineChangeActivity.this,AppConstants.USER_ADDRESS);
                            SharePreferences.putString(MineChangeActivity.this,AppConstants.USER_ADDRESS,mine_change_address.getText().toString());

                            SharePreferences.remove(MineChangeActivity.this,AppConstants.USER_AREA);
                            SharePreferences.putString(MineChangeActivity.this,AppConstants.USER_AREA,mine_change_society.getText().toString());

                            SharePreferences.remove(MineChangeActivity.this,AppConstants.USER_SEX);
                            SharePreferences.putString(MineChangeActivity.this,AppConstants.USER_SEX,user_sex_select);

                            Intent intent_broad = new Intent(AppConstants.BROAD_CON);
                            LocalBroadcastManager.getInstance(MineChangeActivity.this).sendBroadcast(intent_broad);
                            progressDialog.dismiss();
                            MineChangeActivity.this.finish();
                    } else {
                        Log.d("调试", "连接失败");
                        Toast toast=Toast.makeText(MineChangeActivity.this, "请检查网络", Toast.LENGTH_SHORT);
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
    public void showSingleAlertDialog(){
        final String[] items = new String[society_list.size()];
        final String[] select_item = new String[1];
        for(int i = 0 ;i < society_list.size(); i++){
            items[i] = society_list.get(i);
        }
        android.support.v7.app.AlertDialog.Builder alertBuilder = new android.support.v7.app.AlertDialog.Builder(this);
        alertBuilder.setTitle("选择社区");
        alertBuilder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int index) {
                select_item[0] = items[index];
            }
        });
        alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //TODO 业务逻辑代码
                if(select_item[0] == null){
                    if(mine_change_society.getText().toString().equals(society_list.get(0))){
                        Toast.makeText(MineChangeActivity.this,"已经选择该社区", Toast.LENGTH_SHORT).show();
                    }else {
                        mine_change_society.setText(society_list.get(0));
                        AREA_SELECT = 0; //选择了一个社区。
                    }
                }else {
                    if(mine_change_society.getText().toString().equals(select_item[0])){
                        Toast.makeText(MineChangeActivity.this,"已经选择该社区", Toast.LENGTH_SHORT).show();
                    }else {
                        mine_change_society.setText(select_item[0]);
                        AREA_SELECT = 0; //选择了一个社区。
                    }
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
                        JDBCTools.releaseConnection(statement_community,conn);
                    } else {
                        Log.d("调试", "连接失败");
                        Toast toast=Toast.makeText(MineChangeActivity.this, "请检查网络", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }.start();
    }


    //调用相机拍照
    private void take_photo(){
        // 获取 SD 卡根目录
        String saveDir = Environment.getExternalStorageDirectory() + "/com.example.wisdom.park/";
        // 新建目录
        File dir = new File(saveDir);
        if (! dir.exists()) {
            dir.mkdirs();
        }
        // 生成文件名
        SimpleDateFormat t = new SimpleDateFormat("yyyyMMddssSSS");
        String filename = "IMG_" + (t.format(new Date())) + ".jpg";
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoUri = Uri.fromFile(new File(saveDir + filename));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        System.out.println(photoUri);
        startActivityForResult(intent, AppConstants.CAMERA);
    }
    //调用系统相册
    private void select_photo(){
        Intent intent = new Intent(
                Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, AppConstants.ALBUM);
    }

    //从拍照或相册获取图片
    //找到图片路径
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AppConstants.CAMERA:{
                System.out.println("111");
                Uri uri = null;
                if (data != null && data.getData() != null) {
                    uri = data.getData();
                }
                if (uri == null) {
                    if (photoUri != null) {
                        uri = photoUri;
                    }
                }
                System.out.println(uri);
                showImage(DealBitmap.getRealFilePath(MineChangeActivity.this,uri));
                break;
            }
            case AppConstants.ALBUM:
                if (data != null) {
                    Uri uri = data.getData();
                    String imagePath;
                    imagePath = getImageAbsolutePath(this, uri);
                    showImage(imagePath);
                }
                break;
        }
    }
    /*
    * 加载更换头像
    * */
    private void showImage(String imaePath){
        Bitmap bm = BitmapFactory.decodeFile(imaePath);
        compressWithLs(new File(imaePath));
        mine_change_picture.setImageBitmap(bm);
    }
    /**
     * 压缩单张图片 Listener 方式
     */
    private void compressWithLs(File file) {
        Luban.get(this)
                .load(file)
                .putGear(Luban.THIRD_GEAR)
                .setFilename("user_"+System.currentTimeMillis())
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                    }
                    @Override
                    public void onSuccess(File file) {
                        Log.i("path", file.getAbsolutePath());
                        touxiang_path = file.getAbsolutePath();
                    }
                    @Override
                    public void onError(Throwable e) {
                    }
                }).launch();
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

    /*
  * 点击空白区域 Edittext失去焦点 关闭输入法
  * */
    @SuppressLint("ClickableViewAccessibility")
    private void problem_jiaodian() {
        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.mine_relv);
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


}
