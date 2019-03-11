

package com.emi.emireading.widget.view.emimenu;

import android.widget.ListView;

import java.util.List;

/**
 * @author :zhoujian
 * @description : IMenuItem
 * @company :翼迈科技
 * @date 2018年07月4日下午 01:42
 * @Email: 971613168@qq.com
 */
public interface IMenuItem<T> {
    void addItem(T item);
    void addItem(int position, T item);
    void addItemList(List<T> itemList);

    void setListView(ListView listView);
    ListView getListView();

    void setSelectedPosition(int position);
    int getSelectedPosition();

    void removeItem(T item);
    void removeItem(int position);

    void clearItems();

    List<T> getItemList();

    int getContentViewHeight();
}
