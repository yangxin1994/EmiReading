package com.emi.emireading.common;

import android.support.annotation.LayoutRes;
import android.util.SparseIntArray;

import java.util.List;

import static com.emi.emireading.core.adapter.BaseMultiItemQuickEmiAdapter.TYPE_NOT_FOUND;



public abstract class BaseMultiTypeDelegate<T> {

    private static final int DEFAULT_VIEW_TYPE = -0xff;
    private SparseIntArray layouts;
    private boolean autoMode, selfMode;

    public BaseMultiTypeDelegate(SparseIntArray layouts) {
        this.layouts = layouts;
    }

    public BaseMultiTypeDelegate() {
    }

    public final int getDefItemViewType(List<T> data, int position) {
        T item = data.get(position);
        return item != null ? getItemType(item) : DEFAULT_VIEW_TYPE;
    }

    protected abstract int getItemType(T t);

    public final int getLayoutId(int viewType) {
        return this.layouts.get(viewType,TYPE_NOT_FOUND);
    }

    private void addItemType(int type, @LayoutRes int layoutResId) {
        if (this.layouts == null) {
            this.layouts = new SparseIntArray();
        }
        this.layouts.put(type, layoutResId);
    }

    /**
     * auto increase type vale, start from 0.
     *
     * @param layoutResIds layout id arrays
     * @return BaseMultiTypeDelegate
     */
    public BaseMultiTypeDelegate registerItemTypeAutoIncrease(@LayoutRes int... layoutResIds) {
        autoMode = true;
        checkMode(selfMode);
        for (int i = 0; i < layoutResIds.length; i++) {
            addItemType(i, layoutResIds[i]);
        }
        return this;
    }

    /**
     * set your own type one by one.
     *
     * @param type        type value
     * @param layoutResId layout id
     * @return BaseMultiTypeDelegate
     */
    public BaseMultiTypeDelegate registerItemType(int type, @LayoutRes int layoutResId) {
        selfMode = true;
        checkMode(autoMode);
        addItemType(type, layoutResId);
        return this;
    }

    private void checkMode(boolean mode) {
        if (mode) {
            throw new RuntimeException("Don't mess two register mode");
        }
    }
}
