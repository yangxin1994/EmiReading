package com.emi.emireading.upgrade.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.emi.emireading.upgrade.core.CheckVersionService;
import com.emi.emireading.upgrade.event.CommonEvent;
import com.emi.emireading.upgrade.helper.DownloadHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author :zhoujian
 * @description : 更新基类
 * @company :翼迈科技
 * @date :2018/8/19
 * @Email: 971613168@qq.com
 */
public abstract class BaseUpdateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        setTransparent(this);
    }


    /**
     * 设置根布局参数
     */
    private void setRootView(Activity activity) {
        ViewGroup parent = activity.findViewById(android.R.id.content);
        for (int i = 0, count = parent.getChildCount(); i < count; i++) {
            View childView = parent.getChildAt(i);
            if (childView instanceof ViewGroup) {
                childView.setFitsSystemWindows(true);
                ((ViewGroup) childView).setClipToPadding(true);
            }
        }
    }


    /**
     * 使状态栏透明
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void transparentStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public void setTransparent(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        transparentStatusBar(activity);
        setRootView(activity);
    }


    protected void throwWrongIdsException() {
        throw new RuntimeException("customize dialog must use the specify id that lib gives");
    }


    protected DownloadHelper getDownloadConfig() {
        return CheckVersionService.mDownloadHelper;
    }

    protected void checkForceUpdate() {
        if (getDownloadConfig() != null && getDownloadConfig().getForceUpdateListener() != null) {
            getDownloadConfig().getForceUpdateListener().onShouldForceUpdate();
            finish();
        }
    }

    protected void cancelHandler() {
        if (getDownloadConfig() != null && getDownloadConfig().getOnCancelListener() != null) {
            getDownloadConfig().getOnCancelListener().onCancel();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveEvent(CommonEvent commonEvent) {

    }

    /**
     * 显示默认对话框
     */
    public abstract void showDefaultDialog();

    /**
     * 显示自定义对话框
     */
    public abstract void showCustomDialog();
}
