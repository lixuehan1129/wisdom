package com.example.wisdompark19.Main;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.wisdompark19.BlueTooth.Bluetooth;
import com.example.wisdompark19.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by 最美人间四月天 on 2018/1/18.
 */

public class PeripheralActivity extends AppCompatActivity {
    private Button On,Off,Visible,list,service;
    private BluetoothAdapter bluetoothAdapter;
    private LinearLayout linearLayout;
    private Set<BluetoothDevice> pairedDevices;
    private ListView lv,lv1;
    private int broad = 0;
    private int conn = 0;
    private Bluetooth client;
    ArrayList<String> search;
    ArrayList<BluetoothDevice> add;
    ArrayList<String> own;
    ArrayList<BluetoothDevice> have;
    private static final int startService = 0;
    private static final int getMessageOk = 1;
    private static final int sendOver = 2;
    private UUID uuid = UUID.fromString("00001106-0000-1000-8000-00805F9B34FB");


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.peripheral_activity);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue)); //设置顶部系统栏颜色
        Intent intent = getIntent();
        String intent_data = intent.getStringExtra("put_data_waishe");
        Toolbar toolbar = (Toolbar)findViewById(R.id.per_mainTool); //标题栏
        toolbar.setNavigationIcon(R.mipmap.ic_back_white);
        toolbar.setTitle(intent_data);
        back(toolbar);
        findView();
    }

    private void findView(){
//        TextView textView = (TextView)findViewById(R.id.test_text);
//        textView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                if(bluetoothAdapter == null){
//                    Toast.makeText(PeripheralActivity.this,"设备不支持蓝牙",Toast.LENGTH_LONG).show();
//                }else {
//                    if (!bluetoothAdapter.isEnabled()) {
//                        //通过用于弹出选择对话框开启
//                        Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                        startActivityForResult(enabler, 100);
//                    }else {
//                        bluetoothAdapter.disable();
//                    }
//                    Intent enable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//                    enable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600); //3600为蓝牙设备可见时间 startActivity(enable);
//                }
//            }
//        });
        On = (Button)findViewById(R.id.button1);
        Off = (Button)findViewById(R.id.button2);
        Visible = (Button)findViewById(R.id.button3);
        list = (Button)findViewById(R.id.button4);
        service = (Button)findViewById(R.id.button6);
        lv = (ListView)findViewById(R.id.listView1);
        lv1 = (ListView)findViewById(R.id.listView2);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        linearLayout = (LinearLayout)findViewById(R.id.per_lin);
    }

    //开启蓝牙
    public void on(View view){
        if (!bluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(),"Turned on"
                    ,Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Already on",
                    Toast.LENGTH_LONG).show();
        }
    }

    //设置蓝牙可见
    public void visible(View view){
        Intent getVisible = new Intent(BluetoothAdapter.
                ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }

    //开启接收服务
    public void service(View view){
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }
        if(bluetoothAdapter.isEnabled()){
            getMessage();
        }else {
            Toast.makeText(PeripheralActivity.this,"请先打开蓝牙,设置手机可见",Toast.LENGTH_LONG).show();
        }
    }

    public void list(View view){
        linearLayout.setVisibility(View.VISIBLE);
        pairedDevices = bluetoothAdapter.getBondedDevices();
        if(pairedDevices.size()>0){
            own = new ArrayList<>();
            have = new ArrayList<>();
            for(BluetoothDevice bt : pairedDevices){
                own.add(bt.getName());
                have.add(bt);
            }
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>
                    (this,android.R.layout.simple_list_item_1, own);
            lv.setAdapter(adapter);
        }
        //蓝牙连接
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//               try {
//                   connect(have.get(position));
//               }catch (IOException e){
//                   e.printStackTrace();
//               }
               // sendMessage(position);
                sendMessage(position);

//                conn = 1;
//                client = new Bluetooth(have.get(position), handler);
//                try {
//                    client.connect();
//                } catch (Exception e) {
//                    Log.e("TAG", e.toString());
//                }

            }
        });

        //蓝牙搜索配对
        bluetoothAdapter.startDiscovery();
        search = new ArrayList<>();
        add = new ArrayList<>();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);    //绑定状态监听
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);     //搜索完成时监听
        registerReceiver(mReceiver, filter);
        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(add.get(position));
                if(bluetoothAdapter.isDiscovering()){
                    bluetoothAdapter.cancelDiscovery();
                }
                try {
                    //如果想要取消已经配对的设备，只需要将creatBond改为removeBond
                    Method method = BluetoothDevice.class.getMethod("createBond");
                    Log.e(getPackageName(), "开始配对");
                    method.invoke(add.get(position));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Bluetooth.CONNECT_FAILED: {
                    Toast.makeText(PeripheralActivity.this, "连接失败", Toast.LENGTH_LONG).show();
//                    try {
//                        client.connect();
//                    } catch (Exception e) {
//                        Log.e("TAG", e.toString());
//                    }
                    break;
                }
                case Bluetooth.CONNECT_SUCCESS:{
                    Toast.makeText(PeripheralActivity.this, "连接成功", Toast.LENGTH_LONG).show();
                    break;
                }
                case Bluetooth.READ_FAILED:
                    Toast.makeText(PeripheralActivity.this, "读取失败", Toast.LENGTH_LONG).show();
                    break;
                case Bluetooth.WRITE_FAILED:
                    Toast.makeText(PeripheralActivity.this, "写入失败", Toast.LENGTH_LONG).show();
                    break;
                case Bluetooth.DATA:
                    Toast.makeText(PeripheralActivity.this, msg.arg1 + "", Toast.LENGTH_LONG).show();
                    break;
                case Bluetooth.CONNECT_CLOSE:
                    handler.removeMessages(msg.what);
                    break;
            }
        }
    };

    //关闭蓝牙
    public void off(View view){
        linearLayout.setVisibility(View.INVISIBLE);
        if(conn == 1){
            client.setState(Bluetooth.CONNECT_CLOSE);
            client.close();
        }
        bluetoothAdapter.cancelDiscovery();
        bluetoothAdapter.disable();
        Toast.makeText(getApplicationContext(),"Turned off" ,
                Toast.LENGTH_LONG).show();
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            broad = 1;
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getName() != null){
                    if(!search.contains(device.getName())){
                        search.add(device.getName());
                        add.add(device);
                    }
                }
                if(search.size()>0){
                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>
                            (PeripheralActivity.this,android.R.layout.simple_list_item_1,search);
                    lv1.setAdapter(adapter);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d("MainActivity", "搜索结束");

            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_NONE:
                        Log.e(getPackageName(), "取消配对");
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.e(getPackageName(), "配对中");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.e(getPackageName(), "配对成功");
                        break;
                }
            }

        }
    };

    private void getMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream is = null;
                try {
                    BluetoothServerSocket serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("serverSocket", uuid);
                    mHandler.sendEmptyMessage(startService);
                    BluetoothSocket accept = serverSocket.accept();
                    is = accept.getInputStream();

                    byte[] bytes = new byte[1024];
                    int length = is.read(bytes);

                    Message msg = new Message();
                    msg.what = getMessageOk;
                    msg.obj = new String(bytes, 0, length);
                    mHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void sendMessage(final int i) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OutputStream os = null;
                try {
                    BluetoothSocket socket = have.get(i).createRfcommSocketToServiceRecord(uuid);
                    socket.connect();
                    os = socket.getOutputStream();
                    os.write("刘家兴还钱2".getBytes());
                    os.flush();
                    mHandler.sendEmptyMessage(sendOver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case startService:
                    Log.e("log","服务打开");
                    Toast.makeText(PeripheralActivity.this,"服务打开",Toast.LENGTH_LONG).show();
                    break;
                case getMessageOk:
                    Log.e("log","开始");
                    break;
                case sendOver:
                    Log.e("log","发送完毕");
                    break;
            }
        }
    };


    @Override
    public void onDestroy() {
        if(broad == 1){
            unregisterReceiver(mReceiver);
        }
        if(conn == 1){
            client.setState(Bluetooth.CONNECT_CLOSE);
            client.close();
        }
        super.onDestroy();
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
