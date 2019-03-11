package com.emi.emireading.core.listener;

import android.view.View;

import com.emi.emireading.core.adapter.BaseEmiAdapter;


public abstract class OnItemLongClickListener extends BaseClickListener {



    @Override
    public void onItemClick(BaseEmiAdapter adapter, View view, int position) {

    }

    @Override
    public void onItemLongClick(BaseEmiAdapter adapter, View view, int position) {
        onSimpleItemLongClick( adapter,  view,  position);
    }

    @Override
    public void onItemChildClick(BaseEmiAdapter adapter, View view, int position) {

    }

    @Override
    public void onItemChildLongClick(BaseEmiAdapter adapter, View view, int position) {
    }

    /**
     * 简单的点击事件监听
     * @param adapter
     * @param view
     * @param position
     */
    public abstract void onSimpleItemLongClick(BaseEmiAdapter adapter, View view, int position);
}
