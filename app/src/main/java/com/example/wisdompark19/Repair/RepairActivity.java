package com.example.wisdompark19.Repair;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.wisdompark19.Adapter.RepairCheckAdapter;
import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.DealBitmap;
import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.AutoProject.TimeChange;
import com.example.wisdompark19.R;
import com.example.wisdompark19.ViewHelper.DataBaseHelper;
import com.mysql.jdbc.Connection;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by 最美人间四月天 on 2018/1/18.
 */

public class RepairActivity extends AppCompatActivity {

    private List<RepairCheckAdapter.Repair_Check_item> Data;
    private RecyclerView.LayoutManager mLayoutManager;
    private RepairCheckAdapter mRepairCheckAdapter;
    private LocalBroadcastManager broadcastManager;
    private IntentFilter intentFilter;
    private BroadcastReceiver mReceiver;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    public static final int UPDATE_REP = 1;
    private DataBaseHelper dataBaseHelper;

    ArrayList<String> repair_check_name = new ArrayList<String>();
    ArrayList<String> repair_check_phone = new ArrayList<String>();
    ArrayList<String> repair_check_fenlei = new ArrayList<String>();
    ArrayList<String> repair_check_shijian = new ArrayList<String>();
    ArrayList<Integer> repair_check_progress = new ArrayList<>();
    ArrayList<Integer> repair_check_pingjia = new ArrayList<>();
//    ArrayList<String> repair_check_user = new ArrayList<String>();
    ArrayList<Integer> repair_check_id = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repair_acitivity);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue)); //设置顶部系统栏颜色
        Intent intent = getIntent();
        String intent_data = intent.getStringExtra("put_data_repair");
        Toolbar toolbar = (Toolbar)findViewById(R.id.repair_mainTool); //标题栏
        toolbar.setNavigationIcon(R.mipmap.ic_back_white);
        toolbar.setTitle(intent_data);
        back(toolbar);
        findView();
        getBroad();
   //     connectData();
    }

    private void getBroad(){
        broadcastManager = LocalBroadcastManager.getInstance(RepairActivity.this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstants.BROAD_REPAIR);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent){
                //收到广播后所作的操作
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(true);
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


    private void findView(){
        Button repair_make = (Button)findViewById(R.id.repair_make);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.repair_sw);
        mRecyclerView = (RecyclerView)findViewById(R.id.repair_check_rec);
        mLayoutManager = new LinearLayoutManager(RepairActivity.this);
        dataBaseHelper = new DataBaseHelper(RepairActivity.this,AppConstants.SQL_VISION);
        mRecyclerView.setLayoutManager(mLayoutManager);

        repair_make.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RepairActivity.this,RepairMakeActivity.class);
                intent.putExtra("repair_check",0);
                startActivity(intent);
            }
        });

        getData();//加载网络内容改为加载本地数据
        if(SharePreferences.getString(RepairActivity.this,AppConstants.REPAIR_TIME).isEmpty()
                || !SharePreferences.getString(RepairActivity.this,AppConstants.REPAIR_TIME).equals(getTime())){
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                    connectData();
                }
            });//第一次自动加载
            SharePreferences.remove(RepairActivity.this,AppConstants.REPAIR_TIME);
            SharePreferences.putString(RepairActivity.this,AppConstants.REPAIR_TIME,getTime());
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                connectData();
            }
        });
    }

    private void getData(){
        repair_check_name = new ArrayList<>();
        repair_check_phone = new ArrayList<>();
        repair_check_fenlei = new ArrayList<>();
        repair_check_shijian = new ArrayList<>();
        repair_check_progress = new ArrayList<>();
        repair_check_pingjia = new ArrayList<>();
        repair_check_id = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();

        //删除重复数据
        String delete = "delete from repair where repair_id in (select repair_id from repair group by repair_id having count(repair_id) > 1)";
        sqLiteDatabase.execSQL(delete);

        Cursor cursor = null;
        if(SharePreferences.getInt(RepairActivity.this, AppConstants.USER_SORT) == 0){
            cursor = sqLiteDatabase.query("repair",null,"repair_area = ?",new String[]{
                    SharePreferences.getString(this,AppConstants.USER_AREA)
            },null,null,"repair_id desc");
        }else {
            cursor = sqLiteDatabase.query("repair",null,"repair_phone = ?",new String[]{
                    SharePreferences.getString(this,AppConstants.USER_PHONE)
            },null,null,"repair_id desc");
        }
        while (cursor.moveToNext()){
            //从本地数据库读取
            String name = cursor.getString(cursor.getColumnIndex("repair_name"));
            String phone = cursor.getString(cursor.getColumnIndex("repair_phone"));
            String fenlei = cursor.getString(cursor.getColumnIndex("repair_title"));
            String shijian = cursor.getString(cursor.getColumnIndex("repair_time"));
            int progress = cursor.getInt(cursor.getColumnIndex("repair_progress"));
            int pingjia = cursor.getInt(cursor.getColumnIndex("repair_pingjia"));
            int id = cursor.getInt(cursor.getColumnIndex("repair_id"));
            findData(name,phone,fenlei,shijian,id,progress,pingjia);
        }
        cursor.close();
        sqLiteDatabase.close();
        //执行事件
        initData();
        setAdapter();
        setItemClick();
    }

    private Handler handler_rep = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case UPDATE_REP:{
                    getData();
//                    initData();
//                    setAdapter();
//                    setItemClick();
                    swipeRefreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
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
                    if (conn != null) {
                        Log.d("调试", "连接成功,报修管理");
                        Statement stmt = conn.createStatement();
                        //查找信息
                        String sql_connect;
                        if(SharePreferences.getInt(RepairActivity.this, AppConstants.USER_SORT) == 0){
                            sql_connect = "select * from repair where repair_area = '" +
                                    SharePreferences.getString(RepairActivity.this, AppConstants.USER_AREA) +
                                    "' order by repair_time desc limit 10";
                        }else {
                            sql_connect = "select * from repair where repair_phone = '" +
                                    SharePreferences.getString(RepairActivity.this, AppConstants.USER_PHONE) +
                                    "' order by repair_time desc limit 10";
                        }
                        ResultSet resultSet = stmt.executeQuery(sql_connect);
                        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();
                        while (resultSet.next()){
                            if(!repair_check_id.contains(resultSet.getInt("repair_id"))){
                                ContentValues values = new ContentValues();
                                int id = resultSet.getInt("repair_id");
                                values.put("repair_id",id);
                                values.put("repair_name",resultSet.getString("repair_name"));
                                values.put("repair_phone",resultSet.getString("repair_phone"));
                                values.put("repair_time",resultSet.getString("repair_time"));
                                values.put("repair_select_time",resultSet.getString("repair_select_time"));
                                values.put("repair_area",resultSet.getString("repair_area"));
                                values.put("repair_title",resultSet.getString("repair_leixing"));
                                values.put("repair_content",resultSet.getString("repair_content"));
                                values.put("repair_progress",resultSet.getInt("repair_progress"));
                                values.put("repair_pingjia",resultSet.getInt("repair_pingjia"));
                                Blob picture1 = resultSet.getBlob("repair_picture1");
                                if(picture1 != null){
                                    values.put("repair_picture1",DealBitmap.compressImage(picture1,"_picture1_repair"+id));
                                }else {
                                    values.put("repair_picture1", (String) null);
                                }
                                Blob picture2 = resultSet.getBlob("repair_picture2");
                                if(picture2 != null){
                                    values.put("repair_picture2",DealBitmap.compressImage(picture2,"_picture2_repair"+id));
                                }else {
                                    values.put("repair_picture2", (String) null);
                                }
                                Blob picture3 = resultSet.getBlob("repair_picture3");
                                if(picture3 != null){
                                    values.put("repair_picture3",DealBitmap.compressImage(picture3,"_picture3_repair"+id));
                                }else {
                                    values.put("repair_picture3", (String) null);
                                }
                                Blob picture4 = resultSet.getBlob("repair_picture4");
                                if(picture4 != null){
                                    values.put("repair_picture4",DealBitmap.compressImage(picture4,"_picture4_repair"+id));
                                }else {
                                    values.put("repair_picture4", (String) null);
                                }
                                Blob picture5 = resultSet.getBlob("repair_picture5");
                                if(picture5 != null){
                                    values.put("repair_picture5",DealBitmap.compressImage(picture5,"_picture5_repair"+id));
                                }else {
                                    values.put("repair_picture5", (String) null);
                                }
                                Blob picture6 = resultSet.getBlob("repair_picture6");
                                if(picture6 != null){
                                    values.put("repair_picture6",DealBitmap.compressImage(picture6,"_picture6_repair"+id));
                                }else {
                                    values.put("repair_picture6", (String) null);
                                }
                                sqLiteDatabase.insert("repair",null,values);
                            }else {
                                ContentValues values1 = new ContentValues();
                                values1.put("repair_progress",resultSet.getInt("repair_progress"));
                                values1.put("repair_pingjia",resultSet.getInt("repair_pingjia"));
                                sqLiteDatabase.update("repair",values1,"repair_id=?",new String[]{String.valueOf(resultSet.getInt("repair_id"))});
                            }
//                            findData(resultSet.getString("repair_content"),
//                                     resultSet.getInt("repair_id"),
//                                     resultSet.getString("repair_phone"));
                        }
                        sqLiteDatabase.close();
                        Message message = new Message();
                        message.what = UPDATE_REP;
                        handler_rep.sendMessage(message);
                        resultSet.close();
                        JDBCTools.releaseConnection(stmt,conn);
                    }else {
                        Log.d("调试", "连接失败,报修管理");
                        Toast toast = Toast.makeText(RepairActivity.this, "请检查网络", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                }catch (SQLException e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }.start();
    }

    private void findData(String name,String phone,String fenlei,String shijian,int id,int progress,
                          int pingjia){
        repair_check_name.add(name);
        repair_check_phone.add(phone);
        repair_check_fenlei.add(fenlei);
        repair_check_shijian.add(TimeChange.StringToString(shijian));
        repair_check_id.add(id);
        repair_check_progress.add(progress);
        repair_check_pingjia.add(pingjia);
//        repair_check_user.add(user);
    }

    private void initData(){
        Data = new ArrayList<>();
        for(int i=0; i<repair_check_name.size(); i++){
            RepairCheckAdapter newData = new RepairCheckAdapter(Data);
            RepairCheckAdapter.Repair_Check_item repair_check_item = newData.new Repair_Check_item(
                    repair_check_name.get(i),repair_check_phone.get(i),repair_check_fenlei.get(i),repair_check_shijian.get(i),
                    repair_check_progress.get(i),repair_check_pingjia.get(i)
            );
            Data.add(repair_check_item);
        }
    }

    private void setAdapter(){
        mRepairCheckAdapter = new RepairCheckAdapter(Data);
        mRecyclerView.setAdapter(mRepairCheckAdapter);
    }

    private void setItemClick(){
        mRepairCheckAdapter.setmOnItemClickListener(new RepairCheckAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(RepairActivity.this,RepairMakeActivity.class);
                intent.putExtra("repair_check",1);
                intent.putExtra("repair_check_image",repair_check_id.get(position));
//                intent.putExtra("repair_user",repair_check_user.get(position));
                startActivity(intent);
            }
        });
    }

    //获取系统时间，并进行格式转换
    private String getTime(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(new Date());
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
