


package com.emi.emireading.widget.view.emimenu;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author :zhoujian
 * @description : 菜单基类适配器
 * @company :翼迈科技
 * @date 2018年07月4日下午 01:56
 * @Email: 971613168@qq.com
 */
public class MenuBaseAdapter<T> extends BaseAdapter implements IMenuItem<T> {

    private List<T> itemList;
    private ListView listView;

    private int selectedPosition = -1;

    public MenuBaseAdapter() {
        super();
        this.itemList = new ArrayList<>();
    }

    public MenuBaseAdapter(ListView listView) {
        super();
        this.itemList = new ArrayList<>();
        this.listView = listView;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int index) {
        return itemList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(final int index, View view, ViewGroup viewGroup) {
        if(view != null && listView != null && listView.getOnItemClickListener() != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listView.getOnItemClickListener().onItemClick(listView, view, index + listView.getHeaderViewsCount(), getItemId(index));
                }
            });
        }
        return view;
    }

    @Override
    public void addItem(T item) {
        this.itemList.add(item);
        notifyDataSetChanged();
    }

    @Override
    public void addItem(int position, T item) {
        this.itemList.add(position, item);
        notifyDataSetChanged();
    }

    @Override
    public void addItemList(List<T> itemList) {
        this.itemList.addAll(itemList);
        notifyDataSetChanged();
    }

    @Override
    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
    }

    @Override
    public int getSelectedPosition() {
        return this.selectedPosition;
    }

    @Override
    public void setListView(ListView listView) {
        this.listView = listView;
    }

    @Override
    public ListView getListView() {
        return this.listView;
    }

    @Override
    public void removeItem(T item) {
        this.itemList.remove(item);
    }

    @Override
    public void removeItem(int position) {
        this.itemList.remove(position);
    }

    @Override
    public void clearItems() {
        this.itemList.clear();
        notifyDataSetChanged();
    }

    @Override
    public List<T> getItemList() {
        return itemList;
    }

    @Override
    public int getContentViewHeight() {
        int totalHeight = 0;

        for(int i=0; i<getCount(); i++) {
            View view = getView(i, null, getListView());
            view.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += view.getMeasuredHeight();
        }

        totalHeight += (getListView().getDividerHeight() * (getCount() - 1));

        ViewGroup.LayoutParams params = getListView().getLayoutParams();
        params.height = totalHeight;
        getListView().setLayoutParams(params);

        return totalHeight;
    }
}