package com.emi.emireading.core.listener;

import android.view.View;

import com.emi.emireading.core.adapter.BaseEmiAdapter;



public abstract class OnItemChildLongClickListener extends BaseClickListener {


    @Override
    public void onItemClick(BaseEmiAdapter adapter, View view, int position) {

    }

    @Override
    public void onItemLongClick(BaseEmiAdapter adapter, View view, int position) {

    }

    @Override
    public void onItemChildClick(BaseEmiAdapter adapter, View view, int position) {

    }

    @Override
    public void onItemChildLongClick(BaseEmiAdapter adapter, View view, int position) {
        onSimpleItemChildLongClick(adapter,view,position);
    }
    public abstract void onSimpleItemChildLongClick(BaseEmiAdapter adapter, View view, int position);
}
