package com.emi.emireading.widget.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.emi.emireading.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author :zhoujian
 * @description : 自定义标题栏
 * @company :翼迈科技
 * @date: 2017年12月26日上午 11:42
 * @Email: 971613168@qq.com
 */

public class TitleView extends RelativeLayout {
    private Drawable arrowDrawable;
    /** > 绘制的X轴偏移量**/
    private float arrowStartDrawX;
    /**尾部 > 大小**/
    private int arrowSize = -1;
    /**绘制arrow画笔**/
    private Paint arrowPaint;
    /**arrowPaint 颜色**/
    private int arrowColor = Color.parseColor("#000000");
    /** > 绘制的Y轴偏移量**/
    private float arrowStartDrawY;

    /**尾部箭头图标大小**/
    private static final int ARROW_SIZE = 13;

    /**尾部宽**/
    private int arrowDrawableWidth;
    private DisplayMetrics dm;
    /**尾部高**/
    private int arrowDrawableHeight;
    private static final String TAG = "TitleView";
    private ImageButton ivbLeft;
    private ImageView ivRight;
    private TextView tvTitle;
    private TextView tvLeft;
    private float leftTextSize;
    private float titleSize;
    private float rightTextSize;
    private TextView tvRight;
    private int leftIconResource;
    private int rightResource;
    private boolean isShowLeftIcon;
    private boolean isShowRightIcon;
    private String title;
    private String leftIconTitle;
    private String rightIconTitle;
    private String onClickLeft;
    private String onClickRight;
    private int titleColor;
    private boolean isGoBack;
    private Context mContext;
    private LinearLayout llRight;
    public TitleView(Context context) {
        this(context, null);
        mContext = context;
    }

