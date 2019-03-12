package com.emi.emireading.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.emi.emireading.EmiReadingApplication;
import com.emi.emireading.R;
import com.emi.emireading.adpter.CommonSelectEmiAdapter;
import com.emi.emireading.adpter.DataAdapter;
import com.emi.emireading.adpter.ListViewAdapter;
import com.emi.emireading.adpter.UserInfoDetailEmiAdapter;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.common.PreferenceUtils;
import com.emi.emireading.core.threadpool.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.config.UrlConstants;
import com.emi.emireading.core.db.SQLiteHelper;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.request.OkHttpUtils;
import com.emi.emireading.core.request.response.BaseJsonResponseHandler;
import com.emi.emireading.core.request.response.ToastUtils;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.FileUtil;
import com.emi.emireading.core.utils.TimeUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.DetailInfo;
import com.emi.emireading.entities.EmiNetData;
import com.emi.emireading.entities.ExportStrategy;
import com.emi.emireading.entities.LoadStrategy;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.listener.OnProgressListener;
import com.emi.emireading.log.EmiLog;
import com.emi.emireading.service.CreateReaderExcelFileService;
import com.emi.emireading.ui.debug.SingleMeterDebugActivity;
import com.emi.emireading.ui.export.ExportExcel;
import com.emi.emireading.ui.export.ExportTxt;
import com.emi.emireading.ui.load.LoadTxtStrategy;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;
import com.emi.emireading.widget.view.dialog.CustomDialog;
import com.emi.emireading.widget.view.dialog.InputDialog;
import com.emi.emireading.widget.view.dialog.multidialog.EmiMultipleProgressDialog;
import com.emi.emireading.widget.view.emimenu.CustomEmiMenu;
import com.emi.emireading.widget.view.emimenu.IconMenuAdapter;
import com.emi.emireading.widget.view.emimenu.IconPowerMenuItem;
import com.emi.emireading.widget.view.emimenu.MenuAnimation;
import com.emi.emireading.widget.view.emimenu.OnMenuItemClickListener;

import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.emi.emireading.core.config.EmiConstants.ERROR_CODE;
import static com.emi.emireading.core.config.EmiConstants.EXCEPTION_NET_TIME_OUT;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_CURRENT_TAG;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_FILE_NAME;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_METER_FIRM_CODE;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_METER_ID;
import static com.emi.emireading.core.config.EmiConstants.HAS_EXPORT;
import static com.emi.emireading.core.config.EmiConstants.KEY_CURRENT_DATA;
import static com.emi.emireading.core.config.EmiConstants.KEY_READ_DATE;
import static com.emi.emireading.core.config.EmiConstants.KEY_USER_ADDRESS;
import static com.emi.emireading.core.config.EmiConstants.KEY_USER_ID;
import static com.emi.emireading.core.config.EmiConstants.ONE;
import static com.emi.emireading.core.config.EmiConstants.ONE_SECOND;
import static com.emi.emireading.core.config.EmiConstants.PREF_SKIP_TAG;
import static com.emi.emireading.core.config.EmiConstants.PREF_WATER_WARNING_LINE;
import static com.emi.emireading.core.config.EmiConstants.STATE_ALL;
import static com.emi.emireading.core.config.EmiConstants.STATE_FAILED;
import static com.emi.emireading.core.config.EmiConstants.STATE_HAS_READ;
import static com.emi.emireading.core.config.EmiConstants.STATE_NORMAL;
import static com.emi.emireading.core.config.EmiConstants.STATE_NOT_UPLOAD;
import static com.emi.emireading.core.config.EmiConstants.STATE_NO_READ;
import static com.emi.emireading.core.config.EmiConstants.STATE_PEOPLE_RECORDING;
import static com.emi.emireading.core.config.EmiConstants.STATE_SUCCESS;
import static com.emi.emireading.core.config.EmiConstants.STATE_TAG;
import static com.emi.emireading.core.config.EmiConstants.STATE_UPLOAD_SUCCESS;
import static com.emi.emireading.core.config.EmiConstants.STATE_WARNING;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_DBF;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_EXCEL;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_TXT;
import static com.emi.emireading.core.config.EmiExportConstants.INIT_CAPACITY;
import static com.emi.emireading.core.config.UrlConstants.HOST;
import static com.emi.emireading.core.config.UrlConstants.REQUEST_DATA;
import static com.emi.emireading.core.config.UrlConstants.UPLOAD_METER_DATA;
import static com.emi.emireading.core.config.UrlConstants.URL_SERVICE_HOST;
import static com.emi.emireading.ui.CollectorCommunicationActivity.SKIP_TAG_COLLECTOR_COMMUNICATION;
import static com.emi.emireading.ui.ConcentratorActivity.SKIP_TAG_CONCENTRATOR_COMMUNICATION;
import static com.emi.emireading.ui.UserInfoDetailActivity.EXTRA_BUNDLE;
import static com.emi.emireading.ui.UserInfoDetailActivity.EXTRA_EDIT_DATA_LIST;
import static com.emi.emireading.ui.debug.WriteDataToDeviceActivity.SKIP_TAG_CONCENTRATOR_COMMUNICATION_68;
import static com.emi.emireading.ui.debug.WriteDataToDeviceActivitySplitPackage68.SKIP_TAG_CONCENTRATOR_COMMUNICATION_SPLIT_PACKAGE_68;
import static com.emi.emireading.widget.view.dialog.multidialog.EmiMultipleProgressDialog.Style.PIE_DETERMINATE;
import static java.lang.Integer.parseInt;


/**
 * @author :zhoujian
 * @description : 抄表查询和导出
 * @company :翼迈科技
 * @date 2018年03月07日下午 03:36
 * @Email: 971613168@qq.com
 */

public class MeterQueryActivity extends BaseActivity implements View.OnClickListener, OnMenuItemClickListener<IconPowerMenuItem> {
    private Context mContext;
    private ArrayList<String> dirNameList = new ArrayList<>();
    private ArrayList<String> fileNameList = new ArrayList<>();

    /**
     * 用户编号集合（用于数据查重）
     */
    private ArrayList<String> userIdList = new ArrayList<>();
    /**
     * 水表地址集合（用于数据查重）
     */
    private ArrayList<String> meterIdList = new ArrayList<>();

    private ArrayList<DetailInfo> detailInfoArrayList = new ArrayList<>();
    private TextView tvChooseFile;
    private LinearLayout linearInfo;
    private String mChooseDirName;
    private ArrayList<UserInfo> userInfoUIArrayList;
    private ArrayList<UserInfo> tempArrayList;
    private ArrayList<UserInfo> uploadList;
    private DataAdapter dataAdapter;
    private ListView dataListView;
    private int initSize = 200;
    private PopupWindow pop;
    private ListView popListView;
    private int progress;
    private final static int MSG_FINISH_CODE = 0;
    public static final int REQUEST_CODE_DETAIL_INFO = 1;
    private static final int MSG_SHOW_DIALOG = 2;
    private static final int MSG_CLOSE_DIALOG = 7;
    private static final int MSG_EXPORT_PROGRESS = 3;
    private static final int MSG_NOTIFY_DIALOG = 4;
    private static final int REQUEST_CODE_SINGLE_DEBUG = 6;
    private static final int MSG_UPDATE_PROGRESS = 8;
    private static final int MSG_TOAST_INFO = 1001;
    private static final int MSG_SHOW_DUPLICATE_INFO = 1002;

