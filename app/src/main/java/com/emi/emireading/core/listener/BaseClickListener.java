package com.emi.emireading.core.listener;

import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.adapter.BaseViewHolder;

import java.util.Set;

import static com.emi.emireading.core.adapter.BaseEmiAdapter.EMPTY_VIEW;
import static com.emi.emireading.core.adapter.BaseEmiAdapter.FOOTER_VIEW;
import static com.emi.emireading.core.adapter.BaseEmiAdapter.HEADER_VIEW;
import static com.emi.emireading.core.adapter.BaseEmiAdapter.LOADING_VIEW;

/**
 * @author :zhoujian
 * @description : RecyclerView的基类点击监听
 * @company :翼迈科技
 * @date: 2017年11月01日下午 03:012
 * @Email: 971613168@qq.com
 */
public abstract class BaseClickListener implements RecyclerView.OnItemTouchListener {
    private GestureDetectorCompat mGestureDetector;
    private RecyclerView recyclerView;
    protected BaseEmiAdapter baseAdapter;
    public static String TAG = "BaseClickListener";
    private boolean mIsPrePressed = false;
    private boolean mIsShowPress = false;
    private View mPressedView = null;

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        if (recyclerView == null) {
            this.recyclerView = rv;
            this.baseAdapter = (BaseEmiAdapter) recyclerView.getAdapter();
            mGestureDetector = new GestureDetectorCompat(recyclerView.getContext(), new ItemTouchHelperGestureListener(recyclerView));
        } else if (recyclerView != rv) {
            this.recyclerView = rv;
            this.baseAdapter = (BaseEmiAdapter) recyclerView.getAdapter();
            mGestureDetector = new GestureDetectorCompat(recyclerView.getContext(), new ItemTouchHelperGestureListener(recyclerView));
        }
        if (!mGestureDetector.onTouchEvent(e) && e.getActionMasked() == MotionEvent.ACTION_UP && mIsShowPress) {
            if (mPressedView != null) {
                BaseViewHolder vh = (BaseViewHolder) recyclerView.getChildViewHolder(mPressedView);
                if (vh == null || !isHeaderOrFooterView(vh.getItemViewType())) {
                    mPressedView.setPressed(false);
                }
            }
            mIsShowPress = false;
            mIsPrePressed = false;
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        mGestureDetector.onTouchEvent(e);
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    private class ItemTouchHelperGestureListener extends GestureDetector.SimpleOnGestureListener {

        private RecyclerView recyclerView;

        @Override
        public boolean onDown(MotionEvent e) {
            mIsPrePressed = true;
            mPressedView = recyclerView.findChildViewUnder(e.getX(), e.getY());

            super.onDown(e);
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            if (mIsPrePressed && mPressedView != null) {
//                mPressedView.setPressed(true);
                mIsShowPress = true;
            }
            super.onShowPress(e);
        }

        ItemTouchHelperGestureListener(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mIsPrePressed && mPressedView != null) {
                if (recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) {
                    return false;
                }
                final View pressedView = mPressedView;
                BaseViewHolder vh = (BaseViewHolder) recyclerView.getChildViewHolder(pressedView);

                if (isHeaderOrFooterPosition(vh.getLayoutPosition())) {
                    return false;
                }
                Set<Integer> childClickViewIds = vh.getChildClickViewIds();
                Set<Integer> nestViewIds = vh.getNestViews();
                if (childClickViewIds != null && childClickViewIds.size() > 0) {
                    for (Integer childClickViewId : childClickViewIds) {
                        View childView = pressedView.findViewById(childClickViewId);
                        if (childView != null) {
                            if (inRangeOfView(childView, e) && childView.isEnabled()) {
                                if (nestViewIds != null && nestViewIds.contains(childClickViewId)) {
                                    return false;
                                }
                                setPressViewHotSpot(e, childView);
                                childView.setPressed(true);
                                onItemChildClick(baseAdapter, childView, vh.getLayoutPosition() - baseAdapter.getHeaderLayoutCount());
                                resetPressedView(childView);
                                return true;
                            } else {
                                childView.setPressed(false);
                            }
                        }

                    }
                    setPressViewHotSpot(e, pressedView);
                    mPressedView.setPressed(true);
                    for (Integer childClickViewId : childClickViewIds) {
                        View childView = pressedView.findViewById(childClickViewId);
                        if (childView != null) {
                            childView.setPressed(false);
                        }
                    }
                    onItemClick(baseAdapter, pressedView, vh.getLayoutPosition() - baseAdapter.getHeaderLayoutCount());
                } else {
                    setPressViewHotSpot(e, pressedView);
                    mPressedView.setPressed(true);
                    if (childClickViewIds != null && childClickViewIds.size() > 0) {
                        for (Integer childClickViewId : childClickViewIds) {
                            View childView = pressedView.findViewById(childClickViewId);
                            if (childView != null) {
                                childView.setPressed(false);
                            }
                        }
                    }

                    onItemClick(baseAdapter, pressedView, vh.getLayoutPosition() - baseAdapter.getHeaderLayoutCount());
                }
                resetPressedView(pressedView);

            }
            return true;
        }

        private void resetPressedView(final View pressedView) {
            if (pressedView != null) {
                pressedView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (pressedView != null) {
                            pressedView.setPressed(false);
                        }

                    }
                }, 50);
            }

            mIsPrePressed = false;
            mPressedView = null;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            boolean isChildLongClick = false;
            if (recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) {
                return;
            }
            if (mIsPrePressed && mPressedView != null) {
                mPressedView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                BaseViewHolder vh = (BaseViewHolder) recyclerView.getChildViewHolder(mPressedView);
                if (!isHeaderOrFooterPosition(vh.getLayoutPosition())) {
                    Set<Integer> longClickViewIds = vh.getItemChildLongClickViewIds();
                    Set<Integer> nestViewIds = vh.getNestViews();
                    if (longClickViewIds != null && longClickViewIds.size() > 0) {
                        for (Integer longClickViewId : longClickViewIds) {
                            View childView = mPressedView.findViewById(longClickViewId);
                            if (inRangeOfView(childView, e) && childView.isEnabled()) {
                                if (nestViewIds != null && nestViewIds.contains(longClickViewId)) {
                                    isChildLongClick = true;
                                    break;
                                }
                                setPressViewHotSpot(e, childView);
                                onItemChildLongClick(baseAdapter, childView, vh.getLayoutPosition() - baseAdapter.getHeaderLayoutCount());
                                childView.setPressed(true);
                                mIsShowPress = true;
                                isChildLongClick = true;
                                break;
                            }
                        }
                    }
                    if (!isChildLongClick) {

                        onItemLongClick(baseAdapter, mPressedView, vh.getLayoutPosition() - baseAdapter.getHeaderLayoutCount());
                        setPressViewHotSpot(e, mPressedView);
                        mPressedView.setPressed(true);
                        if (longClickViewIds != null) {
                            for (Integer longClickViewId : longClickViewIds) {
                                View childView = mPressedView.findViewById(longClickViewId);
                                childView.setPressed(false);
                            }
                        }
                        mIsShowPress = true;
                    }

                }

            }
        }


    }

    private void setPressViewHotSpot(final MotionEvent e, final View mPressedView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mPressedView != null && mPressedView.getBackground() != null) {
                mPressedView.getBackground().setHotspot(e.getRawX(), e.getY() - mPressedView.getY());
            }
        }
    }

    /**
     * item点击监听
     *
     * @param adapter
     * @param view
     * @param position
     */
    public abstract void onItemClick(BaseEmiAdapter adapter, View view, int position);

    /**
     * item长点击事件
     * @param adapter
     * @param view
     * @param position
     */
    public abstract void onItemLongClick(BaseEmiAdapter adapter, View view, int position);

    /**
     *
     * @param adapter
     * @param view
     * @param position
     */
    public abstract void onItemChildClick(BaseEmiAdapter adapter, View view, int position);

    public abstract void onItemChildLongClick(BaseEmiAdapter adapter, View view, int position);

    public boolean inRangeOfView(View view, MotionEvent ev) {
        int[] location = new int[2];
        if (view == null || !view.isShown()) {
            return false;
        }
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        if (ev.getRawX() < x
                || ev.getRawX() > (x + view.getWidth())
                || ev.getRawY() < y
                || ev.getRawY() > (y + view.getHeight())) {
            return false;
        }
        return true;
    }

    private boolean isHeaderOrFooterPosition(int position) {
        /**
         *  have a headview and EMPTY_VIEW FOOTER_VIEW LOADING_VIEW
         */
        if (baseAdapter == null) {
            if (recyclerView != null) {
                baseAdapter = (BaseEmiAdapter) recyclerView.getAdapter();
            } else {
                return false;
            }
        }
        int type = baseAdapter.getItemViewType(position);
        return (type == EMPTY_VIEW || type == HEADER_VIEW || type == FOOTER_VIEW || type == LOADING_VIEW);
    }

    private boolean isHeaderOrFooterView(int type) {

        return (type == EMPTY_VIEW || type == HEADER_VIEW || type == FOOTER_VIEW || type == LOADING_VIEW);
    }
}


