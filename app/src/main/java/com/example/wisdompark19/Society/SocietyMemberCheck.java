package com.example.wisdompark19.Society;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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

import com.example.wisdompark19.Adapter.SocietyMemberAdapter;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 最美人间四月天 on 2018/1/23.
 */

public class SocietyMemberCheck extends BaseFragment {

    private LocalBroadcastManager broadcastManager;
    private IntentFilter intentFilter;
    private BroadcastReceiver mReceiver;
    private DataBaseHelper dataBaseHelper;
    private List<SocietyMemberAdapter.Item_member> Data;
    private RecyclerView.LayoutManager mLayoutManager;
    private SocietyMemberAdapter mSocietyMemberAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    public static final int UPDATE_MEM = 1;

    ArrayList<Bitmap> member_image = new ArrayList<>();
    ArrayList<String> member_name = new ArrayList<String>();
    ArrayList<String> member_phone = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.society_member_check, container, false);

        findView(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findView(getView());
        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstants.BROAD_CON);
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
        mRecyclerView = (RecyclerView)view.findViewById(R.id.society_member_rec);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.society_member_sr);
        dataBaseHelper = new DataBaseHelper(getActivity(),AppConstants.SQL_VISION);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                connectData();
            }
        });
    }

    private void LocalData(){
        member_image = new ArrayList<>();
        member_name = new ArrayList<>();
        member_phone = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();

        //删除重复数据
        String delete = "delete from user where user_id in (select user_id from user group by user_id having count(user_id) > 1)";
        sqLiteDatabase.execSQL(delete);
        //查询数据
        @SuppressLint("Recycle") Cursor cursor = sqLiteDatabase.query("user",null,
                "user_area = ?",new String[]{
                        SharePreferences.getString(getActivity(),AppConstants.USER_AREA)
                },null,null,"user_sort");
        while (cursor.moveToNext()){
            Bitmap picture = null;
            byte[] bytes = cursor.getBlob(cursor.getColumnIndex("user_picture"));
            if(bytes != null){
                picture = DealBitmap.byteToBit(bytes);
            }
            String name = cursor.getString(cursor.getColumnIndex("user_name"));
            String phone = cursor.getString(cursor.getColumnIndex("user_phone"));
            findData(picture,name,phone);
        }
        cursor.close();
        sqLiteDatabase.close();
        initData();
        setAdapter();
        setItemClick();
    }

    private Handler handler_mem = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case UPDATE_MEM:{
                    LocalData();
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
                    Looper.prepare();
                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
                    if (conn != null) { //判断 如果返回不为空则说明链接成功 如果为null的话则连接失败 请检查你的 mysql服务器地址是否可用 以及数据库名是否正确 并且 用户名跟密码是否正确
                        Log.d("调试", "连接成功,成员列表");
                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
                        //查找信息
                        String sql_connect = "select * from user where user_area = '" +
                                SharePreferences.getString(getActivity(), AppConstants.USER_AREA) +
                                "' order by user_sort";
                        ResultSet resultSet = stmt.executeQuery(sql_connect);
                        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getWritableDatabase();
                        while (resultSet.next()){
                            if(member_phone.contains(resultSet.getString("user_phone"))){
                                ContentValues values = new ContentValues();
                                values.put("user_name",resultSet.getString("user_name"));
                                values.put("user_address",resultSet.getString("user_address"));
                                values.put("user_area",resultSet.getString("user_area"));
                                values.put("user_sort",resultSet.getInt("user_sort"));
                                values.put("user_sex",resultSet.getString("user_sex"));
                                Blob picture = resultSet.getBlob("user_picture");
                                if(picture != null){
                                    values.put("user_picture", DealBitmap.blobToBytes(picture));
                                }else {
                                    values.put("user_picture", (byte[]) null);
                                }
                                sqLiteDatabase.update("user",values,"user_phone = ?",
                                        new String[]{resultSet.getString("user_phone")});
                            }else {
                                ContentValues values = new ContentValues();
                                values.put("user_id",resultSet.getInt("user_id"));
                                values.put("user_name",resultSet.getString("user_name"));
                                values.put("user_phone",resultSet.getString("user_phone"));
                                values.put("user_address",resultSet.getString("user_address"));
                                values.put("user_area",resultSet.getString("user_area"));
                                values.put("user_sort",resultSet.getInt("user_sort"));
                                values.put("user_sex",resultSet.getString("user_sex"));
                                Blob picture = resultSet.getBlob("user_picture");
                                if(picture != null){
                                    values.put("user_picture", DealBitmap.blobToBytes(picture));
                                }else {
                                    values.put("user_picture", (byte[]) null);
                                }
                                sqLiteDatabase.insert("user",null,values);
                            }
                        }
                        sqLiteDatabase.close();
                        Message message = new Message();
                        message.what = UPDATE_MEM;
                        handler_mem.sendMessage(message);
                        resultSet.close();
                        JDBCTools.releaseConnection(stmt,conn);
                    }else {
                        Log.d("调试", "连接失败,成员列表");
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

    private void findData(Bitmap bitmap, String name, String phone){
        member_image.add(bitmap);
        member_name.add(name);
        member_phone.add(phone);
    }

    private void initData(){
        Data = new ArrayList<>();
        for(int i=0; i<member_name.size(); i++){
            SocietyMemberAdapter newData = new SocietyMemberAdapter(Data);
            SocietyMemberAdapter.Item_member item_member = newData.new Item_member(
                    member_name.get(i),member_image.get(i),member_phone.get(i)
            );
            Data.add(item_member);
        }
    }

    private void setAdapter(){
        mSocietyMemberAdapter = new SocietyMemberAdapter(Data);
        mRecyclerView.setAdapter(mSocietyMemberAdapter);
    }

    private void setItemClick(){
        mSocietyMemberAdapter.setOnItemClickListener(new SocietyMemberAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
//                Toast toast=Toast.makeText(getActivity(), member_phone.get(position), Toast.LENGTH_SHORT);
 //               toast.show();
            }
        });
        mSocietyMemberAdapter.setOnItemLongClickListener(new SocietyMemberAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final int position) {
                if(SharePreferences.getInt(getActivity(), AppConstants.USER_SORT) == 0){
                    final String[] items = new String[] {"升为管理员"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            updateUser(member_phone.get(position));
                        }
                    }).create().show();
                }else {
                    Toast.makeText(getActivity(), "您还不是管理员", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUser(final String user_phone){
        new Thread(){
            public void run(){
                try{
                    Looper.prepare();
                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
                    if (conn != null) { //判断 如果返回不为空则说明链接成功 如果为null的话则连接失败 请检查你的 mysql服务器地址是否可用 以及数据库名是否正确 并且 用户名跟密码是否正确
                        Log.d("调试", "连接成功,成员更新");
                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
                        //更新用户信息
                        String sql_update = "update user set user_sort = '" +
                                0 +
                                "' where user_phone = '" +
                                user_phone +
                                "'";
                        stmt.execute(sql_update);
                        JDBCTools.releaseConnection(stmt,conn);
                    }else {
                        Log.d("调试", "连接失败,成员更新");
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
}
