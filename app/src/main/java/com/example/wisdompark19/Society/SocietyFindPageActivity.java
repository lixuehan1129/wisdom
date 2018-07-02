package com.example.wisdompark19.Society;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wisdompark19.Adapter.ImageAdapter;
import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.DealBitmap;
import com.example.wisdompark19.AutoProject.ForbidClickListener;
import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.Mine.MineRegistActivity;
import com.example.wisdompark19.R;
import com.example.wisdompark19.ViewHelper.DataBaseHelper;
import com.example.wisdompark19.ViewHelper.ShowImage;
import com.mysql.jdbc.Connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
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
 * Created by 最美人间四月天 on 2018/3/19.
 */

public class SocietyFindPageActivity extends AppCompatActivity {

    private DataBaseHelper dataBaseHelper;
    private Uri photoUri;
    private int mes_select;
    private List<ImageAdapter.Item_Image> ImageDatas;
    private List<String> ImagePath;
    private List<String> ImageData;
//    private List<Bitmap> ImageGetPath;
    private Button society_find_page_ok;
    private EditText society_find_page_title;
    private EditText society_find_page_content;
    private TextView society_find_page_time;
    private RecyclerView society_find_page_rv;
    private ImageView society_find_page_take;
    private ImageView society_find_page_add;
    private CardView society_find_page_tacv;
    private LinearLayout mLinearLayout;
    private TextView mTextView;
    private CircleImageView mCircleImageView;
//    private String title;
//    private String content;
//    private String time;
//    private String phone;
    private int intent_data_id;
//    public static final int UPDATE_FIND = 1;
//    public static final int UPDATE_YEZHU = 2;
    private String yezhu;
    private Bitmap bitmap = null;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.society_find_page);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue)); //设置顶部系统栏颜色
        Intent intent = getIntent();
        intent_data_id = intent.getIntExtra("put_data_find_id",0);
        mes_select = intent.getIntExtra("put_data_find_select",1);
//        title = intent.getStringExtra("put_data_find_title");
//        content = intent.getStringExtra("put_data_find_content");
//        time = intent.getStringExtra("put_data_find_time");
//        phone = intent.getStringExtra("put_data_find_phone");
        Toolbar toolbar = (Toolbar)findViewById(R.id.society_find_page_mainTool); //标题栏
        toolbar.setNavigationIcon(R.mipmap.ic_back_white);
        toolbar.setTitle("失物招领");
        back(toolbar);
        findView();
    }

    private void findView(){
        society_find_page_time = (TextView) findViewById(R.id.society_find_page_time);
        society_find_page_ok = (Button) findViewById(R.id.society_find_page_ok);
        society_find_page_title = (EditText) findViewById(R.id.society_find_page_name);
        society_find_page_content = (EditText) findViewById(R.id.society_find_page_neirong);
        society_find_page_rv = (RecyclerView) findViewById(R.id.society_find_page_rv);
        mLinearLayout = (LinearLayout) findViewById(R.id.society_find_page_li);
        mTextView = (TextView) findViewById(R.id.society_find_page_yezhu);
        mCircleImageView = (CircleImageView) findViewById(R.id.society_find_page_image);
        society_find_page_take = (ImageView) findViewById(R.id.society_find_page_take);
        society_find_page_add = (ImageView) findViewById(R.id.society_find_page_add);
        society_find_page_tacv = (CardView) findViewById(R.id.society_find_page_tacv);
        dataBaseHelper = new DataBaseHelper(SocietyFindPageActivity.this,AppConstants.SQL_VISION);
        ImageDatas = new ArrayList<>();
        ImagePath = new ArrayList<>();
        ImageData = new ArrayList<>();
        if(mes_select == 1){//进入
            mLinearLayout.getLayoutParams().height = mLinearLayout.getLayoutParams().WRAP_CONTENT;
            society_find_page_ok.setVisibility(View.INVISIBLE);
            society_find_page_tacv.setVisibility(View.INVISIBLE);
            society_find_page_time.setVisibility(View.VISIBLE);
            society_find_page_title.setEnabled(false);
            society_find_page_content.setEnabled(false);
//            society_find_page_title.setText(title);
//            society_find_page_content.setText(content);
//            society_find_page_time.setText(time);
//            ImageGetPath = new ArrayList<>();
//            getUser();
            getData();
        }else {//创建
            mLinearLayout.getLayoutParams().height = 0;
        }

        society_find_page_take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ImageDatas.size()>5){
                    Toast.makeText(SocietyFindPageActivity.this,"最多添加6张",Toast.LENGTH_LONG).show();
                }else {
                    take_photo();
                }

            }
        });
        society_find_page_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ImageDatas.size()>5){
                    Toast.makeText(SocietyFindPageActivity.this,"最多添加6张",Toast.LENGTH_LONG).show();
                }else {
                    select_photo();
                }
            }
        });

        society_find_page_ok.setOnClickListener(new ForbidClickListener() {
            @Override
            public void forbidClick(View v) {
                if(society_find_page_content.getText().toString().isEmpty()){
                    Toast.makeText(SocietyFindPageActivity.this,"内容不能为空",Toast.LENGTH_LONG).show();
                }else {
                    if(ImagePath.size() == 0){
                        Toast.makeText(SocietyFindPageActivity.this,"请添加一张物品图片，以便认领",Toast.LENGTH_LONG).show();
                    }else {
                        if(ImagePath.size()<6){
                            for(int i = 6-ImagePath.size(); i > 0; i--){
                                ImagePath.add(null);
                            }
                        }
                        UpdateData();
                    }

                }
            }
        });
    }

    //异步更新
