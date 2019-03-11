package com.emi.emireading.widget.filepicker.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.emi.emireading.R;

/**
 * @author :zhoujian
 * @description : 自定义CheckBox（Material风格）
 * @company :翼迈科技
 * @date 2019年02月13日下午 01:29
 * @Email: 971613168@qq.com
 */

public class MaterialCheckbox extends View {
    private static final String TAG = "MaterialCheckbox";
    private Context context;
    private int minDim;
    private Paint paint;
    private RectF bounds;
    private boolean checked;
    private OnCheckedChangeListener onCheckedChangeListener;
    private Path tick;

    public MaterialCheckbox(Context context) {
        super(context);
        initView(context);
    }

    public MaterialCheckbox(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MaterialCheckbox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void initView(Context context) {
        this.context = context;
        checked = false;
        tick = new Path();
        paint = new Paint();
        bounds = new RectF();
        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                setChecked(!checked);
                onCheckedChangeListener.onCheckedChanged(MaterialCheckbox.this, isChecked());
            }
        };

        setOnClickListener(onClickListener);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isChecked()) {
            paint.reset();
            paint.setAntiAlias(true);
            bounds.set(minDim / 10, minDim / 10, minDim - (minDim / 10), minDim - (minDim / 10));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                paint.setColor(getResources().getColor(R.color.colorAccent, context.getTheme()));
            } else {
                paint.setColor(getResources().getColor(R.color.colorAccent));
            }
            canvas.drawRoundRect(bounds, minDim / 8, minDim / 8, paint);

            paint.setColor(Color.parseColor("#FFFFFF"));
            paint.setStrokeWidth(minDim / 10);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.BEVEL);
            canvas.drawPath(tick, paint);
        } else {
            paint.reset();
            paint.setAntiAlias(true);
            bounds.set(minDim / 10, minDim / 10, minDim - (minDim / 10), minDim - (minDim / 10));
            paint.setColor(Color.parseColor("#C1C1C1"));
            canvas.drawRoundRect(bounds, minDim / 8, minDim / 8, paint);

            bounds.set(minDim / 5, minDim / 5, minDim - (minDim / 5), minDim - (minDim / 5));
            paint.setColor(Color.parseColor("#FFFFFF"));
            canvas.drawRect(bounds, paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        minDim = Math.min(width, height);
        bounds.set(minDim / 10, minDim / 10, minDim - (minDim / 10), minDim - (minDim / 10));
        tick.moveTo(minDim / 4, minDim / 2);
        tick.lineTo(minDim / 2.5f, minDim - (minDim / 3));

        tick.moveTo(minDim / 2.75f, minDim - (minDim / 3.25f));
        tick.lineTo(minDim - (minDim / 4), minDim / 3);
        setMeasuredDimension(width, height);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        invalidate();
    }

    public void setOnCheckedChangedListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }


    public interface OnCheckedChangeListener {
        /**
         * 选中监听
         *
         * @param checkbox
         * @param isChecked
         */
        void onCheckedChanged(MaterialCheckbox checkbox, boolean isChecked);
    }
}
