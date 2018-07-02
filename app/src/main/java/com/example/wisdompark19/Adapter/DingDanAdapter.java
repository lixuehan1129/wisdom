package com.example.wisdompark19.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wisdompark19.R;

import java.util.List;

/**
 * Created by ROBOSOFT on 2018/5/18.
 */

public class DingDanAdapter extends RecyclerView.Adapter<DingDanAdapter.ViewHolder>{
    private List<Ding_Dan> mDataSet;
    private OnItemClickListener mOnItemClickListener;
    private OnLongItemClickListener mOnLongItemClickListener;
    private Context mContext;

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView dingdan_name;
        TextView dingdan_add;
        TextView dingdan_n;
        TextView dingdan_num;
        TextView dingdan_pro;
        TextView dingdan_time;

        public ViewHolder(View itemView) {
            super(itemView);
            dingdan_name = (TextView) itemView.findViewById(R.id.dingdan_name);
            dingdan_add = (TextView) itemView.findViewById(R.id.dingdan_add);
            dingdan_n = (TextView) itemView.findViewById(R.id.dingdan_n);
            dingdan_num = (TextView) itemView.findViewById(R.id.dingdan_num);
            dingdan_pro = (TextView) itemView.findViewById(R.id.dingdan_pro);
            dingdan_time = (TextView) itemView.findViewById(R.id.dingdan_time);
        }
    }

    //构造器，接受数据集
    public DingDanAdapter(List<Ding_Dan> data){
        mDataSet = data;
    }

    //
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.dingdan_item,parent,false);
        mContext = parent.getContext();
        final RecyclerView.ViewHolder vh = new ViewHolder(v);
        return (ViewHolder)vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        Ding_Dan ding_dan = mDataSet.get(position);
        holder.dingdan_name.setText(ding_dan.getDingdan_name());
        holder.dingdan_add.setText(ding_dan.getDingdan_add());
        holder.dingdan_n.setText(ding_dan.getDingdan_n());
        holder.dingdan_num.setText(String.valueOf(ding_dan.getDingdan_num()));
        if(ding_dan.getDingdan_pro() == 0){
            holder.dingdan_pro.setText("已下单");
        }else if(ding_dan.getDingdan_pro() == 1){
            holder.dingdan_pro.setText("已处理");
        }else if(ding_dan.getDingdan_pro() == 2){
            holder.dingdan_pro.setText("已完成");
        }
        holder.dingdan_time.setText(ding_dan.getDingdan_time());

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

        if(mOnLongItemClickListener !=null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = holder.getLayoutPosition();
                    mOnLongItemClickListener.onLongItemClick(holder.itemView, position);
                    return false;
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

    public interface OnLongItemClickListener{
        void onLongItemClick(View view, int position);
    }
    public void setmOnLongItemClickListener(OnLongItemClickListener mOnLongItemClickListener){
        this.mOnLongItemClickListener = mOnLongItemClickListener;
    }

    public class Ding_Dan{
        private  String dingdan_name;
        private  String dingdan_add;
        private  String dingdan_n;
        private  int dingdan_num;
        private  int dingdan_pro;
        private  String dingdan_time;
        private  int dingdan_id;

        public int getDingdan_id() {
            return dingdan_id;
        }

        public void setDingdan_id(int dingdan_id) {
            this.dingdan_id = dingdan_id;
        }

        public String getDingdan_name() {
            return dingdan_name;
        }

        public void setDingdan_name(String dingdan_name) {
            this.dingdan_name = dingdan_name;
        }

        public String getDingdan_add() {
            return dingdan_add;
        }

        public void setDingdan_add(String dingdan_add) {
            this.dingdan_add = dingdan_add;
        }

        public String getDingdan_n() {
            return dingdan_n;
        }

        public void setDingdan_n(String dingdan_na) {
            this.dingdan_n = dingdan_na;
        }

        public int getDingdan_num() {
            return dingdan_num;
        }

        public void setDingdan_num(int dingdan_num) {
            this.dingdan_num = dingdan_num;
        }

        public int getDingdan_pro() {
            return dingdan_pro;
        }

        public void setDingdan_pro(int dingdan_pro) {
            this.dingdan_pro = dingdan_pro;
        }

        public String getDingdan_time() {
            return dingdan_time;
        }

        public void setDingdan_time(String dingdan_time) {
            this.dingdan_time = dingdan_time;
        }

        public Ding_Dan(String dingdan_name, String dingdan_add, String dingdan_n,
                        int dingdan_num, int dingdan_pro, String dingdan_time,
                        int dingdan_id){
            this.dingdan_name = dingdan_name;
            this.dingdan_add = dingdan_add;
            this.dingdan_n = dingdan_n;
            this.dingdan_num = dingdan_num;
            this.dingdan_pro = dingdan_pro;
            this.dingdan_time = dingdan_time;
            this.dingdan_id = dingdan_id;
        }
    }
}
