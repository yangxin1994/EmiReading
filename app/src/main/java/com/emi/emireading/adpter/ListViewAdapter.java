package com.emi.emireading.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.emi.emireading.R;
import com.emi.emireading.core.utils.CommonViewHolder;

import java.util.List;

/**
 * @author :zhoujian
 * @description : ListViewAdapter
 * @company :翼迈科技
 * @date 2018年03月08日上午 09:14
 * @Email: 971613168@qq.com
 */

public class ListViewAdapter extends BaseAdapter {
    private LayoutInflater inflater;

    private List<String> list;

    public ListViewAdapter(Context context, List<String> list) {
        super();
        this.inflater = LayoutInflater.from(context);
        this.list = list;
    }


    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_item_area, null);
        }
        TextView tv_name = CommonViewHolder.get(convertView, R.id.tvDirName);
        tv_name.setText(list.get(position));
        return convertView;
    }
}

