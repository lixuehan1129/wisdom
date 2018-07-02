package com.example.wisdompark19.Adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.wisdompark19.AutoProject.DealBitmap;
import com.example.wisdompark19.R;

import java.lang.ref.PhantomReference;
import java.util.List;


/**
 * Created by 最美人间四月天 on 2018/3/8.
 */

public class ShopTradeItemAdapter extends RecyclerView.Adapter<ShopTradeItemAdapter.ViewHolder> {

    private List<Shop_Trade_item> mDataSet;
    private OnItemClickListener mOnItemClickListener;
    private Context mContext;

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView shop_item_image;
        TextView shop_item_content;
        TextView shop_item_price;

        public ViewHolder(View itemView) {
            super(itemView);
            shop_item_image = (ImageView)itemView.findViewById(R.id.shop_item_image);
            shop_item_content = (TextView)itemView.findViewById(R.id.shop_item_content);
            shop_item_price = (TextView)itemView.findViewById(R.id.shop_item_price);
        }
    }

    public ShopTradeItemAdapter(List<Shop_Trade_item> data){
        mDataSet = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shop_trade_item,parent,false);
        mContext = parent.getContext();
        final RecyclerView.ViewHolder vh = new ViewHolder(v);
        return (ViewHolder)vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        Shop_Trade_item mShop_Trade_item = mDataSet.get(position);
        String shop_image = mShop_Trade_item.getShop_trade_image();
        String shop_content = mShop_Trade_item.getShop_trade_content();
        //这里的图片来源需要修改
        if(shop_image != null){
            Glide.with(mContext)
                    .load(shop_image)
                    .asBitmap()  //不可加载动图
                    .dontAnimate()//取消淡入淡出动画
                    .thumbnail(0.1f) //先加载十分之一作为缩略图
                    .into(holder.shop_item_image);
        }
        holder.shop_item_content.setText(shop_content);
        holder.shop_item_price.setText(mShop_Trade_item.getShop_trade_price());

        //判断是否设置了监听
        //为View设置监听
        if(mOnItemClickListener !=null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.itemView, position);
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    //Recycleview监听接口
    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener){
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public class Shop_Trade_item{
        private String shop_trade_image;
        private String shop_trade_content;
        private String shop_trade_price;

        public String getShop_trade_price() {
            return shop_trade_price;
        }

        public void setShop_trade_price(String shop_trade_price) {
            this.shop_trade_price = shop_trade_price;
        }

        public String getShop_trade_image() {
            return shop_trade_image;
        }

        public void setShop_trade_image(String shop_trade_image) {
            this.shop_trade_image = shop_trade_image;
        }

        public String getShop_trade_content() {
            return shop_trade_content;
        }

        public void setShop_trade_content(String shop_trade_content) {
            this.shop_trade_content = shop_trade_content;
        }

        public Shop_Trade_item(String shop_trade_image, String shop_trade_content,
                               String shop_trade_price){
            this.shop_trade_image = shop_trade_image;
            this.shop_trade_content = shop_trade_content;
            this.shop_trade_price = shop_trade_price;
        }
    }
}