    public TitleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }


    public TitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init(context,attrs);
        initView(context);
        setWillNotDraw(false);

    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.TitleView);
        dm = Resources.getSystem().getDisplayMetrics();
        leftTextSize = typedArray.getDimension(R.styleable.TitleView_leftButtonTextSize, getResources().getDimension(R.dimen.app_default_textSize));
        rightTextSize = typedArray.getDimension(R.styleable.TitleView_rightButtonTextSize,getResources().getDimension(R.dimen.app_default_textSize));
        title = typedArray.getString(R.styleable.TitleView_titleText);
        titleColor = typedArray.getColor(R.styleable.TitleView_titleColor, Color.WHITE);
        titleSize = typedArray.getDimension(R.styleable.TitleView_titleSize,getResources().getDimension(R.dimen.app_default_textSize));
        leftIconTitle = typedArray.getString(R.styleable.TitleView_leftButtonText);
        rightIconTitle = typedArray.getString(R.styleable.TitleView_rightButtonText);
        onClickLeft =  typedArray.getString(R.styleable.TitleView_onClickLeft);
        onClickRight = typedArray.getString(R.styleable.TitleView_onClickRight);
        isShowLeftIcon = typedArray.getBoolean(R.styleable.TitleView_isShowLeftIcon,true);
        isShowRightIcon = typedArray.getBoolean(R.styleable.TitleView_isShowRightIcon,false);
        leftIconResource = typedArray.getInt(R.styleable.TitleView_leftButtonImage,R.mipmap.icon_back);
        isGoBack = typedArray.getBoolean(R.styleable.TitleView_goBack,true);
        rightResource = typedArray.getInt(R.styleable.TitleView_rightButtonImage,R.drawable.menu);
        typedArray.recycle();
        if(arrowSize == -1){
            arrowSize = applyDimension(ARROW_SIZE);
        }
        arrowPaint = new Paint();
        arrowPaint.setColor(arrowColor);
        arrowPaint.setAntiAlias(true);
        arrowPaint.setStrokeWidth(3.0f);

    }

    /**
     * px2dp
     * @param value
     */
    private int applyDimension(float value){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,value,dm);
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(arrowDrawable != null){
            arrowDrawableWidth = arrowDrawable.getIntrinsicWidth();
            arrowDrawableHeight = arrowDrawable.getIntrinsicHeight();
        }
        if(arrowSize >= 0){
            arrowDrawableWidth = arrowDrawableHeight = arrowSize;
        }
        //        arrowStartDrawX = getWidth() - rightIconPaddingRight - arrowDrawableWidth;//右边图片的x轴坐标
        arrowStartDrawX = 20;
        arrowStartDrawY = (getHeight() - arrowDrawableHeight) / 2.0f;
        Log.i(TAG,"arrowStartDrawY:"+arrowStartDrawY);

    }

    private void initView(final Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.common_toolbar,null);
        ivbLeft = (ImageButton) rootView.findViewById(R.id.ivbLeft);
        ivRight = (ImageView) rootView.findViewById(R.id.ivRight);
        tvTitle =  (TextView) rootView.findViewById(R.id.tvTitle);
        tvLeft =  (TextView) rootView.findViewById(R.id.tvLeft);
        tvRight = (TextView) rootView.findViewById(R.id.tvRight);
        llRight = rootView.findViewById(R.id.ll_action);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,titleSize);
        tvLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX,leftTextSize);
        tvRight.setTextSize(TypedValue.COMPLEX_UNIT_PX,rightTextSize);
        tvTitle.setTextColor(titleColor);
        tvRight.setText(rightIconTitle);
        tvLeft.setText(leftIconTitle);
        tvTitle.setText(title);
        if (isShowLeftIcon) {
            ivbLeft.setBackgroundResource(leftIconResource);
        }else {
            ivbLeft.setVisibility(View.GONE);
        }
        if (isShowRightIcon) {
            ivRight.setBackgroundResource(rightResource);
        }else {
            ivRight.setVisibility(View.GONE);
        }
        initListener(context);
        addView(rootView);
    }

    public void setTitle(String title) {
        if (TextUtils.isEmpty(title)) {
            return;
        }
        tvTitle.setText(title);
    }

    /**
     * 设置左边icon标题
     * @param title
     */
    public void setLeftIconText(String title) {
        if (TextUtils.isEmpty(title)) {
            return;
        }
        tvLeft.setText(title);
    }

    /**
     * 设置右边icon标题
     * @param title
     */
    public void setRightIconText(String title) {
        if (TextUtils.isEmpty(title)) {
            return;
        }
        tvRight.setText(title);
    }
    public void setLeftIcon(int resourceId) {
        ivbLeft.setImageResource(resourceId);
    }

    /**
     * 设置右边icon
     * @param resourceId
     */
    public void setRightIcon(int resourceId) {
        setRightButtonIsShow(true);
        ivRight.setImageResource(resourceId);
        ivRight.setVisibility(View.VISIBLE);
    }

    /**
     * 设置标题颜色
     * @param color
     */
    private void setTitleColor(int color) {
        tvTitle.setTextColor(color);
    }

    private void performClick(final Context context, final View view, final String handlerName) {
        if (view == null || TextUtils.isEmpty(handlerName)) {
            return;
        }
        if (context.isRestricted()) {
            throw new IllegalStateException("The android:onClick attribute cannot "
                    + "be used within a restricted context");
        }
        view.setOnClickListener(new View.OnClickListener() {
            private Method mHandler;
            @Override
            public void onClick(View v) {
                if (mHandler == null) {
                    try {
                        mHandler = context.getClass().getMethod(handlerName,
                                View.class);
                    } catch (NoSuchMethodException e) {
                        int id = view.getId();
                        String idText = id == NO_ID ? "" : " with id '"
                                + context.getResources().getResourceEntryName(
                                id) + "'";
                        throw new IllegalStateException("Could not find a method " +
                                handlerName + "(View) in the activity "
                                + getContext().getClass() + " for onClick handler"
                                + " on view " + context.getClass() + idText, e);
                    }
                }
                try {
                    mHandler.invoke(context, TitleView.this);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Could not execute non "
                            + "public method of the activity", e);
                } catch (InvocationTargetException e) {
                    throw new IllegalStateException("Could not execute "
                            + "method of the activity", e);
                }
            }
        });
    }

    private void initListener(final Context context) {
        if (isGoBack) {
            ivbLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Activity) context).finish();
                }
            });
        }
        if (!TextUtils.isEmpty(onClickLeft)) {
            performClick(context,ivbLeft,onClickLeft);
        }
        if (!TextUtils.isEmpty(onClickRight)) {
            performClick(context,ivRight,onClickRight);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int minSize = (int) mContext.getResources().getDimension(R.dimen.toolbar_height);
        // wrap_content的specMode是AT_MOST模式，这种情况下宽/高等同于specSize
        // 查表得这种情况下specSize等同于parentSize，也就是父容器当前剩余的大小
        // 在wrap_content的情况下如果特殊处理，效果等同match_parent
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(minSize, minSize);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(minSize, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, minSize);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawFooterDrawable(canvas);
    }

    /**
     * 绘制尾部指示图标
     *
     * @param canvas
     */
    private void drawFooterDrawable(Canvas canvas){
        canvas.save();
        canvas.translate(arrowStartDrawX, arrowStartDrawY);
        if(arrowDrawable != null){
            arrowDrawable.setBounds(0, 0, arrowDrawableWidth, arrowDrawableHeight);
            arrowDrawable.draw(canvas);
        }else{
            /**
             * 自行绘制arrow图标，绘制规则是：X轴坐标在arrow中间位置开始绘制，Y轴绘制不变
             */
            final float centerY = arrowDrawableHeight / 2.0f;
            final float lineStartX = arrowDrawableWidth / 2.0f;
            final float lineEndX = arrowDrawableWidth;
            canvas.drawLine(lineStartX,0,lineEndX,centerY,arrowPaint);
            canvas.drawLine(lineStartX,arrowDrawableHeight,lineEndX,centerY,arrowPaint);
        }
        canvas.restore();
    }

    public void setOnClickRightListener(OnClickListener listener){
        setRightButtonIsShow(true);
        llRight.setOnClickListener(listener);
    }

    public void setOnClickRightListener(int rightIcon,OnClickListener listener){
        setRightButtonIsShow(true);
        llRight.setOnClickListener(listener);
        ivRight.setVisibility(View.VISIBLE);
        ivRight.setImageResource(rightIcon);
    }
    public void setRightButtonIsShow(boolean visible){
        if (visible){
            llRight.setVisibility(View.VISIBLE);
        }else {
            llRight.setVisibility(View.INVISIBLE);
        }
    }

    public LinearLayout getRightView(){
        return llRight;
    }
}
