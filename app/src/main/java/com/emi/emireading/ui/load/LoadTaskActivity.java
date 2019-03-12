package com.emi.emireading.ui.load;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.emi.emireading.R;
import com.emi.emireading.adpter.CommonSelectEmiAdapter;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.common.PreferenceUtils;
import com.emi.emireading.core.threadpool.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.CommonViewHolder;
import com.emi.emireading.core.utils.FileUtil;
import com.emi.emireading.core.utils.TimeUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.FileEntity;
import com.emi.emireading.entities.SavedFileInfo;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.listener.OnProgressListener;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;
import com.emi.emireading.widget.view.dialog.CustomDialog;
import com.emi.emireading.widget.view.dialog.multidialog.EmiMultipleProgressDialog;

import org.litepal.LitePal;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.emi.emireading.core.config.EmiConfig.isDebug;
import static com.emi.emireading.core.config.EmiConstants.EMI_MERGE_FILE;
import static com.emi.emireading.core.config.EmiConstants.FILE_TYPE_EXCEL;
import static com.emi.emireading.core.config.EmiConstants.PREF_WATER_WARNING_LINE;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_DBF;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_EXCEL;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_TXT;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_XLS;
import static com.emi.emireading.widget.view.dialog.multidialog.EmiMultipleProgressDialog.Style.BAR_DETERMINATE;
import static com.emi.emireading.widget.view.dialog.multidialog.EmiMultipleProgressDialog.Style.PIE_DETERMINATE;

/**
 * @author :zhoujian
 * @description : 加载任务
 * @company :翼迈科技
 * @date 2018年03月06日下午 03:34
 * @Email: 971613168@qq.com
 */

