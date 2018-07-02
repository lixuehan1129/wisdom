package com.example.wisdompark19.Main;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.DealBitmap;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.AutoProject.TimeChange;
import com.example.wisdompark19.R;
import com.example.wisdompark19.Repair.RepairActivity;
import com.example.wisdompark19.Shop.ShopActivity;
import com.example.wisdompark19.Society.SocietyNewMessagePage;
import com.example.wisdompark19.ViewHelper.BaseFragment;
import com.example.wisdompark19.ViewHelper.DataBaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by 最美人间四月天 on 2018/1/9.
 */

public class MainFragment extends BaseFragment {

    private DataBaseHelper dataBaseHelper;
    private GridView mGridView;
    private TextView textView;
    private int mCurrPos;
    private SliderLayout sliderLayout;
    private ArrayList<Integer> shop_url = new ArrayList<Integer>();
    private LocalBroadcastManager broadcastManager;
    private IntentFilter intentFilter;
    private BroadcastReceiver mReceiver;
     // 上下滚动消息栏内容
    ArrayList<String> card_message_content = new ArrayList<String>();
    ArrayList<String> card_message_time = new ArrayList<String>();
    ArrayList<Integer> card_message_id = new ArrayList<>();
    ArrayList<Bitmap> card_message_image = new ArrayList<>();

    private int[] mImages = {
            R.mipmap.ic_main_pay,
            R.mipmap.ic_main_repair,
            R.mipmap.ic_main_cart,
            R.mipmap.ic_main_code,
            R.mipmap.ic_main_map,
            R.mipmap.ic_main_waishe,
            R.mipmap.ic_main_more,
            0

    };
    private String[] mContent = {
            "生活缴费",
            "报修管理",
            "电商平台",
            "通行证",
            "我的位置",
   //         "外设接口",
            "咨询中心",
            "更多",
            null
    };

    private ViewFlipper viewFlipper;

    public static MainFragment newInstance(String info) {
        Bundle args = new Bundle();
        args.putString("info", info);
        MainFragment mainFragment = new MainFragment();
        mainFragment.setArguments(args);
        return mainFragment;
    }


