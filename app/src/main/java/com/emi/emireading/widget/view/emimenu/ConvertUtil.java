


package com.emi.emireading.widget.view.emimenu;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * @author :zhoujian
 * @description : dip转为pix
 * @company :翼迈科技
 * @date 2018年07月4日下午 01:55
 * @Email: 971613168@qq.com
 */
public class ConvertUtil {
    public static int convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics()));
    }
}
