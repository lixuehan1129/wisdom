package com.example.wisdompark19.Adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.wisdompark19.R;

import java.util.List;

/**
 * Created by 最美人间四月天 on 2018/4/4.
 */

public class RegistAddAdapter extends BaseAdapter{
    private List<mSpinner> mSpinner;

    public RegistAddAdapter(Handler.Callback context, List<mSpinner> persons) {
        mSpinner = persons;
    }

    @Override
    public int getCount() {
        return mSpinner == null ? 0 : mSpinner.size();
    }

    @Override
    public Object getItem(int position) {
        return mSpinner.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.mine_regist_add_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tvName.setText(mSpinner.get(position).getName());
        return convertView;
    }

    static class ViewHolder {
        protected TextView tvName;

        ViewHolder(View rootView) {

            initView(rootView);
        }

        private void initView(View rootView) {
            tvName = (TextView) rootView.findViewById(R.id.mine_regist_add_item);
        }
    }
    //实体类
    public static class mSpinner {

        private String name;

        public mSpinner(String name) {
            super();
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }
}


