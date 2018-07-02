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

import com.example.wisdompark19.Adapter.SocietyComplaintItemAdapter;
import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.DealBitmap;
import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.R;
import com.example.wisdompark19.ViewHelper.BaseFragment;
import com.example.wisdompark19.ViewHelper.DataBaseHelper;
import com.mysql.jdbc.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 最美人间四月天 on 2018/1/23.
 */

public class SocietyMakeComplaint extends BaseFragment {

    private LocalBroadcastManager broadcastManager;
    private IntentFilter intentFilter;
    private BroadcastReceiver mReceiver;
    private DataBaseHelper dataBaseHelper;
    private List<SocietyComplaintItemAdapter.Society_Com_Item> Data;
    private RecyclerView.LayoutManager mLayoutManager;
    private SocietyComplaintItemAdapter mSocietyComplaintItemAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    public static final int UPDATE_COM = 1;

    ArrayList<String> society_com_content = new ArrayList<String>();
    ArrayList<Bitmap> society_com_image = new ArrayList<>();
    ArrayList<Integer> society_com_id = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.society_make_complaint, container, false);

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
        intentFilter.addAction(AppConstants.BROAD_COM);
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
        LocalData();
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
        mRecyclerView = (RecyclerView)view.findViewById(R.id.society_complaint_rec);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.society_complaint_sr);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                connectData();
            }
        });
    }

    private void LocalData(){
        society_com_content = new ArrayList<>();
        society_com_image = new ArrayList<>();
        society_com_id = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();

        //删除重复数据
        String delete = "delete from tucao where tucao_id in (select tucao_id from tucao group by tucao_id having count(tucao_id) > 1)";
        sqLiteDatabase.execSQL(delete);

        Cursor cursor = sqLiteDatabase.query("tucao",null,"tucao_area = ?",new String[]{
                SharePreferences.getString(getActivity(),AppConstants.USER_AREA)
        },null,null,"tucao_id desc");
        while (cursor.moveToNext()){
            //从本地数据库读取
            int id = cursor.getInt(cursor.getColumnIndex("tucao_id"));
            String phone = cursor.getString(cursor.getColumnIndex("tucao_phone"));
            String content = cursor.getString(cursor.getColumnIndex("tucao_content"));
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
            findData(content,picture,id);
        }
        cursor.close();
        sqLiteDatabase.close();
        //执行事件
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
                case UPDATE_COM:{
                    LocalData();
//                    initData();
//                    setAdapter();
//                    setItemClick();
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
                    society_com_image = new ArrayList<>();
                    society_com_content = new ArrayList<>();
                    Looper.prepare();
                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
                    if (conn != null) { //判断 如果返回不为空则说明链接成功 如果为null的话则连接失败 请检查你的 mysql服务器地址是否可用 以及数据库名是否正确 并且 用户名跟密码是否正确
                        Log.d("调试", "连接成功，吐槽界面");
                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
                        //查找信息
                        String sql_connect = "select * from tucao where tucao_area = '" +
                                SharePreferences.getString(getActivity(), AppConstants.USER_AREA) +
                                "' order by tucao_id desc limit 10";
                        ResultSet resultSet = stmt.executeQuery(sql_connect);
                        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();
                        while (resultSet.next()){
                            if(!society_com_id.contains(resultSet.getInt("tucao_id"))){
                                ContentValues values = new ContentValues();
                                values.put("tucao_id",resultSet.getInt("tucao_id"));
                                values.put("tucao_name",resultSet.getString("tucao_name"));
                                values.put("tucao_area",resultSet.getString("tucao_area"));
                                values.put("tucao_phone",resultSet.getString("tucao_phone"));
                                values.put("tucao_time",resultSet.getString("tucao_time"));
                                values.put("tucao_content",resultSet.getString("tucao_content"));
                                sqLiteDatabase.insert("tucao",null,values);
                            }
                        }
                        sqLiteDatabase.close();
                        resultSet.close();
                        Message message = new Message();
                        message.what = UPDATE_COM;
                        handler_find.sendMessage(message);
                        JDBCTools.releaseConnection(stmt,conn);
                    }else {
                        Log.d("调试", "连接失败，吐槽界面");
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

    private void findData(String content, Bitmap bitmap,int id){
        society_com_content.add(content);
        society_com_image.add(bitmap);
        society_com_id.add(id);
    }

    private void initData(){
        Data = new ArrayList<>();
        for(int i=0; i<society_com_content.size(); i++){
            SocietyComplaintItemAdapter newData = new SocietyComplaintItemAdapter(Data);
            SocietyComplaintItemAdapter.Society_Com_Item society_com_item = newData.new Society_Com_Item(
                    society_com_content.get(i),society_com_image.get(i)
            );
            Data.add(society_com_item);
        }
    }

    private void setAdapter(){
        mSocietyComplaintItemAdapter = new SocietyComplaintItemAdapter(Data);
        mRecyclerView.setAdapter(mSocietyComplaintItemAdapter);
    }

    private void setItemClick(){
        mSocietyComplaintItemAdapter.setmOnItemClickListener(new SocietyComplaintItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
//                Toast toast=Toast.makeText(getActivity(), society_com_content.get(position), Toast.LENGTH_SHORT);
//                toast.show();
            }
        });
    }
}
