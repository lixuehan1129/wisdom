package com.example.wisdompark19.Society;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import com.example.wisdompark19.Adapter.SocietyFindAdapter;
import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.DealBitmap;
import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.R;
import com.example.wisdompark19.ViewHelper.BaseFragment;
import com.example.wisdompark19.ViewHelper.DataBaseHelper;
import com.mysql.jdbc.Connection;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by 最美人间四月天 on 2018/1/23.
 */

public class SocietyFindThing extends BaseFragment {

    private LocalBroadcastManager broadcastManager;
    private IntentFilter intentFilter;
    private BroadcastReceiver mReceiver;
    private List<SocietyFindAdapter.Society_Find_item> Data;
    private RecyclerView.LayoutManager mLayoutManager;
    private SocietyFindAdapter mSocietyFindAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DataBaseHelper dataBaseHelper;
    public static final int UPDATE_FIND = 1;

    ArrayList<String> society_find_image1 = new ArrayList<>();
    ArrayList<String> society_find_image2 = new ArrayList<>();
    ArrayList<String> society_find_image3 = new ArrayList<>();
    ArrayList<String> society_find_content = new ArrayList<>();
    ArrayList<Integer> society_find_id = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.society_find_things, container, false);

        findView(view);
        initData();
        setAdapter();
        setItemClick();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findView(getView());
        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstants.BROAD_FIND);
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
    public void onStart(){
        super.onStart();
    }

    @Override
    protected void onFragmentVisibleChange(boolean isVisible) {
        if (isVisible) {
        } else {
            //关闭加载框
        }
    }

    @Override
    protected void onFragmentFirstVisible() {
        //去服务器下载数据
        localData();
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        connectData();
    }

    private void findView(View view){
        mRecyclerView = (RecyclerView)view.findViewById(R.id.society_find_rec);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        dataBaseHelper = new DataBaseHelper(getActivity(),AppConstants.SQL_VISION);
        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.society_find_sr);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                connectData();
            }
        });
    }

    private void localData(){
        society_find_image1 = new ArrayList<>();
        society_find_image2 = new ArrayList<>();
        society_find_image3 = new ArrayList<>();
        society_find_content = new ArrayList<>();
        society_find_id = new ArrayList<>();

        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();

        //删除重复数据
        String delete = "delete from shiwu where shiwu_id in (select shiwu_id from shiwu group by shiwu_id having count(shiwu_id) > 1)";
        sqLiteDatabase.execSQL(delete);

        Cursor cursor = sqLiteDatabase.query("shiwu",null,"shiwu_area = ?",new String[]{
                SharePreferences.getString(getActivity(),AppConstants.USER_AREA)
        },null,null,"shiwu_id desc");
        while (cursor.moveToNext()){
            String bitmap1 = cursor.getString(cursor.getColumnIndex("shiwu_picture1"));
            String bitmap2 = cursor.getString(cursor.getColumnIndex("shiwu_picture2"));
            String bitmap3 = cursor.getString(cursor.getColumnIndex("shiwu_picture3"));
            String content = cursor.getString(cursor.getColumnIndex("shiwu_content"));
            String phone = cursor.getString(cursor.getColumnIndex("shiwu_phone"));
            int id = cursor.getInt(cursor.getColumnIndex("shiwu_id"));
            findData(bitmap1,bitmap2,bitmap3,content,id);
        }
        cursor.close();
        sqLiteDatabase.close();
        //初始化事件
        initData();
        setAdapter();
        setItemClick();
    }


    //异步更新SPinner
    private Handler handler_find = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case UPDATE_FIND:{
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
//                    society_find_id = new ArrayList<>();
//                    society_find_image1 = new ArrayList<>();
//                    society_find_image2 = new ArrayList<>();
//                    society_find_image3 = new ArrayList<>();
//                    society_find_content = new ArrayList<>();
                    Looper.prepare();
                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
                    if (conn != null) { //判断 如果返回不为空则说明链接成功 如果为null的话则连接失败 请检查你的 mysql服务器地址是否可用 以及数据库名是否正确 并且 用户名跟密码是否正确
                        Log.d("调试", "连接成功，失物招领");
                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
                        //查找信息
                        String sql_connect = "select * from shiwu where shiwu_area = '" +
                                SharePreferences.getString(getActivity(), AppConstants.USER_AREA) +
                                "' order by shiwu_id desc limit 3";
                        ResultSet resultSet = stmt.executeQuery(sql_connect);
                        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();
                        while (resultSet.next()){
                            if(!society_find_id.contains(resultSet.getInt("shiwu_id"))){
                                ContentValues values = new ContentValues();
                                int id = resultSet.getInt("shiwu_id");
                                values.put("shiwu_id",id);
                                values.put("shiwu_name",resultSet.getString("shiwu_name"));
                                values.put("shiwu_phone",resultSet.getString("shiwu_phone"));
                                values.put("shiwu_time",resultSet.getString("shiwu_time"));
                                values.put("shiwu_title",resultSet.getString("shiwu_title"));
                                values.put("shiwu_area",resultSet.getString("shiwu_area"));
                                values.put("shiwu_content",resultSet.getString("shiwu_content"));
                                Blob picture1 = resultSet.getBlob("shiwu_picture1");
                                if(picture1 != null){
                                    values.put("shiwu_picture1",DealBitmap.compressImage(picture1,"_picture1_find"+id));
                                }else {
                                    values.put("shiwu_picture1", (String) null);
                                }
                                Blob picture2 = resultSet.getBlob("shiwu_picture2");
                                if(picture2 != null){
                                    values.put("shiwu_picture2",DealBitmap.compressImage(picture2,"_picture2_find"+id));
                                }else {
                                    values.put("shiwu_picture2", (String) null);
                                }
                                Blob picture3 = resultSet.getBlob("shiwu_picture3");
                                if(picture3 != null){
                                    values.put("shiwu_picture3",DealBitmap.compressImage(picture3,"_picture3_find"+id));
                                }else {
                                    values.put("shiwu_picture3", (String) null);
                                }
                                Blob picture4 = resultSet.getBlob("shiwu_picture4");
                                if(picture4 != null){
                                    values.put("shiwu_picture4",DealBitmap.compressImage(picture4,"_picture4_find"+id));
                                }else {
                                    values.put("shiwu_picture4", (String) null);
                                }
                                Blob picture5 = resultSet.getBlob("shiwu_picture5");
                                if(picture5 != null){
                                    values.put("shiwu_picture5",DealBitmap.compressImage(picture5,"_picture5_find"+id));
                                }else {
                                    values.put("shiwu_picture5", (String) null);
                                }
                                Blob picture6 = resultSet.getBlob("shiwu_picture6");
                                if(picture6 != null){
                                    values.put("shiwu_picture6",DealBitmap.compressImage(picture6,"_picture6_find"+id));
                                }else {
                                    values.put("shiwu_picture6", (String) null);
                                }
                                sqLiteDatabase.insert("shiwu",null,values);
                            }
//                            Blob picture1 = resultSet.getBlob("shiwu_picture1");
//                            Blob picture2 = resultSet.getBlob("shiwu_picture2");
//                            Blob picture3 = resultSet.getBlob("shiwu_picture3");
//                            Bitmap bitmap1 = null;
//                            Bitmap bitmap2 = null;
//                            Bitmap bitmap3 = null;
//                            if(picture1 != null){
//                                InputStream inputStream1 = picture1.getBinaryStream();
//                                bitmap1 = DealBitmap.InputToBitmap(inputStream1);
//                            }
//                            if(picture2 != null){
//                                InputStream inputStream2 = picture2.getBinaryStream();
//                                bitmap2 = DealBitmap.InputToBitmap(inputStream2);
//                            }
//                            if(picture3 != null){
//                                InputStream inputStream3 = picture3.getBinaryStream();
//                                bitmap3 = DealBitmap.InputToBitmap(inputStream3);
//                            }
//                            findData(bitmap1,bitmap2,bitmap3,resultSet.getString("shiwu_content"),
//                                    resultSet.getString("shiwu_phone"),resultSet.getString("shiwu_time"),
//                                    resultSet.getInt("shiwu_id"),resultSet.getString("shiwu_title"));
                        }
                        sqLiteDatabase.close();
                        Message message = new Message();
                        message.what = UPDATE_FIND;
                        handler_find.sendMessage(message);
                        resultSet.close();
                        JDBCTools.releaseConnection(stmt,conn);
                    }else {
                        Log.d("调试", "连接失败，失物招领");
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

    private void findData(String bitmap1, String bitmap2, String bitmap3, String content, int id){
        society_find_image1.add(bitmap1);
        society_find_image2.add(bitmap2);
        society_find_image3.add(bitmap3);
        society_find_content.add(content);
        society_find_id.add(id);
    }

    private void initData(){
        Data = new ArrayList<>();
        for(int i=0; i<society_find_content.size(); i++){
            SocietyFindAdapter newData = new SocietyFindAdapter(Data);
            SocietyFindAdapter.Society_Find_item society_find_item = newData.new Society_Find_item(society_find_image1.get(i),
                    society_find_image2.get(i),society_find_image3.get(i),society_find_content.get(i));
            Data.add(society_find_item);
        }
    }

    private void setAdapter(){
        mSocietyFindAdapter = new SocietyFindAdapter(Data);
        mRecyclerView.setAdapter(mSocietyFindAdapter);
    }

    private void setItemClick(){
        mSocietyFindAdapter.setmOnItemClickListener(new SocietyFindAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getActivity(),SocietyFindPageActivity.class);
                intent.putExtra("put_data_find_id",society_find_id.get(position));
                intent.putExtra("put_data_find_select",1);
                startActivity(intent);
            }
        });
    }

    //时间格式转换
    @SuppressLint("SimpleDateFormat")
    private String StringToString(String time){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = formatter.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
