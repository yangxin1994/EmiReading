package com.emi.emireading.widget.view.popup;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.emi.emireading.EmiReadingApplication;
import com.emi.emireading.R;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.DisplayUtil;
import com.emi.emireading.widget.view.EmiRecycleViewDivider;

import java.util.ArrayList;
import java.util.List;

/**
 * @author :zhoujian
 * @description : 仿微信弹出框
 * @company :翼迈科技
 * @date 2018年04月25日下午 02:48
 * @Email: 971613168@qq.com
 */
public class EmiPopupMenu {
    private Activity mContext;
    private PopupWindow mPopupWindow;
    private RecyclerView mRecyclerView;
    private View content;
    private EmiPopupMenuAdapter mAdapter;
    private List<EmiPopupMenuItem> menuItemList;
    private static final int DEFAULT_HEIGHT = 480;
    private int popHeight = DEFAULT_HEIGHT;
    private int popWidth = RecyclerView.LayoutParams.WRAP_CONTENT;
    private boolean showIcon = true;
    private boolean dimBackground = true;
    private boolean isNeedAnimation = true;
    private static final int DEFAULT_ANIM_STYLE = R.style.TRM_ANIM_STYLE;
    private int animationStyle;
    private float alpha = 0.75f;
    private EmiRecycleViewDivider divider;
    private static final String TAG = "EmiPopupMenu";
    public EmiPopupMenu(Activity context) {
        this.mContext = context;
        init();
    }

    private void init() {
        content = LayoutInflater.from(mContext).inflate(R.layout.emi_popup_menu, null);
        mRecyclerView = content.findViewById(R.id.emi_popup_recycler_view);
        divider = new EmiRecycleViewDivider(
                mContext, LinearLayoutManager.HORIZONTAL, 2, ContextCompat.getColor(mContext, R.color.blue));
        mRecyclerView.addItemDecoration(divider);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        menuItemList = new ArrayList<>();
        mAdapter = new EmiPopupMenuAdapter(mContext, this, menuItemList, showIcon);
    }

    private PopupWindow getPopupWindow() {
        mPopupWindow = new PopupWindow(mContext);
        mPopupWindow.setContentView(content);
        mPopupWindow.setHeight(popHeight);
        mPopupWindow.setWidth(popWidth);
        if (isNeedAnimation) {
            mPopupWindow.setAnimationStyle(animationStyle <= 0 ? DEFAULT_ANIM_STYLE : animationStyle);
        }
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable());
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (dimBackground) {
                    setBackgroundAlpha(alpha, 1f, 300);
                }
            }
        });
        mAdapter.setData(menuItemList);
        mAdapter.setShowIcon(showIcon);
        mRecyclerView.setAdapter(mAdapter);
        return mPopupWindow;
    }

    public EmiPopupMenu setHeight(int height) {
        if (height <= 0 && height != RecyclerView.LayoutParams.MATCH_PARENT
                && height != RecyclerView.LayoutParams.WRAP_CONTENT) {
            this.popHeight = DEFAULT_HEIGHT;
        } else {
            this.popHeight = height;
        }
        return this;
    }

    public EmiPopupMenu setWidth(int width) {
        if (width <= 0 && width != RecyclerView.LayoutParams.MATCH_PARENT) {
            this.popWidth = RecyclerView.LayoutParams.WRAP_CONTENT;
        } else {
            this.popWidth = width;
        }
        return this;
    }

    /**
     * 是否显示菜单图标
     *
     * @param show
     * @return
     */
    public EmiPopupMenu isShowIcon(boolean show) {
        this.showIcon = show;
        return this;
    }

    /**
     * 添加单个菜单
     *
     * @param item
     * @return
     */
    public EmiPopupMenu addMenuItem(EmiPopupMenuItem item) {
        menuItemList.add(item);
        return this;
    }

    /**
     * 添加多个菜单
     *
     * @param list
     * @return
     */
    public EmiPopupMenu addMenuList(List<EmiPopupMenuItem> list) {
        menuItemList.addAll(list);
        return this;
    }

    /**
     * 是否让背景变暗
     *
     * @param b
     * @return
     */
    public EmiPopupMenu dimBackground(boolean b) {
        this.dimBackground = b;
        return this;
    }

    /**
     * 否是需要动画
     *
     * @param need
     * @return
     */
    public EmiPopupMenu isNeedAnimation(boolean need) {
        this.isNeedAnimation = need;
        return this;
    }

    /**
     * 设置动画
     *
     * @param style
     * @return
     */
    public EmiPopupMenu setAnimationStyle(int style) {
        this.animationStyle = style;
        return this;
    }

    public EmiPopupMenu setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        mAdapter.setOnMenuItemClickListener(listener);
        return this;
    }

    public EmiPopupMenu showAsDropDown(View anchor) {
        showAsDropDown(anchor, 0, 0);
        return this;
    }

    /**
     * @param anchor
     * @param xOffDp
     * @param yOffDp
     * @return
     */
    public EmiPopupMenu showAsDropDown(View anchor, int xOffDp, int yOffDp) {
        if (mPopupWindow == null) {
            getPopupWindow();
        }
        if (!mPopupWindow.isShowing()) {
            xOffDp = DisplayUtil.dip2px(EmiReadingApplication.getAppContext(), xOffDp);
            yOffDp = DisplayUtil.dip2px(EmiReadingApplication.getAppContext(), yOffDp);
            LogUtil.w(TAG,"xOffDp-------->"+xOffDp);
            LogUtil.w(TAG,"yOffDp-------->"+yOffDp);
            mPopupWindow.showAsDropDown(anchor, xOffDp, yOffDp);
            if (dimBackground) {
                setBackgroundAlpha(1f, alpha, 240);
            }
        }
        return this;
    }

    private void setBackgroundAlpha(float from, float to, int duration) {
        final WindowManager.LayoutParams lp = mContext.getWindow().getAttributes();
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                lp.alpha = (float) animation.getAnimatedValue();
                mContext.getWindow().setAttributes(lp);
            }
        });
        animator.start();
    }

    public void dismiss() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    public interface OnMenuItemClickListener {
        /**
         * 弹出框的item点击事件
         *
         * @param position
         */
        void onMenuItemClick(int position);
    }

    public EmiPopupMenu setDividerMarginRight(int rightDp) {
        divider.setDividerMarginRight(rightDp);
        return this;
    }

    public EmiPopupMenu setDividerMarginLeft(int leftDp) {
        divider.setDividerMarginLeft(leftDp);
        return this;
    }
}
