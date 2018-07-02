package com.example.wisdompark19.Adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wisdompark19.R;

import java.util.List;

/**
 * Created by ROBOSOFT on 2018/6/5.
 */

public class GuideAdapter extends RecyclerView.Adapter<GuideAdapter.ViewHolder>{

    private List<Guide_Item> mDataSet;
    private OnItemClickListener mOnItemClickListener;
    private OnLongItemClickListener mOnLongItemClickListener;

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView guide_que;
        TextView guide_ans;
        TextView guide_time;
        CardView guide_card;

        public ViewHolder(View itemView) {
            super(itemView);
            guide_que = (TextView) itemView.findViewById(R.id.guide_it_que);
            guide_ans = (TextView) itemView.findViewById(R.id.guide_it_ans);
            guide_time = (TextView) itemView.findViewById(R.id.guide_it_time);
            guide_card = (CardView) itemView.findViewById(R.id.guide_card_item);

        }
    }

    //构造器，接受数据集
    public GuideAdapter(List<Guide_Item> data){
        mDataSet = data;
    }

    //
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.guide_item,parent,false);
        final RecyclerView.ViewHolder vh = new ViewHolder(v);
        return (ViewHolder)vh;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        Guide_Item guide_item = mDataSet.get(position);
        holder.guide_que.setText(guide_item.getGuide_que());
        holder.guide_time.setText(guide_item.getGuide_time());
        String answer = guide_item.getGuide_ans();
        if(answer.equals("null")){
            holder.guide_ans.setText("待回答");
            holder.guide_card.setCardBackgroundColor(Color.parseColor("#CCFFFF"));
        }else {
            holder.guide_ans.setText(answer);
            holder.guide_card.setCardBackgroundColor(Color.parseColor("#FFCCCC"));
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

    public class Guide_Item{
        private  String guide_que;
        private  String guide_ans;
        private  String guide_time;
        private  int guide_id;

        public String getGuide_que() {
            return guide_que;
        }

        public void setGuide_que(String guide_que) {
            this.guide_que = guide_que;
        }

        public String getGuide_ans() {
            return guide_ans;
        }

        public void setGuide_ans(String guide_ans) {
            this.guide_ans = guide_ans;
        }

        public String getGuide_time() {
            return guide_time;
        }

        public void setGuide_time(String guide_time) {
            this.guide_time = guide_time;
        }

        public int getGuide_id() {
            return guide_id;
        }

        public void setGuide_id(int guide_id) {
            this.guide_id = guide_id;
        }

        public Guide_Item(String guide_que, String guide_ans, String guide_time,
                          int guide_id){
            this.guide_que = guide_que;
            this.guide_ans = guide_ans;
            this.guide_time = guide_time;
            this.guide_id = guide_id;
        }
    }
}
