package com.example.wisdompark19.Shop;

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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.wisdompark19.Adapter.ShopTradeItemAdapter;
import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.DealBitmap;
import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.AutoProject.SharePreferences;
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
 * 社区交易界面
 */
public class ShopActivity extends AppCompatActivity {

    private LocalBroadcastManager broadcastManager;
    private IntentFilter intentFilter;
    private BroadcastReceiver mReceiver;
    private DataBaseHelper dataBaseHelper;
    private List<ShopTradeItemAdapter.Shop_Trade_item> Data;
    private ShopTradeItemAdapter mShopTradeItemAdapter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    public static final int UPDATE_SHOP = 1;

    ArrayList<String> shop_trade_image = new ArrayList<>(); // 上下滚动消息栏内容
    ArrayList<String> shop_trade_title = new ArrayList<>();
    ArrayList<String> shop_trade_price = new ArrayList<>();
    ArrayList<Integer> shop_trade_id = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onlineshopping_activity);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue)); //设置顶部系统栏颜色
        Intent intent = getIntent();
        String intent_data = intent.getStringExtra("put_data_shop");
        Toolbar toolbar = (Toolbar)findViewById(R.id.shop_mainTool); //标题栏
        toolbar.setNavigationIcon(R.mipmap.ic_back_white);
        toolbar.setTitle(intent_data);
        back(toolbar);
        findView();
        getBroad();
    }

    private void getBroad(){
        broadcastManager = LocalBroadcastManager.getInstance(ShopActivity.this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstants.BROAD_SHOP);
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

    private void findView(){
        Button shop_ok = (Button) findViewById(R.id.shop_ok);
        Button shop_dingdan = (Button) findViewById(R.id.shop_dingdan);
        mRecyclerView = (RecyclerView) findViewById(R.id.shop_trade_rec);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.shop_swip);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,3));
        dataBaseHelper = new DataBaseHelper(ShopActivity.this,AppConstants.SQL_VISION);
        shop_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SharePreferences.getInt(ShopActivity.this,AppConstants.USER_SORT) == 0){
                    Intent intent = new Intent(ShopActivity.this,ShopAddActivity.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(ShopActivity.this,"您不是管理员，没有该权限",Toast.LENGTH_LONG).show();
                }
            }
        });

        shop_dingdan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShopActivity.this,ShopCheckActivity.class);
                startActivity(intent);
            }
        });

        getData();//加载网络内容改为加载本地数据
        if(SharePreferences.getString(ShopActivity.this,AppConstants.SHOP_TIME).isEmpty()
                || !SharePreferences.getString(ShopActivity.this,AppConstants.SHOP_TIME).equals(getTime())){
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                    connectData();
                }
            });//第一次自动加载
            SharePreferences.remove(ShopActivity.this,AppConstants.SHOP_TIME);
            SharePreferences.putString(ShopActivity.this,AppConstants.SHOP_TIME,getTime());
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                connectData();
            }
        });
    }

    private void getData(){
        shop_trade_title = new ArrayList<>();
        shop_trade_price = new ArrayList<>();
        shop_trade_image = new ArrayList<>();
        shop_trade_id = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();

        //删除重复数据
        String delete = "delete from shop where shop_id in (select shop_id from shop group by shop_id having count(shop_id) > 1)";
        sqLiteDatabase.execSQL(delete);

        Cursor cursor = sqLiteDatabase.query("shop",null,"shop_area = ?",new String[]{
                SharePreferences.getString(this,AppConstants.USER_AREA)
        },null,null,"shop_id desc");
        while (cursor.moveToNext()){
            //从本地数据库读取
            String bitmap = cursor.getString(cursor.getColumnIndex("shop_picture1"));
            int id = cursor.getInt(cursor.getColumnIndex("shop_id"));
            String title = cursor.getString(cursor.getColumnIndex("shop_title"));
            String price = cursor.getString(cursor.getColumnIndex("shop_price"));
            findData(bitmap,title,price,id);
        }
        cursor.close();
        sqLiteDatabase.close();
        //执行事件
        initData();
        setAdapter();
    }

    private void connectData(){
        new Thread(){
            public void run(){
                try{
                    Looper.prepare();
                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
                    if (conn != null) { //判断 如果返回不为空则说明链接成功 如果为null的话则连接失败 请检查你的 mysql服务器地址是否可用 以及数据库名是否正确 并且 用户名跟密码是否正确
                        Log.d("调试", "连接成功,商品列表");
                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
                        //查找信息
                        String sql_connect = "select * from shop where shop_area = '" +
                                SharePreferences.getString(ShopActivity.this, AppConstants.USER_AREA) +
                                "' order by shop_id desc limit 10";
                        ResultSet resultSet = stmt.executeQuery(sql_connect);
                        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();
                        while (resultSet.next()){
                            if(!shop_trade_id.contains(resultSet.getInt("shop_id"))){
                                ContentValues values = new ContentValues();
                                int id = resultSet.getInt("shop_id");
                                values.put("shop_id",id);
                                values.put("shop_name",resultSet.getString("shop_name"));
                                values.put("shop_phone",resultSet.getString("shop_phone"));
                                values.put("shop_time",resultSet.getString("shop_time"));
                                values.put("shop_title",resultSet.getString("shop_title"));
                                values.put("shop_area",resultSet.getString("shop_area"));
                                values.put("shop_content",resultSet.getString("shop_content"));
                                values.put("shop_price",resultSet.getString("shop_price"));
                                Blob picture1 = resultSet.getBlob("shop_picture1");
                                if(picture1 != null){
                                    values.put("shop_picture1",DealBitmap.compressImage(picture1,"_picture1_shop"+id));
                                }else {
                                    values.put("shop_picture1", (String) null);
                                }
                                Blob picture2 = resultSet.getBlob("shop_picture2");
                                if(picture2 != null){
                                    values.put("shop_picture2",DealBitmap.compressImage(picture2,"_picture2_shop"+id));
                                }else {
                                    values.put("shop_picture2", (String) null);
                                }
                                Blob picture3 = resultSet.getBlob("shop_picture3");
                                if(picture3 != null){
                                    values.put("shop_picture3",DealBitmap.compressImage(picture3,"_picture3_shop"+id));
                                }else {
                                    values.put("shop_picture3", (String) null);
                                }
                                Blob picture4 = resultSet.getBlob("shop_picture4");
                                if(picture4 != null){
                                    values.put("shop_picture4",DealBitmap.compressImage(picture4,"_picture4_shop"+id));
                                }else {
                                    values.put("shop_picture4", (String) null);
                                }
                                Blob picture5 = resultSet.getBlob("shop_picture5");
                                if(picture5 != null){
                                    values.put("shop_picture5",DealBitmap.compressImage(picture5,"_picture5_shop"+id));
                                }else {
                                    values.put("shop_picture5", (String) null);
                                }
                                Blob picture6 = resultSet.getBlob("shop_picture6");
                                if(picture6 != null){
                                    values.put("shop_picture6",DealBitmap.compressImage(picture6,"_picture6_shop"+id));
                                }else {
                                    values.put("shop_picture6", (String) null);
                                }
                                sqLiteDatabase.insert("shop",null,values);
                            }
//                            Blob picture1 = resultSet.getBlob("shop_picture1");
//                            Bitmap bitmap1 = null;
//                            if(picture1 != null){
//                                InputStream inputStream1 = picture1.getBinaryStream();
//                                bitmap1 = DealBitmap.InputToBitmap(inputStream1);
//                            }
//                            findData(bitmap1,resultSet.getString("shop_title"),
//                                    resultSet.getString("shop_price"),
//                                    resultSet.getInt("shop_id"));
                        }
                        sqLiteDatabase.close();
                        Message message = new Message();
                        message.what = UPDATE_SHOP;
                        handler_shop.sendMessage(message);
                        resultSet.close();
                        JDBCTools.releaseConnection(stmt,conn);
                    }else {
                        Log.d("调试", "连接失败,商品列表");
                        Toast toast = Toast.makeText(ShopActivity.this, "请检查网络", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                }catch (SQLException e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }.start();
    }

    private Handler handler_shop = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case UPDATE_SHOP:{
//                    initData();
//                    setAdapter();
                    getData();
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

    private void findData(String bitmap, String title, String price, int id){
        shop_trade_image.add(bitmap);
        shop_trade_title.add(title);
        shop_trade_price.add("￥"+price);
        shop_trade_id.add(id);
    }

    private void initData(){
        Data = new ArrayList<>();
        for(int i=0; i<shop_trade_title.size(); i++){
            ShopTradeItemAdapter newData = new ShopTradeItemAdapter(Data);
            ShopTradeItemAdapter.Shop_Trade_item shop_trade_item = newData.new Shop_Trade_item(
                    shop_trade_image.get(i),shop_trade_title.get(i),shop_trade_price.get(i)
            );
            Data.add(shop_trade_item);
        }
    }

    private void setAdapter(){
        mShopTradeItemAdapter = new ShopTradeItemAdapter(Data);
        mRecyclerView.setAdapter(mShopTradeItemAdapter);
        setItemClick();
    }

    private void setItemClick(){
        mShopTradeItemAdapter.setmOnItemClickListener(new ShopTradeItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(ShopActivity.this,ShopPageActivity.class);
                intent.putExtra("put_shop_id",shop_trade_id.get(position));
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

