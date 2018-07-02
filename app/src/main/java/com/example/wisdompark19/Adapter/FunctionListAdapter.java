package com.example.wisdompark19.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wisdompark19.R;

import java.util.List;

/**
 * Created by 最美人间四月天 on 2018/1/15.
 */

public class FunctionListAdapter  extends RecyclerView.Adapter<FunctionListAdapter.ViewHolder>{

    private List<Function_item> mDataSet;
    private OnItemClickListener mOnItemClickListener;
    private Context mContext;

    class ViewHolder extends RecyclerView.ViewHolder{

        ImageView function_item_image;
        TextView function_item_name;

        public ViewHolder(View itemView) {
            super(itemView);
            function_item_image = (ImageView)itemView.findViewById(R.id.function_list_card_image);
            function_item_name = (TextView)itemView.findViewById(R.id.function_list_card_name);
        }
    }

    //构造器，接受数据集
    public FunctionListAdapter(List<Function_item> data){
        mDataSet = data;
    }

    //
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.function_list_item,parent,false);
        mContext = parent.getContext();
        final RecyclerView.ViewHolder vh = new ViewHolder(v);
        return (ViewHolder)vh;
    }

    @Override
    public void onBindViewHolder(final FunctionListAdapter.ViewHolder holder, int position) {

        Function_item mFunction_item = mDataSet.get(position);
        String compare_name = mFunction_item.getFunction_item_name();
        switch (compare_name){    //设置图片和名称
            case "0":
            {
                holder.function_item_image.setImageResource(R.mipmap.ic_main_pay);
                holder.function_item_name.setText("生活缴费");
            }
            break;
            case "1":
            {
                holder.function_item_image.setImageResource(R.mipmap.ic_main_map);
                holder.function_item_name.setText("我的位置");
            }
            break;
            case "2":
            {
                holder.function_item_image.setImageResource(R.mipmap.ic_main_cart);
                holder.function_item_name.setText("电商平台");
            }
            break;
            case "3":
            {
                holder.function_item_image.setImageResource(R.mipmap.ic_main_waishe);
                holder.function_item_name.setText("外设接口");
            }
            break;
            case "4":
            {
                holder.function_item_image.setImageResource(R.mipmap.ic_main_repair);
                holder.function_item_name.setText("报修管理");
            }
            break;
            case "5":
            {
                holder.function_item_image.setImageResource(R.mipmap.ic_main_code);
                holder.function_item_name.setText("通行二维码");
            }
            break;
            case "6":
            {
                holder.function_item_image.setImageResource(R.mipmap.ic_main_more);
                holder.function_item_name.setText("更多");
            }
            break;
            case "7":
            {
//                holder.function_item_image.setImageResource(Integer.parseInt(null));
//                holder.function_item_name.setText("");
            }
            break;
            case "8":
            {
//                holder.function_item_image.setImageResource(Integer.parseInt(null));
//                holder.function_item_name.setText("");
            }
            break;
        }

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

    public class Function_item{

//        private String function_item_imageid; //图片
        private String function_item_name;   //描述

        public Function_item(String function_item_name){
//            this.function_item_imageid = function_item_imageid;
            this.function_item_name = function_item_name;
        }

//        public String getFunction_item_imageid() {
//            return function_item_imageid;
//        }
//        public void setFunction_item_imageid(String function_item_imageid) {
//            this.function_item_imageid = function_item_imageid;
//        }
        public String getFunction_item_name() {
            return function_item_name;
        }
        public void setFunction_item_name(String function_item_name) {
            this.function_item_name = function_item_name;
        }

    }
}
