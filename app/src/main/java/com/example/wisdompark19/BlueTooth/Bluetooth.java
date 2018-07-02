package com.example.wisdompark19.BlueTooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-7-7
 * Time: 下午2:02
 * To change this template use File | Settings | File Templates.
 */
public class Bluetooth {
    public static final int CONNECT_FAILED = 0;
    public static final int CONNECT_SUCCESS = 1;
    public static final int WRITE_FAILED = 2;
    public static final int READ_FAILED = 3;
    public static final int DATA = 4;
    public static final int CONNECT_CLOSE = 5;


    private BluetoothDevice device;
    private Handler handler;
    private static BluetoothSocket socket;

    public Bluetooth(BluetoothDevice device, Handler handler) {
        this.device = device;
        this.handler = handler;
    }

    public void connect() {
        new Thread() {
            public void run() {
                BluetoothSocket tmp = null;
                Method method;
                try {
                    method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                    tmp = (BluetoothSocket) method.invoke(device, 1);
                   // device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                } catch (Exception e) {
                    Log.e("CreateSocket", e.toString());
                }

                socket = tmp;
                new Thread(){
                    public void run(){
                        try {
                            socket.connect();
                            setState(CONNECT_SUCCESS);
                        }catch (IOException i){
                            i.printStackTrace();
                            setState(CONNECT_FAILED);
                            Log.e("Connect",i.toString());
                        }
                    }
                }.start();

            }
        }.start();
    }



    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            Log.e("Close", e.toString());
        }
    }

    public void setState(int state) {
        Message msg = handler.obtainMessage();
        msg.what = state;
        handler.sendMessage(msg);
    }
}
