package com.emi.emireading.widget.view.emimenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.emi.emireading.R;

/**
 * @author :zhoujian
 * @description : IconMenu适配器
 * @company :翼迈科技
 * @date 2018年03月09日下午 02:04
 * @Email: 971613168@qq.com
 */
public class IconMenuAdapter extends MenuBaseAdapter<IconPowerMenuItem> {

    public IconMenuAdapter() {
        super();
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        final Context context = viewGroup.getContext();

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                view = inflater.inflate(R.layout.item_icon_menu, viewGroup, false);
            }
        }

        IconPowerMenuItem item = (IconPowerMenuItem) getItem(index);
        final ImageView icon = view.findViewById(R.id.item_icon);
        icon.setImageDrawable(item.getIcon());
        final TextView title = view.findViewById(R.id.item_title);
        title.setText(item.getTitle());
        return super.getView(index, view, viewGroup);
    }
}
