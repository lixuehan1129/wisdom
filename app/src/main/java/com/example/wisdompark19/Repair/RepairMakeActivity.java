package com.example.wisdompark19.Repair;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wisdompark19.Adapter.ImageAdapter;
import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.DealBitmap;
import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.R;
import com.example.wisdompark19.ViewHelper.CustomDatePicker;
import com.example.wisdompark19.ViewHelper.DataBaseHelper;
import com.example.wisdompark19.ViewHelper.ShowImage;
import com.example.xlhratingbar_lib.XLHRatingBar;
import com.mysql.jdbc.Connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

import static com.example.wisdompark19.AutoProject.AbsolutePath.getImageAbsolutePath;

/**
 * Created by 最美人间四月天 on 2018/3/13.
 */

public class RepairMakeActivity extends AppCompatActivity implements View.OnClickListener{

    private Uri photoUri;
    private int mes_select;
    private int intent_data_id = 0;
    private DataBaseHelper dataBaseHelper;
    private List<ImageAdapter.Item_Image> ImageDatas = new ArrayList<>();
    private List<String> ImagePath = new ArrayList<>();
    private List<String> ImageData = new ArrayList<>();
//    private List<Bitmap> ImageGetPath = new ArrayList<>();
//    public static final int UPDATE_REPM = 1;

    private TextView repair_time, repair_name, repair_add;
    private CircleImageView repair_image;
    private EditText repair_edit;
    private RecyclerView repair_rv;
    private Spinner repair_spinner;
    private CustomDatePicker mCustomDatePicker;
    private RelativeLayout repair_progress;
    private RelativeLayout repair_pingjia;
    private XLHRatingBar repair_ratingbar;
    private Switch repair_ok;
    private int progress_i = 0;
    private int pingjia_i = 0;

    private String time;
    private String spinner;
    private String content;
    private String user,add;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repair_make);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue)); //设置顶部系统栏颜色
        Intent intent = getIntent();
        mes_select = intent.getIntExtra("repair_check",1);
        intent_data_id = intent.getIntExtra("repair_check_image",0);
        Toolbar toolbar = (Toolbar)findViewById(R.id.repair_make_mainTool); //标题栏
        toolbar.setNavigationIcon(R.mipmap.ic_back_white);
        toolbar.setTitle("报修管理");
        setSupportActionBar(toolbar);
        back(toolbar);
        findView();
    }

    private void findView(){
        repair_name = (TextView)findViewById(R.id.repair_name);
        repair_add = (TextView)findViewById(R.id.repair_add);
        repair_spinner = (Spinner)findViewById(R.id.repair_spinner);
        repair_time = (TextView)findViewById(R.id.repair_time);
        repair_image = (CircleImageView) findViewById(R.id.repair_image);
        repair_edit = (EditText)findViewById(R.id.repair_make_edit);
        repair_rv = (RecyclerView)findViewById(R.id.repair_make_rv);
        repair_progress = (RelativeLayout) findViewById(R.id.repair_progress);
        repair_progress.setVisibility(View.INVISIBLE);
        repair_pingjia = (RelativeLayout) findViewById(R.id.repair_pingjia);
        repair_pingjia.setVisibility(View.INVISIBLE);
        repair_ratingbar = (XLHRatingBar) findViewById(R.id.repair_ratingBar);
        repair_ok = (Switch) findViewById(R.id.repair_ok);

        Button repair_button_ok = (Button) findViewById(R.id.repair_make_ok);
        repair_button_ok.setOnClickListener(this);

        CardView repair_tacv = (CardView) findViewById(R.id.repair_make_tacv);
        ImageView repair_make_take = (ImageView) findViewById(R.id.repair_make_take);
        ImageView repair_make_add = (ImageView) findViewById(R.id.repair_make_add);


        dataBaseHelper = new DataBaseHelper(RepairMakeActivity.this,AppConstants.SQL_VISION);
        ImageDatas = new ArrayList<>();
        ImagePath = new ArrayList<>();
        ImageData = new ArrayList<>();

        if(mes_select == 1){ //第二次进入时使用
         //   repair_button_ok.setVisibility(View.INVISIBLE);
            repair_button_ok.setText("提交");
            repair_time.setEnabled(false);
            repair_time.setText(null);
            repair_edit.setMinLines(2);
            repair_edit.setEnabled(false);
            repair_tacv.setVisibility(View.INVISIBLE);
            if(SharePreferences.getInt(RepairMakeActivity.this,AppConstants.USER_SORT) == 0){
                repair_progress.setVisibility(View.VISIBLE);
                repair_ok.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked){
                            progress_i = 1; //完成
                        }else {
                            progress_i = 0; //未完成

                        }
                    }
                });
            }
