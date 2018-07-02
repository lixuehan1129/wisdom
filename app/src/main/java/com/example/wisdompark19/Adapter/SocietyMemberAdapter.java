package com.example.wisdompark19.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wisdompark19.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by 最美人间四月天 on 2018/3/9.
 * 社区成员
 */

public class SocietyMemberAdapter extends RecyclerView.Adapter<SocietyMemberAdapter.ViewHolder>{

    private List<Item_member> mDataSet;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private Context mcontext;


    class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView item_member_img;
        TextView item_member_name;

        //public TextView mTextView;
        public ViewHolder(View itemView) {
            super(itemView);
            item_member_img = (CircleImageView) itemView.findViewById(R.id.member_item_img);
            item_member_name = (TextView) itemView.findViewById(R.id.member_item_name);
        }
    }

    public SocietyMemberAdapter(List<SocietyMemberAdapter.Item_member> data){
        mDataSet = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.society_member_item,parent,false);
        final RecyclerView.ViewHolder vh = new ViewHolder(v);
        mcontext = parent.getContext();
        return (ViewHolder) vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //holder.mTextView.setText(mDataSet.get(position));
        Item_member mItem_member = mDataSet.get(position);
        Bitmap url = mItem_member.getItem_member_img_url();
        if(url!=null){
//            Glide.with(mcontext)
//                    .load(url)
//                    .asBitmap()  //不可加载动图
//                    .placeholder(R.mipmap.ic_launcher_round)
//                    .dontAnimate()//取消淡入淡出动画
//                    .thumbnail(0.1f) //先加载十分之一作为缩略图
//                    .into(holder.item_member_img);
            holder.item_member_img.setImageBitmap(url);
        }else {
            holder.item_member_img.setImageResource(R.mipmap.ic_launcher_round);
        }
        holder.item_member_name.setText(mItem_member.getItem_member_name());

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


    public class Item_member{
        private String item_member_id;
        private String item_member_name;
        private Bitmap item_member_img_url;

        public Item_member(String item_member_name,Bitmap item_member_img_url,String item_member_id){
            this.item_member_name = item_member_name;
            this.item_member_img_url = item_member_img_url;
            this.item_member_id=item_member_id;
        }

        public String getItem_member_id(){return item_member_id;}
        public String getItem_member_name(){
            return item_member_name;
        }
        public Bitmap getItem_member_img_url(){
            return item_member_img_url;
        }


    }
}
