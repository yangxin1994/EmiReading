package com.emi.emireading.core.listener;

import android.view.View;

import com.emi.emireading.core.adapter.BaseEmiAdapter;


public abstract   class OnItemClickListener extends BaseClickListener {
    @Override
    public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
        onSimpleItemClick(adapter,view,position);
    }

    @Override
    public void onItemLongClick(BaseEmiAdapter adapter, View view, int position) {

    }

    @Override
    public void onItemChildClick(BaseEmiAdapter adapter, View view, int position) {

    }

    @Override
    public void onItemChildLongClick(BaseEmiAdapter adapter, View view, int position) {

    }
    public abstract void onSimpleItemClick(BaseEmiAdapter adapter, View view, int position);
}
