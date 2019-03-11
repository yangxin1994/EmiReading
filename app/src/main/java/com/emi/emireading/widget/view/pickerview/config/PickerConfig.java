package com.emi.emireading.widget.view.pickerview.config;


import com.emi.emireading.widget.view.pickerview.data.Type;
import com.emi.emireading.widget.view.pickerview.data.WheelCalendar;
import com.emi.emireading.widget.view.pickerview.listener.OnDateSetListener;


/**
 * @author :zhoujian
 * @description : 默认配置
 * @company :翼迈科技
 * @date 2018年07月21日上午 09:56
 * @Email: 971613168@qq.com
 */

public class PickerConfig {

    public Type mType = DefaultConfig.TYPE;
    public int mThemeColor = DefaultConfig.COLOR;

    public String mCancelString = DefaultConfig.CANCEL;
    public String mSureString = DefaultConfig.SURE;
    public String mTitleString = DefaultConfig.TITLE;
    public int mToolBarTVColor = DefaultConfig.TOOLBAR_TV_COLOR;

    public int mWheelTVNormalColor = DefaultConfig.TV_NORMAL_COLOR;
    public int mWheelTVSelectorColor = DefaultConfig.TV_SELECTOR_COLOR;
    public int mWheelTVSize = DefaultConfig.TV_SIZE;
    public boolean cyclic = DefaultConfig.CYCLIC;

    public String mYear = DefaultConfig.YEAR;
    public String mMonth = DefaultConfig.MONTH;
    public String mDay = DefaultConfig.DAY;
    public String mHour = DefaultConfig.HOUR;
    public String mMinute = DefaultConfig.MINUTE;

    /**
     * 最小时间
     */
    public WheelCalendar mMinCalendar = new WheelCalendar(0);


    /**
     * 最大时间
     */
    public WheelCalendar mMaxCalendar = new WheelCalendar(0);

    /**
     * 默认时间
     */
    public WheelCalendar mCurrentCalendar = new WheelCalendar(System.currentTimeMillis());

    public OnDateSetListener mCallBack;
}
