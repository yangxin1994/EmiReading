package com.emi.emireading.core.utils;

import android.graphics.drawable.Drawable;


/**
 * @author :zhoujian
 * @description : Drawable设置相关工具类
 * @company :翼迈科技
 * @date 2018年07月5日上午 10:53
 * @Email: 971613168@qq.com
 */
public class DrawableUtil {

    /**
     * 设置drawable宽高
     *
     * @param drawable
     * @param width
     * @param height
     */
    public static void setDrawableWidthHeight(Drawable drawable, int width, int height) {
        try {
            if (drawable != null) {
                drawable.setBounds(0, 0,
                        width >= 0 ? width : drawable.getIntrinsicWidth(),
                        height >= 0 ? height : drawable.getIntrinsicHeight());
            }
        } catch (Exception e) {
        }
    }

    /**
     * 复制当前drawable
     *
     * @param drawable
     * @return
     */
    public static Drawable getNewDrawable(Drawable drawable) {
        if (drawable == null) {
            return drawable;
        }
        return drawable.getConstantState().newDrawable();
    }

}
