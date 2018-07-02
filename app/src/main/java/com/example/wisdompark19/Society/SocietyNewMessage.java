package com.example.wisdompark19.Society;


import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.wisdompark19.Adapter.NoticeItemAdapter;
import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.DealBitmap;
import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.AutoProject.TimeChange;
import com.example.wisdompark19.R;
import com.example.wisdompark19.ViewHelper.BaseFragment;
import com.example.wisdompark19.ViewHelper.DataBaseHelper;
import com.mysql.jdbc.Connection;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 最美人间四月天 on 2018/1/23.
 */

public class SocietyNewMessage extends BaseFragment {

    private static final String TAG = SocietyFragment.class.getSimpleName();
    private DataBaseHelper dataBaseHelper;
    private LocalBroadcastManager broadcastManager;
    private IntentFilter intentFilter;
    private BroadcastReceiver mReceiver;
    private List<NoticeItemAdapter.Notice_item> Data;
    private RecyclerView.LayoutManager mLayoutManager;
    private NoticeItemAdapter mNoticeItemAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    public static final int UPDATE_CONNECT = 1;

    ArrayList<String> card_message_tell;
    ArrayList<String> card_message_content;
    ArrayList<String> card_message_time;
    ArrayList<Bitmap> card_message_image;
    ArrayList<Integer> card_message_id;
    ArrayList<Integer> card_message_xin;

