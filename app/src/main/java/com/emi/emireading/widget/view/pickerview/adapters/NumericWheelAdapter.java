

package com.emi.emireading.widget.view.pickerview.adapters;

import android.content.Context;
import android.text.TextUtils;

/**
 * @author :zhoujian
 * @description :数字滚轮适配器
 * @company :翼迈科技
 * @date: 2018年07月06日下午 02:17
 * @Email: 971613168@qq.com
 */
public class NumericWheelAdapter extends AbstractWheelTextAdapter {

    /**
     * 默认的最大值
     */
    public static final int DEFAULT_MAX_VALUE = 9;

    /**
     * 默认的最小值
     */
    private static final int DEFAULT_MIN_VALUE = 0;

    private int minValue;
    private int maxValue;

    private String format;
    private String unit;


    public NumericWheelAdapter(Context context) {
        this(context, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    public NumericWheelAdapter(Context context, int minValue, int maxValue) {
        this(context, minValue, maxValue, null);
    }

    public NumericWheelAdapter(Context context, int minValue, int maxValue, String format) {
        this(context, minValue, maxValue, format, null);
    }

    public NumericWheelAdapter(Context context, int minValue, int maxValue, String format, String unit) {
        super(context);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.format = format;
        this.unit = unit;
    }

    @Override
    public CharSequence getItemText(int index) {
        if (index >= 0 && index < getItemsCount()) {
            int value = minValue + index;
            String text = !TextUtils.isEmpty(format) ? String.format(format, value) : Integer.toString(value);
            text = TextUtils.isEmpty(unit) ? text : text + unit;

            return text;
        }
        return null;
    }

    @Override
    public int getItemsCount() {
        return maxValue - minValue + 1;
    }


}
