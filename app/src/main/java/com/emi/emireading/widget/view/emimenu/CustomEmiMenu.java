


package com.emi.emireading.widget.view.emimenu;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author :zhoujian
 * @description : CustomEmiMenu
 * @company :翼迈科技
 * @date 2018年07月4日下午 01:47
 * @Email: 971613168@qq.com
 */
@SuppressWarnings({"WeakerAccess", "unchecked", "unused"})
public class CustomEmiMenu<T, E extends MenuBaseAdapter<T>> extends AbstractEmiMenu<T, E> implements IMenuItem<T> {

    protected CustomEmiMenu(Context context, AbstractMenuBuilder abstractMenuBuilder) {
        super(context, abstractMenuBuilder);
        Builder<T, E> builder = (Builder) abstractMenuBuilder;
        if (builder.menuItemClickListener != null) {
            setOnMenuItemClickListener(builder.menuItemClickListener);
        }
        if (builder.selected != -1) {
            setSelectedPosition(builder.selected);
        }
        this.adapter = builder.adapter;
        this.adapter.setListView(getMenuListView());
        this.menuListView.setAdapter(adapter);
        addItemList(builder.Ts);
    }


    public void setAdapter() {
        this.menuListView.setAdapter(adapter);
    }

    @Override
    protected void initialize(Context context) {
        super.initialize(context);
        this.adapter = (E) (new MenuBaseAdapter<>(menuListView));
    }

    @Override
    public void setListView(ListView listView) {
        getAdapter().setListView(getMenuListView());
    }

    @Override
    public ListView getListView() {
        return getAdapter().getListView();
    }

    @Override
    public void setSelectedPosition(int position) {
        getAdapter().setSelectedPosition(position);
    }

    @Override
    public int getSelectedPosition() {
        return getAdapter().getSelectedPosition();
    }

    @Override
    public void addItem(T item) {
        getAdapter().addItem(item);
    }

    @Override
    public void addItem(int position, T item) {
        getAdapter().addItem(position, item);
    }

    @Override
    public void addItemList(List<T> itemList) {
        getAdapter().addItemList(itemList);
    }

    @Override
    public void removeItem(T item) {
        getAdapter().removeItem(item);
    }

    @Override
    public void removeItem(int position) {
        getAdapter().removeItem(position);
    }

    @Override
    public void clearItems() {
        getAdapter().clearItems();
    }

    @Override
    public List<T> getItemList() {
        return getAdapter().getItemList();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        dismiss();
    }

    @SuppressWarnings("unchecked")
    public static class Builder<T, E extends MenuBaseAdapter<T>> extends AbstractMenuBuilder {

        private OnMenuItemClickListener<T> menuItemClickListener = null;

        private E adapter;
        private List<T> Ts;

        public Builder(Context context, E adapter) {
            this.context = context;
            this.Ts = new ArrayList<>();
            this.adapter = adapter;
            this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public Builder setLifecycleOwner(LifecycleOwner lifecycleOwner) {
            this.lifecycleOwner = lifecycleOwner;
            return this;
        }

        public Builder setShowBackground(boolean show) {
            this.showBackground = show;
            return this;
        }

        public Builder setOnMenuItemClickListener(Object menuItemClickListener) {
            this.menuItemClickListener = (OnMenuItemClickListener<T>) menuItemClickListener;
            return this;
        }

        public Builder setOnBackgroundClickListener(View.OnClickListener onBackgroundClickListener) {
            this.backgroundClickListener = onBackgroundClickListener;
            return this;
        }

        public Builder setOnDismissListener(OnDismissedListener onDismissListener) {
            this.onDismissedListener = onDismissListener;
            return this;
        }

        public Builder setHeaderView(int headerView) {
            this.headerView = layoutInflater.inflate(headerView, null);
            return this;
        }

        public Builder setHeaderView(View headerView) {
            this.headerView = headerView;
            return this;
        }

        public Builder setFooterView(int footerView) {
            this.footerView = layoutInflater.inflate(footerView, null);
            return this;
        }

        public Builder setFooterView(View footerView) {
            this.footerView = footerView;
            return this;
        }

        public Builder setAnimation(MenuAnimation menuAnimation) {
            this.menuAnimation = menuAnimation;
            return this;
        }

        public Builder setAnimationStyle(int style) {
            this.animationStyle = style;
            return this;
        }

        public Builder setMenuRadius(float radius) {
            this.menuRadius = radius;
            return this;
        }

        public Builder setMenuShadow(float shadow) {
            this.menuShadow = shadow;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setDividerHeight(int height) {
            this.dividerHeight = height;
            return this;
        }

        public Builder setDivider(Drawable divider) {
            this.divider = divider;
            return this;
        }

        public Builder setBackgroundColor(int color) {
            this.backgroundColor = color;
            return this;
        }

        public Builder setBackgroundAlpha(float alpha) {
            this.backgroundAlpha = alpha;
            return this;
        }

        public Builder setFocusable(boolean focusable) {
            this.focusable = focusable;
            return this;
        }

        public Builder setSelected(int position) {
            this.selected = position;
            return this;
        }

        public Builder setIsClipping(boolean isClipping) {
            this.isClipping = isClipping;
            return this;
        }

        public Builder addItem(Object item) {
            this.Ts.add((T) item);
            return this;
        }

        public Builder addItem(int position, Object item) {
            this.Ts.add(position, (T) item);
            return this;
        }

        public Builder addItemList(List<T> itemList) {
            this.Ts.addAll(itemList);
            return this;
        }

        public CustomEmiMenu build() {
            return new CustomEmiMenu<>(context, this);
        }
    }

    /**
     * 当更换item需要刷新显示时调用该方法
     */
    public void refresh() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}