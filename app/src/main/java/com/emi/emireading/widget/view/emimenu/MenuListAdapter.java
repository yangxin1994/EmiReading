


package com.emi.emireading.widget.view.emimenu;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.emi.emireading.R;

/**
 * @author :zhoujian
 * @description : 菜单列表适配器
 * @company :翼迈科技
 * @date 2018年07月4日下午 01:47
 * @Email: 971613168@qq.com
 */
public class MenuListAdapter extends MenuBaseAdapter<EmiMenuItem> {

    private int textColor = -2;
    private int menuColor = -2;
    private int selectedTextColor = -2;
    private int selectedMenuColor = -2;

    private boolean selectedEffect = true;

    public MenuListAdapter(ListView listView) {
        super(listView);
    }

    @Override
    public View getView(final int index, View view, ViewGroup viewGroup) {
        final Context context = viewGroup.getContext();

        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_power_menu, viewGroup, false);
        }

        EmiMenuItem emiMenuItem = (EmiMenuItem) getItem(index);

        final View background = view.findViewById(R.id.item_power_menu_layout);
        final TextView title = view.findViewById(R.id.item_power_menu_title);
        title.setText(emiMenuItem.title);

        if(emiMenuItem.isSelected) {

            setSelectedPosition(index);

            if(selectedMenuColor == -2)
                background.setBackgroundColor(context.getResources().getColor(R.color.menu_background));
            else
                background.setBackgroundColor(selectedMenuColor);

            if(selectedTextColor == -2)
                title.setTextColor(context.getResources().getColor(R.color.menu_text_selected));
            else
                title.setTextColor(selectedTextColor);
        } else {
            if(menuColor == -2)
                background.setBackgroundColor(Color.WHITE);
            else
                background.setBackgroundColor(menuColor);

            if(textColor == -2)
                title.setTextColor(context.getResources().getColor(R.color.menu_text_no_selected));
            else
                title.setTextColor(textColor);
        }
        return super.getView(index, view, viewGroup);
    }

    @Override
    public void setSelectedPosition(int position) {
        super.setSelectedPosition(position);

        if(selectedEffect) {
            for (int i = 0; i < getItemList().size(); i++) {
                EmiMenuItem item = (EmiMenuItem) getItem(i);

                item.setIsSelected(false);
                if (i == position) {
                    item.setIsSelected(true);
                }
            }
            notifyDataSetChanged();
        }
    }

    public void setTextColor(int color) {
        this.textColor = color;
    }

    public void setMenuColor(int color) {
        this.menuColor = color;
    }

    public void setSelectedTextColor(int color) {
        this.selectedTextColor = color;
    }

    public void setSelectedMenuColor(int color) {
        this.selectedMenuColor = color;
    }

    public void setSelectedEffect(boolean selectedEffect) {
        this.selectedEffect = selectedEffect;
    }
}