    private CustomEmiMenu emiPopupMenu;
    private EmiMultipleProgressDialog dialog;
    private View view;
    private TextView tvNoData;
    private EditText editTextSearch;
    private int filterWaterUsage = 0;
    private ImageView ivExport;
    /**
     * 异常
     */
    private Button btnAbnormal;
    private Button btnUploadSuccess;
    private Button btnNoUpload;
    private Button btnNoRead;
    private Button btnTag;
    private int currentTag;
    /**
     * 正常
     */
    private Button btnNormalSelect;
    private String mFileName;
    private String currentFileName;
    private ExportStrategy mExportStrategy;
    private boolean isShowPeopleRecord;
    private DecimalFormat decimalFormat = new DecimalFormat("######0");
    private double allRequestSize;
    private boolean uploadEnable;
    private boolean requestFinish;
    private boolean interrupt;
    private int mFilePosition;
    /**
     * 成功
     */
    private ArrayList<UserInfo> successList = new ArrayList<>();
    /**
     * 失败
     */
    private ArrayList<UserInfo> failedList = new ArrayList<>();
    /**
     * 补录
     */
    private ArrayList<UserInfo> peopleRecordList = new ArrayList<>();
    /**
     * 异常
     */
    private ArrayList<UserInfo> abnormalList = new ArrayList<>();

    /**
     * 未抄
     */
    private ArrayList<UserInfo> noReadList = new ArrayList<>();
    /**
     * 未上传
     */
    private ArrayList<UserInfo> noUploadList = new ArrayList<>();

    /**
     * 已上传
     */
    private ArrayList<UserInfo> hasUploadList = new ArrayList<>();

    /**
     * 标记集合
     */
    private ArrayList<UserInfo> tagList = new ArrayList<>();
    private double requestCount = 0;
    /**
     * 将数据源拆分进行分段上传
     */
    private static final int SPLIT_DATA_SIZE = 1;

    private String mExportPath;

    private MyHandler mHandler = new MyHandler(this);

