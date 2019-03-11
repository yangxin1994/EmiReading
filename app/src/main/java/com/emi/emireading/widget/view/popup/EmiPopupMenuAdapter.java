package com.emi.emireading.widget.view.popup;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.emi.emireading.R;

import java.util.List;

/**
 * @author :zhoujian
 * @description : popup弹窗适配器
 * @company :翼迈科技
 * @date 2018年04月25日下午 02:48
 * @Email: 971613168@qq.com
 */

public class EmiPopupMenuAdapter extends RecyclerView.Adapter<EmiPopupMenuAdapter.PopupViewHolder> {
    private Context mContext;
    private List<EmiPopupMenuItem> menuItemList;
    private boolean showIcon;
    private EmiPopupMenu mTopRightMenu;
    private EmiPopupMenu.OnMenuItemClickListener onMenuItemClickListener;

    public EmiPopupMenuAdapter(Context context, EmiPopupMenu topRightMenu, List<EmiPopupMenuItem> menuItemList, boolean show) {
        this.mContext = context;
        this.mTopRightMenu = topRightMenu;
        this.menuItemList = menuItemList;
        this.showIcon = show;
    }

    public void setData(List<EmiPopupMenuItem> data) {
        menuItemList = data;
        notifyDataSetChanged();
    }

    public void setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
        notifyDataSetChanged();
    }

    @Override
    public PopupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.emi_item_popup_menu_list, parent, false);
        return new PopupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PopupViewHolder holder, int position) {
        final EmiPopupMenuItem menuItem = menuItemList.get(position);
            if (showIcon) {
                holder.icon.setVisibility(View.VISIBLE);
                int resId = menuItem.getIcon();
                holder.icon.setImageResource(resId < 0 ? 0 : resId);
            } else {
                holder.icon.setVisibility(View.GONE);
            }
            holder.text.setText(menuItem.getText());

            if (position == 0) {
                holder.container.setBackground(addStateDrawable(mContext, -1, R.drawable.emi_popup_top_pressed));
            } else if (position == menuItemList.size() - 1) {
                holder.container.setBackground(addStateDrawable(mContext, -1, R.drawable.emi_popup_bottom_pressed));
            } else {
                holder.container.setBackground(addStateDrawable(mContext, -1, R.drawable.emi_popup_middle_pressed));
            }
            final int pos = holder.getAdapterPosition();
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onMenuItemClickListener != null) {
                        mTopRightMenu.dismiss();
                        onMenuItemClickListener.onMenuItemClick(pos);
                    }
                }
            });

    }

    private StateListDrawable addStateDrawable(Context context, int normalId, int pressedId) {
        StateListDrawable sd = new StateListDrawable();
        Drawable normal = normalId == -1 ? null : ContextCompat.getDrawable(context, normalId);
        Drawable pressed = pressedId == -1 ? null : ContextCompat.getDrawable(context, pressedId);
        sd.addState(new int[]{android.R.attr.state_pressed}, pressed);
        sd.addState(new int[]{}, normal);
        return sd;
    }

    @Override
    public int getItemCount() {
        return menuItemList == null ? 0 : menuItemList.size();
    }

    class PopupViewHolder extends RecyclerView.ViewHolder {
        ViewGroup container;
        ImageView icon;
        TextView text;

        PopupViewHolder(View itemView) {
            super(itemView);
            container = (ViewGroup) itemView;
            icon = itemView.findViewById(R.id.trm_menu_item_icon);
            text = itemView.findViewById(R.id.trm_menu_item_text);
        }
    }

    public void setOnMenuItemClickListener(EmiPopupMenu.OnMenuItemClickListener listener) {
        this.onMenuItemClickListener = listener;
    }
}