    @Override
    public void onStart(){
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.mainfragment, null);
        findView(view); //界面
        initGridData();
        return view;
    }

    @Override
    protected void onFragmentFirstVisible() {
        //去服务器下载数据
        textView.getLayoutParams().height = textView.getLayoutParams().WRAP_CONTENT;
        LocalData();
        initRoll();
//        getData();
    }

     //这是干什么的，给忘了？？
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        broadcastManager = LocalBroadcastManager.getInstance(getActivity());
//        intentFilter = new IntentFilter();
//        mReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent){
//                //收到广播后所作的操作
//                LocalData();
//                initRoll();
//            }
//        };
//        broadcastManager.registerReceiver(mReceiver, intentFilter);
//    }
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        broadcastManager.unregisterReceiver(mReceiver);
//    }

    //初始化界面
    private void findView(View view){
        Toolbar mToolber = (Toolbar)view.findViewById(R.id.mainFragment_mainTool);
        mToolber.setTitle("主页");
        mGridView = (GridView)view.findViewById(R.id.mainFragment_gridview);
        //滚动通知
        viewFlipper = (ViewFlipper)view.findViewById(R.id.roll_flipper);
        textView = (TextView)view.findViewById(R.id.roll_vis);
        dataBaseHelper = new DataBaseHelper(getActivity(),AppConstants.SQL_VISION);
        sliderLayout = (SliderLayout)view.findViewById(R.id.main_slider);
    }

    private void initRoll(){
        shop_url = new ArrayList<Integer>();
        shop_url.add(R.mipmap.ic_top1);
        shop_url.add(R.mipmap.ic_top2);
        shop_url.add(R.mipmap.ic_top3);
        shop_url.add(R.mipmap.ic_top4);
        rollPicture();
    }
    //图片滚动
    private void rollPicture(){
        for (int i=0; i<shop_url.size(); i++){
            DefaultSliderView textSliderView = new DefaultSliderView(getActivity());
            textSliderView
                    .image(shop_url.get(i))
                    .setScaleType(BaseSliderView.ScaleType.Fit);  //图片缩放类型
            textSliderView.bundle(new Bundle());
            sliderLayout.addSlider(textSliderView); //添加页面
        }
        sliderLayout.setDuration(8000);
        sliderLayout.addOnPageChangeListener(onPageChangeListener);
    }



    //页面改变监听
    private ViewPagerEx.OnPageChangeListener onPageChangeListener=new ViewPagerEx.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageSelected(int position) {
            Log.d("ansen", "Page Changed: " + position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {}
    };

    private void initGridData(){
        ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i < 8; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("ItemImage", mImages[i]);// 图片
            map.put("ItemText", mContent[i]);// 名称
            lstImageItem.add(map);
        }
        //构建一个适配器
        SimpleAdapter simple = new SimpleAdapter(getActivity(), lstImageItem, R.layout.gridview_item,
                new String[] { "ItemImage", "ItemText" }, new int[] {R.id.gridview_item_card_image,
                R.id.gridview_item_card_name });
        mGridView.setAdapter(simple);
        //添加监听事件
        mGridView.setOnItemClickListener(new GridView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:{
                        Intent intent = new Intent(getActivity(),PayActivity.class);
                        intent.putExtra("put_data_pay","生活缴费");
                        startActivity(intent);
                    }break;
                    case 1:{
                        Intent intent = new Intent(getActivity(),RepairActivity.class);
                        intent.putExtra("put_data_repair","报修管理");
                        startActivity(intent);
                    }break;
                    case 2:{
                        Intent intent = new Intent(getActivity(),ShopActivity.class);
                        intent.putExtra("put_data_shop","电商平台");
                        startActivity(intent);
                    }break;
                    case 3:{
                        Intent intent = new Intent(getActivity(),CodeActivity.class);
                        intent.putExtra("put_data_code","通行证");
                        startActivity(intent);
                    }break;
                    case 4:{
                        Intent intent = new Intent(getActivity(),MapActivity.class);
                        intent.putExtra("put_data_weizhi","我的位置");
                        startActivity(intent);
                    }break;
                    case 5:{
                   //     Intent intent = new Intent(getActivity(),PeripheralActivity.class);
                    //    intent.putExtra("put_data_waishe","外设接口");
                        Intent intent = new Intent(getActivity(),GuideActivity.class);
                        intent.putExtra("put_data_waishe","咨询中心");
                        startActivity(intent);
                    }break;
                    case 6:{
                        Toast toast=Toast.makeText(getActivity(), "正在更新", Toast.LENGTH_SHORT);
                        toast.show();
                    }break;
                    case 7:{
                    }break;

                }
            }

        });
    }

