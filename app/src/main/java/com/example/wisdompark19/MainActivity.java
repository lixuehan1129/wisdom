package com.example.wisdompark19;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ashokvarma.bottomnavigation.TextBadgeItem;
import com.example.wisdompark19.Adapter.SectionsPagerAdapter;
import com.example.wisdompark19.AutoProject.AppConstants;
import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.AutoProject.SharePreferences;
import com.example.wisdompark19.Main.MainFragment;
import com.example.wisdompark19.Mine.MineFragment;
import com.example.wisdompark19.Society.SocietyFragment;
import com.example.wisdompark19.ViewHelper.NoScollViewPager;
import com.mysql.jdbc.Connection;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BottomNavigationBar.OnTabSelectedListener, ViewPager.OnPageChangeListener {

    private NoScollViewPager viewPager;
    private BottomNavigationBar bottomNavigationBar;
    private List<Fragment> fragments;
    private LocalBroadcastManager broadcastManager;
    private IntentFilter intentFilter;
    private BroadcastReceiver mReceiver;
    private int unread = 0;
    private TextBadgeItem badgeItem = new TextBadgeItem();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue)); //设置顶部系统栏颜色
        findView(); //初始化布局
        getBroad();
        Bugly.init(getApplicationContext(), "7ad29ea377", false);
        Beta.autoInit = true;
        Beta.autoCheckUpgrade = true;
        Beta.smallIconId = R.mipmap.icon_app_f;
        setQuanXian();
        createConnect();
    }

    private void getBroad(){
        broadcastManager = LocalBroadcastManager.getInstance(MainActivity.this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstants.BROAD_UNREAD);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent){
                //收到广播后所作的操作
                if(unread != intent.getIntExtra("unread",0)){
                    unread = intent.getIntExtra("unread",0);
                    System.out.println("接受广播,数量"+unread);
                    if(unread >= 2){
                        badgeItem.setText(String.valueOf(unread-1));
                        badgeItem.show();
                    }else {
                        badgeItem.hide();
                    }
                }
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
        viewPager = (NoScollViewPager) findViewById(R.id.viewpager);
        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation);
        viewPager.setOffscreenPageLimit(2);
        initView();
    }

    private void initView() {
        initBottomNavigationBar();
        initViewPager();
    }

    private void initBottomNavigationBar() {
        bottomNavigationBar.setTabSelectedListener(this);

        badgeItem.setBackgroundColorResource(R.color.colorRed);
       // badgeItem.hide();//角标
        badgeItem.setText("0");
      //  badgeItem.hide();

        bottomNavigationBar.clearAll();
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        bottomNavigationBar.setActiveColor(R.color.colorBlue)
                           .setInActiveColor(R.color.colorGray_1);

        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.mipmap.ic_home_white, "主页"))
                .addItem(new BottomNavigationItem(R.mipmap.ic_society_white, "社区").setBadgeItem(badgeItem))
                .addItem(new BottomNavigationItem(R.mipmap.ic_mine_white, "我的"))
                .initialise();//所有的设置需在调用该方法前完成

        //设置图标文字大小
        setBottomNavigationItem(bottomNavigationBar,6,28,13);
    }

    private void initViewPager() {
        fragments = new ArrayList<Fragment>();
        fragments.add(new MainFragment());
        fragments.add(new SocietyFragment());
        fragments.add(new MineFragment());
        viewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager(), fragments));
        viewPager.addOnPageChangeListener(this);
        viewPager.setCurrentItem(0);
    }

    private void setQuanXian(){
        //获取权限
        List<String> permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.BLUETOOTH);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.VIBRATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.VIBRATE);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.CAMERA);
        }
        if(!permissionList.isEmpty()){
            String[] permissions= permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }
    }
    //Android按返回键，程序进入后台运行，不关闭程序
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
           // moveTaskToBack(false);
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void createConnect(){
        new  Thread(){
            public  void run(){
                try {
                    Looper.prepare();
                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
                    if(conn!=null){ //判断 如果返回不为空则说明链接成功 如果为null的话则连接失败 请检查你的 mysql服务器地址是否可用 以及数据库名是否正确 并且 用户名跟密码是否正确
                        Log.d("调试","连接成功，成员更新");
                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
                        String sql = "select * from user where user_phone = '" +
                                SharePreferences.getString(MainActivity.this,AppConstants.USER_PHONE) +
                                "'"; //要执行的sql语句
                        ResultSet rs = stmt.executeQuery(sql); //使用executeQury方法执行sql语句 返回ResultSet对象 即查询的结果
                        while (rs.next()) {
                            int user_sort = rs.getInt("user_sort");
                            String name = rs.getString("user_name");
                            String area = rs.getString("user_area");
                            String address = rs.getString("user_address");
                            SharePreferences.remove(MainActivity.this,AppConstants.USER_SORT);
                            SharePreferences.putInt(MainActivity.this,AppConstants.USER_SORT,user_sort);

                            SharePreferences.remove(MainActivity.this,AppConstants.USER_NAME);
                            SharePreferences.putString(MainActivity.this,AppConstants.USER_NAME,name);

                            SharePreferences.remove(MainActivity.this,AppConstants.USER_AREA);
                            SharePreferences.putString(MainActivity.this,AppConstants.USER_AREA,area);

                            SharePreferences.remove(MainActivity.this,AppConstants.USER_ADDRESS);
                            SharePreferences.putString(MainActivity.this,AppConstants.USER_ADDRESS,address);
                        }
//                        Intent intent_broad = new Intent(AppConstants.BROAD_CON);
//                        LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent_broad);
                        rs.close();
                        JDBCTools.releaseConnection(stmt,conn);
                    }else{
                        Log.d("调试","连接失败");
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    Looper.loop();
                }
            }
        }.start();
    }

    private void showTip(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,str,Toast.LENGTH_LONG).show();
            }
        });
    }


