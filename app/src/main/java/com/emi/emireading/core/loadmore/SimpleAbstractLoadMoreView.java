package com.emi.emireading.core.loadmore;


import com.emi.emireading.R;


/**
 * @author :zhoujian
 * @description : 上拉刷新和下拉加载控件
 * @company :翼迈科技
 * @date: 2017年07月25日上午 11:53
 * @Email: 971613168@qq.com
 */
public final class SimpleAbstractLoadMoreView extends AbstractLoadMoreView {

    @Override
    public int getLayoutId() {
        return R.layout.view_load_more;
    }

    @Override
    protected int getLoadingViewId() {
        return R.id.load_more_loading_view;
    }

    @Override
    protected int getLoadFailViewId() {
        return R.id.load_more_load_fail_view;
    }

    @Override
    protected int getLoadEndViewId() {
        return R.id.load_more_load_end_view;
    }
}
