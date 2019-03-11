package com.emi.emireading.widget.view.radius.delegate;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;

/**
 * Created: AriesHoo on 2018/5/25 11:08
 * E-Mail: AriesHoo@126.com
 * Function:CheckBox及RadioButton管理类
 * Description:
 * 1、2018-5-25 11:27:05 新增代理解决泛型链式调用BUG
 */
public class RadiusCompoundButtonDelegate extends RadiusCompoundDelegate<RadiusCompoundButtonDelegate> {

    public RadiusCompoundButtonDelegate(CompoundButton view, Context context, AttributeSet attrs) {
        super(view, context, attrs);
    }

    @Override
    public void init() {
        super.init();
    }
}