public class LoadTaskActivity extends BaseActivity implements View.OnClickListener {
    private Context mContext;
    private boolean hasFile;
    private static final int MSG_ERROR_CODE = -1;
    private static final int PROGRESS_CODE = 102;
    private PopupWindow popupWindow;
    private TextView tvChooseArea;
    private List<String> dirNameList;
    private EmiMultipleProgressDialog progressDialog;
    private String choseFileName;
    private String chooseRealFileName = "";
    private EditText etDataFilter;
    private MyHandler mHandler = new MyHandler(this);

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_chose_area:
                dirNameList.clear();
                hasFile = checkFolderHasFile(EmiConfig.FILE_TYPE, EmiConfig.NeedFilePath);
                if (hasFile) {
                    showList();
                } else {
                    ToastUtil.showShortToast("文件夹没有所需文件");
                }
                break;
            case R.id.bt_load:
                if (TextUtils.isEmpty(choseFileName)) {
                    ToastUtil.showShortToast("请选择文件！");
                    return;
                }
                doLoad(choseFileName);
                break;
            case R.id.bt_save:
                editWaterWarningLine();
                ToastUtil.showShortToast("保存成功");
                break;
            default:
                break;
        }
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_loadtask;
    }

    @Override
    protected void initIntent() {
        mContext = this;
    }

    @Override
    protected void initUI() {
        initView();
    }

    @Override
    protected void initData() {
        dirNameList = new ArrayList<>();
        etDataFilter.setText(String.valueOf(PreferenceUtils.getInt(PREF_WATER_WARNING_LINE, 50)));
    }


    private void initView() {
        tvChooseArea = findViewById(R.id.tv_chose_area);
        tvChooseArea.setOnClickListener(this);
        etDataFilter = findViewById(R.id.et_data);
        findViewById(R.id.bt_load).setOnClickListener(this);
        findViewById(R.id.bt_save).setOnClickListener(this);
    }


    private boolean checkFolderHasFile(String fileType, String path) {
        boolean flag = false;
        switch (fileType) {
            case SUFFIX_TXT:
                flag = checkTxtFile(path);
                break;
            case FILE_TYPE_EXCEL:
                flag = checkExcelFile(path);
                break;
            case SUFFIX_DBF:
                flag = checkDbfFile(path);
                break;
            case SUFFIX_XLS:
                flag = checkExcelFile(path);
                break;
            default:
                break;
        }
        return flag;
    }


    /**
     * 检测文件夹下是否有txt文件
     *
     * @param path
     * @return
     */
    private boolean checkTxtFile(String path) {
        boolean flag = false;
        File rootFile = new File(path);
        File[] rootFiles = rootFile.listFiles();
        if (rootFiles == null) {
            return false;
        }
        for (File file : rootFiles) {
            if (file.isDirectory()) {
                flag = true;
                dirNameList.add(file.getName());
            }
        }
        return flag;
    }

    /**
     * 检测文件夹下是否有excel文件
     *
     * @param path
     * @return
     */
    private boolean checkExcelFile(String path) {
        boolean flag = false;
        File rootFile = new File(path);
        File[] rootFiles = rootFile.listFiles();
        if (rootFiles == null) {
            return false;
        }
        if (rootFiles.length > 0) {
            for (File file : rootFiles) {
                String fileName = file.getName();
                if (fileName.contains(SUFFIX_EXCEL)) {
                    dirNameList.add(fileName);
                    flag = true;
                }
            }
            return flag;
        }
        return false;
    }

    /**
     * 检测文件夹下是否有dbf文件
     *
     * @param path
     * @return
     */
    private boolean checkDbfFile(String path) {
        boolean flag = false;
        File rootFile = new File(path);
        File[] rootFiles = rootFile.listFiles();
        if (rootFiles != null && rootFiles.length > 0) {
            for (File file : rootFiles) {
                String fileName = file.getName();
                if (fileName.contains(SUFFIX_DBF)) {
                    dirNameList.add(fileName);
                    flag = true;
                }
            }
            return flag;
        }
        return flag;
    }


    public void showList() {
        View view = LayoutInflater.from(this).inflate(R.layout.pop, null);
        popupWindow = new PopupWindow(view, tvChooseArea.getMeasuredWidth(), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //设置焦点为可点击
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.color.chose_bg));
        //可以试试设为false的结果
        popupWindow.setFocusable(true);
        //将window视图显示在myButton下面
        popupWindow.showAsDropDown(tvChooseArea);
        ListView listView = view.findViewById(R.id.listView);
        listView.setAdapter(new ListViewAdapter(LoadTaskActivity.this, dirNameList));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tvChooseArea.setText(dirNameList.get(position));
                choseFileName = dirNameList.get(position);
                LogUtil.i(TAG, "文件名：" + choseFileName);
                popupWindow.dismiss();
            }
        });
    }


    private class ListViewAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<String> list;

        public ListViewAdapter(Context context, List<String> list) {
            super();
            this.inflater = LayoutInflater.from(context);
            this.list = list;
        }


        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.layout_item_area, null);
            }
            TextView tvName = CommonViewHolder.get(convertView, R.id.tvDirName);
            tvName.setText(list.get(position));
            return convertView;
        }
    }

    private void editWaterWarningLine() {
        PreferenceUtils.putInt(PREF_WATER_WARNING_LINE, Integer.parseInt(etDataFilter.getText().toString()));
    }


    /**
     * @param fileName
     */
    public void showForceLoadDialog(final String fileName, final String realName) {
        LogUtil.d("showForceLoadDialog--》" + realName);
        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        builder.setTitle("强制加载");
        builder.setMessage("检测到该文件已经加载，是否强制加载?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                deleteDataByFileName(realName);
                notifyFileExplore(EmiConfig.TempPath);
                doLoad(fileName);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                closeDialog();
                chooseRealFileName = "";
            }
        });
        builder.create().show();
    }


    /**
     * @param fileName
     * @return
     */
    private boolean deleteDataByFileName(String fileName) {
        if (fileName != null) {
            String fileNameNoSuffix = FileUtil.clearFileSuffix(fileName);
            LogUtil.w(TAG, "要删除的文件名：" + fileName);
            getSqOperator().delete(fileNameNoSuffix);
            String filePath = EmiConfig.TempPath + "/" + fileNameNoSuffix + SUFFIX_TXT;
            return FileUtil.deleteFile(filePath);
        } else {
            return false;
        }
    }


    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        super.onDestroy();
    }


    private static class MyHandler extends Handler {
        WeakReference<Activity> mWeakReference;

        private MyHandler(Activity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final LoadTaskActivity activity = (LoadTaskActivity) mWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case PROGRESS_CODE:
                        activity.updateProgress(msg.arg1);
                        break;
                    case MSG_ERROR_CODE:
                        activity.closeDialog();
                        if (isDebug) {
                            ToastUtil.showShortToast((String) msg.obj);
                        } else {
                            ToastUtil.showShortToast("加载异常");
                        }
                        break;
                    default:
                        ToastUtil.showShortToast("未匹配到该文件类型");
                        activity.closeDialog();
                        break;
                }
            }
        }
    }

    private void updateProgress(int progress) {
        if (progressDialog != null) {
            progressDialog.setProgress(progress);
        }
    }

    private void showLoadingDialog() {
        progressDialog = EmiMultipleProgressDialog.create(mContext)
                .setCancellable(false).setStyle(BAR_DETERMINATE).setMaxProgress(100)
                .setLabel("加载中...").setAutoDismiss(false);
        progressDialog.setLabel("正在解析文件");
        progressDialog.show();
    }

    private void closeDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void sendEmptyMsg(int what) {
        mHandler.sendEmptyMessage(what);
    }

    private void sendErrorMsg(String errorMsg) {
        Message message = mHandler.obtainMessage();
        message.what = MSG_ERROR_CODE;
        message.obj = errorMsg;
        mHandler.sendMessage(message);
    }

    private void loadExcelFile(final String filePath, final String fileName) {
        final LoadSaveExcelStrategy loadSaveExcelStrategy = new LoadSaveExcelStrategy();
        loadSaveExcelStrategy.setOnProgressListener(new ExcelStrategyCallBack());
        loadSaveExcelStrategy.readExcel(filePath, fileName);

    }

    private class ExcelStrategyCallBack implements OnProgressListener<ArrayList<UserInfo>> {
        /**
         * 以下回调全部在子线程执行，禁止UI操作
         */

        @Override
        public void onFinish(final ArrayList<UserInfo> dataList, final String fileName) {
            doSaveToSQ(fileName, dataList);
            closeDialog();
            showProgressDialog();
        }


        @Override
        public void onProgress(int progress) {
            sendProgress(progress);
        }


        @Override
        public void onError(String errorMsg) {
            if (isDebug) {
                LogUtil.d("异常信息"+errorMsg);
            } else {
                ToastUtil.showShortToast(getResources().getString(R.string.msg_load_error));
                LogUtil.d("异常信息"+errorMsg);
            }
            closeDialog();
        }


    }


    private void sendProgress(int progress) {
        Message message = mHandler.obtainMessage();
        message.what = PROGRESS_CODE;
        message.arg1 = progress;
        mHandler.sendMessage(message);
    }

    private void generateTxt(ArrayList<UserInfo> userInfoArrayList, String fileName) {
        if (userInfoArrayList != null) {
            CreateTxtFile createTxtFile = new CreateTxtFile(userInfoArrayList, fileName);
            createTxtFile.createTxtFile();
        }
    }


    private void showProgressDialog() {
        closeDialog();
        progressDialog = EmiMultipleProgressDialog.create(mContext)
                .setCancellable(false).setStyle(PIE_DETERMINATE).setMaxProgress(100)
                .setLabel("加载中...").setAutoDismiss(false);
        progressDialog.setLabel("正在保存到数据库...");
        progressDialog.show();
    }

    private void loadTXT(String filePath555, String filePathBook) {
        LoadTxtStrategy loadTxtStrategy = new LoadTxtStrategy(filePath555, filePathBook, choseFileName);
        loadTxtStrategy.setOnProgressListener(new OnProgressListener<ArrayList<UserInfo>>() {
            @Override
            public void onProgress(int progress) {
                sendProgress(progress);
            }

            @Override
            public void onFinish(ArrayList<UserInfo> userInfoArrayList, String fileName) {
                LogUtil.d(TAG, "合并后传回来的数据长度：" + userInfoArrayList.size());
                closeDialog();
                showProgressDialog();
                if (userInfoArrayList.size() > 0) {
                    LogUtil.d(TAG, "fileName = " + fileName);
                    doSaveToSQ(fileName, userInfoArrayList);
                } else {
                    ToastUtil.showShortToast("数据不匹配");
                    closeDialog();
                }
            }

            @Override
            public void onError(String errorMsg) {
                ToastUtil.showShortToast(errorMsg);
                closeDialog();
            }
        });
        loadTxtStrategy.loadTxt();
    }


    public void insertDataToSqlite(ArrayList<UserInfo> userInfoArrayList, OnProgressListener onProgressListener) {
        getSqOperator().setOnInsertListener(onProgressListener).insert(userInfoArrayList);
    }


    private void doSaveToSQ(final String fileName, final ArrayList<UserInfo> dataList) {
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                //生成txt文件
                LogUtil.d(TAG, "文件名称：" + fileName + ",datalist长度：" + dataList.size());
                UserInfo userInfo;
                if (dataList.isEmpty()) {
                    userInfo = new UserInfo();
                    userInfo.filename = fileName;
                } else {
                    userInfo = dataList.get(0);
                }
                LogUtil.d(TAG, "文件名称：loadStrategyJson长度:" +userInfo.loadStrategyJson.length());
                saveLoadInfo(userInfo);
                generateTxt(dataList, fileName);
                insertDataToSqlite(dataList, new OnProgressListener() {
                    @Override
                    public void onProgress(int progress) {
                        sendProgress(progress);
                    }

                    @Override
                    public void onFinish(Object dataList, String fileName) {
                        //加载成功
                        closeDialog();
                        ToastUtil.showShortToast("加载完成");
                    }

                    @Override
                    public void onError(String errorMsg) {
                        closeDialog();
                    }
                });
            }
        });
    }


    private void showSelectFileDialog(final ArrayList<String> fileNameList, final String mergePath) {
        CommonSelectEmiAdapter selectFileAdapter = new CommonSelectEmiAdapter(fileNameList);
        CommonSelectDialog.Builder builder = new CommonSelectDialog.Builder(mContext);
        builder.setTitle("请选择要抄表的文件");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                closeDialog();
                dialog.dismiss();
            }
        });
        final CommonSelectDialog deviceDialog = builder.create();
        selectFileAdapter.setOnItemClickListener(new BaseEmiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
                deviceDialog.dismiss();
                chooseRealFileName = fileNameList.get(position);
                String bookPath = EmiConfig.NeedFilePath + "/" + choseFileName + "/" + chooseRealFileName;
                String realName = FileUtil.getFileName(bookPath, false);
                realName = realName + SUFFIX_TXT;
                boolean isShowForceDialog = checkIsExist(realName);
                LogUtil.d(TAG, "选择的文件--->" + realName);
                if (isShowForceDialog) {
                    showForceLoadDialog(choseFileName, fileNameList.get(position));
                } else {
                    closeDialog();
                    showLoadingDialog();
                    loadTXT(mergePath, bookPath);
                    chooseRealFileName = "";
                }
            }
        });
        selectFileAdapter.bindToRecyclerView(builder.recyclerView);
        deviceDialog.setCancelable(false);
        deviceDialog.setCanceledOnTouchOutside(false);
        builder.setAdapter(selectFileAdapter);
        deviceDialog.show();
    }


    private boolean checkIsExist(String realName) {
        return getSqOperator().checkFileIsExist(realName);
    }


    private void doLoad(final String fileName) {
        final String filePath;
        closeDialog();
        showLoadingDialog();
        LogUtil.d(TAG, "解析的excel名：" + fileName);
        switch (EmiConfig.FILE_SUFFIX) {
            case SUFFIX_EXCEL:
                filePath = EmiConfig.NeedFilePath + "/" + fileName;
                if (checkIsExist(fileName)) {
                    showForceLoadDialog(fileName, fileName);
                } else {
                    if (isFile(filePath)) {
                        loadExcelFile(filePath, fileName);
                    } else {
                        ToastUtil.showShortToast("无效的文件路径");
                    }
                }
                break;
            case SUFFIX_TXT:
                //判断555文件夹下是否有对应555文件
                mergeTxt(fileName);
                break;
            default:
                break;
        }

    }

    /**
     * 获取某路径下所有txt表册文件
     *
     * @param dirPath
     * @return
     */
    private ArrayList<FileEntity> getTxtFileEntityList(String dirPath) {
        ArrayList<FileEntity> fileEntityArrayList = new ArrayList<>();
        LogUtil.i("getTxtFileEntityList---->dirPath" + dirPath);
        File file = new File(dirPath);
        File[] fileArray = file.listFiles();
        for (File currentFile : fileArray) {
            //判断是不是555.txt文件
            boolean isMergeFile = EMI_MERGE_FILE.equalsIgnoreCase(currentFile.getName());
            if (!isMergeFile && SUFFIX_TXT.contains(FileUtil.getFileSuffix(currentFile.getPath().toLowerCase()))) {
                FileEntity fileEntity = new FileEntity();
                fileEntity.fileName = FileUtil.clearFileSuffix(currentFile.getName()) + SUFFIX_TXT;
                fileEntity.filePath = currentFile.getPath();
                fileEntityArrayList.add(fileEntity);
            }
        }
        return fileEntityArrayList;
    }


    private void doLoadTxt(ArrayList<FileEntity> fileEntities, String mergePath) {
        switch (fileEntities.size()) {
            case 0:
                ToastUtil.showShortToast("未找到表册文件");
                closeDialog();
                break;
            case 1:
                chooseRealFileName = fileEntities.get(0).fileName;
                LogUtil.d("当前只有一个文件，路径为：" + EmiConfig.NeedFilePath + "/" + choseFileName + "/" + chooseRealFileName);
                if (checkIsExist(chooseRealFileName)) {
                    showForceLoadDialog(choseFileName, chooseRealFileName);
                } else {
                    loadTXT(mergePath, EmiConfig.NeedFilePath + "/" + choseFileName + "/" + chooseRealFileName);
                }
                chooseRealFileName = "";
                break;
            default:
                ArrayList<String> fileNameList = new ArrayList<>();
                for (FileEntity fileEntity : fileEntities) {
                    fileNameList.add(fileEntity.fileName);
                }
                if (TextUtils.isEmpty(chooseRealFileName)) {
                    showSelectFileDialog(fileNameList, mergePath);
                } else {
                    loadTXT(mergePath, EmiConfig.NeedFilePath + "/" + choseFileName + "/" + chooseRealFileName);
                    chooseRealFileName = "";
                }
                break;
        }
    }


    private void mergeTxt(String choseFileName) {
        String merge555DirPath;
        String bookDirPath;
        merge555DirPath = EmiConfig.EMI_MERGE_PATH + "/" + choseFileName;
        bookDirPath = EmiConfig.NeedFilePath + "/" + choseFileName;
        boolean mergeDirIsExist555 = isMergeFileExist(merge555DirPath);
        if (mergeDirIsExist555) {
            LogUtil.d(TAG, "555文件路径：" + merge555DirPath);
            LogUtil.d("555文件夹下有对应555文件");
            LogUtil.w("开始获取该路径的表册文件");
            doLoadTxt(getTxtFileEntityList(bookDirPath), merge555DirPath + "/" + EMI_MERGE_FILE);
        } else {
            //555文件夹中无对应文件，需要判断表册文件中是否存在555文件
            boolean bookDirIsExist555 = isMergeFileExist(bookDirPath);
            if (bookDirIsExist555) {
                doLoadTxt(getTxtFileEntityList(bookDirPath), bookDirPath + "/" + EMI_MERGE_FILE);
                LogUtil.d(TAG, "表册文件中的555目录：" + bookDirPath + "/" + EMI_MERGE_FILE);
            } else {
                ToastUtil.showShortToast("未检测到" + EMI_MERGE_FILE + "文件");
                closeDialog();
            }
        }
    }

    /**
     * 保存加载记录
     *
     * @param userInfo
     */
    private void saveLoadInfo(UserInfo userInfo) {
        if (userInfo == null || userInfo.filename == null) {
            return;
        }
        List<SavedFileInfo> list = LitePal.select("savedFileName")
                .where("savedFileName = ?", userInfo.filename)
                .find(SavedFileInfo.class);
        if (list != null && !list.isEmpty()) {
            LogUtil.e("检测到文件名存在");
            LitePal.deleteAll(SavedFileInfo.class, "savedFileName = ? ", userInfo.filename);
        }
        createSaveInfo(userInfo).save();
        LogUtil.i("文件已保存");
    }

    private SavedFileInfo createSaveInfo(UserInfo userInfo) {
        SavedFileInfo savedFileInfo = new SavedFileInfo();
        if (userInfo != null) {
            savedFileInfo.savedFileName = userInfo.filename;
            savedFileInfo.savedPath = userInfo.filePath;
            savedFileInfo.savedTime = TimeUtil.getCurrentTime();
        }
        return savedFileInfo;
    }
}
