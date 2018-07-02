package com.example.wisdompark19.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.wisdompark19.AutoProject.DealBitmap;
import com.example.wisdompark19.R;

import java.util.List;

/**
 * Created by 最美人间四月天 on 2017/5/11.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private Context mcontext;
    private List<Item_Image> mDataSet;


    class ViewHolder extends RecyclerView.ViewHolder{

        ImageView item_text;

        public ViewHolder(View itemView) {
            super(itemView);
            item_text = (ImageView) itemView.findViewById(R.id.id_num);
        }
    }
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.check_image,parent,false);
        mcontext = parent.getContext();
        final RecyclerView.ViewHolder vh = new ViewHolder(v);
        return (ViewHolder) vh;
    }

    public ImageAdapter(List<Item_Image> data){
        mDataSet = data;
    }

    @Override
    public void onBindViewHolder(final ImageAdapter.ViewHolder holder, int position) {
        Item_Image item_image = mDataSet.get(position);
        String url = item_image.getItem_image();
        if(url!=null){
//          holder.item_text.setImageBitmap(DealBitmap.centerSquareScaleBitmap(url));
            Glide.with(mcontext)
                    .load(url)
                    .asBitmap()  //不可加载动图
                    .dontAnimate()//取消淡入淡出动画
                    .thumbnail(0.1f) //先加载十分之一作为缩略图
                    .into(holder.item_text);
        }
        //判断是否设置了监听器
        if(mOnItemClickListener != null){
            //为ItemView设置监听器
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition(); // 1
                    mOnItemClickListener.onItemClick(holder.itemView,position); // 2
                }
            });
        }
        if(mOnItemLongClickListener != null){
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = holder.getLayoutPosition();
                    mOnItemLongClickListener.onItemLongClick(holder.itemView,position);
                    //返回true 表示消耗了事件 事件不会继续传递
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener{
        void onItemLongClick(View view, int position);
    }
    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener){
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener mOnItemLongClickListener) {
        this.mOnItemLongClickListener = mOnItemLongClickListener;
    }

    public class Item_Image {
        private String item_image;

        public String getItem_image() {
            return item_image;
        }

        public void setItem_image(String item_image) {
            this.item_image = item_image;
        }

        public Item_Image(String item_image){
            this.item_image = item_image;
        }
    }
}
