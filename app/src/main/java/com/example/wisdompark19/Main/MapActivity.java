package com.example.wisdompark19.Main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.wisdompark19.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 最美人间四月天 on 2018/1/18.
 * https://www.jianshu.com/p/a2d694950e3a
 * 修改KEY之后
 */

public class MapActivity extends AppCompatActivity {

    private LocationClient mLocationClient;
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean  isFirstLocate = true;
//    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //地图初始化
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.map_activity);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue)); //设置顶部系统栏颜色
        Intent intent = getIntent();
        String intent_data = intent.getStringExtra("put_data_weizhi");
        Toolbar toolbar = (Toolbar)findViewById(R.id.mainTool); //标题栏
        toolbar.setNavigationIcon(R.mipmap.ic_back_white);
        toolbar.setTitle(intent_data);
        back(toolbar);
        setMap();
        findView();
        setQuanXian();
    }

    private void setMap(){
        mapView = (MapView)findViewById(R.id.baidu_map);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
    }

    private void findView(){

//        textView = (TextView)findViewById(R.id.test_text);
//        textView.setVisibility(View.INVISIBLE);
        mLocationClient = new LocationClient(this);
        mLocationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                StringBuilder currentPosition =  new StringBuilder();
                currentPosition.append("维度：").append(bdLocation.getLatitude()).append("\n");
                currentPosition.append("经度：").append(bdLocation.getLongitude()).append("\n");
                currentPosition.append("国家：").append(bdLocation.getCountry()).append("\n");
                currentPosition.append("省：").append(bdLocation.getProvince()).append("\n");
                currentPosition.append("市：").append(bdLocation.getCity()).append("\n");
                currentPosition.append("区：").append(bdLocation.getDistrict()).append("\n");
                currentPosition.append("街道：").append(bdLocation.getStreet()).append("\n");
                currentPosition.append("定位方式：");
                Log.e("tag","当前的定位方式="+bdLocation.getLocType());

                if(bdLocation.getLocType() == BDLocation.TypeGpsLocation){
                    currentPosition.append("GPS");
                }else if(bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){
                    currentPosition.append("网络");
                }

                if(bdLocation.getLocType() == BDLocation.TypeGpsLocation ||bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){
                    if(isFirstLocate){
                        LatLng ll = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                        MapStatus.Builder builder = new MapStatus.Builder();
                        builder.target(ll).zoom(18.0f);
                        baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                        isFirstLocate = false;
                    }
                    MyLocationData.Builder  locationBuilder = new MyLocationData.Builder();
                    locationBuilder.latitude(bdLocation.getLatitude());
                    locationBuilder.longitude(bdLocation.getLongitude());
                    MyLocationData locationData = locationBuilder.build();
                    baiduMap.setMyLocationData(locationData);
                }
//                textView.setText(currentPosition);
                 System.out.println(currentPosition);
            }
        });
//        mLocationClient.start();
    }

    private void requestLocation() {
        LocationClientOption  option = new LocationClientOption();
        option.setScanSpan(1500); //刷新时间
        option.setAddrType("all");
        option.setCoorType("bd09ll");// 可选，默认gcj02，设置返回的定位结果坐标系
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for (int result: grantResults
                            ) {
                        if(result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this, "必须同意所有的权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else{
                    Toast.makeText(this, "发生了错误", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void setQuanXian(){
        //获取权限
        List<String> permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if(!permissionList.isEmpty()){
            String[] permissions= permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MapActivity.this,permissions,1);
        }else{
            requestLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
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