//    private void init(){
//        Stetho.initializeWithDefaults(this);
//        new OkHttpClient.Builder()
//                .addNetworkInterceptor(new StethoInterceptor())
//                .build();
//    }


    @Override
    public void onTabSelected(int position) {
        viewPager.setCurrentItem(position);
    }

    @Override
    public void onTabUnselected(int position) {

    }

    @Override
    public void onTabReselected(int position) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        bottomNavigationBar.selectTab(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    /**
     @param bottomNavigationBar，需要修改的 BottomNavigationBar
     @param space 图片与文字之间的间距
     @param imgLen 单位：dp，图片大小，应 <= 36dp
     @param textSize 单位：dp，文字大小，应 <= 20dp

     使用方法：直接调用setBottomNavigationItem(bottomNavigationBar, 6, 26, 10);
     代表将bottomNavigationBar的文字大小设置为10dp，图片大小为26dp，二者间间距为6dp
     **/

    private void setBottomNavigationItem(BottomNavigationBar bottomNavigationBar, int space, int imgLen, int textSize){
        Class barClass = bottomNavigationBar.getClass();
        Field[] fields = barClass.getDeclaredFields();
        for(int i = 0; i < fields.length; i++){
            Field field = fields[i];
            field.setAccessible(true);
            if(field.getName().equals("mTabContainer")){
                try{
                    //反射得到 mTabContainer
                    LinearLayout mTabContainer = (LinearLayout) field.get(bottomNavigationBar);
                    for(int j = 0; j < mTabContainer.getChildCount(); j++){
                        //获取到容器内的各个Tab

                        View view = mTabContainer.getChildAt(j);
                        //获取到Tab内的各个显示控件
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip2px(56));
                        FrameLayout container = (FrameLayout) view.findViewById(R.id.fixed_bottom_navigation_container);
                        container.setLayoutParams(params);
                        container.setPadding(dip2px(12), dip2px(0), dip2px(12), dip2px(0));

                        //获取到Tab内的文字控件
                        TextView labelView = (TextView) view.findViewById(com.ashokvarma.bottomnavigation.R.id.fixed_bottom_navigation_title);
                        //计算文字的高度DP值并设置，setTextSize为设置文字正方形的对角线长度，所以：文字高度（总内容高度减去间距和图片高度）*根号2即为对角线长度，此处用DP值，设置该值即可。
                        labelView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
                        labelView.setIncludeFontPadding(false);
                        labelView.setPadding(0,0,0,dip2px(20-textSize - space/2));

                        //获取到Tab内的图像控件
                        ImageView iconView = (ImageView) view.findViewById(com.ashokvarma.bottomnavigation.R.id.fixed_bottom_navigation_icon);
                        //设置图片参数，其中，MethodUtils.dip2px()：换算dp值
                        params = new FrameLayout.LayoutParams(dip2px(imgLen), dip2px(imgLen));
                        params.setMargins(0,0,0,space/2);
                        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                        iconView.setLayoutParams(params);
                    }
                } catch (IllegalAccessException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public int dip2px(float dpValue) {
        final float scale = getApplication().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}


