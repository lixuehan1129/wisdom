package com.example.wisdompark19.FaceUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.Main.FaceActivity;
import com.example.wisdompark19.R;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.mysql.jdbc.Connection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FaceTestActivity extends AppCompatActivity {

    private final static String TAG = FaceTestActivity.class.getSimpleName();

    private Button face_test_create,face_test_join,face_test_delete,face_test_add,face_test_ide;
    private ImageView face_test_image;
    private TextView face_test_text;
    private String name,score;
    // 拍照得到的照片文件
    private File mPictureFile;
    // 选择图片后返回
    private ProgressDialog mProDialog;
    public static final int REQUEST_PICTURE_CHOOSE = 1;
    // 拍照后返回
    private final static int REQUEST_CAMERA_IMAGE = 2;
    // 裁剪图片成功后返回
    public static final int REQUEST_INTENT_CROP = 3;

    private Bitmap mImageBitmap = null;
    private byte[] mImageData = null;

    //采用身份识别接口进行在线人脸识别
    private IdentityVerifier mIdVerifier;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_test);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue));
        Intent intent = getIntent();
        String intent_data = intent.getStringExtra("put_data_test");
        Toolbar toolbar = (Toolbar)findViewById(R.id.face_test_mainTool);
        toolbar.setNavigationIcon(R.mipmap.ic_back_white);
        toolbar.setTitle(intent_data);
        setMenu(toolbar);
        // 对象初始化监听器
        mIdVerifier = IdentityVerifier.createVerifier(FaceTestActivity.this, new InitListener() {
            @Override
            public void onInit(int errorCode) {
                if (ErrorCode.SUCCESS == errorCode) {
                   // showTip("引擎初始化成功");
                } else {
                    showTip("引擎初始化失败，错误码：" + errorCode);
                }
            }
        });
        findView();
    }

    /**
     *退出当前Activity时被调用,调用之后Activity就结束了
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIdVerifier.destroy();
    }

    private void findView(){
        face_test_create = (Button) findViewById(R.id.face_test_create);
        face_test_join = (Button) findViewById(R.id.face_test_join);
        face_test_delete = (Button) findViewById(R.id.face_test_delete);
        face_test_image = (ImageView) findViewById(R.id.face_test_image);
        face_test_add = (Button) findViewById(R.id.face_test_add);
        face_test_ide = (Button) findViewById(R.id.face_test_ide);
        face_test_text = (TextView) findViewById(R.id.face_test_text);
        mProDialog = new ProgressDialog(FaceTestActivity.this);

        final String groupName = SharePreferences.getString(FaceTestActivity.this, AppConstants.USER_AREA) + "11";
        final String authId = SharePreferences.getString(FaceTestActivity.this,AppConstants.USER_PHONE);
        final String id = "4124828422";
        face_test_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FaceGroupManager.createGroup(FaceTestActivity.this,groupName,authId);
                System.out.println("加入的ID" + FaceGroupManager.createGroup(FaceTestActivity.this,groupName,authId));
            }
        });

        face_test_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FaceGroupManager.joinGroup(FaceTestActivity.this,id,authId);
            }
        });

        face_test_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FaceGroupManager.deleteGroup(FaceTestActivity.this,id,authId);
            }
        });

        face_test_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        face_test_ide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mImageData) {
                    mProDialog.setMessage("鉴别中...");
                    mProDialog.show();
                    // 清空参数
                    mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
                    // 设置业务场景
                    mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ifr");
                    // 设置业务类型
                    mIdVerifier.setParameter(SpeechConstant.MFV_SST, "identify");
                    // 设置监听器，开始会话
                    mIdVerifier.startWorking(new IdentityListener() {
                        @Override
                        public void onResult(IdentityResult identityResult, boolean b) {
                            Log.d(TAG, identityResult.getResultString());
                         //   dismissProDialog();
                            handleResult(identityResult);
                        }

                        @Override
                        public void onError(SpeechError speechError) {
                            dismissProDialog();
                            showTip(speechError.getPlainDescription(true));
                        }

                        @Override
                        public void onEvent(int i, int i1, int i2, Bundle bundle) {

                        }
                    });

                    // 子业务执行参数，若无可以传空字符传
                    StringBuffer params = new StringBuffer();
                    params.append(",group_id=" + id +",topc=3");
                    // 向子业务写入数据，人脸数据可以一次写入
                    mIdVerifier.writeData("ifr", params.toString(), mImageData, 0, mImageData.length);
                    // 写入完毕
                    mIdVerifier.stopWrite("ifr");
                } else {
                    showTip("请选择图片后再鉴别");
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

    protected void handleResult(IdentityResult result) {
        if (null == result) {
            return;
        }
        try {
            String resultStr = result.getResultString();
            JSONObject resultJson = new JSONObject(resultStr);
            if(ErrorCode.SUCCESS == resultJson.getInt("ret"))
            {
                try {
                    JSONObject obj = new JSONObject(resultStr);
                    // 组名称

                    JSONObject ifv_result = obj.getJSONObject("ifv_result");
                //    System.out.println("身份鉴别1"+ifv_result);
                    JSONArray candidates = ifv_result.getJSONArray("candidates");
                //    System.out.println("身份鉴别2"+candidates);
                    JSONObject face_ide = (JSONObject) candidates.get(0);
                //    System.out.println("身份鉴别3"+face_ide);
                    // 鉴别结果
                    String face_phone = face_ide.optString("user");
                    String face_score = face_ide.optString("score");
                    score = face_score;
                    connectName(face_phone);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                showTip("鉴别失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void connectName(final String face_phone){
        new Thread(){
            public void run(){
                try {
                    Looper.prepare();
                    Connection conn = JDBCTools.getConnection();
                    if (conn != null) { //判断
                        Log.d("调试", "连接成功");
                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
                        String sql_name = "select user_name from user where user_phone = '" +
                                face_phone +
                                "' limit 1";
                        ResultSet resultSet = stmt.executeQuery(sql_name);
                        if(resultSet.first()){
                            name = resultSet.getString("user_name");
                            Message message = new Message();
                            message.what = 1;
                            handler_face.sendMessage(message);
                        }
                        resultSet.close();
                        JDBCTools.releaseConnection(stmt,conn);
                        dismissProDialog();
                    }
                }catch (SQLException e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }.start();
    }

    private Handler handler_face = new Handler(new Handler.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case 1:{
                  face_test_text.setText("姓名:  " + name + "\n" + "相似度:  " + score);
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
        if (requestCode == REQUEST_PICTURE_CHOOSE ) {
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
            cropPicture(this,Uri.fromFile(new File(fileSrc)));
        } else if (requestCode == REQUEST_CAMERA_IMAGE) {
            if (null == mPictureFile) {
                showTip("拍照失败，请重试");
                return;
            }

            fileSrc = mPictureFile.getAbsolutePath();
            updateGallery(fileSrc);
            // 跳转到图片裁剪页面
            cropPicture(this,Uri.fromFile(new File(fileSrc)));
        } else if (requestCode == REQUEST_INTENT_CROP) {

            // 获取返回数据
            Bitmap bmp = data.getParcelableExtra("data");

            // 获取裁剪后图片保存路径
            fileSrc = getImagePath();

            // 若返回数据不为null，保存至本地，防止裁剪时未能正常保存
            if(null != bmp){
                saveBitmapToFile(bmp);
            }

            // 获取图片的宽和高
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            mImageBitmap = BitmapFactory.decodeFile(fileSrc, options);

            // 压缩图片
            options.inSampleSize = Math.max(1, (int) Math.ceil(Math.max(
                    (double) options.outWidth / 1024f,
                    (double) options.outHeight / 1024f)));
            options.inJustDecodeBounds = false;
            mImageBitmap = BitmapFactory.decodeFile(fileSrc, options);

            // 若mImageBitmap为空则图片信息不能正常获取
            if(null == mImageBitmap) {
                showTip("图片信息无法正常获取！");
                return;
            }

            // 部分手机会对图片做旋转，这里检测旋转角度
            int degree = readPictureDegree(fileSrc);
            if (degree != 0) {
                // 把图片旋转为正的方向
                mImageBitmap = rotateImage(degree, mImageBitmap);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            //可根据流量及网络状况对图片进行压缩
            mImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            mImageData = baos.toByteArray();

            face_test_image.setImageBitmap(mImageBitmap);
        }
    }


    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree 旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    private void updateGallery(String filename) {
        MediaScannerConnection.scanFile(this, new String[] {filename}, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    @Override
                    public void onScanCompleted(String path, Uri uri) {

                    }
                });
    }

    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotateImage(int angle, Bitmap bitmap) {
        // 图片旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 得到旋转后的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    /**
     * Toast弹出提示
     * @param str
     */
    private void showTip(final String str) {
        Toast.makeText(FaceTestActivity.this,str,Toast.LENGTH_LONG).show();
    }


    /***
     * 裁剪图片
     * @param activity Activity
     * @param uri 图片的Uri
     */
    public void cropPicture(Activity activity, Uri uri) {
        Intent innerIntent = new Intent("com.android.camera.action.CROP");
        innerIntent.setDataAndType(uri, "image/*");
        innerIntent.putExtra("crop", "true");// 才能出剪辑的小方框，不然没有剪辑功能，只能选取图片
        innerIntent.putExtra("aspectX", 1); // 放大缩小比例的X
        innerIntent.putExtra("aspectY", 1);// 放大缩小比例的X   这里的比例为：   1:1
        innerIntent.putExtra("outputX", 320);  //这个是限制输出图片大小
        innerIntent.putExtra("outputY", 320);
        innerIntent.putExtra("return-data", true);
        // 切图大小不足输出，无黑框
        innerIntent.putExtra("scale", true);
        innerIntent.putExtra("scaleUpIfNeeded", true);
        innerIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(getImagePath())));
        innerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        activity.startActivityForResult(innerIntent, REQUEST_INTENT_CROP);
    }

    /**
     * 设置保存图片路径
     * @return
     */
    private String getImagePath(){
        String path;
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        path =  Environment.getExternalStorageDirectory().getAbsolutePath() +"/MFVDemo/";
        File folder = new File(path);
        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }
        path += "mfvtest.jpg";
        return path;
    }

    /**
     * 保存Bitmap至本地
     * @param bmp
     */
    private void saveBitmapToFile(Bitmap bmp){
        String file_path = getImagePath();
        File file = new File(file_path);
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dismissProDialog() {
        if (null != mProDialog) {
            mProDialog.dismiss();
        }
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
}
