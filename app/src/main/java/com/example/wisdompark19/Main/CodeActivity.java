package com.example.wisdompark19.Main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.example.wisdompark19.BlueTooth.BluetoothService;
import com.example.wisdompark19.BlueTooth.DeviceListActivity;
import com.example.wisdompark19.R;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.DialogInterface.*;

/**
 * Created by 最美人间四月天 on 2018/1/18.
 */

public class CodeActivity extends AppCompatActivity {

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the services
    public static BluetoothService mService = null;

    private TextView codeBlue;




    private ImageView imageView;
    private ImageView imageView_f;
    private TextView code_cancel;
    private TextView code_ok;
    private TextView code_time;
    private EditText code_name;
    private String code_sex_select;
    private int TimeCheck = 0;

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
        regiestBroast();
        goBlueTooth();
    }


    private void findView(){
        imageView = (ImageView) findViewById(R.id.code_activity_iv);
        codeBlue = (TextView) findViewById(R.id.code_blue);
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

        LinearLayout li_face = (LinearLayout) findViewById(R.id.code_activity_li);
        li_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CodeActivity.this,FaceActivity.class);
                intent.putExtra("put_data_face","人脸录入");
                startActivity(intent);
            }
        });
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
            final String newContent = user_number + "," + user_name + "," + model + "," + time + "," + user_address + ","
                    + md5Content.substring(md5Content.length()-8);
            System.out.println(newContent);
            final Bitmap bitmap = QRCodeUtil.createQRCodeBitmap(newContent,640,640);
            imageView.setImageBitmap(bitmap);

            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    goBLue(newContent);
                    return false;
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
        code_time = (TextView)view.findViewById(R.id.code_auto_time);
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

        final String[] Items = new String[] {"一天", "一周", "一个月", "永久"};

        code_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(CodeActivity.this)
                        .setTitle("选择时间")
                        .setItems(Items, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TimeCheck = which;
                                code_time.setText(Items[which]);
                            }
                        }).create().show();
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
                    final String newContent = code_number + "," + code_name.getText().toString() + "," + "访客" + "," + getTime() + "," + "访客邀请" + ","
                            + md5Content.substring(md5Content.length()-8);
                    System.out.println(newContent);
                    final Bitmap bitmap = QRCodeUtil.createQRCodeBitmap(newContent,640,640);
                    imageView_f.setImageBitmap(bitmap);
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm.isActive()){
                        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(),0);

                    }

                    imageView_f.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            goBLue(newContent);
                            return false;
                        }
                    });
                }
            }
        });
    }


    //获取系统时间，并进行格式转换
    private String getTime(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date nowData = new Date();
        if(TimeCheck == 1){
            Date nowDate1 = new Date(nowData.getTime() + (long) 7 * 24 * 60 * 60 * 1000);
            return simpleDateFormat.format(nowDate1);
        }else if(TimeCheck == 2){
            Date nowDate2 = new Date(nowData.getTime() + (long) 30 * 24 * 60 * 60 * 1000);
            return simpleDateFormat.format(nowDate2);
        }else if(TimeCheck == 3){
            Date nowDate3 = new Date(nowData.getTime());
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat3 = new SimpleDateFormat("2099-MM-dd HH:mm:ss");
            return simpleDateFormat3.format(nowDate3);
        }else {
            return simpleDateFormat.format(nowData);
        }

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

    //连接蓝牙
    private void goBlueTooth(){
        codeBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initBlue();
            }
        });
    }

    private void initBlue(){
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "您的设备不支持蓝牙", Toast.LENGTH_LONG).show();
            return;
        }
        if(mBluetoothAdapter.isEnabled()) {
           initView();
        }else {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            initView();
        }

    }

    private void initView(){

        if (mService == null) {
            mService = new BluetoothService(CodeActivity.this, mHandler);
        }

        String items[] = {"连接设备", "断开"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(CodeActivity.this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0 :{
                        Intent serverIntent = new Intent(CodeActivity.this, DeviceListActivity.class);
                        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                        break;
                    }
                    case 1 :{
                        mService.stop();
                        break;
                    }
                    default :
                        break;
                }
            }
        });
        builder.create().show();
    }


    //蓝牙打印
    private void goBLue(final String content){
        String item[] = {"打印二维码"};
        new AlertDialog.Builder(CodeActivity.this)
                .setTitle(null)
                .setItems(item, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mService == null){
                            Toast.makeText(CodeActivity.this, "蓝牙没有连接", Toast.LENGTH_SHORT).show();
                            return;
                        }else if (mService.getState() != BluetoothService.STATE_CONNECTED) {
                            Toast.makeText(CodeActivity.this, "没有连接到设备", Toast.LENGTH_SHORT).show();
                            return;
                        }
//                        Intent intent = new Intent(CodeActivity.this, PrintActivity.class);
//                        intent.putExtra("code_content",content);
//                        startActivity(intent);
                        sendMessage(QRCodeUtil.createQRCodeBitmap(content,360,360));
                        sendMessage(" \n");
                        sendMessage(" \n");
                    }
                })
                .create().show();
    }

    /**
     * 打印
     * @param message
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, "蓝牙没有连接", Toast.LENGTH_SHORT).show();
            return;
        }
      //  mService.printCenter();
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothService to write
            byte[] send;
            try {
                send = message.getBytes("GB2312");
            } catch (UnsupportedEncodingException e) {
                send = message.getBytes();
            }
            mService.write(send);
        }
    }


    private void sendMessage(Bitmap bitmap) {
        // Check that we're actually connected before trying anything
        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, "蓝牙没有连接", Toast.LENGTH_SHORT).show();
            return;
        }
       // mService.printCenter();
        // 发送打印图片前导指令
        byte[] start = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1B,
                0x40, 0x1B, 0x33, 0x00 };
        mService.write(start);
        byte[] draw2PxPoint = DealBitmap.draw2PxPoint(bitmap);
        mService.write(draw2PxPoint);
        // 发送结束指令
        byte[] end = { 0x1d, 0x4c, 0x1f, 0x00 };
        mService.write(end);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "蓝牙已打开", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "蓝牙没有打开", Toast.LENGTH_LONG).show();
                    finish();
                }
        }
    }

    // The Handler that gets information back from the BluetoothService
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            codeBlue.setText("已连接");
                            Toast.makeText(CodeActivity.this,"长按二维码打印",Toast.LENGTH_LONG).show();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            codeBlue.setText("正在连接...");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            codeBlue.setText("连接蓝牙");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    //byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    //String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    //byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    //String readMessage = new String(readBuf, 0, msg.arg1);
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(CodeActivity.this, "连接至"
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(stateChangeReceiver);
        if (mService != null) {
            mService.stop();
        }
    }

    //监听蓝牙连接状态
    private void regiestBroast() {
        IntentFilter connectedFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(stateChangeReceiver, connectedFilter);
    }

    private BroadcastReceiver stateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int action = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR);
            switch (action) {
                case BluetoothAdapter.STATE_TURNING_OFF: {
                    System.out.println("没有吗");
                    if (mService != null) {
                        System.out.println("没有吗");
                        mService.stop();
                    }
                    break;
                }
                case BluetoothAdapter.STATE_ON: {
                //    initView();
                    break;
                }
            }
        }
    };
}
