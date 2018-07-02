package com.example.wisdompark19.Main;


import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.base.bj.paysdk.domain.TrPayResult;
import com.base.bj.paysdk.listener.PayResultListener;
import com.base.bj.paysdk.utils.TrPay;
import com.example.wisdompark19.Adapter.PayItemAdapter;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 最美人间四月天 on 2018/1/18.
 */

public class PayActivity extends AppCompatActivity {

    private List<PayItemAdapter.Pay_item> Data;
    private RecyclerView.LayoutManager mLayoutManager;
    private PayItemAdapter mPayItemAdapter;
    private RecyclerView mRecyclerView;
    private DataBaseHelper dataBaseHelper;
    private LinearLayout pay_water;
    private LinearLayout pay_electric;
    private LinearLayout pay_gas;
    private LinearLayout pay_property;
    private CardView pay_card;
    private TextView pay_name,pay_yue;
    private EditText pay_count;
    private TextView pay_cancel, pay_ok;
    private String name = null;
    private Dialog dialog;

    private String yue_shui = "正在加载",yue_dian = "正在加载",yue_qi = "正在加载",yue_wu = "正在加载";

    ArrayList<String> count_name; // 上下滚动消息栏内容
    ArrayList<String> count_fee;
    ArrayList<String> count_time;
    ArrayList<String> count_pay;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_activity);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlue)); //设置顶部系统栏颜色
        Intent intent = getIntent();
        String intent_data = intent.getStringExtra("put_data_pay");
        Toolbar toolbar = (Toolbar)findViewById(R.id.pay_mainTool); //标题栏
        toolbar.setNavigationIcon(R.mipmap.ic_back_white);
        toolbar.setTitle(intent_data);
        back(toolbar);
        getYue();
        findView();
    }

    private void findView(){
        pay_water = (LinearLayout) findViewById(R.id.pay_water);
        pay_electric = (LinearLayout) findViewById(R.id.pay_electric);
        pay_gas = (LinearLayout) findViewById(R.id.pay_gas);
        pay_property = (LinearLayout) findViewById(R.id.pay_property);
        pay_card = (CardView) findViewById(R.id.pay_card);
        pay_name = (TextView) findViewById(R.id.pay_name);
        pay_yue = (TextView) findViewById(R.id.pay_yue);
        pay_count = (EditText) findViewById(R.id.pay_count);
        pay_cancel = (TextView) findViewById(R.id.pay_cancel);
        pay_ok = (TextView) findViewById(R.id.pay_ok);
        mRecyclerView = (RecyclerView)findViewById(R.id.rv_pay_item);
        pay_card.setVisibility(View.INVISIBLE);
        dataBaseHelper = new DataBaseHelper(PayActivity.this,AppConstants.SQL_VISION);
        ButtonClick();
        getData();
    }

    private void ButtonClick(){
        pay_water.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pay_name.setText("水费");
                if(yue_shui != null){
                    pay_yue.setText(yue_shui);
                }
                pay_card.setVisibility(View.VISIBLE);
                name = "水费";
            }
        });
        pay_electric.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pay_name.setText("电费");
                if(yue_dian != null){
                    pay_yue.setText(yue_dian);
                }
                pay_card.setVisibility(View.VISIBLE);
                name = "电费";
            }
        });
        pay_gas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pay_name.setText("天然气");
                if(yue_qi != null){
                    pay_yue.setText(yue_qi);
                }
                pay_card.setVisibility(View.VISIBLE);
                name = "天然气";
            }
        });
        pay_property.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pay_name.setText("物业费");
                if(yue_wu != null){
                    pay_yue.setText(yue_wu);
                }
                pay_card.setVisibility(View.VISIBLE);
                name = "物业费";
            }
        });

        pay_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pay_card.setVisibility(View.INVISIBLE);
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()){
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(),0);

                }
            }
        });

        pay_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name != null){
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm.isActive()){
                        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(),0);

                    }
                   if(pay_count.getText().toString().trim().isEmpty()) {
                       Toast.makeText(PayActivity.this,"请输入费用",Toast.LENGTH_LONG).show();
                   }else {
                       pay_card.setVisibility(View.INVISIBLE);
                      // showMyDialog();
                       callPay();
                   }
                }
            }
        });
    }

    private void getData(){
        count_name = new ArrayList<>();
        count_fee = new ArrayList<>();
        count_time = new ArrayList<>();
        count_pay = new ArrayList<>();

        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query("pay",null,null,null,null,null,
                "pay_time desc");
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex("pay_name"));
            String fee = cursor.getString(cursor.getColumnIndex("pay_yue"));
            String time = cursor.getString(cursor.getColumnIndex("pay_time"));
            String pay = cursor.getString(cursor.getColumnIndex("pay_count"));
            if(fee.substring(0,1).equals(".")){
                fee = "0" + fee;
            }
            initRollData(name,fee,time,pay);
        }
        cursor.close();
        sqLiteDatabase.close();
        initData();
    }

    private void getYue(){
        //获取余额
//        final SQLiteDatabase sqLiteDatabase = dataBaseHelper.getReadableDatabase();
//        Cursor cursor = null;
//        if(i == 1){
//            cursor = sqLiteDatabase.query("pay",new String[]{"pay_yue"},"pay_name = ?",
//                    new String[]{"水费"},null,null,"pay_time desc","1");
//        }else if(i == 2){
//            cursor = sqLiteDatabase.query("pay",new String[]{"pay_yue"},"pay_name = ?",
//                    new String[]{"电费"},null,null,"pay_time desc","1");
//        }else if(i == 3){
//            cursor = sqLiteDatabase.query("pay",new String[]{"pay_yue"},"pay_name = ?",
//                    new String[]{"天然气"},null,null,"pay_time desc","1");
//        }else if(i == 4){
//            cursor = sqLiteDatabase.query("pay",new String[]{"pay_yue"},"pay_name = ?",
//                    new String[]{"物业费"},null,null,"pay_time desc","1");
//        }
//        if(cursor != null){
//            while (cursor.moveToNext()){
//                yue = cursor.getString(cursor.getColumnIndex("pay_yue"));
//                if(yue.substring(0,1).equals(".")){
//                    yue = "0" + yue;
//                }
//            }
//            cursor.close();
//        }
//        sqLiteDatabase.close();
        new Thread(){
            public void run(){
                try{
                    Looper.prepare();//用于toast
                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
                    if (conn != null) { //判断 如果返回不为空则说明链接成功 如果为null的话则连接失败 请检查你的 mysql服务器地址是否可用 以及数据库名是否正确 并且 用户名跟密码是否正确
                        Log.d("调试", "连接成功,yue");
                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
                        //查询余额

                        String sql_yue_shui = "select pay_yue from pay where pay_area = '" +
                                   SharePreferences.getString(PayActivity.this,AppConstants.USER_AREA) +
                                   "' and pay_phone = '" +
                                   SharePreferences.getString(PayActivity.this,AppConstants.USER_PHONE) +
                                   "' and pay_name = '水费' order by pay_time desc limit 1";

                        String sql_yue_dian = "select pay_yue from pay where pay_area = '" +
                                    SharePreferences.getString(PayActivity.this,AppConstants.USER_AREA) +
                                    "' and pay_phone = '" +
                                    SharePreferences.getString(PayActivity.this,AppConstants.USER_PHONE) +
                                    "' and pay_name = '电费' order by pay_time desc limit 1";

                        String sql_yue_qi = "select pay_yue from pay where pay_area = '" +
                                    SharePreferences.getString(PayActivity.this,AppConstants.USER_AREA) +
                                    "' and pay_phone = '" +
                                    SharePreferences.getString(PayActivity.this,AppConstants.USER_PHONE) +
                                    "' and pay_name = '天然气' order by pay_time desc limit 1";

                        String sql_yue_wu = "select pay_yue from pay where pay_area = '" +
                                    SharePreferences.getString(PayActivity.this,AppConstants.USER_AREA) +
                                    "' and pay_phone = '" +
                                    SharePreferences.getString(PayActivity.this,AppConstants.USER_PHONE) +
                                    "' and pay_name = '物业费' order by pay_time desc limit 1";

                        ResultSet resultSet_shui = stmt.executeQuery(sql_yue_shui);
                        if (resultSet_shui.first()){
                            yue_shui = resultSet_shui.getString("pay_yue");
                        }else {
                            yue_shui = "0";
                        }
                        resultSet_shui.close();
                        ResultSet resultSet_dian = stmt.executeQuery(sql_yue_dian);
                        if (resultSet_dian.first()){
                            yue_dian = resultSet_dian.getString("pay_yue");
                        }else {
                            yue_dian = "0";
                        }
                        resultSet_dian.close();
                        ResultSet resultSet_qi = stmt.executeQuery(sql_yue_qi);
                        if (resultSet_qi.first()){
                            yue_qi = resultSet_qi.getString("pay_yue");
                        }else {
                            yue_qi = "0";
                        }
                        resultSet_qi.close();
                        ResultSet resultSet_wu = stmt.executeQuery(sql_yue_wu);
                        if (resultSet_wu.first()){
                            yue_wu = resultSet_wu.getString("pay_yue");
                        }else {
                            yue_wu = "0";
                        }
                        resultSet_wu.close();
                        JDBCTools.releaseConnection(stmt,conn);
                    }else {
                        Log.d("调试", "连接失败");
                        Toast toast = Toast.makeText(PayActivity.this, "请检查网络", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }catch (SQLException e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }.start();
    }

    private void initRollData(String name, String fee, String time, String pay){
        count_name.add(name);
        count_fee.add(fee); //余额
        count_time.add(time);
        count_pay.add(pay);
    }

    private void initData(){
        Data = new ArrayList<>();
        for(int i=0; i<count_name.size(); i++){
            PayItemAdapter newData = new PayItemAdapter(Data);
            PayItemAdapter.Pay_item pay_item = newData.new Pay_item(count_name.get(i),
                    count_fee.get(i),count_time.get(i),count_pay.get(i));
            Data.add(pay_item);
        }

        mLayoutManager = new LinearLayoutManager(PayActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mPayItemAdapter = new PayItemAdapter(Data);
        mRecyclerView.setAdapter(mPayItemAdapter);
    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.pay_weixin:{
////                Toast.makeText(this,"微信",Toast.LENGTH_SHORT).show();
//                weiXin();
//                dialog.dismiss();
//                break;
//            }
//            case R.id.pay_zhifubao:{
////                Toast.makeText(this,"支付宝",Toast.LENGTH_SHORT).show();
//                zhiFuBao();
//                dialog.dismiss();
//                break;
//            }
//            case R.id.pay_dismiss:{
////                Toast.makeText(this,"取消",Toast.LENGTH_SHORT).show();
//                dialog.dismiss();
//                break;
//            }
//        }
//    }

    private void callPay(){
        final String userid = SharePreferences.getString(this,AppConstants.USER_PHONE);
        String tradename = pay_name.getText().toString().trim();
        String outtradeno = userid + TimeChange.getBigTime();
        long amount = changeY2F(pay_count.getText().toString().trim());
        String backparams = tradename + outtradeno;
        // String backparams = null;
        // String notifyurl = null;
        String notifyurl = "http://101.200.13.92/notify/alipayTestNotify";
        //update(userid,tradename,outtradeno,"微信支付");
        /**
         * 发起快捷支付调用
         */
        TrPay.getInstance(this).callPay(tradename, outtradeno, amount, backparams, notifyurl, userid, new PayResultListener() {
            /**
             * 支付完成回调
             * @param context      上下文
             * @param outtradeno   商户系统订单号
             * @param resultCode   支付状态(RESULT_CODE_SUCC：支付成功、RESULT_CODE_FAIL：支付失败)
             * @param resultString 支付结果
             * @param payType      支付类型（1：支付宝 2：微信）
             * @param amount       支付金额
             * @param tradename    商品名称
             */
            @Override
            public void onPayFinish(Context context, String outtradeno, int resultCode, String resultString, int payType, Long amount, String tradename) {
                if (resultCode == TrPayResult.RESULT_CODE_SUCC.getId()) {//1：支付成功回调
                    TrPay.getInstance((Activity) context).closePayView();//关闭快捷支付页面
                    String type;
                    if(payType == 1){
                        type = "支付宝";
                    }else {
                        type = "微信";
                    }
                    update(userid,tradename,outtradeno,type);
                    System.out.println("支付成功");
                    getYue();
                   // Toast.makeText(PayActivity.this, resultString, Toast.LENGTH_LONG).show();
                    //支付成功逻辑处理
                } else if (resultCode == TrPayResult.RESULT_CODE_FAIL.getId()) {//2：支付失败回调
                    Toast.makeText(PayActivity.this, resultString, Toast.LENGTH_LONG).show();
                    //支付失败逻辑处理
                }
            }
        });
    }


    private void update(final String userid, final String tradename, final String outtradeno, final String select){

        final String user_name = SharePreferences.getString(PayActivity.this, AppConstants.USER_NAME);
        final String user_area = SharePreferences.getString(PayActivity.this, AppConstants.USER_AREA);
        final String pay_time = TimeChange.getBigTime();
        final String pay_money = pay_count.getText().toString().trim();
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String pay_yu = decimalFormat.format(Double.parseDouble(pay_yue.getText().toString().trim()) +
                              Double.parseDouble(pay_count.getText().toString().trim()));
        if(pay_yu.substring(0,1).equals(".")){
            pay_yu = "0" + pay_yu;
        }
        SQLiteDatabase sqLiteDatabase = dataBaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("pay_user",user_name);
        values.put("pay_phone",userid);
        values.put("pay_area",user_area);
        values.put("pay_time",pay_time);
        values.put("pay_name",tradename);
        values.put("pay_count",pay_money);
        values.put("pay_number",outtradeno);
        values.put("pay_yue",pay_yu);
        values.put("pay_select",select);
        sqLiteDatabase.insert("pay",null,values);
        sqLiteDatabase.close();
        getData();

        final String finalPay_yu = pay_yu;
        new Thread(){
            public void run(){
                try{
                    Looper.prepare();//用于toast
                    Connection conn = JDBCTools.getConnection("shequ","Zz123456");
                    if (conn != null) { //判断 如果返回不为空则说明链接成功 如果为null的话则连接失败 请检查你的 mysql服务器地址是否可用 以及数据库名是否正确 并且 用户名跟密码是否正确
                        Log.d("调试", "连接成功");
                        Statement stmt = conn.createStatement(); //根据返回的Connection对象创建 Statement对象
                        //上传
                        java.sql.PreparedStatement preparedStatement = null;
                        String newmessage_sql_insert = "insert into pay (pay_user,pay_phone,pay_area,pay_time," +
                                "pay_name,pay_count,pay_number,pay_yue,pay_select) values(?,?,?,?,?,?,?,?,?)";
                        preparedStatement = (java.sql.PreparedStatement)conn.prepareStatement(newmessage_sql_insert,Statement.RETURN_GENERATED_KEYS);
                        preparedStatement.setString(1, user_name);
                        preparedStatement.setString(2, userid);
                        preparedStatement.setString(3, user_area);
                        preparedStatement.setString(4, pay_time);
                        preparedStatement.setString(5, tradename);
                        preparedStatement.setString(6, pay_money);
                        preparedStatement.setString(7, outtradeno);
                        preparedStatement.setString(8, finalPay_yu);
                        preparedStatement.setString(9,select);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        JDBCTools.releaseConnection(stmt,conn);
                    }else {
                        Log.d("调试", "连接失败");
                        Toast toast = Toast.makeText(PayActivity.this, "请检查网络", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }catch (SQLException e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }.start();
    }



//    private void showMyDialog(){
//        dialog = new Dialog(this,R.style.ActionSheetDialogStyle);
//        //填充对话框的布局
//        View inflate = LayoutInflater.from(this).inflate(R.layout.pay_activity_bottom, null);
//        //初始化控件
//        TextView pay_weixin = (TextView) inflate.findViewById(R.id.pay_weixin);
//        TextView pay_zhifubao = (TextView) inflate.findViewById(R.id.pay_zhifubao);
//        TextView pay_dismiss = (TextView) inflate.findViewById(R.id.pay_dismiss);
////        pay_weixin.setOnClickListener(this);
////        pay_zhifubao.setOnClickListener(this);
////        pay_dismiss.setOnClickListener(this);
//        //将布局设置给Dialog
//        dialog.setContentView(inflate);
//        Window dialogWindow = dialog.getWindow();
//        dialogWindow.setGravity(Gravity.BOTTOM);
//        dialogWindow.setWindowAnimations(R.style.ActionSheetDialogStyle); // 添加动画
//        //获取当前Activity所在的窗体
//        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
//        lp.x = 0; // 新位置X坐标
//        lp.y = -20; // 新位置Y坐标
//        lp.width = (int) getResources().getDisplayMetrics().widthPixels; // 宽度
////      lp.height = WindowManager.LayoutParams.WRAP_CONTENT; // 高度
////      lp.alpha = 9f; // 透明度
//        inflate.measure(0, 0);
//        lp.height = inflate.getMeasuredHeight();
//        lp.alpha = 9f; // 透明度
//        dialogWindow.setAttributes(lp);
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.show();//显示对话框
//    }

    /**
     * 将元为单位的转换为分 替换小数点，支持以逗号区分的金额
     *
     * @param amount
     * @return
     */
    public static long changeY2F(String amount){
        String currency =  amount.replaceAll("\\$|\\￥|\\,", "");  //处理包含, ￥ 或者$的金额
        int index = currency.indexOf(".");
        int length = currency.length();
        Long amLong = 0l;
        if(index == -1){
            amLong = Long.valueOf(currency+"00");
        }else if(length - index >= 3){
            amLong = Long.valueOf((currency.substring(0, index+3)).replace(".", ""));
        }else if(length - index == 2){
            amLong = Long.valueOf((currency.substring(0, index+2)).replace(".", "")+0);
        }else{
            amLong = Long.valueOf((currency.substring(0, index+1)).replace(".", "")+"00");
        }
        return amLong;
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
