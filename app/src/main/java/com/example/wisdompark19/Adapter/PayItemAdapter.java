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
 * Created by 最美人间四月天 on 2018/1/26.
 */

public class PayItemAdapter extends RecyclerView.Adapter<PayItemAdapter.ViewHolder> {

    private List<Pay_item> mDataSet;
    private OnItemClickListener mOnItemClickListener;
    private Context mContext;

    //新添加数据后未修改内容2018.3.8
    class ViewHolder extends RecyclerView.ViewHolder{
        TextView count_name;
        TextView count_fee;
        TextView count_time;
        TextView count_pay;

        public ViewHolder(View itemView) {
            super(itemView);
             count_name = (TextView)itemView.findViewById(R.id.count_name);
             count_fee = (TextView)itemView.findViewById(R.id.count_fee);
             count_time = (TextView)itemView.findViewById(R.id.count_time);
             count_pay = (TextView)itemView.findViewById(R.id.count_pay);
        }
    }

    //构造器，接受数据集
    public PayItemAdapter(List<Pay_item> data){
        mDataSet = data;
    }

    //
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pay_item,parent,false);
        mContext = parent.getContext();
        final RecyclerView.ViewHolder vh = new ViewHolder(v);
        return (ViewHolder)vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Pay_item mPay_item = mDataSet.get(position);
        String count_name = mPay_item.getCount_name();
        String count_fee = mPay_item.getCount_fee();
        String count_time = mPay_item.getCount_time();
        String count_pay = mPay_item.getCount_pay();
        holder.count_name.setText(count_name);
        holder.count_fee.setText(count_fee);
        holder.count_time.setText(count_time);
        holder.count_pay.setText(count_pay);

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

    public class Pay_item{
        private String count_name;
        private String count_fee;
        private String count_time;
        private String count_pay;

        public Pay_item(String count_name, String count_fee, String count_time, String count_pay){
            this.count_name = count_name;
            this.count_fee = count_fee;
            this.count_time = count_time;
            this.count_pay = count_pay;
        }
        public String getCount_name() {
            return count_name;
        }

        public void setCount_name(String count_name) {
            this.count_name = count_name;
        }

        public String getCount_fee() {
            return count_fee;
        }

        public void setCount_fee(String count_fee) {
            this.count_fee = count_fee;
        }

        public String getCount_time() {
            return count_time;
        }

        public void setCount_time(String count_time) {
            this.count_time = count_time;
        }

        public String getCount_pay() {
            return count_pay;
        }

        public void setCount_pay(String count_pay) {
            this.count_pay = count_pay;
        }
    }
}