    @Override
    public void onStart(){
        super.onStart();
        localData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.society_new_message, container, false);
        findView(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findView(getView());
        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstants.BROAD_MES);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent){
                //收到广播后所作的操作
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                });
                connectData();
            }
        };
        broadcastManager.registerReceiver(mReceiver, intentFilter);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        broadcastManager.unregisterReceiver(mReceiver);
    }


    @Override
    protected void onFragmentFirstVisible() {
        localData();
        //去服务器下载数据
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        connectData();
    }

    private void findView(View view){
        dataBaseHelper = new DataBaseHelper(getActivity(),AppConstants.SQL_VISION);
        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.society_new_message_sr);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.rv_notice_item);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                connectData();
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        // TODO Auto-generated method stub
//                        mSwipeRefreshLayout.setRefreshing(false);
//                    }
//                }, 6000);
            }
        });
    }

    private void localData(){
        int unRead = 1;
        card_message_tell = new ArrayList<>();
        card_message_content = new ArrayList<>();
        card_message_time = new ArrayList<>();
        card_message_image = new ArrayList<>();
        card_message_id = new ArrayList<>();
        card_message_xin = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();

        //删除重复数据
        String delete = "delete from newmessage where newmessage_id in (select newmessage_id from newmessage group by newmessage_id having count(newmessage_id) > 1)";
        sqLiteDatabase.execSQL(delete);

        Cursor cursor = sqLiteDatabase.query("newmessage",null,"newmessage_area = ?",new String[]{
                SharePreferences.getString(getActivity(),AppConstants.USER_AREA)
        },null,null,"newmessage_id desc");
        while (cursor.moveToNext()){
            //从本地数据库读取
            String phone = cursor.getString(cursor.getColumnIndex("newmessage_phone"));
            String title = cursor.getString(cursor.getColumnIndex("newmessage_title"));
            String content = cursor.getString(cursor.getColumnIndex("newmessage_content"));
            String time = cursor.getString(cursor.getColumnIndex("newmessage_time"));
            int id = cursor.getInt(cursor.getColumnIndex("newmessage_id"));
            int xin = cursor.getInt(cursor.getColumnIndex("newmessage_xin"));
            Cursor cursor_phone = sqLiteDatabase.query("user",null,
                    "user_phone = ?",new String[]{phone},null,null,null);
            Bitmap picture = null;
            if(cursor_phone != null){
                while (cursor_phone.moveToNext()){
                    //查找成员头像
                    byte[] bytes = null;
                    bytes = cursor_phone.getBlob(cursor_phone.getColumnIndex("user_picture"));
                    if(bytes != null){
                        picture = DealBitmap.byteToBit(bytes);
                    }
                }
                cursor_phone.close();
            }
            if(xin != 1){
                unRead++;
            }
            initRollData(title,content,time,id,picture,xin);
        }
        cursor.close();
        sqLiteDatabase.close();

        Intent intent_broad = new Intent(AppConstants.BROAD_UNREAD);
        intent_broad.putExtra("unread",unRead);
        System.out.println("sdsadasdsa");
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent_broad);
        //执行事件
        initData();
        setAdapter();
        setItemClick();
    }

    //异步更新
    private Handler handler_connect = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case UPDATE_CONNECT:{
                    localData();
                    mSwipeRefreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });
                    break;
                }
                default:
                    break;
            }
            return false;
        }
    });

    private void connectData(){
        new Thread(){
            public void run(){
                try{
//                    card_message_tell = new ArrayList<>();
//                    card_message_content = new ArrayList<>();
//                    card_message_time = new ArrayList<>();
//                    card_message_image = new ArrayList<>();
//                    card_message_id = new ArrayList<>();
                    Looper.prepare();
                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
                    if (conn != null) { //判断 如果返回不为空则说明链接成功 如果为null的话则连接失败 请检查你的 mysql服务器地址是否可用 以及数据库名是否正确 并且 用户名跟密码是否正确
                        Log.d("调试", "连接成功,新的消息");
                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
                        //查找信息
                        String sql_connect = "select * from newmessage where newmessage_area = '" +
                                SharePreferences.getString(getActivity(), AppConstants.USER_AREA) +
                                "' order by newmessage_id desc limit 10";
                        ResultSet resultSet = stmt.executeQuery(sql_connect);
//                        List<String> content_name = new ArrayList<>();
                        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();
                        while (resultSet.next()){
                            if(!card_message_id.contains(resultSet.getInt("newmessage_id"))){
                                ContentValues values = new ContentValues();
                                int id = resultSet.getInt("newmessage_id");
                                values.put("newmessage_id",id);
                                values.put("newmessage_name",resultSet.getString("newmessage_name"));
                                values.put("newmessage_phone",resultSet.getString("newmessage_phone"));
                                values.put("newmessage_time",resultSet.getString("newmessage_time"));
                                values.put("newmessage_title",resultSet.getString("newmessage_title"));
                                values.put("newmessage_area",resultSet.getString("newmessage_area"));
                                values.put("newmessage_content",resultSet.getString("newmessage_content"));
                                values.put("newmessage_xin",0);
                                Blob picture1 = resultSet.getBlob("newmessage_picture1");
                                if(picture1 != null){
                                    values.put("newmessage_picture1",DealBitmap.compressImage(picture1,"_picture1_message"+id));
                                }else {
                                    values.put("newmessage_picture1", (String) null);
                                }
                                Blob picture2 = resultSet.getBlob("newmessage_picture2");
                                if(picture2 != null){
                                    values.put("newmessage_picture2",DealBitmap.compressImage(picture2,"_picture2_message"+id));
                                }else {
                                    values.put("newmessage_picture2", (String) null);
                                }
                                Blob picture3 = resultSet.getBlob("newmessage_picture3");
                                if(picture3 != null){
                                    values.put("newmessage_picture3",DealBitmap.compressImage(picture3,"_picture3_message"+id));
                                }else {
                                    values.put("newmessage_picture3", (String) null);
                                }
                                Blob picture4 = resultSet.getBlob("newmessage_picture4");
                                if(picture4 != null){
                                    values.put("newmessage_picture4",DealBitmap.compressImage(picture4,"_picture4_message"+id));
                                }else {
                                    values.put("newmessage_picture4", (String) null);
                                }
                                Blob picture5 = resultSet.getBlob("newmessage_picture5");
                                if(picture5 != null){
                                    values.put("newmessage_picture5",DealBitmap.compressImage(picture5,"_picture5_message"+id));
                                }else {
                                    values.put("newmessage_picture5", (String) null);
                                }
                                Blob picture6 = resultSet.getBlob("newmessage_picture6");
                                if(picture6 != null){
                                    values.put("newmessage_picture6",DealBitmap.compressImage(picture6,"_picture6_message"+id));
                                }else {
                                    values.put("newmessage_picture6", (String) null);
                                }
                                sqLiteDatabase.insert("newmessage",null,values);
                            }
//                            content_name.add(resultSet.getString("newmessage_phone"));
//                            initRollData(resultSet.getString("newmessage_title"),
//                                    resultSet.getString("newmessage_content"),
//                                    resultSet.getString("newmessage_time"),
//                                    resultSet.getInt("newmessage_id"));
                        }
                        sqLiteDatabase.close();
//                        for(int i = 0; i<content_name.size(); i++){
//                            String sql_content_name = "select * from user where user_phone = '" +
//                                    content_name.get(i) +
//                                    "'";
//                            ResultSet resultSet_content_name = stmt.executeQuery(sql_content_name);
//                            resultSet_content_name.next();
//                            Bitmap picture_path = null;
//                            Blob content_picture = resultSet_content_name.getBlob("user_picture");
//                            if(content_picture != null){
//                                InputStream inputStream = content_picture.getBinaryStream();
//                                picture_path = DealBitmap.InputToBitmap(inputStream);
//                            }
//                            card_message_image.add(picture_path); //发布者头像
//                            resultSet_content_name.close();
//                        }
                        Message message = new Message();
                        message.what = UPDATE_CONNECT;
                        handler_connect.sendMessage(message);
                        resultSet.close();
                        JDBCTools.releaseConnection(stmt,conn);
                    }else {
                        Log.d("调试", "连接失败,新的消息");
                        Toast toast = Toast.makeText(getActivity(), "请检查网络", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                }catch (SQLException e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }.start();
    }


    private void initRollData(String title, String content, String time, int id, Bitmap picture,int xin){
        if(title.isEmpty()){
            card_message_tell.add(content);
        }else {
            card_message_tell.add(title);
        }
        card_message_content.add(content);
        card_message_time.add(TimeChange.StringToString(time));
        card_message_id.add(id);
        card_message_image.add(picture);
        card_message_xin.add(xin);
    }

    private void initData(){
        Data = new ArrayList<>();
        for(int i=0; i<card_message_content.size(); i++){
            NoticeItemAdapter newData = new NoticeItemAdapter(Data);
            NoticeItemAdapter.Notice_item notice_item = newData.new Notice_item(card_message_tell.get(i),
                    card_message_content.get(i),card_message_time.get(i),card_message_image.get(i),
                    card_message_id.get(i),card_message_xin.get(i));
            Data.add(notice_item);
        }
    }

    private void setAdapter(){
        mNoticeItemAdapter = new NoticeItemAdapter(Data);
        mRecyclerView.setAdapter(mNoticeItemAdapter);
    }

    private void setItemClick(){
        mNoticeItemAdapter.setmOnItemClickListener(new NoticeItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
 //               Toast toast=Toast.makeText(getActivity(), card_message_tell.get(position), Toast.LENGTH_SHORT);
 //               toast.show();
                Intent intent = new Intent(getActivity(), SocietyNewMessagePage.class);
                intent.putExtra("put_data_mes_id",card_message_id.get(position));
                intent.putExtra("put_data_mes_select",1);
                startActivity(intent);
            }
        });
    }
}
