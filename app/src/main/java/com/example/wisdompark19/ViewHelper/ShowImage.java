package com.example.wisdompark19.ViewHelper;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.wisdompark19.AutoProject.DealBitmap;
import com.example.wisdompark19.AutoProject.JDBCTools;
import com.example.wisdompark19.R;
import com.mysql.jdbc.Connection;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;


/**
 * Created by 最美人间四月天 on 2017/5/18.
 */

public class ShowImage extends AppCompatActivity {
    private DragImageView img;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        full(true);
        setContentView(R.layout.show_image);
        RelativeLayout back = (RelativeLayout)this.findViewById(R.id.back_act);
        img = (DragImageView)this.findViewById(R.id.large_image);
        Bundle bundle = this.getIntent().getExtras();
        String Image_select = bundle.getString("image_select_name");

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;//加载到内存中
        Bitmap bm = BitmapFactory.decodeFile(Image_select,options);
        img.setImageBitmap(bm);

        back.setOnClickListener(new View.OnClickListener() { // 点击返回
            public void onClick(View paramView) {
                System.out.println("点击图片返回");
                full(false);
                ShowImage.this.finish();
            }
        });

    }

    //显示图片时隐藏状态栏
    private void full(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }
}
