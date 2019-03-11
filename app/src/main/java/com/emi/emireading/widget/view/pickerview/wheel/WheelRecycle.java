package com.emi.emireading.widget.view.pickerview.wheel;

import android.view.View;
import android.widget.LinearLayout;

import java.util.LinkedList;
import java.util.List;

/**
 * @author :zhoujian
 * @description :滚轮回收复用
 * @company :翼迈科技
 * @date: 2018年07月06日下午 02:19
 * @Email: 971613168@qq.com
 */
public class WheelRecycle {
    private List<View> items;

    private List<View> emptyItems;

    private WheelView wheel;


    public WheelRecycle(WheelView wheel) {
        this.wheel = wheel;
    }

    public int recycleItems(LinearLayout layout, int firstItem, ItemsRange range, int currentItem) {
        int index = firstItem;
        for (int i = 0; i < layout.getChildCount(); ) {
            if (!range.contains(index)) {
                recycleView(layout.getChildAt(i), index, currentItem);
                layout.removeViewAt(i);
                if (i == 0) {
                    // first item
                    firstItem++;
                }
            } else {
                // go to next item
                i++;
            }
            index++;
        }
        return firstItem;
    }

    public View getItem() {
        return getCachedView(items);
    }

    public View getEmptyItem() {
        return getCachedView(emptyItems);
    }

    public void clearAll() {
        if (items != null) {
            items.clear();
        }
        if (emptyItems != null) {
            emptyItems.clear();
        }
    }

    private List<View> addView(View view, List<View> cache) {
        if (cache == null) {
            cache = new LinkedList<View>();
        }

        cache.add(view);
        return cache;
    }

    private void recycleView(View view, int index, int current) {
        int count = wheel.getViewAdapter().getItemsCount();
        if ((index < 0 || index >= count) && !wheel.isCyclic()) {
            emptyItems = addView(view, emptyItems);
        } else {
            while (index < 0) {
                index = count + index;
            }
            index %= count;
            items = addView(view, items);
        }
    }

    private View getCachedView(List<View> cache) {
        if (cache != null && cache.size() > 0) {
            View view = cache.get(0);
            cache.remove(0);
            return view;
        }
        return null;
    }

}
