package com.example.wisdompark19.Main;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.DealBitmap;
import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.FaceUtil.FaceUtil;
import com.example.wisdompark19.R;
import com.example.wisdompark19.ViewHelper.DataBaseHelper;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.SpeechError;
import com.mysql.jdbc.Connection;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FaceActivity extends AppCompatActivity {

    private final String TAG = "FaceActivity";

    private final int REQUEST_PICTURE_CHOOSE = 1;
    private final int REQUEST_CAMERA_IMAGE = 2;
    public static final int UPDATE_FACE = 1;

    private Bitmap localImage = null;
    private Bitmap mImage = null;
    private byte[] mImageData = null;
    // authid为6-18个字符长度，用于唯一标识用户
    private String mAuthid = null;
    // 进度对话框
    private ProgressDialog mProDialog;

    // 拍照得到的照片文件
    private File mPictureFile;

    private DataBaseHelper dataBaseHelper;


    private ImageView face_image;
    private TextView face_add;
    private TextView face_rem;
    private TextView face_reg;

    private int face_sure = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_activity);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue));
        Intent intent = getIntent();
        String intent_data = intent.getStringExtra("put_data_face");
        Toolbar toolbar = (Toolbar)findViewById(R.id.face_mainTool);
        toolbar.setNavigationIcon(R.mipmap.ic_back_white);
        toolbar.setTitle(intent_data);
        setMenu(toolbar);
        findView();
        connectData();
    }

    /**
     *Activity创建或者从后台重新回到前台时被调用
     */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     *退出当前Activity时被调用,调用之后Activity就结束了
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mIdVerifier.destroy();
    }

    //初始化控件
    private void findView(){
        face_image = (ImageView) findViewById(R.id.face_image);
        face_add = (TextView) findViewById(R.id.face_add);
        face_rem = (TextView) findViewById(R.id.face_rem);
        face_reg = (TextView) findViewById(R.id.face_reg);
        dataBaseHelper = new DataBaseHelper(FaceActivity.this,AppConstants.SQL_VISION);
        //获取业主手机号，手机号为唯一，标识唯一用户
        mAuthid = SharePreferences.getString(FaceActivity.this, AppConstants.USER_PHONE);

        mProDialog = new ProgressDialog(this);
        mProDialog.setCancelable(true);
        mProDialog.setTitle("请稍后");

        ButtonClick();

        mProDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });

    }

    //点击事件
    private void ButtonClick(){
     //   int ret = ErrorCode.SUCCESS;

        //添加人脸照片
        face_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                face_sure = 1;
                takePhoto();
            }
        });
        //修改人脸信息
        face_rem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        //注册人脸信息
        face_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(mAuthid)){
                    if(null != mImageData){
                        mProDialog.setMessage("注册中...");
                        mProDialog.show();
                        saveData();
                    }else {
                        showTip("请选择照片后注册");
                    }
                }
            }
        });

    }





    private void takePhoto(){
        // 设置相机拍照后照片保存路径,获取 SD 卡根目录
        String saveDir = Environment.getExternalStorageDirectory() + "/com.example.wisdom.park/";
        // 新建目录
        File dir = new File(saveDir);
        if (! dir.exists()) {
            dir.mkdirs();
        }
        // 生成文件名
        SimpleDateFormat t = new SimpleDateFormat("yyyyMMddssSSS");
        String filename = "IMG_" + (t.format(new Date())) + ".jpg";
        mPictureFile = new File(saveDir, filename);
        // 启动拍照,并保存到临时文件
        Intent mIntent = new Intent();
        mIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        mIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPictureFile));
        mIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
        startActivityForResult(mIntent, REQUEST_CAMERA_IMAGE);
    }


    private void saveData(){
        new Thread(){
            public void run(){
                try{
                    Looper.prepare();
                    Connection conn = JDBCTools.getConnection();
                    if (conn != null) { //判断 如果返回不为空则说明链接成功 如果为null的话则连接失败 请检查你的 mysql服务器地址是否可用 以及数据库名是否正确 并且 用户名跟密码是否正确
                        Log.d("调试", "连接成功");
                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
                        //上传
                        java.sql.PreparedStatement preparedStatement = null;
                        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getWritableDatabase();
                        if(face_sure == 1){
                            String sql_saveData = "insert into face (face_phone,face_society,face_picture) " +
                                    "values(?,?,?)";
                            preparedStatement = (java.sql.PreparedStatement)conn.prepareStatement(sql_saveData,Statement.RETURN_GENERATED_KEYS);
                            preparedStatement.setString(1, mAuthid);
                            preparedStatement.setString(2, SharePreferences.getString(FaceActivity.this, AppConstants.USER_AREA));
                            if(mPictureFile.exists()){
                                try {
                                    InputStream inputStream = new FileInputStream(mPictureFile);
                                    preparedStatement.setBinaryStream(3,inputStream,mPictureFile.length());
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                            //注册前首先加入群组
                         //   FaceGroupManager.joinGroup(FaceActivity.this,"4124828422",mAuthid);
                            //信息保存本地
                            ContentValues values = new ContentValues();
                            values.put("face_phone",mAuthid);
                            values.put("face_society",SharePreferences.getString(FaceActivity.this, AppConstants.USER_AREA));
                            values.put("face_picture",DealBitmap.Bitmap2Bytes(mImage));
                            sqLiteDatabase.insert("face",null,values);
                        }else {
                            String sql_saveData = "update face set face_picture = ? where face_phone = ?";
                            preparedStatement = (java.sql.PreparedStatement)conn.prepareStatement(sql_saveData);
                            if(mPictureFile.exists()){
                                try {
                                    InputStream inputStream = new FileInputStream(mPictureFile);
                                    preparedStatement.setBinaryStream(1,inputStream,mPictureFile.length());
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }else if(localImage != null){
                                InputStream inputStream = null;
                                inputStream = DealBitmap.BitmapToInput(localImage);
                                preparedStatement.setBinaryStream(1,inputStream,mPictureFile.length());
                            }
                            preparedStatement.setString(2,mAuthid);
                            ContentValues values = new ContentValues();
                            if(mImage == null){
                                values.put("face_picture",DealBitmap.Bitmap2Bytes(localImage));
                            }else {
                                values.put("face_picture",DealBitmap.Bitmap2Bytes(mImage));
                            }
                            sqLiteDatabase.update("face",values,"face_phone = ?",
                                    new String[]{mAuthid});
                        }
                        sqLiteDatabase.close();
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        JDBCTools.releaseConnection(stmt,conn);
                        if (null != mProDialog) {
                            mProDialog.dismiss();
                        }//关闭进度框
                    }else {
                        Log.d("调试", "连接失败");
                        Toast toast = Toast.makeText(FaceActivity.this, "请检查网络", Toast.LENGTH_SHORT);
                        toast.show();

                        if (null != mProDialog) {
                            mProDialog.dismiss();
                        }
                    }
                }catch (SQLException e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }.start();
    }

    private void connectData(){
        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getWritableDatabase();
        //删除重复数据
        String delete = "delete from face where face_phone in (select face_phone from face group by face_phone having count(face_phone) > 1)";
        sqLiteDatabase.execSQL(delete);

        if(!mAuthid.isEmpty()){
            @SuppressLint("Recycle") Cursor cursor = sqLiteDatabase.query("face",new String[]{"face_picture"},"face_phone = ?",
                    new String[]{mAuthid},null,null,null,"1");
            if(cursor.getCount() == 0){
                connectNewData();
                face_rem.getLayoutParams().height = 0;
                face_add.getLayoutParams().height = Toolbar.LayoutParams.WRAP_CONTENT;
            }else {
                while (cursor.moveToNext()){
                    byte[] bytes = cursor.getBlob(cursor.getColumnIndex("face_picture"));
                    if(bytes != null){
                        localImage = DealBitmap.byteToBit(bytes);
                        face_image.setImageBitmap(localImage);
                    }
                }
                face_add.getLayoutParams().height = 0;
                face_rem.getLayoutParams().height = Toolbar.LayoutParams.WRAP_CONTENT;
            }
            cursor.close();
        }
        sqLiteDatabase.close();
    }

    private void connectNewData(){
        final ProgressDialog progressDialog = ProgressDialog.show(FaceActivity.this,"","正在检查更新...",true);
        new Thread(){
            public void run(){
                try{
                    Looper.prepare();
                    Connection conn = JDBCTools.getConnection();
                    if (conn != null) { //判断 如果返回不为空则说明链接成功 如果为null的话则连接失败 请检查你的 mysql服务器地址是否可用 以及数据库名是否正确 并且 用户名跟密码是否正确
                        Log.d("调试", "连接成功,人脸信息");
                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
                        //查找信息
                        String sql_connect = "select * from face where face_phone = '" +
                                mAuthid +
                                "' limit 1";
                        ResultSet resultSet = stmt.executeQuery(sql_connect);
                        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getWritableDatabase();
                        if (resultSet.first()){
                            ContentValues values = new ContentValues();
                            Blob picture = resultSet.getBlob("face_picture");
                            if(picture != null){
                                values.put("face_phone",resultSet.getString("face_phone"));
                                values.put("face_society",resultSet.getString("face_society"));
                                values.put("face_picture",DealBitmap.blobToBytes(picture));
                                sqLiteDatabase.insert("face",null,values);
                            }
                            Message message = new Message();
                            message.what = UPDATE_FACE;
                            handler_face.sendMessage(message);
                        }else {
                            showTip("您还没有添加人脸信息！");
                        }
                        sqLiteDatabase.close();
                        resultSet.close();
                        JDBCTools.releaseConnection(stmt,conn);
                        progressDialog.dismiss();
                    }else {
                        Log.d("调试", "连接失败,人脸信息");
                        Toast toast = Toast.makeText(FaceActivity.this, "请检查网络", Toast.LENGTH_SHORT);
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

    private Handler handler_face = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case UPDATE_FACE:{
                    connectData();
                    break;
                }
                default:
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        String fileSrc = null;
        if (requestCode == REQUEST_PICTURE_CHOOSE) {
            if ("file".equals(data.getData().getScheme())) {
                // 有些低版本机型返回的Uri模式为file
                fileSrc = data.getData().getPath();
            } else {
                // Uri模型为content
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(data.getData(), proj,
                        null, null, null);
                cursor.moveToFirst();
                int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                fileSrc = cursor.getString(idx);
                cursor.close();
            }
            // 跳转到图片裁剪页面
            FaceUtil.cropPicture(this,Uri.fromFile(new File(fileSrc)));
        } else if (requestCode == REQUEST_CAMERA_IMAGE) {
            if (null == mPictureFile) {
                showTip("拍照失败，请重试");
                return;
            }

            fileSrc = mPictureFile.getAbsolutePath();
            updateGallery(fileSrc);
            // 跳转到图片裁剪页面
            FaceUtil.cropPicture(this,Uri.fromFile(new File(fileSrc)));
        } else if (requestCode == FaceUtil.REQUEST_CROP_IMAGE) {
            // 获取返回数据
            Bitmap bmp = data.getParcelableExtra("data");
            // 若返回数据不为null，保存至本地，防止裁剪时未能正常保存
            if(null != bmp){
                FaceUtil.saveBitmapToFile(FaceActivity.this, bmp);
            }
            // 获取图片保存路径
            fileSrc = FaceUtil.getImagePath(FaceActivity.this);
            // 获取图片的宽和高
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            mImage = BitmapFactory.decodeFile(fileSrc, options);

            // 压缩图片
            options.inSampleSize = Math.max(1, (int) Math.ceil(Math.max(
                    (double) options.outWidth / 1024f,
                    (double) options.outHeight / 1024f)));
            options.inJustDecodeBounds = false;
            mImage = BitmapFactory.decodeFile(fileSrc, options);


            // 若mImageBitmap为空则图片信息不能正常获取
            if(null == mImage) {
                showTip("图片信息无法正常获取！");
                return;
            }

            // 部分手机会对图片做旋转，这里检测旋转角度
            int degree = FaceUtil.readPictureDegree(fileSrc);
            if (degree != 0) {
                // 把图片旋转为正的方向
                mImage = FaceUtil.rotateImage(degree, mImage);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            //可根据流量及网络状况对图片进行压缩
            mImage.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            mImageData = baos.toByteArray();

            face_image.setImageBitmap(mImage);
        }

    }

    @Override
    public void finish() {
        if (null != mProDialog) {
            mProDialog.dismiss();
        }
        super.finish();
    }

    private void updateGallery(String filename) {
        MediaScannerConnection.scanFile(this, new String[] {filename}, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    @Override
                    public void onScanCompleted(String path, Uri uri) {

                    }
                });
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showTip(final String str) {
        Toast.makeText(FaceActivity.this,str,Toast.LENGTH_LONG).show();
    }
}
