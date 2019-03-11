package com.emi.emireading.widget.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.emi.emireading.R;
import com.emi.emireading.widget.view.emimenu.SystemUtil;

/**
 * @author :zhoujian
 * @description : 自定义CheckBox
 * @company :翼迈科技
 * @date 2019年01月24日上午 10:34
 * @Email: 971613168@qq.com
 */

public class EmiCheckBox extends View {

    private Drawable checkDrawable;
    private static Paint paint;
    private static Paint eraser;
    private static Paint eraser2;
    private static Paint backgroundPaint;
    private static TextPaint textPaint;

    private Bitmap drawBitmap;
    private Bitmap checkBitmap;
    private Canvas bitmapCanvas;
    private Canvas checkCanvas;

    private StaticLayout textLayout;
    private float textWidth;
    private float textHeight;
    private float textLeft;

    private boolean drawBackground;

    private float progress;
    private ObjectAnimator checkAnimator;
    private boolean isCheckAnimation = true;

    private boolean attachedToWindow;
    private boolean isChecked;

    private int size;
    private int checkOffset;
    private int normalColor;
    private int disabledColor;
    private int borderColor;
    private int unCheckedColor;
    private boolean unchecked_invisible;
    private boolean showBorderInCheckState;

    private final static float progressBounceDiff = 0.2f;

    public EmiCheckBox(Context context) {
        this(context, null);
    }

