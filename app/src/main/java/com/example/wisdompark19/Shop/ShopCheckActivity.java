package com.example.wisdompark19.Shop;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.wisdompark19.Adapter.DingDanAdapter;
import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.AutoProject.TimeChange;
import com.example.wisdompark19.R;
import com.mysql.jdbc.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ROBOSOFT on 2018/5/18.
 */

public class ShopCheckActivity extends AppCompatActivity {

    private List<DingDanAdapter.Ding_Dan> Data;
    private DingDanAdapter dingDanAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    public static final int UPDATE_DINGDAN = 1;

    ArrayList<String> dingdan_name;
    ArrayList<String> dingdan_add;
    ArrayList<String> dingdan_n;
    ArrayList<Integer> dingdan_num;
    ArrayList<Integer> dingdan_pro;
    ArrayList<String> dingdan_time;
    ArrayList<Integer> dingdan_id;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shop_dingdan);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue)); //设置顶部系统栏颜色
        Toolbar toolbar = (Toolbar)findViewById(R.id.shop_dingdan_mainTool); //标题栏
        toolbar.setNavigationIcon(R.mipmap.ic_back_white);
        toolbar.setTitle("查看订单");
        back(toolbar);
        findView();
    }

    private void findView(){
        recyclerView = (RecyclerView) findViewById(R.id.shop_dingdan_rec);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.shop_dingdan_sw);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                connectData();
            }
        });//第一次自动加载
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                connectData();
            }
        });
    }

    //获取订单数据
    private void connectData(){
        dingdan_name = new ArrayList<>();
        dingdan_add = new ArrayList<>();
        dingdan_n = new ArrayList<>();
        dingdan_num = new ArrayList<>();
        dingdan_pro = new ArrayList<>();
        dingdan_time = new ArrayList<>();
        dingdan_id = new ArrayList<>();
        new Thread(){
            public void run(){
                try { Looper.prepare();
                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
                    if (conn != null) {
                        Log.d("调试", "连接成功,订单查询");
                        Statement stmt = conn.createStatement();
                        String sql_dingdan = null;
                        if(SharePreferences.getInt(ShopCheckActivity.this, AppConstants.USER_SORT) == 0){
                            sql_dingdan = "select * from dingdan where dingdan_area = '" +
                                    SharePreferences.getString(ShopCheckActivity.this, AppConstants.USER_AREA) +
                                    "' order by dingdan_progress,dingdan_id desc";
                        }else {
                            sql_dingdan = "select * from dingdan where dingdan_phone = '" +
                                    SharePreferences.getString(ShopCheckActivity.this, AppConstants.USER_PHONE) +
                                    "' order by dingdan_progress,dingdan_id desc";
                        }
                        ResultSet resultSet = stmt.executeQuery(sql_dingdan);
                        while (resultSet.next()){
                            findData(resultSet.getString("dingdan_user")+
                                            "("+resultSet.getString("dingdan_phone")+")",
                                    resultSet.getString("dingdan_add"),
                                    resultSet.getString("dingdan_name"),
                                    resultSet.getInt("dingdan_num"),
                                    resultSet.getInt("dingdan_progress"),
                                    resultSet.getString("dingdan_time"),
                                    resultSet.getInt("dingdan_id"));
                        }
                        Message message = new Message();
                        message.what = UPDATE_DINGDAN;
                        handler_dingdan.sendMessage(message);
                        resultSet.close();
                        JDBCTools.releaseConnection(stmt,conn);
                    }
                }catch (SQLException e){
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }.start();
    }

    private Handler handler_dingdan = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case UPDATE_DINGDAN:{
                    initData();
                    setAdapter();
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

    private void findData(String name, String add, String n, int num,
                          int pro, String time, int id){
        dingdan_name.add(name);
        dingdan_add.add(add);
        dingdan_n.add(n);
        dingdan_num.add(num);
        dingdan_pro.add(pro);
        dingdan_time.add(TimeChange.StringToString1(time));
        dingdan_id.add(id);
    }

    private void initData(){
        Data = new ArrayList<>();
        for(int i=0; i<dingdan_n.size(); i++){
            DingDanAdapter newData = new DingDanAdapter(Data);
            DingDanAdapter.Ding_Dan ding_dan = newData.new Ding_Dan(
                    dingdan_name.get(i),dingdan_add.get(i),dingdan_n.get(i),
                    dingdan_num.get(i),dingdan_pro.get(i),dingdan_time.get(i),
                    dingdan_id.get(i)
            );
            Data.add(ding_dan);
        }
    }

    private void setAdapter(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        dingDanAdapter = new DingDanAdapter(Data);
        recyclerView.setAdapter(dingDanAdapter);
        if(SharePreferences.getInt(ShopCheckActivity.this,AppConstants.USER_SORT) == 0){
            setItemClick();
        }
    }

    private void setItemClick(){
        dingDanAdapter.setmOnItemClickListener(new DingDanAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                String items[] = {"接受订单", "处理完成"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(ShopCheckActivity.this);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0 :{
                                if(dingdan_pro.get(position) > 0){
                                    Toast.makeText(ShopCheckActivity.this,"已接受订单",Toast.LENGTH_LONG).show();
                                }else {
                                    updateOld(1,dingdan_id.get(position));
                                }
                                break;
                            }
                            case 1 :{
                                if(dingdan_pro.get(position) == 2){
                                    Toast.makeText(ShopCheckActivity.this,"已完成订单",Toast.LENGTH_LONG).show();
                                }else {
                                    updateOld(2,dingdan_id.get(position));
                                }
                                break;
                            }
                            default :
                                break;
                        }
                    }
                });
                builder.create().show();
            }
        });
    }

    private void updateOld(final int i,final int id){
        final ProgressDialog progressDialog = ProgressDialog.show(ShopCheckActivity.this,"","正在修改",true);
        new Thread(){
            public void run(){
                try{
                    Looper.prepare();//用于toast
                    Connection conn_update = JDBCTools.getConnection("shequ","Zz123456");
                    if(conn_update != null){
                        Log.d("调试", "连接成功，修改订单");
                        Statement stmt = conn_update.createStatement();
                        String update_old = "update dingdan set dingdan_progress = ? where dingdan_id = ?";
                        PreparedStatement preparedStatement;
                        preparedStatement = conn_update.prepareStatement(update_old);
                        preparedStatement.setInt(1,i);
                        preparedStatement.setInt(2,id);
                        preparedStatement.executeUpdate();//执行更新操作
                        preparedStatement.close();
                        JDBCTools.releaseConnection(stmt,conn_update);
                        progressDialog.dismiss();
                        swipeRefreshLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(true);
                                connectData();
                            }
                        });
                    }
                }catch (SQLException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
                Looper.loop();
            }
        }.start();
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
