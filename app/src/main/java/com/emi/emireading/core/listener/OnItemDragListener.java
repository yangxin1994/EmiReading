package com.emi.emireading.core.listener;

import android.support.v7.widget.RecyclerView;

/**
 * @author :zhoujian
 * @description : 拖拽效果监听
 * @company :翼迈科技
 * @date: 2017年11月01日下午 03:08
 * @Email: 971613168@qq.com
 */
public interface OnItemDragListener {
    void onItemDragStart(RecyclerView.ViewHolder viewHolder, int pos);

    void onItemDragMoving(RecyclerView.ViewHolder source, int from, RecyclerView.ViewHolder target, int to);

    void onItemDragEnd(RecyclerView.ViewHolder viewHolder, int pos);

}
