package com.emi.emireading.widget.view.emimenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.emi.emireading.R;

/**
 * @author :zhoujian
 * @description :
 * @company :翼迈科技
 * @date 2018年07月5日下午 01:47
 * @Email: 971613168@qq.com
 */

public class CenterMenuAdapter extends MenuBaseAdapter<String> {

    public CenterMenuAdapter() {
        super();
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        final Context context = viewGroup.getContext();

        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_title_menu, viewGroup, false);
        }

        String item = (String) getItem(index);
        final TextView title = view.findViewById(R.id.item_title);
        title.setText(item);
        title.setTextColor(context.getResources().getColor(R.color.md_grey_800));
        return super.getView(index, view, viewGroup);
    }

    @Override
    public void setSelectedPosition(int position) {
        notifyDataSetChanged();
    }
}
