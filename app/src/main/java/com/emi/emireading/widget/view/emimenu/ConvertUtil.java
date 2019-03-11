


package com.emi.emireading.widget.view.emimenu;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public class ConvertUtil {
    public static int convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics()));
    }
}
