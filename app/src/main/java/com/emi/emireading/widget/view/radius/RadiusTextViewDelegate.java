package com.emi.emireading.widget.view.radius;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.emi.emireading.widget.view.radius.delegate.RadiusTextDelegate;

/**
 * @author :zhoujian
 * @description : TextView及EditText代理类
 * @company :翼迈科技
 * @date 2018年05月6日下午 03:32
 * @Email: 971613168@qq.com
 */
public class RadiusTextViewDelegate extends RadiusTextDelegate<RadiusTextViewDelegate> {

    public RadiusTextViewDelegate(TextView view, Context context, AttributeSet attrs) {
        super(view, context, attrs);
    }

    @Override
    public void init() {
        super.init();
    }
}
