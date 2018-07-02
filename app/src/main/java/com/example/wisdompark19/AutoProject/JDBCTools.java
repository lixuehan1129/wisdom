package com.example.wisdompark19.AutoProject;

import android.util.Log;

import com.mysql.jdbc.Connection;

import java.sql.DriverManager;


/**
 * Created by 最美人间四月天 on 2018/3/29.
 *
 */

public class JDBCTools {
    //数据库连接
    public static Connection getConnection(String user, String pass) {
        Connection conn = null;//声明连接对象
        String driver = "com.mysql.jdbc.Driver";// 驱动程序类名
        String url = "jdbc:mysql://60.205.140.219:3306/shequ?" // 数据库URL
                + "useUnicode=true&characterEncoding=utf-8";// 防止乱码
        //使用 DriverManger.getConnection链接数据库  第一个参数为连接地址 第二个参数为用户名 第三个参数为连接密码  返回一个Connection对象
        try {
            Class.forName(driver);// 注册(加载)驱动程序
            conn = (Connection) DriverManager.getConnection(url, user, pass);// 获取数据库连接
            Log.d("调试", "JDBC连接成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
    //释放数据库连接
    public static void releaseConnection(java.sql.Statement stmt, Connection conn) {
        try {
            if (stmt != null)
                stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (conn != null)
                conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