//    private void getData(){
//        new Thread(){
//            public void run(){
//                try {
//                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
//                    if(conn!=null){ //判断 如果返回不为空则说明链接成功 如果为null的话则连接失败 请检查你的 mysql服务器地址是否可用 以及数据库名是否正确 并且 用户名跟密码是否正确
//                        Log.d("调试","连接成功,滚动消息");
//                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
//                        String sql = "select * from newmessage where newmessage_area = '" +
//                                SharePreferences.getString(getActivity(),AppConstants.USER_AREA) +
//                                "' order by newmessage_id desc limit 3";
//                        ResultSet rs = stmt.executeQuery(sql); //使用executeQury方法执行sql语句 返回ResultSet对象 即查询的结果
//                        List<String> content_name = new ArrayList<>();
//                        while (rs.next()) {
//                            content_name.add(rs.getString("newmessage_phone"));
//                            initRollData(rs.getString("newmessage_title"),rs.getString("newmessage_content"),
//                                    rs.getString("newmessage_time"),rs.getInt("newmessage_id"));
//                        }
//                        rs.close();
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
//                        Message message = new Message();
//                        message.what = UPDATE_ROLL;
//                        handler_roll.sendMessage(message);
//                        JDBCTools.releaseConnection(stmt,conn);
//                    }else{
//                        Log.d("调试","连接失败，滚动消息");
//                    }
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//    }
//
//    private Handler handler_roll = new Handler(new Handler.Callback() {
//
//        @Override
//        public boolean handleMessage(Message msg) {
//            // TODO Auto-generated method stub
//            switch (msg.what){
//                case UPDATE_ROLL:{
//                    if(card_message_content.size() > 0){
//                        textView.getLayoutParams().height = 0;
//                        initRollNotice();
//                    }
//                    break;
//                }
//                default:
//                    break;
//            }
//            return false;
//        }
//    });

    private void LocalData(){
        card_message_content = new ArrayList<>();
        card_message_time = new ArrayList<>();
        card_message_image = new ArrayList<>();
        card_message_id = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query("newmessage",null,"newmessage_area = ?",new String[]{
                SharePreferences.getString(getActivity(),AppConstants.USER_AREA)
        },null,null,"newmessage_id desc","3");
        while (cursor.moveToNext()){
            //从本地数据库读取
            String phone = cursor.getString(cursor.getColumnIndex("newmessage_phone"));
            String content = cursor.getString(cursor.getColumnIndex("newmessage_content"));
            String time = cursor.getString(cursor.getColumnIndex("newmessage_time"));
            int id = cursor.getInt(cursor.getColumnIndex("newmessage_id"));
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
            initRollData(content,time,id,picture);
        }
        cursor.close();
        sqLiteDatabase.close();
        if(card_message_id.size() > 0){
            textView.getLayoutParams().height = 0;
            initRollNotice();
        }
    }

    private void initRollData(String content, String time, int id, Bitmap bitmap){
        // 滚动消息栏的显示内容
        card_message_content.add(content);
        card_message_time.add(TimeChange.StringToString(time));
        card_message_id.add(id);
        card_message_image.add(bitmap);
    }

    // 上下滚动消息栏
    private void initRollNotice() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
               if(getActivity() == null){
                   return;
               }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            moveNext();
                        }
                    });
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 0, 9000);
    }

    private void moveNext() {
        setView(this.mCurrPos, this.mCurrPos + 1);
        viewFlipper.setInAnimation(getActivity(), R.anim.in_bottomtop);
        viewFlipper.setOutAnimation(getActivity(), R.anim.out_bottomtop);
        viewFlipper.showNext();
    }

    // 将titleList 文本添加到 textView 中
    private void setView(int curr, int next) {
        View noticeView = getLayoutInflater().inflate(R.layout.notice_item_main, null);
        CardView cardView = (CardView)noticeView.findViewById(R.id.card_message1);
        final CircleImageView card_message_image_tv = (CircleImageView)cardView.findViewById(R.id.card_message_image1);
        final TextView card_message_content_tv = (TextView)cardView.findViewById(R.id.card_message_content1);
        final TextView card_message_time_tv = (TextView)cardView.findViewById(R.id.card_message_time1);
        final TextView card_message_id_tv = (TextView)cardView.findViewById(R.id.card_message_id1);
        if ((curr < next) && (next > (card_message_content.size() - 1))) {
            next = 0;
        } else if ((curr > next) && (next < 0)) {
            next = card_message_content.size() - 1;
        }
        card_message_content_tv.setText(card_message_content.get(next));
        card_message_time_tv.setText(card_message_time.get(next));
        card_message_image_tv.setImageBitmap(card_message_image.get(next));
        card_message_id_tv.setText(String.valueOf(card_message_id.get(next)));

        // 点击文本跳转到网络链接中
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(getActivity(), SocietyNewMessagePage.class);
                intent.putExtra("put_data_mes_id",Integer.valueOf(card_message_id_tv.getText().toString()));
                intent.putExtra("put_data_mes_select",1);
                startActivity(intent);
            }
        });
        if (viewFlipper.getChildCount() > 1) {
            viewFlipper.removeViewAt(0);
        }
        viewFlipper.addView(noticeView, viewFlipper.getChildCount());
        mCurrPos = next;
    }
}