//    private Handler handler_find = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message msg) {
//            // TODO Auto-generated method stub
//            switch (msg.what){
//                case UPDATE_FIND:{
//                    for(int i = 0; i<ImageGetPath.size(); i++){
//                        showLoadImage(ImageGetPath.get(i));
//                    }
//                    updateLoad();
//                    break;
//                }
//                case UPDATE_YEZHU:{
//                    mTextView.setText(yezhu);
//                    if(bitmap != null){
//                        mCircleImageView.setImageBitmap(bitmap);
//                    }else {
//                        mCircleImageView.setImageResource(R.mipmap.ic_launcher_round);
//                    }
//                    break;
//                }
//                default:
//                    break;
//            }
//            return false;
//        }
//    });

//    private void getUser(){
//        new Thread(){
//            public void run(){
//                try{
//                    Looper.prepare();
//                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
//                    if (conn != null) { //判断 如果返回不为空则说明链接成功 如果为null的话则连接失败 请检查你的 mysql服务器地址是否可用 以及数据库名是否正确 并且 用户名跟密码是否正确
//                        Log.d("调试", "连接成功,失物界面");
//                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
//                        //查找信息
//                        String sql_connect = "select * from user where user_phone = '" +
//                                phone +
//                                "'";
//                        ResultSet resultSet = stmt.executeQuery(sql_connect);
//                        resultSet.next();
//                        yezhu = resultSet.getString("user_name") + "(" +
//                                phone + ")";
//                        Blob blob = resultSet.getBlob("user_picture");
//                        if(blob != null){
//                            InputStream inputStream = blob.getBinaryStream();
//                            bitmap = DealBitmap.InputToBitmap(inputStream);
//                        }
//                        Message message = new Message();
//                        message.what = UPDATE_YEZHU;
//                        handler_find.sendMessage(message);
//                        resultSet.close();
//                        JDBCTools.releaseConnection(stmt,conn);
//                    }else {
//                        Log.d("调试", "连接失败，失物招领");
//                        Toast toast = Toast.makeText(SocietyFindPageActivity.this, "请检查网络", Toast.LENGTH_SHORT);
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

    //从本地读取数据
    @SuppressLint("SetTextI18n")
    private void getData(){
        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query("shiwu",null,"shiwu_id = ?",new String[]{String.valueOf(intent_data_id)},
                null,null,null);
        if(cursor.moveToFirst()){
            String name = cursor.getString(cursor.getColumnIndex("shiwu_name"));
            String phone = cursor.getString(cursor.getColumnIndex("shiwu_phone"));
            mTextView.setText(name + "(" + phone + ")");
            Cursor cursor_phone = sqLiteDatabase.query("user",null,
                    "user_phone = ?",new String[]{phone},null,null,null);
            if(cursor_phone != null){
                if(cursor_phone.moveToFirst()){
                    Bitmap picture = null;
                    //查找成员头像
                    byte[] bytes = null;
                    bytes = cursor_phone.getBlob(cursor_phone.getColumnIndex("user_picture"));
                    if(bytes != null){
                        picture = DealBitmap.byteToBit(bytes);
                        if(picture != null){
                            mCircleImageView.setImageBitmap(picture);
                        }else {
                            mCircleImageView.setImageResource(R.mipmap.ic_launcher_round);
                        }
                    }
                }
                cursor_phone.close();
            }
            society_find_page_title.setText(cursor.getString(cursor.getColumnIndex("shiwu_title")));
            society_find_page_content.setText(cursor.getString(cursor.getColumnIndex("shiwu_content")));
            society_find_page_time.setText(cursor.getString(cursor.getColumnIndex("shiwu_time")));
            String picture1 = cursor.getString(cursor.getColumnIndex("shiwu_picture1"));
            String picture2 = cursor.getString(cursor.getColumnIndex("shiwu_picture2"));
            String picture3 = cursor.getString(cursor.getColumnIndex("shiwu_picture3"));
            String picture4 = cursor.getString(cursor.getColumnIndex("shiwu_picture4"));
            String picture5 = cursor.getString(cursor.getColumnIndex("shiwu_picture5"));
            String picture6 = cursor.getString(cursor.getColumnIndex("shiwu_picture6"));
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
//        new Thread(){
//            public void run(){
//                try{
//                    Looper.prepare();
//                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
//                    if (conn != null) { //判断 如果返回不为空则说明链接成功 如果为null的话则连接失败 请检查你的 mysql服务器地址是否可用 以及数据库名是否正确 并且 用户名跟密码是否正确
//                        Log.d("调试", "连接成功,消息界面");
//                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
//                        //查找信息
//                        String sql_connect = "select * from shiwu where shiwu_id = '" +
//                                intent_data_id +
//                                "'";
//                        ResultSet resultSet = stmt.executeQuery(sql_connect);
//                        resultSet.next();
//                        Blob picture1 = resultSet.getBlob("shiwu_picture1");
//                        Blob picture2 = resultSet.getBlob("shiwu_picture2");
//                        Blob picture3 = resultSet.getBlob("shiwu_picture3");
//                        Blob picture4 = resultSet.getBlob("shiwu_picture4");
//                        Blob picture5 = resultSet.getBlob("shiwu_picture5");
//                        Blob picture6 = resultSet.getBlob("shiwu_picture6");
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
//                        System.out.println(ImageGetPath);
//                        Message message = new Message();
//                        message.what = UPDATE_FIND;
//                        handler_find.sendMessage(message);
//                        resultSet.close();
//                        JDBCTools.releaseConnection(stmt,conn);
//                    }else {
//                        Log.d("调试", "连接失败,消息界面");
//                        Toast toast = Toast.makeText(SocietyFindPageActivity.this, "请检查网络", Toast.LENGTH_SHORT);
//                        toast.show();
//                    }
//
//                }catch (SQLException e) {
//                    e.printStackTrace();
//                }
//                Looper.loop();
//            }
//        }.start();
    }

    //上传数据
    private void UpdateData(){
        final ProgressDialog progressDialog = ProgressDialog.show(SocietyFindPageActivity.this,"","正在上传",true);
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
                        String newmessage_sql_insert = "insert into shiwu (shiwu_name,shiwu_phone,shiwu_area,shiwu_time," +
                                "shiwu_title,shiwu_content,shiwu_picture1,shiwu_picture2," +
                                "shiwu_picture3,shiwu_picture4,shiwu_picture5,shiwu_picture6) values(?,?,?,?,?,?,?,?,?,?,?,?)";
                        preparedStatement = (java.sql.PreparedStatement)conn.prepareStatement(newmessage_sql_insert,Statement.RETURN_GENERATED_KEYS);
                        preparedStatement.setString(1, SharePreferences.getString(SocietyFindPageActivity.this, AppConstants.USER_NAME));
                        preparedStatement.setString(2, SharePreferences.getString(SocietyFindPageActivity.this, AppConstants.USER_PHONE));
                        preparedStatement.setString(3, SharePreferences.getString(SocietyFindPageActivity.this, AppConstants.USER_AREA));
                        preparedStatement.setString(4, getTime());
                        preparedStatement.setString(5, society_find_page_title.getText().toString());
                        preparedStatement.setString(6, society_find_page_content.getText().toString());
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
                        Intent intent_broad = new Intent(AppConstants.BROAD_FIND);
                        LocalBroadcastManager.getInstance(SocietyFindPageActivity.this).sendBroadcast(intent_broad);
                        progressDialog.dismiss();
                        SocietyFindPageActivity.this.finish();
                    }else {
                        Log.d("调试", "连接失败");
                        Toast toast = Toast.makeText(SocietyFindPageActivity.this, "请检查网络", Toast.LENGTH_SHORT);
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

    /*
  * 加载图片
  * */
//    private void showLoadImage(Bitmap bitmap){
//        ImageAdapter first = new ImageAdapter(ImageDatas);
//        ImageAdapter.Item_Image item_image = first.new Item_Image(bitmap);
//        ImageDatas.add(item_image);
//    }

    //加载网络图片的大图
//    private void updateLoad(){
//        society_find_page_rv.setLayoutManager(new GridLayoutManager(this,3));
//        ImageAdapter mAdapter = new ImageAdapter(ImageDatas);
//        society_find_page_rv.setAdapter(mAdapter);
//        mAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                Intent intent = new Intent(SocietyFindPageActivity.this,ShowImage.class);
//                Bundle bundle = new Bundle();
//                int pos = position+1;
//                bundle.putString("select_fenlei","shiwu");
//                bundle.putString("image_select_name","shiwu_picture"+pos);
//                bundle.putInt("image_select_id",1);
//                bundle.putInt("image_select_new",intent_data_id);
//                intent.putExtras(bundle);
//                startActivity(intent);
//            }
//        });
//    }

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

    //显示图片
    private void update(){
        society_find_page_rv.setLayoutManager(new GridLayoutManager(this,3));
        ImageAdapter mAdapter = new ImageAdapter(ImageDatas);
        society_find_page_rv.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(SocietyFindPageActivity.this,ShowImage.class);
                Bundle bundle = new Bundle();
                bundle.putString("image_select_name",ImageData.get(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }


    //获取系统时间，并进行格式转换
    private String getTime(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(new Date());
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
                showImage(DealBitmap.getRealFilePath(SocietyFindPageActivity.this,uri));
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
                        System.out.println("1"+ImagePath.size());
                        ImagePath.add(image_load_path);
                        System.out.println("1"+ImagePath.size());

                        // Glide.with(RegisterActivity.this).load(file).into(image);
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
}
