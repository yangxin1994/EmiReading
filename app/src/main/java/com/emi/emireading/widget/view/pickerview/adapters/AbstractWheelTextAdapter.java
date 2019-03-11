
package com.emi.emireading.widget.view.pickerview.adapters;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.emi.emireading.R;
import com.emi.emireading.widget.view.pickerview.config.PickerConfig;

/**
 * @author :zhoujian
 * @description :抽象滚轮文本适配器
 * @company :翼迈科技
 * @date: 2018年07月06日下午 02:17
 * @Email: 971613168@qq.com
 */
public abstract class AbstractWheelTextAdapter extends AbstractWheelAdapter {

    public static final int TEXT_VIEW_ITEM_RESOURCE = -1;

    public static final int LABEL_COLOR = 0xFF700070;
    /**
     * 默认字体大小
     */
    public static final int DEFAULT_TEXT_SIZE = 24;
    protected static final int NO_RESOURCE = 0;
    protected Context context;
    protected LayoutInflater inflater;
    protected int itemResourceId;
    protected int itemTextResourceId;
    protected int emptyItemResourceId;

    private int padding;

    private PickerConfig mPickerConfig;

    protected AbstractWheelTextAdapter(Context context) {
        this(context, TEXT_VIEW_ITEM_RESOURCE);
    }

    protected AbstractWheelTextAdapter(Context context, int itemResource) {
        this(context, itemResource, NO_RESOURCE);
    }

    protected AbstractWheelTextAdapter(Context context, int itemResource, int itemTextResource) {
        this.context = context;
        itemResourceId = itemResource;
        itemTextResourceId = itemTextResource;
        padding = context.getResources().getDimensionPixelSize(R.dimen.textview_default_padding);

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * 根据id获取item
     *
     * @return the item resource Id
     */
    public int getItemResource() {
        return itemResourceId;
    }


    public void setItemResource(int itemResourceId) {
        this.itemResourceId = itemResourceId;
    }


    public int getItemTextResource() {
        return itemTextResourceId;
    }


    public void setItemTextResource(int itemTextResourceId) {
        this.itemTextResourceId = itemTextResourceId;
    }


    public int getEmptyItemResource() {
        return emptyItemResourceId;
    }


    public void setEmptyItemResource(int emptyItemResourceId) {
        this.emptyItemResourceId = emptyItemResourceId;
    }


    protected abstract CharSequence getItemText(int index);

    @Override
    public View getItem(int index, View convertView, ViewGroup parent) {
        if (index >= 0 && index < getItemsCount()) {
            if (convertView == null) {
                convertView = getView(itemResourceId, parent);
            }
            TextView textView = getTextView(convertView, itemTextResourceId);
            if (textView != null) {
                CharSequence text = getItemText(index);
                if (text == null) {
                    text = "";
                }
                textView.setText(text);
                if (itemResourceId == TEXT_VIEW_ITEM_RESOURCE) {
                    configureTextView(textView);
                }
            }
            return convertView;
        }
        return null;
    }

    @Override
    public View getEmptyItem(View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = getView(emptyItemResourceId, parent);
        }
        if (emptyItemResourceId == TEXT_VIEW_ITEM_RESOURCE && convertView instanceof TextView) {
            configureTextView((TextView) convertView);
        }

        return convertView;
    }


    /**
     * 设置textView
     *
     * @param view
     */
    protected void configureTextView(TextView view) {
        if (mPickerConfig == null) {
            mPickerConfig = new PickerConfig();
        }
        view.setTextColor(mPickerConfig.mWheelTVNormalColor);

        view.setGravity(Gravity.CENTER);
        view.setPadding(0, padding, 0, padding);
        view.setTextSize(mPickerConfig.mWheelTVSize);
        view.setLines(1);
//        view.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
    }

    /**
     * 从view中加载文本资源
     *
     * @param view
     * @param textResource
     * @return TextView
     */
    private TextView getTextView(View view, int textResource) {
        TextView text = null;
        try {
            if (textResource == NO_RESOURCE && view instanceof TextView) {
                text = (TextView) view;
            } else if (textResource != NO_RESOURCE) {
                text = (TextView) view.findViewById(textResource);
            }
        } catch (ClassCastException e) {
            Log.e("AbstractWheelAdapter", "你必须为TextView设置一个资源ID");
            throw new IllegalStateException(
                    "你必须为TextView设置一个资源ID", e);
        }

        return text;
    }

    private View getView(int resource, ViewGroup parent) {
        switch (resource) {
            case NO_RESOURCE:
                return null;
            case TEXT_VIEW_ITEM_RESOURCE:
                return new TextView(context);
            default:
                return inflater.inflate(resource, parent, false);
        }
    }

    @Override
    public PickerConfig getConfig() {
        if (mPickerConfig == null){
            mPickerConfig = new PickerConfig();
        }
        return mPickerConfig;
    }

    @Override
    public void setConfig(PickerConfig config) {
        mPickerConfig = config;
    }
}