//            repair_ratingbar.setOnRatingChangeListener(new XLHRatingBar.OnRatingChangeListener() {
//                @Override
//                public void onChange(int countSelected) {
//                    Toast.makeText(RepairMakeActivity.this, countSelected+"", Toast.LENGTH_LONG).show();
//                }
//            });
            getData();
//            if(name.equals(SharePreferences.getString(RepairMakeActivity.this, AppConstants.USER_PHONE))){
//                String imageBase64 = SharePreferences.getString(RepairMakeActivity.this, AppConstants.USER_PICTURE);
//                Bitmap user_bitmap = DealBitmap.StringToBitmap(imageBase64);
//                repair_image.setImageBitmap(user_bitmap);
//                String name1 = SharePreferences.getString(RepairMakeActivity.this,AppConstants.USER_NAME) +
//                        "(" + SharePreferences.getString(RepairMakeActivity.this,AppConstants.USER_PHONE) +
//                        ")";
//                repair_name.setText(name1);
//            }
//            connectData();
        }else {  //创建时使用
            String imageBase64 = SharePreferences.getString(RepairMakeActivity.this, AppConstants.USER_PICTURE);
            Bitmap user_bitmap = DealBitmap.StringToBitmap(imageBase64);
            repair_image.setImageBitmap(user_bitmap);
            String name = SharePreferences.getString(RepairMakeActivity.this,AppConstants.USER_NAME) +
                    "(" + SharePreferences.getString(RepairMakeActivity.this,AppConstants.USER_PHONE) +
                    ")";
            repair_name.setText(name);
            repair_add.setText(SharePreferences.getString(RepairMakeActivity.this,AppConstants.USER_ADDRESS));
            repair_time.setOnClickListener(RepairMakeActivity.this);
            repair_make_take.setOnClickListener(this);
            repair_make_add.setOnClickListener(this);
            repair_edit.setMinLines(3);
            initDatePicker();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.repair_time:{
                mCustomDatePicker.show(repair_time.getText().toString());
                break;
            }
            case R.id.repair_make_ok:{
                if(mes_select == 1){
                    updateOld(); //报修信息更新
                }else {
                    if(repair_edit.getText().toString().isEmpty()){
                        Toast.makeText(RepairMakeActivity.this,"内容不能为空",Toast.LENGTH_LONG).show();
                    }else {
                        if(ImagePath.size()<6){
                            for(int i = 6-ImagePath.size(); i > 0; i--){
                                ImagePath.add(null);
                            }
                        }
                        UpdateData();
                    }
                }
                break;
            }
            case R.id.repair_make_add:{
                if(ImageDatas.size()>5){
                    Toast.makeText(RepairMakeActivity.this,"最多添加6张",Toast.LENGTH_LONG).show();
                }else {
                    select_photo();
                }
                break;
            }
            case R.id.repair_make_take:{
                if(ImageDatas.size()>5){
                    Toast.makeText(RepairMakeActivity.this,"最多添加6张",Toast.LENGTH_LONG).show();
                }else {
                    take_photo();
                }
                break;
            }
        }
    }

    //本地数据
    @SuppressLint("SetTextI18n")
    private void getData(){
        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query("repair",null,"repair_id = ?",new String[]{String.valueOf(intent_data_id)},
                null,null,null);
        if(cursor.moveToFirst()){
            String phone = cursor.getString(cursor.getColumnIndex("repair_phone"));
            repair_ratingbar.setCountSelected(cursor.getInt(cursor.getColumnIndex("repair_pingjia")));
            if(SharePreferences.getString(RepairMakeActivity.this,AppConstants.USER_PHONE).equals(phone)){
                repair_pingjia.setVisibility(View.VISIBLE);
                repair_ratingbar.setOnRatingChangeListener(new XLHRatingBar.OnRatingChangeListener() {
                    @Override
                    public void onChange(int countSelected) {
                        pingjia_i = countSelected;
                    }
                });
            }
            int progress = cursor.getInt(cursor.getColumnIndex("repair_progress"));
            progress_i = progress;
            if(progress == 0){
                repair_ok.setChecked(false);
            }else {
                repair_ok.setChecked(true);
            }
            Cursor cursor_phone = sqLiteDatabase.query("user",null,
                    "user_phone = ?",new String[]{phone},null,null,null);
            if(cursor_phone != null){
                while (cursor_phone.moveToNext()){
                    Bitmap picture = null;
                    //查找成员头像
                    byte[] bytes = null;
                    bytes = cursor_phone.getBlob(cursor_phone.getColumnIndex("user_picture"));
                    if(bytes != null){
                        picture = DealBitmap.byteToBit(bytes);
                        if(picture != null){
                            repair_image.setImageBitmap(picture);
                        }else {
                            repair_image.setImageResource(R.mipmap.ic_launcher_round);
                        }
                    }
                    repair_name.setText(cursor_phone.getString(cursor_phone.getColumnIndex("user_name")) +
                    "(" + phone + ")");
                    repair_add.setText(cursor_phone.getString(cursor_phone.getColumnIndex("user_address")));
                }
                cursor_phone.close();
            }
            initSpinner(repair_spinner,cursor.getString(cursor.getColumnIndex("repair_title")));
            repair_edit.setText(cursor.getString(cursor.getColumnIndex("repair_content")));
            repair_time.setText(cursor.getString(cursor.getColumnIndex("repair_select_time")));
            String picture1 = cursor.getString(cursor.getColumnIndex("repair_picture1"));
            String picture2 = cursor.getString(cursor.getColumnIndex("repair_picture2"));
            String picture3 = cursor.getString(cursor.getColumnIndex("repair_picture3"));
            String picture4 = cursor.getString(cursor.getColumnIndex("repair_picture4"));
            String picture5 = cursor.getString(cursor.getColumnIndex("repair_picture5"));
            String picture6 = cursor.getString(cursor.getColumnIndex("repair_picture6"));
            if(picture1 != null){
                showImage(picture1);
            }
            if(picture2 != null){
                showImage(picture2);
            }
            if(picture3 != null){
                showImage(picture3);
            }
            if(picture4 != null){
                showImage(picture4);
            }
            if(picture5 != null){
                showImage(picture5);
            }
            if(picture6 != null){
                showImage(picture6);
            }
        }
        sqLiteDatabase.close();
    }

    private void updateOld(){
        final ProgressDialog progressDialog = ProgressDialog.show(RepairMakeActivity.this,"","正在上传",true);
        new Thread(){
            public void run(){
                try{
                    Looper.prepare();//用于toast
                    Connection conn_update = JDBCTools.getConnection("shequ","Zz123456");
                    if(conn_update != null){
                        Log.d("调试", "连接成功");
                        Statement stmt = conn_update.createStatement();
                        String update_old = "update repair set repair_progress = ?, repair_pingjia = ? where repair_id = ?";
                        PreparedStatement preparedStatement;
                        preparedStatement = conn_update.prepareStatement(update_old);
                        preparedStatement.setInt(1,progress_i);
                        preparedStatement.setInt(2,pingjia_i);
                        preparedStatement.setInt(3,intent_data_id);
                        preparedStatement.executeUpdate();//执行更新操作
                        preparedStatement.close();
                        JDBCTools.releaseConnection(stmt,conn_update);
                        Intent intent_broad = new Intent(AppConstants.BROAD_REPAIR);
                        LocalBroadcastManager.getInstance(RepairMakeActivity.this).sendBroadcast(intent_broad);
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

    //上传数据
    private void UpdateData(){
        final ProgressDialog progressDialog = ProgressDialog.show(RepairMakeActivity.this,"","正在上传",true);
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
                        String repair_sql_insert = "insert into repair (repair_name,repair_phone,repair_area,repair_time," +
                                "repair_leixing,repair_content,repair_picture1,repair_picture2," +
                                "repair_picture3,repair_picture4,repair_picture5,repair_picture6,repair_select_time,repair_progress,repair_pingjia) " +
                                "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                        preparedStatement = (java.sql.PreparedStatement)conn.prepareStatement(repair_sql_insert,Statement.RETURN_GENERATED_KEYS);
                        preparedStatement.setString(1, SharePreferences.getString(RepairMakeActivity.this, AppConstants.USER_NAME));
                        preparedStatement.setString(2, SharePreferences.getString(RepairMakeActivity.this, AppConstants.USER_PHONE));
                        preparedStatement.setString(3, SharePreferences.getString(RepairMakeActivity.this, AppConstants.USER_AREA));
                        preparedStatement.setString(4, getTime());
                        preparedStatement.setString(5, repair_spinner.getSelectedItem().toString());
                        preparedStatement.setString(6, repair_edit.getText().toString());
                        preparedStatement.setString(13,repair_time.getText().toString());
                        preparedStatement.setInt(14,0);
                        preparedStatement.setInt(15,0);
                        for(int i = 0; i < 6; i++){
                            File file = null;
                            System.out.println(ImagePath.get(i));
                            if(ImagePath.get(i) != null){
                                file = new File(ImagePath.get(i));
                                if(file.exists()){
                                    try {
                                        InputStream inputStream = new FileInputStream(file);
                                        preparedStatement.setBinaryStream(7+i,inputStream,file.length());
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    preparedStatement.setBinaryStream(7+i,null, 0);
                                }
                            }else {
                                preparedStatement.setBinaryStream(7+i,null, 0);
                            }
                        }
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        JDBCTools.releaseConnection(stmt,conn);
                        Intent intent_broad = new Intent(AppConstants.BROAD_REPAIR);
                        LocalBroadcastManager.getInstance(RepairMakeActivity.this).sendBroadcast(intent_broad);
                        progressDialog.dismiss();
                        finish();
                    }else {
                        Log.d("调试", "连接失败");
                        Toast toast = Toast.makeText(RepairMakeActivity.this, "请检查网络", Toast.LENGTH_SHORT);
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

//    private void connectData(){
//        new Thread(){
//            public void run(){
//                try{
//                    Looper.prepare();
//                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
//                    if (conn != null) { //判断 如果返回不为空则说明链接成功 如果为null的话则连接失败 请检查你的 mysql服务器地址是否可用 以及数据库名是否正确 并且 用户名跟密码是否正确
//                        Log.d("调试", "连接成功,消息界面");
//                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
//                        //查找信息
//                        String sql_connect = "select * from repair where repair_id = '" +
//                                intent_data_id +
//                                "'";
//                        ResultSet resultSet = stmt.executeQuery(sql_connect);
//                        resultSet.next();
//                        time = resultSet.getString("repair_select_time");
//                        spinner = resultSet.getString("repair_leixing");
//                        content = resultSet.getString("repair_content");
//                        Blob picture1 = resultSet.getBlob("repair_picture1");
//                        Blob picture2 = resultSet.getBlob("repair_picture2");
//                        Blob picture3 = resultSet.getBlob("repair_picture3");
//                        Blob picture4 = resultSet.getBlob("repair_picture4");
//                        Blob picture5 = resultSet.getBlob("repair_picture5");
//                        Blob picture6 = resultSet.getBlob("repair_picture6");
//                        if(picture1 != null){
//                            InputStream inputStream1 = picture1.getBinaryStream();
//                            Bitmap bitmap1 = DealBitmap.InputToBitmap(inputStream1);
//                            ImageGetPath.add(bitmap1);
//                        }
//                        if(picture2 != null){
//                            InputStream inputStream2 = picture2.getBinaryStream();
//                            Bitmap bitmap2 = DealBitmap.InputToBitmap(inputStream2);
//                            ImageGetPath.add(bitmap2);
//                        }
//                        if(picture3 != null){
//                            InputStream inputStream3 = picture3.getBinaryStream();
//                            Bitmap bitmap3 = DealBitmap.InputToBitmap(inputStream3);
//                            ImageGetPath.add(bitmap3);
//                        }
//                        if(picture4 != null){
//                            InputStream inputStream4 = picture4.getBinaryStream();
//                            Bitmap bitmap4 = DealBitmap.InputToBitmap(inputStream4);
//                            ImageGetPath.add(bitmap4);
//                        }
//                        if(picture5 != null){
//                            InputStream inputStream5 = picture5.getBinaryStream();
//                            Bitmap bitmap5 = DealBitmap.InputToBitmap(inputStream5);
//                            ImageGetPath.add(bitmap5);
//                        }
//                        if(picture6 != null){
//                            InputStream inputStream6 = picture6.getBinaryStream();
//                            Bitmap bitmap6 = DealBitmap.InputToBitmap(inputStream6);
//                            ImageGetPath.add(bitmap6);
//                        }
//                        resultSet.close();
//                        if(!name.equals(SharePreferences.getString(RepairMakeActivity.this, AppConstants.USER_PHONE))){
//                            String sql_user = "select * from user where user_phone = '" +
//                                    name +
//                                    "'" ;
//                            ResultSet resultSet1 = stmt.executeQuery(sql_user);
//                            resultSet1.next();
//                            user = resultSet1.getString("user_name") + "(" + name + ")";
//                            add = resultSet1.getString("user_address");
//                            Blob blob = resultSet1.getBlob("user_picture");
//                            if(blob != null){
//                                InputStream inputStream = blob.getBinaryStream();
//                                bitmap = DealBitmap.InputToBitmap(inputStream);
//                            }
//                        }
//                        System.out.println(ImageGetPath);
//                        Message message = new Message();
//                        message.what = UPDATE_REPM;
//                        handler_rep.sendMessage(message);
//
//                        JDBCTools.releaseConnection(stmt,conn);
//                    }else {
//                        Log.d("调试", "连接失败,消息界面");
//                        Toast toast = Toast.makeText(RepairMakeActivity.this, "请检查网络", Toast.LENGTH_SHORT);
//                        toast.show();
//                    }
//
//                }catch (SQLException e) {
//                    e.printStackTrace();
//                }
//                Looper.loop();
//            }
//        }.start();
//    }
//
//    private Handler handler_rep = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message msg) {
//            // TODO Auto-generated method stub
//            switch (msg.what){
//                case UPDATE_REPM:{
//                    repair_time.setText(time);
//                    initSpinner(repair_spinner,spinner);
//                    repair_edit.setText(content,null);
//                    for(int i = 0; i<ImageGetPath.size(); i++){
//                        showLoadImage(ImageGetPath.get(i));
//                    }
//                    if(!name.equals(SharePreferences.getString(RepairMakeActivity.this, AppConstants.USER_PHONE))){
//                        if(bitmap != null){
//                            repair_image.setImageBitmap(bitmap);
//                        }else {
//                            repair_image.setImageResource(R.mipmap.ic_launcher_round);
//                        }
//                        repair_name.setText(user);
//                        repair_add.setText(add);
//                    }
//                    updateLoad();
//                    break;
//                }
//                default:
//                    break;
//            }
//            return false;
//        }
//    });

    private void initSpinner(Spinner spinner, String s){
        ArrayList<String> spinners = new ArrayList<>();
        spinners.add(s);
        //设置ArrayAdapter内置的item样式-这里是单行显示样式
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinners);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        //设置Adapter了
        spinner.setAdapter(adapter);
        spinner.setEnabled(false);
    }
    /*
  * 加载图片
  * */
    private void showImage(String image_Path){
        File file = new File(image_Path);
        if(mes_select == 1){
            ImagePath.add(image_Path);
        }else {
            compressWithLs(file);
        }
        ImageAdapter first = new ImageAdapter(ImageDatas);
        ImageAdapter.Item_Image item_image = first.new Item_Image(image_Path);
        ImageData.add(image_Path);
        ImageDatas.add(item_image);
        update();
    }

    private void update(){
        repair_rv.setLayoutManager(new GridLayoutManager(this,3));
        ImageAdapter mAdapter = new ImageAdapter(ImageDatas);
        repair_rv.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(RepairMakeActivity.this,ShowImage.class);
                Bundle bundle = new Bundle();
                bundle.putString("image_select_name",ImageData.get(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    /*
   * 加载图片
   * */
//    private void showLoadImage(Bitmap bitmap){
//        ImageAdapter first = new ImageAdapter(ImageDatas);
//        ImageAdapter.Item_Image item_image = first.new Item_Image(bitmap);
//        ImageDatas.add(item_image);
//    }
//
//    //加载网络图片的大图
//    private void updateLoad(){
//        repair_rv.setLayoutManager(new GridLayoutManager(this,3));
//        ImageAdapter mAdapter = new ImageAdapter(ImageDatas);
//        repair_rv.setAdapter(mAdapter);
//        mAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                Intent intent = new Intent(RepairMakeActivity.this,ShowImage.class);
//                Bundle bundle = new Bundle();
//                int pos = position+1;
//                bundle.putString("select_fenlei","repair");
//                bundle.putString("image_select_name","repair_picture"+pos);
//                bundle.putInt("image_select_id",1);
//                bundle.putInt("image_select_new",intent_data_id);
//                intent.putExtras(bundle);
//                startActivity(intent);
//            }
//        });
//    }

    //获取系统时间，并进行格式转换
    private String getTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = simpleDateFormat.format(new Date());
        return dateString;
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
                showImage(DealBitmap.getRealFilePath(RepairMakeActivity.this,uri));
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
                        String image_load_path = file.getAbsolutePath();
                        ImagePath.add(image_load_path);
                        // Glide.with(RegisterActivity.this).load(file).into(image);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }).launch();
    }

    //自定义时间选择
    private void initDatePicker() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        String now = sdf.format(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH)+3);
        String future = sdf.format(calendar.getTime());
        repair_time.setText(now);

        mCustomDatePicker = new CustomDatePicker(this, new CustomDatePicker.ResultHandler() {
            @Override
            public void handle(String time) { // 回调接口，获得选中的时间
                repair_time.setText(time);
            }
        }, now, future); // 初始化日期格式请用：yyyy-MM-dd HH:mm，否则不能正常运行
        mCustomDatePicker.showSpecificTime(true); // 显示时和分
        mCustomDatePicker.setIsLoop(true); // 允许循环滚动
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
