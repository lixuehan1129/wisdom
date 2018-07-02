package com.example.wisdompark19.Main;

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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.wisdompark19.Adapter.GuideAdapter;
import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.AutoProject.TimeChange;
import com.example.wisdompark19.R;
import com.example.wisdompark19.ViewHelper.DataBaseHelper;
import com.mysql.jdbc.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ROBOSOFT on 2018/6/5.
 */

public class GuideActivity extends AppCompatActivity {

    private LocalBroadcastManager broadcastManager;
    private IntentFilter intentFilter;
    private BroadcastReceiver mReceiver;
    private DataBaseHelper dataBaseHelper;
    public static final int UPDATE_GUIDE = 1;
    private List<GuideAdapter.Guide_Item> Data;
    private GuideAdapter guideAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private ArrayList<String> guide_que;
    private ArrayList<String> guide_ans;
    private ArrayList<String> guide_time;
    private ArrayList<Integer> guide_id;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_guide);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue)); //设置顶部系统栏颜色
        Intent intent = getIntent();
        String intent_data = intent.getStringExtra("put_data_waishe");
        Toolbar toolbar = (Toolbar)findViewById(R.id.guide_mainTool); //标题栏
        toolbar.setNavigationIcon(R.mipmap.ic_back_white);
        toolbar.setTitle(intent_data);
        back(toolbar);
        findView();
        getBroad();
    }

    private void getBroad(){
        broadcastManager = LocalBroadcastManager.getInstance(GuideActivity.this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstants.BROAD_GUIDE);
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
        recyclerView = (RecyclerView)findViewById(R.id.guide_re);
        Button button = (Button) findViewById(R.id.guide_btn);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.guide_sw);
        dataBaseHelper = new DataBaseHelper(GuideActivity.this,AppConstants.SQL_VISION);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GuideActivity.this,GuideSelectActivity.class);
                intent.putExtra("guide_select",0);
                intent.putExtra("guide_select_que","");
                startActivity(intent);
            }
        });
        getData();//加载网络内容改为加载本地数据
        if(SharePreferences.getString(GuideActivity.this, AppConstants.GUIDE_TIME).isEmpty()
                || !SharePreferences.getString(GuideActivity.this,AppConstants.GUIDE_TIME).equals(getTime())){
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                    connectData();
                }
            });//第一次自动加载
            SharePreferences.remove(GuideActivity.this,AppConstants.GUIDE_TIME);
            SharePreferences.putString(GuideActivity.this,AppConstants.GUIDE_TIME,getTime());
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                connectData();
            }
        });

    }

    private void getData(){
        guide_que = new ArrayList<>();
        guide_ans = new ArrayList<>();
        guide_time = new ArrayList<>();
        guide_id = new ArrayList<>();

        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();

        //删除重复数据
        String delete = "delete from guide where guide_id in (select guide_id from guide group by guide_id having count(guide_id) > 1)";
        sqLiteDatabase.execSQL(delete);

        Cursor cursor = sqLiteDatabase.query("guide",null,"guide_area = ?",new String[]{
                SharePreferences.getString(this,AppConstants.USER_AREA)},null,
                null,"guide_id desc");
        while (cursor.moveToNext()){
            //从本地数据库读取
            String que = cursor.getString(cursor.getColumnIndex("guide_que"));
            String ans = cursor.getString(cursor.getColumnIndex("guide_ans"));
            String time = cursor.getString(cursor.getColumnIndex("guide_time"));
            int id = cursor.getInt(cursor.getColumnIndex("guide_id"));
            findData(que,ans,time,id);
        }
        cursor.close();
        sqLiteDatabase.close();
        //执行事件
        initData();
    }

    private Handler handler_guide = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case UPDATE_GUIDE:{
                    getData();
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
                        Log.d("调试", "连接成功,指南");
                        Statement stmt = conn.createStatement();
                        //查找信息
                        String sql_connect = "select * from guide where guide_area = '" +
                                SharePreferences.getString(GuideActivity.this, AppConstants.USER_AREA) +
                                "' order by guide_id desc";
                        ResultSet resultSet = stmt.executeQuery(sql_connect);
                        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getWritableDatabase();
                        while (resultSet.next()){
                            if(guide_id.contains(resultSet.getInt("guide_id"))){
                                ContentValues values = new ContentValues();
                                if(resultSet.getString("guide_ans")!=null){
                                    values.put("guide_ans",resultSet.getString("guide_ans"));
                                }else {
                                    values.put("guide_ans","null");
                                }
                                sqLiteDatabase.update("guide",values,"guide_id = ?",
                                        new String[]{String.valueOf(resultSet.getInt("guide_id"))});
                            }else {
                                ContentValues values = new ContentValues();
                                values.put("guide_id",resultSet.getInt("guide_id"));
                                values.put("guide_phone",resultSet.getString("guide_phone"));
                                values.put("guide_area",resultSet.getString("guide_area"));
                                values.put("guide_time",resultSet.getString("guide_time"));
                                values.put("guide_que",resultSet.getString("guide_que"));
                                if(resultSet.getString("guide_ans")!=null){
                                    values.put("guide_ans",resultSet.getString("guide_ans"));
                                }else {
                                    values.put("guide_ans","null");
                                }
                                sqLiteDatabase.insert("guide",null,values);
                            }
                        }
                        sqLiteDatabase.close();
                        Message message = new Message();
                        message.what = UPDATE_GUIDE;
                        handler_guide.sendMessage(message);
                        resultSet.close();
                        JDBCTools.releaseConnection(stmt,conn);
                    }else {
                        Log.d("调试", "连接失败,办事指南");
                        Toast toast = Toast.makeText(GuideActivity.this, "请检查网络", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                }catch (SQLException e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }.start();
    }


    private void findData(String que, String ans, String time, int i){
        guide_que.add(que);
        guide_ans.add(ans);
        guide_time.add(TimeChange.StringToString(time));
        guide_id.add(i);
    }

    private void initData(){
        Data = new ArrayList<>();
        for(int i=0; i<guide_id.size(); i++){
            GuideAdapter newData = new GuideAdapter(Data);
            GuideAdapter.Guide_Item guide_item = newData.new Guide_Item(
                    guide_que.get(i),guide_ans.get(i),guide_time.get(i),guide_id.get(i)
            );
            Data.add(guide_item);
        }
        setAdapter();
    }

    private void setAdapter(){
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        guideAdapter = new GuideAdapter(Data);
        recyclerView.setAdapter(guideAdapter);
        setItemClick();
    }
    private void setItemClick() {
        guideAdapter.setmOnItemClickListener(new GuideAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(guide_ans.get(position).equals("null")){
                    Intent intent = new Intent(GuideActivity.this,GuideSelectActivity.class);
                    intent.putExtra("guide_select",guide_id.get(position));
                    intent.putExtra("guide_select_que",guide_que.get(position));
                    startActivity(intent);
                }
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

