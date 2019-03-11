package com.emi.emireading.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;

import com.emi.emireading.EmiReadingApplication;
import com.emi.emireading.R;
import com.emi.emireading.adpter.UserInfoDetailEmiAdapter;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.common.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.db.SQLiteHelper;
import com.emi.emireading.core.request.response.ToastUtils;
import com.emi.emireading.core.utils.DisplayUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.FileUtil;
import com.emi.emireading.core.utils.TimeUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.DetailInfo;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.widget.view.EmiRecycleViewDivider;
import com.emi.emireading.widget.view.TitleView;
import com.emi.emireading.widget.view.emimenu.CustomEmiMenu;
import com.emi.emireading.widget.view.emimenu.IconMenuAdapter;
import com.emi.emireading.widget.view.emimenu.IconPowerMenuItem;
import com.emi.emireading.widget.view.emimenu.MenuAnimation;
import com.emi.emireading.widget.view.emimenu.OnMenuItemClickListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.emi.emireading.core.config.EmiConstants.EMPTY_METER_DATA;
import static com.emi.emireading.core.config.EmiConstants.ERROR_METER_DATA;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_CHANNEL_NUMBER;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_CURRENT_TAG;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_FILE_NAME;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_POSITION;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_USER_ID;
import static com.emi.emireading.core.config.EmiConstants.IMG_JPG;
import static com.emi.emireading.core.config.EmiConstants.STATE_ALL;
import static com.emi.emireading.core.config.EmiConstants.STATE_FAILED;
import static com.emi.emireading.core.config.EmiConstants.STATE_NO_READ;
import static com.emi.emireading.core.config.EmiConstants.STATE_PEOPLE_RECORDING;
import static com.emi.emireading.core.config.EmiConstants.STATE_SUCCESS;
import static com.emi.emireading.core.config.EmiConstants.STATE_WARNING;
import static com.emi.emireading.ui.MeterQueryActivityNew.REQUEST_CODE_DETAIL_INFO;

/**
 * @author :zhoujian
 * @description : 用户详情
 * @company :翼迈科技
 * @date 2018年03月09日下午 02:04
 * @Email: 971613168@qq.com
 */

