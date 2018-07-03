package com.example.wisdompark19.Main;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.FaceUtil.FaceUtil;
import com.example.wisdompark19.R;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FaceActivity extends AppCompatActivity {

    private final String TAG = "FaceActivity";

    private final int REQUEST_PICTURE_CHOOSE = 1;
    private final int REQUEST_CAMERA_IMAGE = 2;

    private Bitmap mImage = null;
    private byte[] mImageData = null;
    // authid为6-18个字符长度，用于唯一标识用户
    private String mAuthid = null;
    private Toast mToast;
    // 进度对话框
    private ProgressDialog mProDialog;

    // 拍照得到的照片文件
    private File mPictureFile;
    // FaceRequest对象，集成了人脸识别的各种功能
    //private FaceRequest mFaceRequest;

    //采用身份识别接口进行在线人脸识别
    private IdentityVerifier mIdVerifier;

    // 模型操作
    private int mModelCmd;
    // 删除模型
    private final static int MODEL_DEL = 1;

    private ImageView face_image;
    private Button face_add;
    private Button face_rem;
    private Button face_reg;

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
    }

    //初始化控件
    private void findView(){
        face_image = (ImageView) findViewById(R.id.face_image);
        face_add = (Button) findViewById(R.id.face_add);
        face_rem = (Button) findViewById(R.id.face_rem);
        face_reg = (Button) findViewById(R.id.face_reg);
        //获取业主手机号，手机号为唯一，标识唯一用户
        mAuthid = SharePreferences.getString(FaceActivity.this, AppConstants.USER_PHONE);

        mProDialog = new ProgressDialog(this);
        mProDialog.setCancelable(true);
        mProDialog.setTitle("请稍后");

        mProDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // cancel进度框时,取消正在进行的操作
                if (null != mIdVerifier) {
                    mIdVerifier.cancel();
                }
            }
        });

        mIdVerifier = IdentityVerifier.createVerifier(FaceActivity.this, new InitListener() {
            @Override
            public void onInit(int errorCode) {
                if (ErrorCode.SUCCESS == errorCode) {
                    showTip("引擎初始化成功");
                } else {
                    showTip("引擎初始化失败，错误码：" + errorCode);
                }
            }
        });

        ButtonClick();
    }

    //点击事件
    private void ButtonClick(){
        int ret = ErrorCode.SUCCESS;

        //添加人脸照片
        face_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        //修改人脸信息
        face_rem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 人脸模型删除
                mModelCmd = MODEL_DEL;
                executeModelCommand("delete");
            }
        });
        //注册人脸信息
        face_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(mAuthid)){
                    if (null != mImageData) {
                        mProDialog.setMessage("注册中...");
                        mProDialog.show();
                        // 设置用户标识，格式为6-18个字符（由字母、数字、下划线组成，不得以数字开头，不能包含空格）。
                        // 当不设置时，云端将使用用户设备的设备ID来标识终端用户。
                        // 设置人脸注册参数
                        // 清空参数
                        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
                        // 设置会话场景
                        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ifr");
                        // 设置会话类型
                        mIdVerifier.setParameter(SpeechConstant.MFV_SST, "enroll");
                        // 设置用户id
                        mIdVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthid);
                        // 设置监听器，开始会话
                        mIdVerifier.startWorking(mEnrollListener);

                        // 子业务执行参数，若无可以传空字符传
                        StringBuffer params = new StringBuffer();
                        // 向子业务写入数据，人脸数据可以一次写入
                        mIdVerifier.writeData("ifr", params.toString(), mImageData, 0, mImageData.length);
                        // 停止写入
                        mIdVerifier.stopWrite("ifr");
                    } else {
                        showTip("请选择图片后再注册");
                    }
                }
            }
        });

        if( ErrorCode.SUCCESS != ret ){
            mProDialog.dismiss();
            showTip( "出现错误："+ret );
        }
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



    //移除人脸模型
    private void executeModelCommand(String cmd) {
        // 设置人脸模型操作参数
        // 清空参数
        mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
        // 设置会话场景
        mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ifr");
        // 用户id
        mIdVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthid);

        // 设置模型参数，若无可以传空字符传
        StringBuffer params = new StringBuffer();
        // 执行模型操作
        mIdVerifier.execute("ifr", cmd, params.toString(), mModelListener);
    }


    /**
     * 人脸注册监听器
     */
    private IdentityListener mEnrollListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d(TAG, result.getResultString());

            if (null != mProDialog) {
                mProDialog.dismiss();
            }

            try {
                JSONObject object = new JSONObject(result.getResultString());
                int ret = object.getInt("ret");

                if (ErrorCode.SUCCESS == ret) {
                    showTip("注册成功");
                }else {
                    showTip(new SpeechError(ret).getPlainDescription(true));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }

        @Override
        public void onError(SpeechError error) {
            if (null != mProDialog) {
                mProDialog.dismiss();
            }

            showTip(error.getPlainDescription(true));
        }

    };


    /**
     * 人脸模型操作监听器,移除
     */
    private IdentityListener mModelListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d(TAG, result.getResultString());

            JSONObject jsonResult = null;
            int ret = ErrorCode.SUCCESS;
            try {
                jsonResult = new JSONObject(result.getResultString());
                ret = jsonResult.getInt("ret");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // 根据操作类型判断结果类型
            switch (mModelCmd) {
                case MODEL_DEL:
                    if (ErrorCode.SUCCESS == ret) {
                        showTip("删除成功");
                        takePhoto();
                    } else {
                        showTip("删除失败");
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }

        @Override
        public void onError(SpeechError error) {
            // 弹出错误信息
            showTip(error.getPlainDescription(true));
        }

    };



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
        mToast.setText(str);
        mToast.show();
    }
}
