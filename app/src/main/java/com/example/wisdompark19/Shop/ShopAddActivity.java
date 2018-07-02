package com.example.wisdompark19.Shop;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.wisdompark19.Adapter.ImageAdapter;
import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.DealBitmap;
import com.example.wisdompark19.AutoProject.ForbidClickListener;
import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.R;
import com.example.wisdompark19.ViewHelper.ShowImage;
import com.mysql.jdbc.Connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

import static com.example.wisdompark19.AutoProject.AbsolutePath.getImageAbsolutePath;

/**
 * Created by ROBOSOFT on 2018/4/19.
 */

public class ShopAddActivity extends AppCompatActivity {
    private Uri photoUri;
    private List<ImageAdapter.Item_Image> ImageDatas;
    private List<String> ImagePath;
    private List<String> ImageData;
    private EditText shop_add_title;
    private EditText shop_add_content;
    private EditText shop_add_price;
    private RecyclerView shop_add_rv;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shop_add);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue)); //设置顶部系统栏颜色
        Toolbar toolbar = (Toolbar)findViewById(R.id.shop_add_mainTool); //标题栏
        toolbar.setNavigationIcon(R.mipmap.ic_back_white);
        toolbar.setTitle("发布商品信息");
        back(toolbar);
        findView();
    }

    private void findView(){
        shop_add_title = (EditText) findViewById(R.id.shop_add_title);
        shop_add_content = (EditText)findViewById(R.id.shop_add_content);
        shop_add_price = (EditText)findViewById(R.id.shop_add_price);
        shop_add_rv = (RecyclerView) findViewById(R.id.shop_add_rv);
        Button shop_add_ok = (Button) findViewById(R.id.shop_add_ok);
        ImageView shop_add_take = (ImageView) findViewById(R.id.shop_add_take);
        ImageView shop_add_add = (ImageView) findViewById(R.id.shop_add_add);

        ImageDatas = new ArrayList<>();
        ImagePath = new ArrayList<>();
        ImageData = new ArrayList<>();

        shop_add_take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ImageDatas.size()>5){
                    Toast.makeText(ShopAddActivity.this,"最多添加6张",Toast.LENGTH_LONG).show();
                }else {
                    take_photo();
                }

            }
        });
        shop_add_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ImageDatas.size()>5){
                    Toast.makeText(ShopAddActivity.this,"最多添加6张",Toast.LENGTH_LONG).show();
                }else {
                    select_photo();
                }
            }
        });

        shop_add_ok.setOnClickListener(new ForbidClickListener() {

            @Override
            public void forbidClick(View v) {
                if(shop_add_content.getText().toString().isEmpty() || shop_add_title.getText().toString().isEmpty() ||
                        shop_add_price.getText().toString().isEmpty()){
                    Toast.makeText(ShopAddActivity.this,"内容不能为空",Toast.LENGTH_LONG).show();
                }else {
                    if(ImagePath.size() == 0){
                        Toast.makeText(ShopAddActivity.this,"请添加商品图片",Toast.LENGTH_LONG).show();
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

    private void UpdateData(){
        final ProgressDialog progressDialog = ProgressDialog.show(ShopAddActivity.this,"","正在上传",true);
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
                        String shop_sql_insert = "insert into shop (shop_name,shop_phone,shop_area,shop_time," +
                                "shop_title,shop_content,shop_price,shop_picture1,shop_picture2," +
                                "shop_picture3,shop_picture4,shop_picture5,shop_picture6) " +
                                "values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
                        preparedStatement = (java.sql.PreparedStatement)conn.prepareStatement(shop_sql_insert,Statement.RETURN_GENERATED_KEYS);
                        preparedStatement.setString(1, SharePreferences.getString(ShopAddActivity.this, AppConstants.USER_NAME));
                        preparedStatement.setString(2, SharePreferences.getString(ShopAddActivity.this, AppConstants.USER_PHONE));
                        preparedStatement.setString(3, SharePreferences.getString(ShopAddActivity.this, AppConstants.USER_AREA));
                        preparedStatement.setString(4, getTime());
                        preparedStatement.setString(5, shop_add_title.getText().toString());
                        preparedStatement.setString(6, shop_add_content.getText().toString());
                        preparedStatement.setString(7, shop_add_price.getText().toString());
                        for(int i = 0; i < 6; i++){
                            File file = null;
                            System.out.println(ImagePath.get(i));
                            if(ImagePath.get(i) != null){
                                file = new File(ImagePath.get(i));
                                if(file.exists()){
                                    try {
                                        InputStream inputStream = new FileInputStream(file);
                                        preparedStatement.setBinaryStream(8+i,inputStream,file.length());
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    preparedStatement.setBinaryStream(8+i,null, 0);
                                }
                            }else {
                                preparedStatement.setBinaryStream(8+i,null, 0);
                            }
                        }
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        JDBCTools.releaseConnection(stmt,conn);
                        Intent intent_broad = new Intent(AppConstants.BROAD_SHOP);
                        LocalBroadcastManager.getInstance(ShopAddActivity.this).sendBroadcast(intent_broad);
                        progressDialog.dismiss();
                        finish();
                    }else {
                        Log.d("调试", "连接失败");
                        Toast toast = Toast.makeText(ShopAddActivity.this, "请检查网络", Toast.LENGTH_SHORT);
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
                showImage(DealBitmap.getRealFilePath(ShopAddActivity.this,uri));
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
   * 加载图片
   * */
    private void showImage(String image_Path){
        File file = new File(image_Path);
        compressWithLs(file);
        ImageAdapter first = new ImageAdapter(ImageDatas);
        ImageAdapter.Item_Image item_image = first.new Item_Image(image_Path);
        ImageData.add(image_Path);
        ImageDatas.add(item_image);
        update();
    }

    private void update(){
        shop_add_rv.setLayoutManager(new GridLayoutManager(this,3));
        ImageAdapter mAdapter = new ImageAdapter(ImageDatas);
        shop_add_rv.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(ShopAddActivity.this,ShowImage.class);
                Bundle bundle = new Bundle();
                bundle.putString("image_select_name",ImageData.get(position));
                bundle.putInt("image_select_id",0);
                bundle.putInt("image_select_new",0);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
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
