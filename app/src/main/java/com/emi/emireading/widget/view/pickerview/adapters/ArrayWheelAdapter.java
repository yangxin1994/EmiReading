
package com.emi.emireading.widget.view.pickerview.adapters;

import android.content.Context;


/**
 * @author :zhoujian
 * @description :数组滚轮适配器
 * @company :翼迈科技
 * @date: 2018年07月06日下午 02:17
 * @Email: 971613168@qq.com
 */
public class ArrayWheelAdapter<T> extends AbstractWheelTextAdapter {

    private T[] items;

    public ArrayWheelAdapter(Context context, T items[]) {
        super(context);

        //setEmptyItemResource(TEXT_VIEW_ITEM_RESOURCE);
        this.items = items;
    }

    @Override
    public CharSequence getItemText(int index) {
        if (index >= 0 && index < items.length) {
            T item = items[index];
            if (item instanceof CharSequence) {
                return (CharSequence) item;
            }
            return item.toString();
        }
        return null;
    }

    @Override
    public int getItemsCount() {
        return items.length;
    }
}
