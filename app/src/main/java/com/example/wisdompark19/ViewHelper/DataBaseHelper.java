package com.example.wisdompark19.ViewHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ROBOSOFT on 2018/4/24.
 */

public class DataBaseHelper extends SQLiteOpenHelper {

    //定义数据库文件名
    public static final String TABLE_NAME = "wisdomPark.db";

    public static final String CREATE_USER = "create table user ("
            + "id integer primary key autoincrement, "
            + "user_id integer, "
            + "user_name text, "
            + "user_phone text, "
            + "user_address text, "
            + "user_area integer, "
            + "user_sort integer, "
            + "user_sex text, "
            + "user_picture blob)";

    public static final String CREATE_NEWMESSAGE ="create table newmessage ("
            + "id integer primary key autoincrement, "
            + "newmessage_id integer, "
            + "newmessage_xin integer, "
            + "newmessage_name text, "
            + "newmessage_area text, "
            + "newmessage_phone text, "
            + "newmessage_time text, "
            + "newmessage_title text, "
            + "newmessage_content text, "
            + "newmessage_picture1 text, "
            + "newmessage_picture2 text, "
            + "newmessage_picture3 text, "
            + "newmessage_picture4 text, "
            + "newmessage_picture5 text, "
            + "newmessage_picture6 text)";

    public static final String CREATE_SHIWU ="create table shiwu ("
            + "id integer primary key autoincrement, "
            + "shiwu_id integer, "
            + "shiwu_name text, "
            + "shiwu_phone text, "
            + "shiwu_area text, "
            + "shiwu_time text, "
            + "shiwu_title text, "
            + "shiwu_content text, "
            + "shiwu_picture1 text, "
            + "shiwu_picture2 text, "
            + "shiwu_picture3 text, "
            + "shiwu_picture4 text, "
            + "shiwu_picture5 text, "
            + "shiwu_picture6 text)";

    public static final String CREATE_TUCAO ="create table tucao ("
            + "id integer primary key autoincrement, "
            + "tucao_id integer, "
            + "tucao_name text, "
            + "tucao_area text, "
            + "tucao_phone text, "
            + "tucao_time text, "
            + "tucao_content text)";

    public static final String CREATE_REPAIR ="create table repair ("
            + "id integer primary key autoincrement, "
            + "repair_id integer, "
            + "repair_name text, "
            + "repair_phone text, "
            + "repair_time text, "
            + "repair_select_time text, "
            + "repair_area text, "
            + "repair_title text, " //报修类型
            + "repair_content text, "
            + "repair_picture1 text, "
            + "repair_picture2 text, "
            + "repair_picture3 text, "
            + "repair_picture4 text, "
            + "repair_picture5 text, "
            + "repair_picture6 text, "
            + "repair_progress integer, "
            + "repair_pingjia integer)";

    public static final String CREATE_SHOP ="create table shop ("
            + "id integer primary key autoincrement, "
            + "shop_id integer, "
            + "shop_name text, "
            + "shop_phone text, "
            + "shop_time text, "
            + "shop_title text, "
            + "shop_content text, "
            + "shop_area text, "
            + "shop_price text, "
            + "shop_picture1 text, "
            + "shop_picture2 text, "
            + "shop_picture3 text, "
            + "shop_picture4 text, "
            + "shop_picture5 text, "
            + "shop_picture6 text)";

    public static final String CREATE_PAY ="create table pay ("
            + "pay_id integer primary key autoincrement, "
            + "pay_user text, "
            + "pay_phone text, "
            + "pay_area text, "
            + "pay_time text, "
            + "pay_name text, "
            + "pay_count text, "
            + "pay_number text, "
            + "pay_yue text, "
            + "pay_select text)";

    public static final String CREATE_GUIDE ="create table guide ("
            + "id integer primary key autoincrement, "
            + "guide_id integer, "
            + "guide_phone text, "
            + "guide_area text, "
            + "guide_que text, "
            + "guide_ans text, "
            + "guide_time text)";

    private Context mContext;

    /**
     * @param context
     * @param name    数据库文件的名称
     * @param factory null
     * @param version 数据库文件的版本
     */
    private DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                               int version) {
        super(context, name, factory, version);
    }

    // 对外提供构造函数
    public DataBaseHelper(Context context, int version) {
        //调用该类中的私有构造函数
        this(context, TABLE_NAME, null, version);
    }


    // 当第一次创建数据的时候回调方法
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER);
        db.execSQL(CREATE_NEWMESSAGE);
        db.execSQL(CREATE_SHIWU);
        db.execSQL(CREATE_TUCAO);
        db.execSQL(CREATE_REPAIR);
        db.execSQL(CREATE_SHOP);
        db.execSQL(CREATE_PAY);
        db.execSQL(CREATE_GUIDE);
    }

    //数据库升级
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists user");
        db.execSQL("drop table if exists newmessage");
        db.execSQL("drop table if exists shiwu");
        db.execSQL("drop table if exists tucao");
        db.execSQL("drop table if exists repair");
        db.execSQL("drop table if exists shop");
        db.execSQL("drop table if exists pay");
        db.execSQL("drop table if exists guide");
        onCreate(db);
    }

    // 当数据库被打开时回调该方法
    @Override
    public void onOpen(SQLiteDatabase db) {
        Log.d("DB", "onOpen");
    }
}

