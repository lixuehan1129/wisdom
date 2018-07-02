package com.example.wisdompark19.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wisdompark19.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by 最美人间四月天 on 2018/1/25.
 */

public class NoticeItemAdapter extends RecyclerView.Adapter<NoticeItemAdapter.ViewHolder> {

    private List<Notice_item> mDataSet;
    private OnItemClickListener mOnItemClickListener;
    private Context mContext;

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView card_message_tell;
        TextView card_message_content;
        TextView card_message_time;
        CircleImageView card_message_image;
        ImageView card_message_xin;

        public ViewHolder(View itemView) {
            super(itemView);
            card_message_tell = (TextView)itemView.findViewById(R.id.card_message_tell);
            card_message_content = (TextView)itemView.findViewById(R.id.card_message_content);
            card_message_time = (TextView)itemView.findViewById(R.id.card_message_time);
            card_message_image = (CircleImageView)itemView.findViewById(R.id.card_message_image);
            card_message_xin = (ImageView)itemView.findViewById(R.id.card_message_xin);
        }
    }

    //构造器，接受数据集
    public NoticeItemAdapter(List<Notice_item> data){
        mDataSet = data;
    }

    //
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notice_item,parent,false);
        mContext = parent.getContext();
        final RecyclerView.ViewHolder vh = new ViewHolder(v);
        return (ViewHolder)vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        Notice_item mNotice_item = mDataSet.get(position);
        String card_message_tell = mNotice_item.getCard_message_tell();
        String card_message_content = mNotice_item.getCard_message_content();
        String card_message_time = mNotice_item.getCard_message_time();
        Bitmap url = mNotice_item.getCard_message_image();
        int card_message_x = mNotice_item.getCard_message_xin();
        if(url!=null){
//            Glide.with(mContext)
//                    .load(url)
//                    .placeholder(R.mipmap.ic_launcher_round)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .override(100,100)
//                    .into(holder.card_message_image);
            holder.card_message_image.setImageBitmap(url);
        }else {
            holder.card_message_image.setImageResource(R.mipmap.ic_launcher_round);
        }

        if(card_message_x == 1){
            holder.card_message_xin.setVisibility(View.INVISIBLE);
        }else {
            holder.card_message_xin.setVisibility(View.VISIBLE);
        }
        holder.card_message_tell.setText(card_message_tell);
        holder.card_message_content.setText(card_message_content);
        holder.card_message_time.setText(card_message_time);

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

    public class Notice_item{
        private String card_message_tell;
        private String card_message_content;
        private String card_message_time;
        private Bitmap card_message_image;
        private int card_message_id;
        private int card_message_xin;

        public Notice_item(String card_message_tell, String card_message_content, String card_message_time,
                           Bitmap card_message_image, int card_message_id, int card_message_xin){
            this.card_message_tell = card_message_tell;
            this.card_message_content = card_message_content;
            this.card_message_time = card_message_time;
            this.card_message_image = card_message_image;
            this.card_message_id = card_message_id;
            this.card_message_xin = card_message_xin;
        }
        public int getCard_message_id() {
            return card_message_id;
        }

        public void setCard_message_id(int card_message_id) {
            this.card_message_id = card_message_id;
        }
        public Bitmap getCard_message_image() {
            return card_message_image;
        }

        public void setCard_message_image(Bitmap card_message_image) {
            this.card_message_image = card_message_image;
        }
        public String getCard_message_tell() {
            return card_message_tell;
        }

        public void setCard_message_tell(String card_message_tell) {
            this.card_message_tell = card_message_tell;
        }

        public String getCard_message_content() {
            return card_message_content;
        }

        public void setCard_message_content(String card_message_content) {
            this.card_message_content = card_message_content;
        }

        public String getCard_message_time() {
            return card_message_time;
        }

        public void setCard_message_time(String card_message_time) {
            this.card_message_time = card_message_time;
        }

        public int getCard_message_xin() {
            return card_message_xin;
        }

        public void setCard_message_xin(int card_message_xin) {
            this.card_message_xin = card_message_xin;
        }

    }
}