    private List<EmiNetData> mEmiNetDataList = new ArrayList<>();
    private int limitUsage;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_query;
    }

    @Override
    protected void initIntent() {
        mContext = this;
        sqLiteHelper = new SQLiteHelper(EmiReadingApplication.getAppContext());
        limitUsage = PreferenceUtils.getInt(PREF_WATER_WARNING_LINE, 50);
    }

    @Override
    protected void initUI() {
        initView();
    }

    @Override
    protected void initData() {
      /*  ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                //经检测，该控件初始化比较耗时，为避免卡顿，建议放在子线程初始化
//                initPopMenu();
            }
        });*/
        initPopMenu();
        dirNameList = new ArrayList<>();
        userInfoUIArrayList = new ArrayList<>();
        dataAdapter = new DataAdapter(mContext, userInfoUIArrayList);
        dataListView.setAdapter(dataAdapter);
        EmiConfig.isShowCreateDateDialog = EmiUtils.isShowCreateDate();
        isShowPeopleRecord = EmiUtils.isShowPeopleRecord();
        editTextSearch.addTextChangedListener(new MyTextWatcher());
        dataListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                EmiConstants.userInfoArrayList = userInfoUIArrayList;
                Intent intent = new Intent(mContext, UserInfoDetailActivity.class);
                intent.putExtra(EmiConstants.EXTRA_POSITION, position);
                intent.putExtra(EXTRA_FILE_NAME, mFileName);
                intent.putExtra(EmiConstants.EXTRA_CURRENT_TAG, currentTag);
                startActivityForResult(intent, REQUEST_CODE_DETAIL_INFO);
            }
        });
        dataAdapter.setOnFilterListener(new DataAdapter.OnFilterListener() {
            @Override
            public void filterResult(ArrayList<UserInfo> filterDataList) {
                userInfoUIArrayList = filterDataList;
            }
        });
        dataListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                doSkipSingleDebugActivity(userInfoUIArrayList, position);
                return true;
            }
        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && view == null) {
            initPop();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sp:
                if (pop.isShowing()) {
                    pop.dismiss();
                } else {
                    pop.showAsDropDown(tvChooseFile);
                }
                if (dirNameList.isEmpty()) {
                    ToastUtil.showShortToast("没有数据");
                    return;
                }
                break;
            case R.id.bt_normal:
                //全部
                clearSearch();
                currentTag = EmiConstants.STATE_ALL;
                showDataByState(EmiConstants.STATE_ALL);
                break;
            case R.id.bt_unormal:
                //异常
                clearSearch();
                currentTag = EmiConstants.STATE_WARNING;
                showDataByState(EmiConstants.STATE_WARNING);
                break;
            case R.id.bt_fail:
                clearSearch();
                currentTag = EmiConstants.STATE_FAILED;
                showDataByState(EmiConstants.STATE_FAILED);
                break;
            case R.id.btnExport:
                doExport();
                break;
            case R.id.btnNoRead:
                clearSearch();
                currentTag = EmiConstants.STATE_NO_READ;
                showDataByState(EmiConstants.STATE_NO_READ);
                break;
            case R.id.bt_upload_failed:
                clearSearch();
                currentTag = EmiConstants.STATE_NOT_UPLOAD;
                showDataByState(EmiConstants.STATE_NOT_UPLOAD);
                LogUtil.d("未上传的数据集合长度：" + noUploadList.size());
                break;
            case R.id.bt_has_upload:
                clearSearch();
                currentTag = EmiConstants.STATE_UPLOAD_SUCCESS;
                showDataByState(EmiConstants.STATE_UPLOAD_SUCCESS);
                break;
            case R.id.btnNormalSelect:
                clearSearch();
                currentTag = STATE_HAS_READ;
                showDataByState(STATE_HAS_READ);
                break;
            case R.id.ivExport:
                showMoreFunctionMenu();
                break;
            case R.id.ivBack:
                finish();
                break;
            case R.id.btnTag:
                clearSearch();
                currentTag = STATE_TAG;
                showDataByState(STATE_TAG);
                break;
            default:
                break;
        }
    }


    private void initView() {
        tvChooseFile = findViewById(R.id.sp);
        btnAbnormal = findViewById(R.id.bt_unormal);
        dataListView = findViewById(R.id.cr_lv_info);
        btnNormalSelect = findViewById(R.id.btnNormalSelect);
        linearInfo = findViewById(R.id.linear_info);
        tvNoData = findViewById(R.id.tv_data);
        editTextSearch = findViewById(R.id.et_search);
        btnUploadSuccess = findViewById(R.id.bt_has_upload);
        btnNoRead = findViewById(R.id.btnNoRead);
        btnTag = findViewById(R.id.btnTag);
        findViewById(R.id.btnExport).setOnClickListener(this);
        findViewById(R.id.bt_fail).setOnClickListener(this);
        findViewById(R.id.ivBack).setOnClickListener(this);
        btnNoUpload = findViewById(R.id.bt_upload_failed);
        findViewById(R.id.btnNoRead).setOnClickListener(this);
        findViewById(R.id.btnNormalSelect).setOnClickListener(this);
        tvChooseFile.setOnClickListener(this);
        findViewById(R.id.bt_normal).setOnClickListener(this);
        btnAbnormal.setOnClickListener(this);
        btnNormalSelect.setOnClickListener(this);
        btnUploadSuccess.setOnClickListener(this);
        btnNoUpload.setOnClickListener(this);
        btnNoRead.setOnClickListener(this);
        btnTag.setOnClickListener(this);
        ivExport = findViewById(R.id.ivExport);
        ivExport.setOnClickListener(this);
    }

    private void isShowEmptyViewByData() {
        if (userInfoUIArrayList.isEmpty()) {
            linearInfo.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
        } else {
            linearInfo.setVisibility(View.VISIBLE);
            tvNoData.setVisibility(View.GONE);
        }
    }

    private void initPop() {
        view = LayoutInflater.from(this).inflate(R.layout.pop, null);
        popListView = view.findViewById(R.id.listView);
        pop = new PopupWindow(view, tvChooseFile.getMeasuredWidth(), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        pop.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.color.chose_bg));
        //设置焦点为可点击
        //可以试试设为false的结果
        pop.setFocusable(true);
        //将window视图显示在myButton下面
        fileNameList = getTempNameList(getTempFileNameList());
        loadDirNameList();
        LogUtil.d("获取到的txt文件：" + dirNameList.size());
        popListView.setAdapter(new ListViewAdapter(mContext, dirNameList));
        popListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showLoadDialog();
                clearData();
                mChooseDirName = dirNameList.get(position);
                mFilePosition = position;
                currentTag = STATE_ALL;
                doSelect(getSelectFileNameList(mChooseDirName));
                tvChooseFile.setText(mChooseDirName);
                clearSearch();
                pop.dismiss();
            }
        });
    }


    private void showDataByState(int state) {
        if (tempArrayList != null) {
            userInfoUIArrayList.clear();
            switch (state) {
                case STATE_ALL:
                    userInfoUIArrayList.addAll(tempArrayList);
                    break;
                case STATE_FAILED:
                    userInfoUIArrayList.addAll(failedList);
                    break;
                case STATE_PEOPLE_RECORDING:
                    userInfoUIArrayList.addAll(peopleRecordList);
                    break;
                case STATE_NO_READ:
                    userInfoUIArrayList.addAll(noReadList);
                    break;
                case STATE_SUCCESS:
                    userInfoUIArrayList.addAll(successList);
                    break;
                case STATE_WARNING:
                    userInfoUIArrayList.addAll(abnormalList);
                    break;
                case STATE_UPLOAD_SUCCESS:
                    userInfoUIArrayList.addAll(hasUploadList);
                    break;
                case STATE_HAS_READ:
                    userInfoUIArrayList.addAll(successList);
                    userInfoUIArrayList.addAll(abnormalList);
                    userInfoUIArrayList.addAll(peopleRecordList);
                    userInfoUIArrayList.addAll(failedList);
                    break;
                case STATE_NOT_UPLOAD:
                    userInfoUIArrayList.addAll(noUploadList);
                    break;
                case STATE_TAG:
                    userInfoUIArrayList.addAll(tagList);
                    break;
                default:
                    break;
            }
            dataAdapter.notifyDataSetChanged();
            isShowEmptyViewByData();
        } else {
            ToastUtil.showShortToast("请先选择文件");
        }

    }


    /**
     * 判断数据库是否有该文件名
     *
     * @param fileName
     */
    private boolean checkTempDataFromSQ(String fileName) {
        LogUtil.d(TAG, "需要查询数据库的文件名：" + fileName);
        return getSqOperator().checkFileIsExist(fileName);
    }


    private ArrayList<String> getTempNameList(ArrayList<String> fileNameList) {
        ArrayList<String> dirNameList = new ArrayList<>();
        for (int i = 0; i < fileNameList.size(); i++) {
            //获取带后缀的文件名
            dirNameList.add(fileNameList.get(i));
        }
        for (int i = dirNameList.size() - 1; i >= 0; i--) {
            if (TextUtils.isEmpty(dirNameList.get(i))) {
                dirNameList.remove(i);
            }
        }
        return removeSameData(dirNameList);
    }


    private ArrayList<String> removeSameData(ArrayList<String> dataList) {
        for (int i = 0; i < dataList.size(); i++) {
            for (int j = i + 1; j < dataList.size(); j++) {
                if (dataList.get(i).equals(dataList.get(j))) {
                    dataList.remove(j);
                    j--;
                }
            }
        }
        return dataList;
    }


    @Override
    public void onItemClick(int position, IconPowerMenuItem item) {
        switch (item.getTitle()) {
            case "筛选补录":
                //筛选补录
                selectPeopleRecord();
                break;
            case "统计查询":
                //统计查询
                showAllCount();
                break;
            case "数据查重":
                //数据查重
                analysis();
                break;
            case "集中器通讯A":
                //采集器通讯
                collectorCommunication();
                break;
            case "上传数据":
                //                uploadMeterData();
                //                test();
                //                concentratorCommunication68();
                //                concentratorCommunicationSplitPackage68();
                break;
            case "集中器通讯B":
                concentratorCommunication();
                break;
            case "集中器通讯C":
                concentratorCommunicationSplitPackage68();
                break;
            case "集中器通讯D":
                concentratorCommunicationSplitPackage68Old();
                break;
            default:
                break;
        }
        emiPopupMenu.dismiss();
    }

    private void demo() {
        String uploadUrl = "http://192.168.0.111:8080/newemi/mobile/checkVersion";
        HashMap<String, String> params = new HashMap<>(initSize);
        params.put("versionCode", "1");
        OkHttpUtils.getInstance().post(mContext, uploadUrl, params, new BaseJsonResponseHandler() {
            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                //                updateProgress();
                requestFinish = true;
                LogUtil.i("返回的数据：" + response.toString());
                List<EmiNetData> list = resolveServerCallbackData(response.toString());
                LogUtil.d("返回的数据大小：" + list.size());
                mEmiNetDataList.addAll(list);
            }

            @Override
            public void onError(int statusCode, String errorMsg) {
                uploadEnable = false;
                requestFinish = true;
                LogUtil.e("errorMsg：" + errorMsg);
                if (errorMsg.contains(EXCEPTION_NET_TIME_OUT)) {
                    sendToastMsg("网络请求超时");
                } else {
                    sendToastMsg("服务器异常");
                }
            }
        });

    }

    private static class MyHandler extends Handler {
        WeakReference<Activity> mWeakReference;

        private MyHandler(Activity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final MeterQueryActivity activity = (MeterQueryActivity) mWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_UPDATE_PROGRESS:
                        activity.updateProgress(msg.arg1);
                        break;
                    case MSG_CLOSE_DIALOG:
                        activity.closeDialog();
                        break;
                    case MSG_SHOW_DIALOG:
                        activity.showExportDialog("正在导出,请稍后...");
                        break;
                    case MSG_FINISH_CODE:
                        LogUtil.i("获取到的数据：" + activity.userInfoUIArrayList.size());
                        LogUtil.i("获取的activity.currentTag：" + activity.currentTag);
                        activity.closeDialog();
                        activity.updateData();
                        activity.showDataByState(activity.currentTag);
                        break;
                    case ERROR_CODE:
                        ToastUtil.showShortToast("查询异常");
                        activity.closeDialog();
                        break;
                    case MSG_EXPORT_PROGRESS:
                        LogUtil.d("导出进度：" + msg.arg1);
                        activity.updateProgress(msg.arg1);
                        break;
                    case MSG_NOTIFY_DIALOG:
                        activity.updateDialogTitle("该文件检测到有已抄数据,正在同步数据库...");
                        break;
                    case MSG_SHOW_DUPLICATE_INFO:
                        activity.showDuplicateDialog();
                        activity.closeDialog();
                        break;
                    case MSG_TOAST_INFO:
                        ToastUtil.showShortToast((String) msg.obj);
                        activity.closeDialog();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void updateData() {
        userInfoUIArrayList.clear();
        userInfoUIArrayList.addAll(tempArrayList);
        isShowEmptyViewByData();
        dataAdapter.notifyDataSetChanged();
    }

    private void clearData() {
        userInfoUIArrayList.clear();
        dataAdapter.notifyDataSetChanged();
    }


    /**
     * 根据选中的dirName查找的对应的fileNameList
     *
     * @param dirName
     */
    private ArrayList<String> getSelectFileNameList(String dirName) {
        String fileName;
        String queryDirName;
        ArrayList<String> tempFileList = getTempFileNameList();
        ArrayList<String> fileNameList = new ArrayList<>();
        LogUtil.d(TAG, "getSelectFileNameList--->dirName= " + dirName);
        for (int i = 0; i < tempFileList.size(); i++) {
            //获取到转换之前带后缀的文件名
            fileName = tempFileList.get(i);
            LogUtil.i(TAG, "getSelectFileNameList--->fileName= " + fileName);
            //根据fileName 去数据库查询对应的dirName
            queryDirName = getSqOperator().queryDirName(fileName);
            LogUtil.w(TAG, "getSelectFileNameList--->queryDirName= " + queryDirName);
            if (dirName.equals(queryDirName)) {
                //如果相同，dirName和当前的fileName对应
                fileNameList.add(fileName);
            }
        }
        return fileNameList;
    }


    private void sendEmptyMsg(int what) {
        mHandler.sendEmptyMessage(what);
    }

    private void sendMsg(int what, int args) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        message.arg1 = args;
        mHandler.sendMessage(message);
    }

    private void sendErrorMsg() {
        mHandler.sendEmptyMessage(ERROR_CODE);
    }


    private void doSelect(ArrayList<String> fileNameList) {
        switch (fileNameList.size()) {
            case 0:
                ToastUtil.showShortToast("无匹配数据");
                closeDialog();
                break;
            case 1:
                LogUtil.d(TAG, "fileNameList----->名称：" + fileNameList.get(0));
                queryDataFromSQ(fileNameList.get(0));
                break;
            default:
                showSelectTxtFileDialog(fileNameList);
                break;
        }

    }

    private class QueryDataRunnable implements Runnable {
        private QueryDataRunnable(String fileName) {
            this.fileName = fileName;
            mFileName = fileName;
            currentFileName = fileName;
        }

        private String fileName;

        @Override
        public void run() {
            try {
                tempArrayList = (ArrayList<UserInfo>) getSqOperator().find(fileName);
                if (SUFFIX_TXT.equals(EmiConfig.FILE_SUFFIX)) {
                    boolean hasExport = getSqOperator().checkHasExport(fileName);
                    LogUtil.i("是否已经导出过：" + hasExport);
                    mExportPath = getTxtExportFileName();
                    LogUtil.i("是否已经导出过文件路径：" + mExportPath);
                    if (!hasExport && FileUtil.checkFileExsit(mExportPath)) {
                        sendEmptyMsg(MSG_NOTIFY_DIALOG);
                        //这里执行加载文件数据操作
                        updateUserInfo(readExportTxtFile(mExportPath));
                        getSqOperator().updateData(tempArrayList);
                    }
                }
                loadAllKindData(tempArrayList);
                sendEmptyMsg(MSG_FINISH_CODE);
            } catch (Exception e) {
                LogUtil.e("QueryDataRunnable()---->" + e.toString());
                sendErrorMsg();
            }
        }

    }


    private void showLoadDialog() {
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundResource(R.drawable.spin_animation);
        AnimationDrawable drawable = (AnimationDrawable) imageView.getBackground();
        drawable.start();
        dialog = EmiMultipleProgressDialog.create(this)
                .setCustomView(imageView)
                .setLabel("正在查询数据...");
        dialog.show();
    }

    private void showDialog(String message) {
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundResource(R.drawable.spin_animation);
        AnimationDrawable drawable = (AnimationDrawable) imageView.getBackground();
        drawable.start();
        dialog = EmiMultipleProgressDialog.create(this)
                .setCustomView(imageView)
                .setLabel(message);
        dialog.show();
    }

    private void closeDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        EmiConstants.userInfoArrayList = null;
        interrupt = true;
        requestFinish = true;
        if (emiPopupMenu != null) {
            List<IconPowerMenuItem> list = (List<IconPowerMenuItem>) emiPopupMenu.getItemList();
            IconPowerMenuItem iconPowerMenuItem;
            if (list != null) {
                for (int i = list.size() - 1; i >= 0; i--) {
                    iconPowerMenuItem = list.get(i);
                    if (iconPowerMenuItem.getIcon() instanceof BitmapDrawable) {
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) iconPowerMenuItem.getIcon();
                        Bitmap bitmap = bitmapDrawable.getBitmap();
                        if (bitmap != null && !bitmap.isRecycled()) {
                            bitmap.recycle();
                            bitmap = null;
                        }
                    }
                    emiPopupMenu.removeItem(iconPowerMenuItem);
                    System.gc();
                }
            }
        }

    }


    private void clearSearch() {
        editTextSearch.setText("");
    }

    private class MyTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            dataAdapter.getFilter().filter(editTextSearch.getText().toString());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_DETAIL_INFO:
                if (data != null) {
                    Bundle bundle = data.getBundleExtra(EXTRA_BUNDLE);
                    doEditCallBck((ArrayList<UserInfo>) bundle.getSerializable(EXTRA_EDIT_DATA_LIST));
                    showDataByState(bundle.getInt(EXTRA_CURRENT_TAG));
                    LogUtil.d(TAG, "获取的tag=" + bundle.getInt(EXTRA_CURRENT_TAG));
                    refreshList(mFilePosition);
                }
                break;
            default:
                break;
        }
    }


    private void loadAllKindData(ArrayList<UserInfo> userInfoArrayList) {
        abnormalList.clear();
        failedList.clear();
        successList.clear();
        peopleRecordList.clear();
        noUploadList.clear();
        noReadList.clear();
        hasUploadList.clear();
        tagList.clear();
        for (UserInfo userInfo : userInfoArrayList) {
            switch (userInfo.state) {
                case STATE_WARNING:
                    abnormalList.add(userInfo);
                    break;
                case STATE_FAILED:
                    failedList.add(userInfo);
                    break;
                case STATE_SUCCESS:
                    successList.add(userInfo);
                    break;
                case STATE_PEOPLE_RECORDING:
                    if (isShowPeopleRecord) {
                        peopleRecordList.add(userInfo);
                    } else {
                        successList.add(userInfo);
                    }
                    break;
                case STATE_NO_READ:
                    noReadList.add(userInfo);
                    break;
                default:
                    break;
            }
            switch (userInfo.uploadState) {
                case STATE_NOT_UPLOAD:
                    noUploadList.add(userInfo);
                    break;
                case STATE_UPLOAD_SUCCESS:
                    hasUploadList.add(userInfo);
                    break;
                default:
                    break;
            }
            if (userInfo.meterTag == STATE_TAG) {
                tagList.add(userInfo);
            }
        }

    }


    private void doEditCallBck(ArrayList<UserInfo> editDataList) {
        for (UserInfo editUserInfo : editDataList) {
            boolean flag = abnormalList.remove(editUserInfo);
            boolean flag1 = failedList.remove(editUserInfo);
            noReadList.remove(editUserInfo);
            int index = checkEdit(editUserInfo);
            LogUtil.d("abnormalList--移除结果：" + flag);
            LogUtil.i("failedList--移除结果：" + flag1);
            if (index > -1) {
                LogUtil.w("是否存在？，选择替换索引为" + index + "的数据");
                if (isShowPeopleRecord) {
                    peopleRecordList.set(index, editUserInfo);
                } else {
                    successList.set(index, editUserInfo);
                }
            } else {
                LogUtil.i("是否存在？，添加的数据:" + editUserInfo.accountnum + "读数修改为：" + editUserInfo.curdata);
                if (isShowPeopleRecord) {
                    peopleRecordList.add(editUserInfo);
                } else {
                    successList.add(editUserInfo);
                }
            }
        }
    }


    private int checkEdit(UserInfo userInfo) {
        ArrayList<UserInfo> userInfoArrayList;
        if (isShowPeopleRecord) {
            userInfoArrayList = peopleRecordList;
        } else {
            userInfoArrayList = successList;
        }
        for (int i = 0; i < userInfoArrayList.size(); i++) {
            if (userInfoArrayList.get(i).equals(userInfo)) {
                return i;
            }
        }
        return -1;
    }


    private class ExportRunnable implements Runnable {
        private String exportPath;
        private ExportStrategy strategy;
        private List<UserInfo> userInfoList;

        public ExportRunnable(ExportStrategy strategy, List<UserInfo> dataList, String exportPath) {
            this.strategy = strategy;
            this.exportPath = exportPath;
            this.userInfoList = dataList;
        }

        @Override
        public void run() {
            //先获取加载策略；
            exportByStrategy(strategy, userInfoList, exportPath);
        }
    }

    /**
     * 加载导出策略
     *
     * @param json
     */
    private ExportStrategy getExportStrategy(String json) {
        LitePal.deleteAll(ExportStrategy.class);
        List<ExportStrategy> strategies = LitePal.findAll(ExportStrategy.class);
        if (strategies != null && strategies.size() > 0) {
            LogUtil.d("数据库有模板");
            return strategies.get(0);
        } else {
            ExportStrategy exportStrategy = new ExportStrategy();
            HashMap<String, String> userDefineNameMap = new HashMap<>(INIT_CAPACITY);
            userDefineNameMap.put(KEY_USER_ID, "用户编号");
            userDefineNameMap.put(KEY_CURRENT_DATA, "本次读数");
            userDefineNameMap.put(KEY_USER_ADDRESS, "用户地址");
            userDefineNameMap.put(KEY_READ_DATE, "抄表日期");
            exportStrategy.setTableNameJson(JSON.toJSON(userDefineNameMap).toString());
            LogUtil.w("生成的json：" + JSON.toJSON(userDefineNameMap).toString());
            LogUtil.w("数据库无模板");
            exportStrategy.setCityName("翼迈");
            exportStrategy.setExportFormatJson(json);
            exportStrategy.setExportFileType(SUFFIX_EXCEL);
            exportStrategy.save();
            return exportStrategy;
        }
    }

    private ArrayList<UserInfo> getExportDataListByState(int state) {
        LogUtil.d("当前导出类型：" + state);
        ArrayList<UserInfo> userInfoList = new ArrayList<>();
        switch (state) {
            //失败
            case STATE_FAILED:
                userInfoList.addAll(failedList);
                return userInfoList;
            //人工
            case STATE_PEOPLE_RECORDING:
                userInfoList.addAll(peopleRecordList);
                return userInfoList;
            //异常
            case STATE_WARNING:
                userInfoList.addAll(abnormalList);
                return userInfoList;
            //成功
            case STATE_SUCCESS:
                userInfoList.addAll(successList);
                return userInfoList;
            //全部
            case STATE_ALL:
                userInfoList.addAll(tempArrayList);
                LogUtil.d("数据长度：" + userInfoList.size());
                return userInfoList;
            case STATE_NORMAL:
                userInfoList.addAll(successList);
                userInfoList.addAll(abnormalList);
                userInfoList.addAll(peopleRecordList);
                LogUtil.d("数据长度：" + userInfoList.size());
                return userInfoList;
            case STATE_NO_READ:
                userInfoList.addAll(noReadList);
                LogUtil.d("数据长度：" + userInfoList.size());
                return userInfoList;
            case STATE_HAS_READ:
                userInfoList.addAll(successList);
                userInfoList.addAll(abnormalList);
                userInfoList.addAll(peopleRecordList);
                userInfoList.addAll(failedList);
                return userInfoList;
            default:
                LogUtil.i("数据长度：" + userInfoList.size());
                userInfoList.addAll(tempArrayList);
                return userInfoList;
        }

    }


    private String getFileNameByState(String fileName) {
        StringBuilder sb = new StringBuilder("");
        String fileNameNoSuffix = FileUtil.clearFileSuffix(fileName);
        String suffix = FileUtil.getFileSuffix(fileName);
        sb.append(fileNameNoSuffix);
        switch (EmiConfig.EXPORT_TYPE) {
            case STATE_FAILED:
                sb.append("(");
                sb.append("失败");
                sb.append(")");
                sb.append(suffix);
                return sb.toString();
            //人工
            case STATE_PEOPLE_RECORDING:
                sb.append("(");
                sb.append("补录");
                sb.append(")");
                sb.append(suffix);
                return sb.toString();
            //异常
            case STATE_WARNING:
                sb.append("(");
                sb.append("异常");
                sb.append(")");
                sb.append(suffix);
                return sb.toString();
            //成功
            case STATE_SUCCESS:
                sb.append("(");
                sb.append("正常");
                sb.append(")");
                sb.append(suffix);
                return sb.toString();
            //全部
            case STATE_ALL:
                sb.append(suffix);
                return sb.toString();
            case STATE_NORMAL:
                sb.append("(");
                sb.append("正常-异常-补录");
                sb.append(")");
                sb.append(suffix);
                return sb.toString();
            case STATE_NO_READ:
                sb.append("(");
                sb.append("未抄");
                sb.append(")");
                sb.append(suffix);
                return sb.toString();
            case STATE_HAS_READ:
                sb.append("(");
                sb.append("已抄");
                sb.append(")");
                sb.append(suffix);
                return sb.toString();
            default:
                sb.append(suffix);
                return sb.toString();
        }
    }


    /**
     * Excel用户自定义导出
     *
     * @param dataList
     * @param exportPath
     */
    private void exportExcelByUserDefine(ExportStrategy exportStrategy, List<UserInfo> dataList, final String exportPath) {
        if (mFileName != null) {
            ExportExcel exportExcel = new ExportExcel(exportStrategy, exportPath, dataList);
            exportExcel.setOnProgressListener(new OnProgressListener() {
                @Override
                public void onProgress(int progress) {
                    LogUtil.i("当前进度：" + progress);
                    sendMsg(MSG_EXPORT_PROGRESS, progress);
                }

                @Override
                public void onFinish(Object dataList, String fileName) {
                    ToastUtil.showShortToast("导出成功");
                    notificationFile(mContext, exportPath);
                    closeDialog();
                }

                @Override
                public void onError(String errorMsg) {
                    ToastUtil.showShortToast(errorMsg);
                    closeDialog();
                    LogUtil.e("异常:" + errorMsg);
                }
            });
            exportExcel.userDefinedExport();
        } else {
            LogUtil.e("文件名为null");
        }
    }

    private void exportByStrategy(ExportStrategy exportStrategy, List<UserInfo> dataList, String exportPath) {
        switch (exportStrategy.getExportFileType()) {
            case SUFFIX_EXCEL:
                if (mChooseDirName != null) {
                    exportExcelByUserDefine(exportStrategy, dataList, exportPath);
                }
                break;
            case SUFFIX_TXT:
            /*    if (mChooseDirName != null) {
                    exportTxt(dataList, false);
                }*/
                break;
            case SUFFIX_DBF:
                break;
            default:
                break;
        }


    }


    /**
     * 过滤用水量
     *
     * @param userInfoList
     * @param waterUsage
     * @return
     */
    private ArrayList<UserInfo> filterWaterUsage(List<UserInfo> userInfoList, int waterUsage) {
        LogUtil.w(TAG, "过滤的用水量：" + waterUsage);
        ArrayList<UserInfo> resultList = new ArrayList<>();
        for (UserInfo userInfo : userInfoList) {
            LogUtil.i(TAG, "userInfo用水量：" + userInfo.curyl);
            if (userInfo.curyl > waterUsage) {
                resultList.add(userInfo);
            }
        }
        return resultList;
    }

    /**
     * 过滤用水量
     */
    private void showFilterDialog() {
        final InputDialog.Builder builder = new InputDialog.Builder(mContext);
        builder.setTitle("输入需要过滤的用水量");
        builder.setMessage("");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    filterWaterUsage = parseInt(builder.editText.getText().toString());
                } catch (NumberFormatException e) {
                    filterWaterUsage = -Integer.MAX_VALUE;
                }
                dialog.dismiss();
                export();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * 导出文件
     */
    private void export() {
        showExportDialog("正在导出文件...");
        mExportStrategy = EmiConfig.exportStrategy;
        ArrayList<UserInfo> dataList = getExportDataListByState(EmiConfig.EXPORT_TYPE);
        if (EmiConfig.isFilter) {
            dataList = filterWaterUsage(dataList, filterWaterUsage);
        }
        switch (mExportStrategy.getExportFileType()) {
            case SUFFIX_EXCEL:
                mFileName = getFileNameByState(currentFileName);
                final String exportPath = EmiConfig.GeneratePath + "/" + FileUtil.clearFileSuffix(mFileName) + SUFFIX_EXCEL;
                ThreadPoolManager.EXECUTOR.execute(new ExportRunnable(mExportStrategy, dataList, exportPath));
                break;
            case SUFFIX_TXT:
                if (!TextUtils.isEmpty(mFileName)) {
                    mExportPath = getTxtExportFileName();
                    LogUtil.i(TAG, "文件路径：" + mExportPath);
                    if (EmiConfig.isShowCreateDateDialog) {

                        showIsCreateDateDialog(dataList);
                    } else {
                        exportTxt(dataList, false);
                    }
                } else {
                    ToastUtil.showShortToast("导出路径不正确");
                    closeDialog();
                }
                break;
            default:
                ToastUtil.showShortToast("未获取导出到导出的文件类型");
                break;
        }
        createNewReaderExcelFile();
    }

    private void showExportDialog(String text) {
        dialog = EmiMultipleProgressDialog.create(mContext);
        dialog.setCancellable(false).setStyle(PIE_DETERMINATE).setMaxProgress(100)
                .setLabel(text);
        dialog.show();
    }

    private void updateProgress(int progress) {
        if (dialog != null) {
            dialog.setProgress(progress);
        }
    }

    private void sendProgressMsg(int progress) {
        sendMsg(MSG_UPDATE_PROGRESS, progress);
    }

    private boolean fileIsExist(String filePath) {
        return new File(filePath).exists();
    }


    private void doExport() {
        if (mFileName == null) {
            ToastUtil.showShortToast("请先选择文件");
            return;
        }
        if (EmiConfig.isFilter) {
            showFilterDialog();
        } else {
            export();
        }
    }

    /**
     * 上传表读数
     */
    private void uploadMeterData() {
        uploadEnable = true;
        uploadList = new ArrayList<>();
        uploadList.addAll(successList);
        uploadList.addAll(failedList);
        uploadList.addAll(abnormalList);
        uploadList.addAll(peopleRecordList);
        //去除重复数据
        removeSameUser(uploadList);
        LogUtil.w(TAG, "数据集合长度:" + uploadList.size());
        if (uploadList.size() > 0) {
            final List<List<UserInfo>> requestList = EmiStringUtil.splitList(uploadList, SPLIT_DATA_SIZE);
            allRequestSize = requestList.size();
            LogUtil.i(TAG, "分割后的数据集合长度:" + requestList.size());
            showProgressDialog();
            requestCount = 0;
        } else {
            ToastUtil.showShortToast("没有要上传的数据");
        }
    }


    private void showProgressDialog() {
        dialog = EmiMultipleProgressDialog.create(mContext)
                .setCancellable(false).setStyle(PIE_DETERMINATE).setMaxProgress(100)
                .setAutoDismiss(true);
        dialog.setLabel("数据正在上传到服务器...");
        dialog.show();
    }

    /**
     * 跳转到统计页面
     */
    private void skipActivityCount() {
        if (mFileName != null) {
            Intent intent = new Intent(mContext, ActivityCount.class);
            intent.putExtra(EXTRA_FILE_NAME, mFileName);
            startActivity(intent);
        } else {
            ToastUtil.showShortToast("请选择文件");
        }
    }

    private void loadDirNameList() {
        dirNameList.clear();
        String name;
        for (String fileName : fileNameList) {
            name = getSqOperator().queryDirName(fileName);
            if (!TextUtils.isEmpty(name)) {
                LogUtil.d(TAG, "fileName=" + fileName);
                dirNameList.add(name);
            }
        }
        dirNameList = removeSameData(dirNameList);
    }


    private void showSelectTxtFileDialog(final ArrayList<String> fileNameList) {
        CommonSelectEmiAdapter selectFileAdapter = new CommonSelectEmiAdapter(fileNameList);
        CommonSelectDialog.Builder builder = new CommonSelectDialog.Builder(MeterQueryActivity.this);
        builder.setTitle("请选择要查询的文件");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                tvChooseFile.setText("选择小区");
                closeDialog();
            }
        });
        final CommonSelectDialog dialog = builder.create();
        selectFileAdapter.setOnItemClickListener(new BaseEmiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
                mFilePosition = position;
                queryDataFromSQ(fileNameList.get(position));
                dialog.dismiss();
                clearSearch();
            }
        });
        selectFileAdapter.bindToRecyclerView(builder.recyclerView);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        builder.setAdapter(selectFileAdapter);
        dialog.show();
    }


    private void queryDataFromSQ(String fileName) {
        ThreadPoolManager.EXECUTOR.execute(new QueryDataRunnable(fileName));
    }


    private void exportTxt(List<UserInfo> dataList, boolean isCreateDate) {
        if (mFileName != null && EmiConfig.exportStrategy != null) {
            ExportTxt exportTxt = new ExportTxt(EmiConfig.exportStrategy, mExportPath, dataList);
            exportTxt.setOnProgressListener(new OnProgressListener() {
                @Override
                public void onProgress(int progress) {
                    sendMsg(MSG_EXPORT_PROGRESS, progress);
                }

                @Override
                public void onFinish(Object dataList, String filePath) {
                    ToastUtil.showShortToast("导出成功");
                    LogUtil.w(TAG, "导出成功---filePath：" + filePath);
                    notificationFile(mContext, filePath);
                    closeDialog();
                }

                @Override
                public void onError(String errorMsg) {
                    ToastUtil.showShortToast(errorMsg);
                    closeDialog();
                    LogUtil.e("异常:" + errorMsg);
                }
            });
            exportTxt.exportTxtFile(isCreateDate);
        } else {
            LogUtil.e("文件名为null");
            ToastUtil.showShortToast("未找到导出策略!");
        }
    }


    private void showIsCreateDateDialog(final List<UserInfo> dataList) {
        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        builder.setTitle("提示");
        builder.setMessage("文件结果中需要生成日期吗?");
        builder.setPositiveButton("需要", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exportTxt(dataList, true);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("不需要", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exportTxt(dataList, false);
                dialog.dismiss();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                closeDialog();
            }
        });
        builder.create().show();
    }

    public void updateDialogTitle(String text) {
        dialog.setLabel(text);
    }

    /**
     * 获取TXT文件的导出路径
     */
    private String getTxtExportFileName() {
        StringBuilder sb = new StringBuilder("");
        sb.append(FileUtil.clearFileSuffix(mFileName));
        sb.setCharAt(sb.length() - 1, ONE);
        return EmiConfig.GeneratePath + "/" + sb.toString() + SUFFIX_TXT;
    }

    /**
     * 读取已经导出的TXT文件
     *
     * @param mFilePath
     */
    private List<UserInfo> readExportTxtFile(String mFilePath) {
        List<UserInfo> userInfoList = new ArrayList<>();
        int indexMeterId = 0;
        int indexLastData = 1;
        int indexCurrentData = 2;
        int indexCurrentUsage = 3;
        LoadTxtStrategy loadTxtStrategy = new LoadTxtStrategy();
        List<String> list = loadTxtStrategy.getTxtData(mFilePath);
        String[] lineContentArray;
        UserInfo userInfo;
        int line = PreferenceUtils.getInt(PREF_WATER_WARNING_LINE, 50);
        for (String lineTxt : list) {
            lineContentArray = lineTxt.split(EmiConfig.SPLIT_CHAR);
            if (lineContentArray.length > indexCurrentUsage) {
                userInfo = new UserInfo();
                userInfo.accountnum = lineContentArray[indexMeterId];
                userInfo.lastdata = EmiStringUtil.stringToInt(lineContentArray[indexLastData]);
                userInfo.curdata = EmiStringUtil.stringToInt(lineContentArray[indexCurrentData]);
                userInfo.curyl = EmiStringUtil.stringToInt(lineContentArray[indexCurrentUsage]);
                userInfo.state = STATE_SUCCESS;
                if (userInfo.curyl < 0 || userInfo.curyl >= line) {
                    userInfo.state = STATE_WARNING;
                }
                userInfo.curreaddate = TimeUtil.getCurrentTime();
                userInfoList.add(userInfo);
            }
        }
        return userInfoList;
    }

    /**
     * 更新数据库中数据
     *
     * @param userInfoList
     */
    private void updateUserInfo(List<UserInfo> userInfoList) {
        int size = tempArrayList.size();
        UserInfo currentUserInfo;
        for (UserInfo userInfo : userInfoList) {
            for (int i = size - 1; i >= 0; i--) {
                currentUserInfo = tempArrayList.get(i);
                if (userInfo.accountnum.equals(currentUserInfo.accountnum)) {
                    currentUserInfo.curdata = userInfo.curdata;
                    currentUserInfo.curyl = currentUserInfo.curdata - currentUserInfo.lastdata;
                    currentUserInfo.channel = currentUserInfo.channelNumber;
                    userInfo.channelNumber = currentUserInfo.channelNumber;
                    userInfo.channel = currentUserInfo.channel;
                    currentUserInfo.curreaddate = userInfo.curreaddate;
                    currentUserInfo.hasExport = HAS_EXPORT;
                    currentUserInfo.state = userInfo.state;
                    if (userInfo.state == STATE_WARNING && currentUserInfo.curyl < limitUsage && currentUserInfo.curyl > -1) {
                        currentUserInfo.state = STATE_SUCCESS;
                    }
                    break;
                }
            }
        }
    }


    @Override
    protected void onResume() {
        loadExportStrategy();
        super.onResume();
    }


    private void doSkipSingleDebugActivity(List<UserInfo> userInfoList, int position) {
        if (userInfoList != null && !userInfoList.isEmpty()) {
            UserInfo userInfo = userInfoList.get(position);
            if (userInfo.meteraddr != null && userInfo.firmCode != null) {
                Intent intent = new Intent();
                intent.setClass(mContext, SingleMeterDebugActivity.class);
                intent.putExtra(EXTRA_METER_ID, userInfo.meteraddr);
                intent.putExtra(EXTRA_METER_FIRM_CODE, userInfo.firmCode);
                startActivityForResult(intent, REQUEST_CODE_SINGLE_DEBUG);
            }
        }
    }


    private void selectPeopleRecord() {
        clearSearch();
        currentTag = EmiConstants.STATE_PEOPLE_RECORDING;
        showDataByState(EmiConstants.STATE_PEOPLE_RECORDING);
    }


    private void showMoreFunctionMenu() {
        if (emiPopupMenu != null && !emiPopupMenu.isShowing()) {
            emiPopupMenu.showAsDropDown(ivExport, -80, 0);
        }
    }

    @SuppressWarnings("unchecked")
    private void initPopMenu() {
        emiPopupMenu = new CustomEmiMenu.Builder<>(mContext, new IconMenuAdapter())
                .addItem(new IconPowerMenuItem(getBitmapDrawable(R.mipmap.icon_select_record), "筛选补录"))
                .addItem(new IconPowerMenuItem(getBitmapDrawable(R.mipmap.icon_data_count, 8), "统计查询"))
                .addItem(new IconPowerMenuItem(getBitmapDrawable(R.mipmap.icon_select_ducupile), "数据查重"))
                .setLifecycleOwner(this)
                .setOnMenuItemClickListener(this)
                .setAnimation(MenuAnimation.FADE)
                .setMenuRadius(10f)
                .setMenuShadow(10f)
                .build();
        if (EmiConfig.IS_SUPPORT_UPLOAD) {
            emiPopupMenu.addItem(new IconPowerMenuItem(getBitmapDrawable(R.mipmap.icon_upload_data), "上传数据"));
        }
        if (EmiUtils.isProfessionalMode()) {
            emiPopupMenu.addItem(new IconPowerMenuItem(getBitmapDrawable(R.mipmap.icon_concentrator_68), "集中器通讯A"));
            emiPopupMenu.addItem(new IconPowerMenuItem(getBitmapDrawable(R.mipmap.icon_concentrator), "集中器通讯B"));
            emiPopupMenu.addItem(new IconPowerMenuItem(getBitmapDrawable(R.mipmap.icon_concentrator_68), "集中器通讯C"));
            emiPopupMenu.addItem(new IconPowerMenuItem(getBitmapDrawable(R.mipmap.icon_concentrator_68), "集中器通讯D"));
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                emiPopupMenu.setAdapter();
            }
        });
    }

    /**
     * 统计
     */
    private void showAllCount() {
        if (EmiUtils.isEmpty(mFileName)) {
            ToastUtil.showShortToast("请先选择文件");
            return;
        }
        skipActivityCount();
    }

    /**
     * 采集器通讯
     */
    private void collectorCommunication() {
        if (TextUtils.isEmpty(mFileName)) {
            ToastUtil.showShortToast("请先选择数据文件");
            return;
        }
        Intent intent = new Intent();
        intent.setClass(mContext, ChannelListActivity.class);
        intent.putExtra(EXTRA_FILE_NAME, mFileName);
        intent.putExtra(PREF_SKIP_TAG, SKIP_TAG_COLLECTOR_COMMUNICATION);
        startActivity(intent);
    }


    /**
     * 集中器通讯
     */
    private void concentratorCommunication() {
        if (TextUtils.isEmpty(mFileName)) {
            ToastUtil.showShortToast("请先选择数据文件");
            return;
        }
        Intent intent = new Intent();
        intent.setClass(mContext, ChannelListActivity.class);
        intent.putExtra(EXTRA_FILE_NAME, mFileName);
        intent.putExtra(PREF_SKIP_TAG, SKIP_TAG_CONCENTRATOR_COMMUNICATION);
        startActivity(intent);
    }


    /**
     * 数据写入到设备68协议（分包）
     */
    private void concentratorCommunicationSplitPackage68() {
        if (TextUtils.isEmpty(mFileName)) {
            ToastUtil.showShortToast("请先选择数据文件");
            return;
        }
        Intent intent = new Intent();
        intent.setClass(mContext, ChannelListActivity.class);
        intent.putExtra(EXTRA_FILE_NAME, mFileName);
        intent.putExtra(PREF_SKIP_TAG, SKIP_TAG_CONCENTRATOR_COMMUNICATION_SPLIT_PACKAGE_68);
        startActivity(intent);
    }

    /**
     * 数据写入到设备68协议（安庆最早时期）（分包）
     */
    private void concentratorCommunicationSplitPackage68Old() {
        if (TextUtils.isEmpty(mFileName)) {
            ToastUtil.showShortToast("请先选择数据文件");
            return;
        }
        Intent intent = new Intent();
        intent.setClass(mContext, ChannelListActivity.class);
        intent.putExtra(EXTRA_FILE_NAME, mFileName);
        intent.putExtra(PREF_SKIP_TAG, SKIP_TAG_CONCENTRATOR_COMMUNICATION_68);
        startActivity(intent);
    }


    /**
     * 判断集合是否有重复数据
     */
    private Map<String, Integer> getDuplicateStringMap(List<String> valueList) {
        Map<String, Integer> map = new HashMap<>(10);
        //用于存放重复的元素的list
        for (String s : valueList) {
            //1:map.containsKey()   检测key是否重复
            if (map.containsKey(s)) {
                //获取重复的数据
                Integer num = map.get(s);
                map.put(s, num + 1);
            } else {
                map.put(s, 1);
            }
        }
        return map;
    }

    private List<DetailInfo> getDuplicateList() {
        meterIdList.clear();
        userIdList.clear();
        for (UserInfo userInfo : tempArrayList) {
            userIdList.add(userInfo.accountnum);
            meterIdList.add(userInfo.meteraddr);
        }
        List<DetailInfo> detailInfoList = new ArrayList<>();
        DetailInfo detailInfo;
        String label;
        int duplicateCount;
        Map<String, Integer> meterIdMap = getDuplicateStringMap(meterIdList);
        Map<String, Integer> userIdListMap = getDuplicateStringMap(userIdList);
        for (Map.Entry<String, Integer> entry : meterIdMap.entrySet()) {
            if (entry.getValue() > 1) {
                duplicateCount = entry.getValue() - 1;
                label = "水表地址" + "'" + entry.getKey() + "'" + "重复" + duplicateCount + "次";
                detailInfo = new DetailInfo(label, "");
                detailInfo.setLabelColor(R.color.blue);
                detailInfoList.add(detailInfo);
            }
        }
        for (Map.Entry<String, Integer> entry : userIdListMap.entrySet()) {
            if (entry.getValue() > 1) {
                duplicateCount = entry.getValue() - 1;
                label = "用户编号" + "'" + entry.getKey() + "'" + "重复" + duplicateCount + "次";
                detailInfo = new DetailInfo(label, "");
                detailInfo.setLabelColor(R.color.green);
                detailInfoList.add(detailInfo);
            }
        }
        return detailInfoList;
    }


    private void analysis() {
        if (TextUtils.isEmpty(mFileName)) {
            ToastUtil.showShortToast("请先选择文件");
            return;
        }
        showDialog("正在分析数据...");
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                detailInfoArrayList.clear();
                detailInfoArrayList.addAll(getDuplicateList());
                if (detailInfoArrayList.isEmpty()) {
                    sendToastMsg("无重复数据");
                } else {
                    sendEmptyMsg(MSG_SHOW_DUPLICATE_INFO);
                }
            }
        });
    }


    private void showDuplicateDialog() {
        UserInfoDetailEmiAdapter adapter = new UserInfoDetailEmiAdapter(detailInfoArrayList);
        CommonSelectDialog.Builder builder = new CommonSelectDialog.Builder(mContext);
        builder.setTitle("检测到以下数据存在重复");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final CommonSelectDialog commonDialog = builder.create();
        commonDialog.setCancelable(false);
        commonDialog.setCanceledOnTouchOutside(false);
        adapter.bindToRecyclerView(builder.recyclerView);
        builder.setAdapter(adapter);
        commonDialog.show();
    }

    private void sendToastMsg(String msg) {
        Message message = mHandler.obtainMessage();
        message.what = MSG_TOAST_INFO;
        message.obj = msg;
        mHandler.sendMessage(message);
    }

    private void updateProgress() {
        requestCount++;
        progress = Integer.parseInt(decimalFormat.format((requestCount / allRequestSize) * 100));
        if (dialog != null) {
            dialog.setProgress(progress);
        }
    }

    private int getCurrentProgress(int current) {
        return Integer.parseInt(decimalFormat.format((current / allRequestSize) * 100));
    }

    private EmiNetData getNetData(UserInfo userInfo) {
        EmiNetData emiNetData = new EmiNetData();
        if (userInfo != null) {
            emiNetData.setWmCode(userInfo.meteraddr);
            if (userInfo.state == EmiConstants.STATE_FAILED) {
                emiNetData.setData("FF");
            } else {
                emiNetData.setData(String.valueOf(userInfo.curdata));
            }
            emiNetData.setReadDate(userInfo.curreaddate);
            emiNetData.setCoCode(userInfo.channelNumber);
            emiNetData.setUserNo(userInfo.accountnum);
        }
        return emiNetData;
    }


    private void test() {
        uploadEnable = true;
        uploadList = new ArrayList<>();
        uploadList.addAll(successList);
        uploadList.addAll(failedList);
        uploadList.addAll(abnormalList);
        uploadList.addAll(peopleRecordList);
        removeSameUser(uploadList);
        if (uploadList.isEmpty()) {
            ToastUtil.showShortToast("没有需要上传的数据");
            return;
        }
        requestCount = 0;
        showProgressDialog();
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                final List<List<UserInfo>> requestList = EmiStringUtil.splitList(uploadList, SPLIT_DATA_SIZE);
                allRequestSize = requestList.size();
                mEmiNetDataList.clear();
                for (int i = 0; i < requestList.size(); i++) {
                    if (!interrupt) {
                        requestFinish = false;
                        sendProgressMsg(getCurrentProgress(i));
                        if (uploadEnable) {
                            EmiLog.i(TAG, "正在执行第" + i + 1 + "次网络请求");
                            requestServer(requestList.get(i));
                        }
                        while (!requestFinish) {
                            delays(ONE_SECOND);
                            EmiLog.d("正在等待相应...");
                        }
                        EmiLog.w(TAG, "已经跳出循环");
                    } else {
                        stopCurrentThread();
                        break;
                    }
                }
                detailInfoArrayList.clear();
                DetailInfo detailInfo;
                String label;
                String value;
                for (EmiNetData emiNetData : mEmiNetDataList) {
                    label = "用户编号:" + emiNetData.getUserNo();
                    value = "水表地址:" + emiNetData.getCoCode();
                    detailInfo = new DetailInfo(label, value);
                    detailInfoArrayList.add(detailInfo);
                }
                LogUtil.w("上传结束");
            }
        });
    }


    private void requestServer(List<UserInfo> userInfoList) {
        String uploadUrl = HOST + URL_SERVICE_HOST + UPLOAD_METER_DATA;
        JSONArray requestArray = new JSONArray();
        for (UserInfo userInfo : userInfoList) {
            requestArray.add(getNetData(userInfo));
        }
        HashMap<String, String> params = new HashMap<>(initSize);
        params.put(REQUEST_DATA, requestArray.toJSONString());
        OkHttpUtils.getInstance().post(mContext, uploadUrl, params, new BaseJsonResponseHandler() {
            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                updateProgress();
                requestFinish = true;
                LogUtil.i("返回的数据：" + response.toString());
                List<EmiNetData> list = resolveServerCallbackData(response.toString());
                LogUtil.d("返回的数据大小：" + list.size());
                mEmiNetDataList.addAll(list);
            }

            @Override
            public void onError(int statusCode, String errorMsg) {
                uploadEnable = false;
                requestFinish = true;
                LogUtil.e("errorMsg：" + errorMsg);
                if (errorMsg.contains(EXCEPTION_NET_TIME_OUT)) {
                    sendToastMsg("网络请求超时");
                } else {
                    sendToastMsg("服务器异常");
                }
            }
        });
    }


    private List<EmiNetData> resolveServerCallbackData(String callbackJson) {
        List<EmiNetData> emiNetDataList = new ArrayList<>();
        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(callbackJson);
        String code = jsonObject.getString("code");
        if (UrlConstants.REQUEST_SUCCESS.equalsIgnoreCase(code)) {
            return emiNetDataList;
        } else {
            com.alibaba.fastjson.JSONArray result = jsonObject.getJSONArray("result");
            return com.alibaba.fastjson.JSONObject.parseArray(result.toJSONString(), EmiNetData.class);
        }
    }


    private void showUploadResultDialog() {
        UserInfoDetailEmiAdapter adapter = new UserInfoDetailEmiAdapter(detailInfoArrayList);
        CommonSelectDialog.Builder builder = new CommonSelectDialog.Builder(mContext);
        builder.setTitle("以下" + detailInfoArrayList.size() + "条数据上传失败");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final CommonSelectDialog commonDialog = builder.create();
        commonDialog.setCancelable(false);
        commonDialog.setCanceledOnTouchOutside(false);
        adapter.bindToRecyclerView(builder.recyclerView);
        builder.setAdapter(adapter);
        commonDialog.show();
    }

    public BitmapDrawable getBitmapDrawable(int imageId, int inSampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imageId, options);
        return new BitmapDrawable(getResources(), bitmap);
    }

    public BitmapDrawable getBitmapDrawable(int imageId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imageId, options);
        return new BitmapDrawable(getResources(), bitmap);
    }


    private void createNewReaderExcelFile() {
        if (tempArrayList.isEmpty()) {
            return;
        }
        try {
            LoadStrategy loadStrategy = com.alibaba.fastjson.JSONObject.parseObject(tempArrayList.get(0).loadStrategyJson, LoadStrategy.class);
            if (loadStrategy != null && "翼迈".equals(loadStrategy.getCity())) {
                Intent i = new Intent(MeterQueryActivity.this, CreateReaderExcelFileService.class);
                i.putExtra(EmiConstants.EXTRA_FILE_NAME, tempArrayList.get(0).filename);
                startService(i);
            }
        } catch (Exception e) {
            if (EmiConfig.isDebug) {
                ToastUtils.showToastFailed(e.toString());
            }
            LogUtil.e("createNewReaderExcelFile--->" + e.toString());
        }
    }


    private void refreshList(int position) {
        queryDataFromSQ(fileNameList.get(position));
    }
}