    public EmiCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmiCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EmiCheckBox);
        normalColor =
                a.getColor(R.styleable.EmiCheckBox_checkbox_color,
                        getColor(context, R.color.colorAccent));
        unCheckedColor =
                a.getColor(R.styleable.EmiCheckBox_unchecked_color,
                        getColor(context, R.color.transparent_70_black));
        disabledColor =
                a.getColor(R.styleable.EmiCheckBox_disabled_color,
                        getColor(context, R.color.color_e5e5e5));
        borderColor =
                a.getColor(R.styleable.EmiCheckBox_border_color, Color.WHITE);
        size = a.getDimensionPixelSize(R.styleable.EmiCheckBox_checkbox_size, SystemUtil.dpToPx(22));
        unchecked_invisible = a.getBoolean(R.styleable.EmiCheckBox_invisible_in_unchecked_state, false);
        checkDrawable = a.getDrawable(R.styleable.EmiCheckBox_checked_drawable);
        showBorderInCheckState = a.getBoolean(R.styleable.EmiCheckBox_show_border_in_check_state, false);
        a.recycle();
        init();
    }

    private void init() {
        if (paint == null) {
            textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(SystemUtil.dpToPx(16));
            paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            eraser = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            eraser.setColor(0);
            eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            eraser2 = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            eraser2.setColor(0);
            eraser2.setStyle(Paint.Style.STROKE);
            eraser2.setStrokeWidth(SystemUtil.dpToPx(28));
            eraser2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            backgroundPaint.setStyle(Paint.Style.STROKE);
            backgroundPaint.setStrokeWidth(SystemUtil.dpToPx(2));
        }
        backgroundPaint.setColor(borderColor);
        setCheckOffset(SystemUtil.dpToPx(1));
        setDrawBackground(true);
        drawBitmap =
                Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_4444);
        bitmapCanvas = new Canvas(drawBitmap);
        checkBitmap =
                Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_4444);
        checkCanvas = new Canvas(checkBitmap);
    }

    public void setProgress(float value) {
        progress = value;
        invalidate();
    }

    public void setDrawBackground(boolean value) {
        drawBackground = value;
    }

    public void setCheckOffset(int value) {
        checkOffset = value;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public float getProgress() {
        return progress;
    }

    public void setNormalColor(int value) {
        normalColor = value;
    }

    private void cancelCheckAnimator() {
        if (checkAnimator != null) {
            checkAnimator.cancel();
        }
    }

    private void animateToCheckedState(boolean newCheckedState) {
        isCheckAnimation = newCheckedState;
        checkAnimator = ObjectAnimator.ofFloat(this, "progress", newCheckedState ? 1 : 0);
        checkAnimator.setDuration(300);
        checkAnimator.start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attachedToWindow = false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    public void setChecked(boolean checked, boolean animated) {
        if (checked == isChecked) {
            return;
        }
        isChecked = checked;
        if (!isChecked) {
            if (unchecked_invisible) {
                setVisibility(View.INVISIBLE);
            }
        } else {
            setVisibility(VISIBLE);
        }
        refresh(animated);
    }

    public void refresh(boolean animated) {
        if (attachedToWindow && animated) {
            animateToCheckedState(isChecked);
        } else {
            cancelCheckAnimator();
            setProgress(isChecked ? 1.0f : 0.0f);
        }
    }

    public boolean isChecked() {
        return isChecked;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getVisibility() != VISIBLE) {
            return;
        }
        if (drawBackground || progress != 0) {
            drawBitmap.eraseColor(0);
            float radX = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / 2;
            float radY = (getMeasuredHeight() - getPaddingTop() - getPaddingBottom()) / 2;
            float rad = Math.min(radX, radY);
            float paddingLeft = getPaddingLeft() + radX - rad;
            float paddingTop = getPaddingTop() + radY - rad;
            float drawRad = rad;

            float roundProgress = progress >= 0.5f ? 1.0f : progress / 0.5f;
            float checkProgress = progress < 0.5f ? 0.0f : (progress - 0.5f) / 0.5f;

            float roundProgressCheckState = isCheckAnimation ? progress : (1.0f - progress);
            if (roundProgressCheckState < progressBounceDiff) {
                drawRad -= SystemUtil.dpToPx(2) * roundProgressCheckState / progressBounceDiff;
            } else if (roundProgressCheckState < progressBounceDiff * 2) {
                drawRad -= SystemUtil.dpToPx(2) - SystemUtil.dpToPx(2)
                        * (roundProgressCheckState - progressBounceDiff) / progressBounceDiff;
            }
            if (drawBackground) {
                paint.setColor(unCheckedColor);
                canvas.drawCircle(paddingLeft + rad, paddingTop + rad, drawRad
                        - SystemUtil.dpToPx(1), paint);
                canvas.drawCircle(paddingLeft + rad, paddingTop + rad, drawRad
                        - SystemUtil.dpToPx(1), backgroundPaint);
            }
            paint.setColor(isEnabled() ? normalColor : disabledColor);
            bitmapCanvas.drawCircle(rad, rad, showBorderInCheckState ? rad - SystemUtil.dpToPx(1.2f) : rad,
                    paint);
            bitmapCanvas.drawCircle(rad, rad,
                    showBorderInCheckState
                            ? (rad - SystemUtil.dpToPx(1.2f)) * (1 - roundProgress)
                            : rad * (1 - roundProgress),
                    eraser);
            canvas.drawBitmap(drawBitmap, paddingLeft, paddingTop, null);

            checkBitmap.eraseColor(0);
            if (textLayout != null) {
                checkCanvas.save();
                checkCanvas.translate(rad - (textWidth) / 2,
                        rad - (textHeight) / 2);
                textLayout.draw(checkCanvas);
                checkCanvas.restore();
            } else if (checkDrawable != null) {
                int w = checkDrawable.getIntrinsicWidth();
                int h = checkDrawable.getIntrinsicHeight();
                int x = (getMeasuredWidth() - w) / 2;
                int y = (getMeasuredHeight() - h) / 2;

                checkDrawable.setBounds(x, y + checkOffset, x + w, y + h + checkOffset);
                checkDrawable.draw(checkCanvas);
            }
            eraser2.setStrokeWidth(rad);
            checkCanvas.drawCircle(rad,
                    rad,
                    rad * checkProgress + rad / 2, eraser2);

            canvas.drawBitmap(checkBitmap, paddingLeft, paddingTop, null);
        }
    }

    public void setText(String text) {
        if (!TextUtils.isEmpty(text)) {
            textLayout =
                    new StaticLayout(text, textPaint,
                            SystemUtil.dpToPx(100), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            if (textLayout.getLineCount() > 0) {
                textLeft = textLayout.getLineLeft(0);
                textWidth = textLayout.getLineWidth(0);
                textHeight = textLayout.getLineBottom(0);
                invalidate();
            } else {
                textLayout = null;
            }
        }
    }


    private int getColor(Context context, int id) {
        return ContextCompat.getColor(context, id);
    }
}
