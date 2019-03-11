package com.emi.emireading.core.utils;

import android.util.SparseArray;
import android.view.View;

/**
 * @author :chx
 * 描述:万能ViewHolder
 * Created by chx on 2017/3/29.
 * 邮箱:snake_chenhx@163.com
 */

public class CommonViewHolder {
    /**
     * @param view 所有缓存View的根View
     * @param id   缓存View的唯一标识
     * @return
     */
    public static <T extends View> T get(View view, int id) {

        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        //如果根view没有用来缓存View的集合
        if (viewHolder == null) {
            viewHolder = new SparseArray<View>();
            //创建集合和根View关联
            view.setTag(viewHolder);
        }
        //获取根View储存在集合中的孩纸
        View chidlView = viewHolder.get(id);
        //如果没有改孩纸
        if (chidlView == null) {
            //找到该孩纸
            chidlView = view.findViewById(id);
            //保存到集合
            viewHolder.put(id, chidlView);
        }
        return (T) chidlView;
    }
}