public class UserInfoDetailActivity extends BaseActivity implements View.OnClickListener, OnMenuItemClickListener<IconPowerMenuItem> {
    private Context context;
    private RecyclerView recyclerView;
    private UserInfoDetailEmiAdapter userInfoDetailAdapter;
    private ArrayList<DetailInfo> infoArrayList;
    private UserInfo currentUserInfo;
    private final int REQUEST_CODE_TAKE_PHOTO = 102;
    private int position;
    private EditText etEditData;
    private TitleView titleView;
    private boolean isShowPeopleRecord;
    public static final String EXTRA_EDIT_DATA_LIST = "EXTRA_EDIT_DATA_LIST";
    public static final String EXTRA_BUNDLE = "EXTRA_BUNDLE";
    private static final String STRING_TAKE_PHOTO = "拍     照";
    private static final String STRING_DEFAULT_IMAGE_NAME = "未知";
    private static final String STRING_TAG = "标     记";
    private static final String STRING_TAG_CANCEL = "取消标记";
    private int tag;
    private CustomEmiMenu emiPopupMenu;
    private ArrayList<UserInfo> dataList = new ArrayList<>();
    private String channelNumber;
    private ArrayList<UserInfo> editList = new ArrayList<>();
    private String mFileName;
    private File photoFile;
    private Handler mHandler = new Handler();
    private IconPowerMenuItem ipmTag;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_userinfo_detail;
    }

    @Override
    protected void initIntent() {
        context = this;
        Intent intent = getIntent();
        channelNumber = intent.getStringExtra(EXTRA_CHANNEL_NUMBER);
        position = intent.getIntExtra(EXTRA_POSITION, 0);
        tag = intent.getIntExtra(EXTRA_CURRENT_TAG, 0);
        mFileName = intent.getStringExtra(EXTRA_FILE_NAME);
        dataList.clear();
        dataList.addAll(EmiConstants.userInfoArrayList);
        if (channelNumber != null) {
            //该方法在"任务查询"页面跳转过来时调用
            getSameChannelData(channelNumber);
        }
        currentUserInfo = dataList.get(position);
    }

    @Override
    protected void initUI() {
        initView();
        sqLiteHelper = new SQLiteHelper(EmiReadingApplication.getAppContext());

    }

    @Override
    protected void initData() {
        infoArrayList = new ArrayList<>();
        isShowPeopleRecord = EmiUtils.isShowPeopleRecord();
        loadInfo(currentUserInfo);
        userInfoDetailAdapter = new UserInfoDetailEmiAdapter(infoArrayList);
        EmiRecycleViewDivider divider = new EmiRecycleViewDivider(
                this, LinearLayoutManager.HORIZONTAL, 2, ContextCompat.getColor(this, R.color.progress_dialog_gray_color));
        recyclerView.addItemDecoration(divider);
        recyclerView.setAdapter(userInfoDetailAdapter);
        userInfoDetailAdapter.bindToRecyclerView(recyclerView);
        titleView.setOnClickRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreFunctionMenu();
            }
        });
        titleView.setRightButtonIsShow(EmiUtils.isDebugMode());
    }

    private void skipEditFileActivity() {
        Intent intent = new Intent();
        intent.setClass(context, ModifyFileActivity.class);
        intent.putExtra(EXTRA_FILE_NAME, mFileName);
        intent.putExtra(EXTRA_USER_ID, currentUserInfo.accountnum);
        startActivity(intent);
    }

    private void initView() {
        findViewById(R.id.bt_up).setOnClickListener(this);
        findViewById(R.id.bt_back).setOnClickListener(this);
        findViewById(R.id.bt_next).setOnClickListener(this);
        etEditData = findViewById(R.id.et_data);
        findViewById(R.id.bt_save).setOnClickListener(this);
        recyclerView = findViewById(R.id.recyclerView);
        titleView = findViewById(R.id.titleView);
        titleView.setRightIcon(R.mipmap.ic_menu_abs_more);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        initPopMenu();
        refreshTagByStatus(currentUserInfo);
    }

    private void loadInfo(UserInfo userInfo) {
        if (userInfo != null) {
            infoArrayList.clear();
            infoArrayList.add(new DetailInfo("用户编号:", userInfo.accountnum, R.color.blue));
            infoArrayList.add(new DetailInfo("用户名:", userInfo.username, R.color.blue));
            infoArrayList.add(new DetailInfo("用户地址:", userInfo.useraddr, R.color.blue));
            infoArrayList.add(new DetailInfo("水表地址:", userInfo.meteraddr, R.color.blue));
            infoArrayList.add(new DetailInfo("上次读数:", String.valueOf(userInfo.lastdata), R.color.blue));
            infoArrayList.add(new DetailInfo("上次用量:", String.valueOf(userInfo.lastyl), R.color.blue));
            if (userInfo.state == STATE_FAILED) {
                infoArrayList.add(new DetailInfo("本次读数:", ERROR_METER_DATA, R.color.green));
                infoArrayList.add(new DetailInfo("本次用量:", EMPTY_METER_DATA, R.color.blue));
            } else if (userInfo.state == STATE_NO_READ || userInfo.state == STATE_ALL) {
                infoArrayList.add(new DetailInfo("本次读数:", EMPTY_METER_DATA, R.color.green));
                infoArrayList.add(new DetailInfo("本次用量:", EMPTY_METER_DATA, R.color.blue));
            } else {
                infoArrayList.add(new DetailInfo("本次读数:", String.valueOf(userInfo.curdata), R.color.green));
                infoArrayList.add(new DetailInfo("本次用量:", String.valueOf(userInfo.curyl), R.color.green));
            }
            DetailInfo detailInfo = new DetailInfo("抄表状态:", getStateLabel(userInfo.state));
            switch (userInfo.state) {
                case STATE_SUCCESS:
                    detailInfo.setValueColor(R.color.green);
                case STATE_WARNING:
                    detailInfo.setValueColor(R.color.yellow);
                    break;
                case STATE_FAILED:
                    detailInfo.setValueColor(R.color.red);
                    break;
                case STATE_NO_READ:
                    detailInfo.setValueColor(R.color.blue);
                    break;
                case STATE_PEOPLE_RECORDING:
                    detailInfo.setValueColor(R.color.green);
                    break;
                default:
                    detailInfo.setValueColor(R.color.text_black);
                    break;
            }
            infoArrayList.add(detailInfo);
            infoArrayList.add(new DetailInfo("通道板号:", userInfo.channelNumber, R.color.blue));
            infoArrayList.add(new DetailInfo("厂商代码:", userInfo.firmCode, R.color.blue));
            if (TextUtils.isEmpty(userInfo.channelAddress)) {
                infoArrayList.add(new DetailInfo("通道板位置:", userInfo.useraddr, R.color.blue));
            } else {
                infoArrayList.add(new DetailInfo("通道板位置:", userInfo.channelAddress, R.color.blue));
            }
            infoArrayList.add(new DetailInfo("抄表时间:", TimeUtil.getTimeString(userInfo.getCurreaddate()), R.color.blue));
        } else {
            ToastUtil.showShortToast("数据有误");
        }

    }

    /**
     * 根据状态获取状态标签
     */
    private String getStateLabel(int state) {
        String label;
        switch (state) {
            case STATE_NO_READ:
                label = "未抄";
                break;
            case STATE_FAILED:
                label = "失败";
                break;
            case STATE_WARNING:
                label = "异常";
                break;
            case STATE_PEOPLE_RECORDING:
                if (isShowPeopleRecord) {
                    label = "人工补录";
                } else {
                    label = "正常";
                }
                break;
            case STATE_SUCCESS:
                label = "正常";
                break;
            default:
                label = "未知";
                break;
        }
        return label;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_back:
                finish();
                break;
            case R.id.bt_save:
                if (TextUtils.isEmpty(etEditData.getText().toString())) {
                    ToastUtil.showShortToast("请输入读数");
                    return;
                }
                UserInfo userInfo = dataList.get(position);
                doSave(userInfo);
                doEdit(userInfo);
                ToastUtil.showShortToast("保存成功");
                break;
            case R.id.bt_up:
                if (position <= 0) {
                    ToastUtil.showShortToast("上一户没有了");
                } else {
                    position = position - 1;
                    loadInfo(dataList.get(position));
                    etEditData.setText("");
                    userInfoDetailAdapter.notifyDataSetChanged();
                    currentUserInfo = dataList.get(position);
                }
                refreshTagByStatus(currentUserInfo);
                //上一户
                break;
            case R.id.bt_next:
                //下一户
                if (position >= dataList.size() - 1) {
                    ToastUtil.showShortToast("没有下一户了");
                } else {
                    position = position + 1;
                    loadInfo(dataList.get(position));
                    etEditData.setText("");
                    userInfoDetailAdapter.notifyDataSetChanged();
                    currentUserInfo = dataList.get(position);
                }
                refreshTagByStatus(currentUserInfo);
                break;
            default:
                break;
        }
    }


    private void doSave(UserInfo userInfo) {
        if (!infoArrayList.isEmpty()) {
            int lastData = userInfo.getLastdata();
            String time = TimeUtil.getCurrentTime();
            userInfo.curreaddate = time;
            try {
                userInfo.curdata = Integer.parseInt(etEditData.getText().toString());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            userInfo.curyl = userInfo.curdata - lastData;
            userInfo.state = STATE_PEOPLE_RECORDING;
            getSqOperator().updateData(userInfo.filename, userInfo.accountnum, userInfo.curdata, userInfo.curyl, userInfo.state, time, userInfo.channel);
            infoArrayList.get(6).setValue(String.valueOf(userInfo.curdata));
            infoArrayList.get(7).setValue(String.valueOf(userInfo.curyl));
            infoArrayList.get(8).setValue(getStateLabel(userInfo.state));
            infoArrayList.get(9).setValue(userInfo.firmCode);
            infoArrayList.get(12).setValue(TimeUtil.getTimeString(time));
            userInfoDetailAdapter.notifyDataSetChanged();
        } else {
            ToastUtil.showShortToast("数据有误无法保存!");
        }

    }


    @Override
    public void finish() {
        Intent data = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_EDIT_DATA_LIST, editList);
        bundle.putInt(EXTRA_CURRENT_TAG, tag);
        data.putExtra(EXTRA_BUNDLE, bundle);
        setResult(REQUEST_CODE_DETAIL_INFO, data);
        super.finish();
    }

    /**
     * 筛选通道号相同的数据集合
     *
     * @param channelNumber
     */
    private void getSameChannelData(String channelNumber) {
        if (EmiStringUtil.isNotEmpty(channelNumber)) {
            for (int i = dataList.size() - 1; i >= 0; i--) {
                if (!channelNumber.equals(dataList.get(i).channelNumber)) {
                    dataList.remove(i);
                }
            }
        }
    }

    private void doEdit(UserInfo userInfo) {
        int index = checkEditData(userInfo);
        if (index > -1) {
            editList.set(index, userInfo);
        } else {
            editList.add(userInfo);
        }
    }

    /**
     * 判断编辑的数据集合是否已经保存
     */
    private int checkEditData(UserInfo userInfo) {
        for (int i = 0; i < editList.size(); i++) {
            if (editList.get(i).equals(userInfo)) {
                return i;
            }
        }
        return -1;
    }


    private void takePhoto() {
        //获取路径
        String path = EmiConfig.EMI_PHOTO_PATH + File.separator;
        String photoFileName;//定义文件名
        photoFileName = STRING_DEFAULT_IMAGE_NAME;
        if (!TextUtils.isEmpty(currentUserInfo.useraddr)) {
            photoFileName = currentUserInfo.useraddr;
        }
        photoFile = new File(path, photoFileName);
        //文件夹不存在
        if (!photoFile.getParentFile().exists()) {
            photoFile.getParentFile().mkdirs();
        }
        Uri imageUri;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            //拍照的照片路径
            imageUri = Uri.fromFile(photoFile);
        } else {
            imageUri = FileProvider.getUriForFile(context, getResources().getString(R.string.provider_authorities), photoFile);
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        // 7.0加入访问权限
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //保存照片
        if (requestCode == REQUEST_CODE_TAKE_PHOTO && resultCode == RESULT_OK) {
            Resources resources = this.getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            doSavePhoto(getImageUri(), width, height);
        }
    }


    /**
     * 通过uri获取图片文件
     *
     * @param uri
     */
    public File getImageFileFormUri(Uri uri, String imagePath) {
        InputStream initialStream = null;
        try {
            initialStream = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] buffer = new byte[0];
        try {
            if (initialStream != null) {
                buffer = new byte[initialStream.available()];
                initialStream.read(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        File targetFile = new File(imagePath);
        OutputStream outStream = null;
        try {
            outStream = new FileOutputStream(targetFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            if (outStream != null) {
                outStream.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return targetFile;
    }


    /**
     * 保存图片
     */
    private void createImageFile(Bitmap bp) {
        String imageName;
        if (TextUtils.isEmpty(currentUserInfo.accountnum)) {
            imageName = STRING_DEFAULT_IMAGE_NAME;
        } else {
            imageName = currentUserInfo.useraddr;
        }
        File file = new File(EmiConfig.EMI_PHOTO_PATH + File.separator + imageName + IMG_JPG);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //添加时间水印
            Bitmap newBp = addWaterMark(bp);
            // 把数据写入文件
            if (newBp != null) {
                newBp.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                fos.flush();
                fos.close();
                notifyFileExplore(file.getPath());
                if (!newBp.isRecycled()) {
                    newBp.recycle();
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * 添加信息水印
     *
     * @param bitmap
     */
    private Bitmap addWaterMark(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        // 获取原始图片与水印图片的宽与高
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        Canvas mCanvas = new Canvas(newBitmap);
        // 往位图中开始画入src原始图片
        mCanvas.drawBitmap(bitmap, 0, 0, null);
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
        //添加文字
        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setDither(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = currentUserInfo.username + "-" + sdf.format(new Date(System.currentTimeMillis()));
        textPaint.setColor(Color.RED);
        int textSize = DisplayUtil.sp2px(context, 12);
        textPaint.setTextSize(textSize);
        int width = mCanvas.getWidth();
        int height = mCanvas.getHeight();
        int imageHeightDip = DisplayUtil.px2dip(context, height);
        float contentDip = (imageHeightDip / 10) * 9;
        float y = DisplayUtil.dip2px(context, contentDip);
        if (!TextUtils.isEmpty(currentUserInfo.useraddr)) {
            mCanvas.drawText("位置信息:" + currentUserInfo.useraddr, (float) width / 2, y, textPaint);
        }
        mCanvas.drawText("用户编号：" + currentUserInfo.accountnum + " 水表地址:" + currentUserInfo.meteraddr, width / 2, y + textSize, textPaint);
        mCanvas.drawText(time, width / 2, y + textSize * 2, textPaint);
        mCanvas.save();
        mCanvas.restore();
        return newBitmap;
    }


    public Bitmap getBitmapFromFile(File dst, int width, int height) {
        if (null != dst && dst.exists()) {
            BitmapFactory.Options opts = null;
            if (width > 0 && height > 0) {
                opts = new BitmapFactory.Options();
                //设置inJustDecodeBounds为true后，decodeFile并不分配空间，此时计算原始图片的长度和宽度
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(dst.getPath(), opts);
                // 计算图片缩放比例
                final int minSideLength = Math.min(width, height);
                opts.inSampleSize = computeSampleSize(opts, minSideLength, width * height);
                opts.inJustDecodeBounds = false;
            }
            try {
                return BitmapFactory.decodeFile(dst.getPath(), opts);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    private void showMoreFunctionMenu() {
        if (emiPopupMenu != null && !emiPopupMenu.isShowing()) {
            emiPopupMenu.showAsDropDown(titleView.getRightView(), -80, 0);
        }
    }

    @SuppressWarnings("unchecked")
    private void initPopMenu() {
        emiPopupMenu = new CustomEmiMenu.Builder<>(context, new IconMenuAdapter())
                .addItem(new IconPowerMenuItem(ContextCompat.getDrawable(context, R.mipmap.ic_take_photo), STRING_TAKE_PHOTO))
                .setLifecycleOwner(this)
                .setOnMenuItemClickListener(this)
                .setAnimation(MenuAnimation.FADE)
                .setMenuRadius(10f)
                .setMenuShadow(10f)
                .build();
        if (ipmTag == null) {
            ipmTag = new IconPowerMenuItem(ContextCompat.getDrawable(context, R.mipmap.ic_edit_file), STRING_TAG);
        }
        if (currentUserInfo == null) {
            ToastUtils.showToastNormal("未获取到用户信息");
            return;
        }
        boolean isTag = getSqOperator().checkMeterTag(currentUserInfo.filename, currentUserInfo.accountnum, currentUserInfo.meteraddr);
        if (isTag) {
            ipmTag.setIcon(ContextCompat.getDrawable(context, R.mipmap.ic_star_uncheck));
        } else {
            ipmTag.setIcon(ContextCompat.getDrawable(context, R.mipmap.ic_star_check));
        }
        emiPopupMenu.addItem(ipmTag);
        if (EmiUtils.isEnableModifyFile()) {
            emiPopupMenu.addItem(new IconPowerMenuItem(ContextCompat.getDrawable(context, R.mipmap.ic_edit_file), "修改文件"));
        }
    }

    @Override
    public void onItemClick(int position, IconPowerMenuItem item) {
        switch (item.getTitle()) {
            case "修改文件":
                closePopup();
                skipEditFileActivity();
                break;
            case STRING_TAKE_PHOTO:
                closePopup();
                takePhoto();
                break;
            case STRING_TAG:
                saveTagByStatus(currentUserInfo);
                refreshTagByStatus(currentUserInfo);
                break;
            case STRING_TAG_CANCEL:
                saveTagByStatus(currentUserInfo);
                refreshTagByStatus(currentUserInfo);
                break;
            default:
                closePopup();
                break;
        }

    }

    private void closePopup() {
        if (emiPopupMenu != null) {
            emiPopupMenu.dismiss();
        }
    }

    public int computeSampleSize(BitmapFactory.Options options,
                                 int minSideLength, int maxNumOfPixels) {
        int defaultSize = 8;
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);
        int roundedSize;
        if (initialSize <= defaultSize) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    /**
     * 计算initialSampleSize
     *
     * @param options
     * @param minSideLength
     * @param maxNumOfPixels
     * @return
     */
    private int computeInitialSampleSize(BitmapFactory.Options options,
                                         int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math
                .floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    /**
     * 根据Uri保存图片到指定文件夹
     *
     * @param imageUri
     */
    private void doSavePhoto(final Uri imageUri, final int width, final int height) {
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                String imageTempPath = EmiConfig.TempPath + "/" + "temp" + IMG_JPG;
                File imageFile = getImageFileFormUri(imageUri, imageTempPath);
                if (imageFile != null && imageFile.isFile()) {
                    Bitmap bitmap = getBitmapFromFile(imageFile, width, height);
                    if (bitmap != null) {
                        createImageFile(bitmap);
                    }
                }
                boolean isSuccess = FileUtil.deleteFile(imageTempPath);
                boolean isDelete = FileUtil.deleteFile(photoFile);
                if (isSuccess && isDelete) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showShortToast("保存成功");
                        }
                    });
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private Uri getImageUri() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.N ?
                Uri.fromFile(photoFile) :
                FileProvider.getUriForFile(context, getResources().getString(R.string.provider_authorities), photoFile);
    }


    private void refreshTagByStatus(UserInfo userInfo) {
        if (userInfo == null || ipmTag == null) {
            return;
        }
        if (getSqOperator().checkMeterTag(userInfo.filename, userInfo.accountnum, userInfo.meteraddr)) {
            ipmTag.setIcon(ContextCompat.getDrawable(context, R.mipmap.ic_star_uncheck));
            ipmTag.setTitle(STRING_TAG_CANCEL);
        } else {
            ipmTag.setIcon(ContextCompat.getDrawable(context, R.mipmap.ic_star_check));
            ipmTag.setTitle(STRING_TAG);
        }
        emiPopupMenu.refresh();
    }


    private void saveTagByStatus(UserInfo userInfo) {
        if (userInfo == null) {
            return;
        }
        boolean tag = getSqOperator().checkMeterTag(userInfo.filename, userInfo.accountnum, userInfo.meteraddr);
        if (tag) {
            getSqOperator().updateMeterTag(userInfo.filename, userInfo.accountnum, userInfo.meteraddr, false);
            ToastUtils.showToastNormal("您取消了对用户:" + userInfo.accountnum + "的标记");
        } else {
            getSqOperator().updateMeterTag(userInfo.filename, userInfo.accountnum, userInfo.meteraddr, true);
            ToastUtils.showToastNormal("用户编号:" + userInfo.accountnum + "标记成功");
        }
    }


}
