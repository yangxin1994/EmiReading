package com.emi.emireading.upgrade.core;

import android.app.Dialog;
import android.content.Context;

/**
 * @author :zhoujian
 * @description : zj
 * @company :翼迈科技
 * @date :2018/8/19
 * @Email: 971613168@qq.com
 */
public class BaseDialog extends Dialog {
    private int res;

    public BaseDialog(Context context, int theme, int res) {
        super(context, theme);
        // TODO 自动生成的构造函数存根
        setContentView(res);
        this.res = res;
        setCanceledOnTouchOutside(false);
    }

}