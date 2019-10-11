package com.emi.emireading.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.emi.emireading.R;
import com.emi.emireading.adpter.CommonSelectEmiAdapter;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.db.MyOperator;
import com.emi.emireading.core.db.SQLiteHelper;
import com.emi.emireading.core.log.LogTool;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.request.response.ToastUtils;
import com.emi.emireading.core.threadpool.ThreadPoolManager;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.FileUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.FileEditInfo;
import com.emi.emireading.entities.LoadStrategy;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.widget.view.TitleView;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;
import com.emi.emireading.widget.view.dialog.multidialog.EmiMultipleProgressDialog;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.emi.emireading.core.config.EmiConfig.GBK;
import static com.emi.emireading.core.config.EmiConfig.IS_GBK;
import static com.emi.emireading.core.config.EmiConfig.UTF;
import static com.emi.emireading.core.config.EmiConstants.EMI_MERGE_FILE;
import static com.emi.emireading.core.config.EmiConstants.ERROR_CODE;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_FILE_NAME;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_USER_ID;
import static com.emi.emireading.core.config.EmiConstants.NEW_LINE;
import static com.emi.emireading.core.config.EmiConstants.SHEET_NAME;
import static com.emi.emireading.core.config.EmiConstants.SUCCESS_CODE;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_DBF;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_EXCEL;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_EXCEL_2007;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_TXT;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;

/**
 * @author :zhoujian
 * @description : 修改文件
 * @company :翼迈科技
 * @date 2018年01月23日上午 10:09
 * @Email: 971613168@qq.com
 */

public class ModifyFileActivity extends BaseActivity implements View.OnClickListener {
    private EditText etMeterId;
    private EditText etFirmCode;
    private EditText etChannel;
    private EditText etUserId;
    private TextView tvLocation;
    private Button btnQuery;
    private Button btnModify;
    private TitleView titleView;
    private MyOperator myOperator;
    private SQLiteHelper sqLiteHelper;
    private String mFileName;
    private String mUserName;
    private String originalUserId;
    private String originalMeterId;
    private String originalFirmCode;
    private String originalChannel;
    private ArrayList<UserInfo> userInfoArrayList;
    private MyHandler mHandler = new MyHandler(this);
    private Context mContext;
    private static final int MSG_DATA_NOT_FOUND = -1;
    private String userAddress = "";
    private int lineWidth = 15 * 256;
    private ArrayList<FileEditInfo> editBeanArrayList;
    /**
     * 当前选择的要修改的用户
     */
    private UserInfo selectUserInfo;
    private String meterId;
    private String filePath;
    private TextView tvUserName;
    private LoadStrategy mLoadStrategy;
    private int meterIdIndex = -1;
    private int channelIndex = -1;
    private int firmCodeIndex = -1;
    private boolean mergeEnable = false;
    private String splitChar = "";
    private int mergeInfoIndex = -1;
    private boolean mModifyMeterIdEnable;
    private boolean mModifyChannelEnable;
    private boolean mModifyFirmCodeEnable;
    private EmiMultipleProgressDialog dialog;
    private static final int MSG_TOAST = 2001;
    /**
     * 仅在合肥模式下使用
     */
    private String currentFileDirName = "";

    @Override
    protected int getContentLayout() {
        return R.layout.activity_modify_file;
    }

    @Override
    protected void initIntent() {
        mContext = this;
        Intent intent = getIntent();
        mFileName = intent.getStringExtra(EXTRA_FILE_NAME);
        meterId = intent.getStringExtra(EXTRA_USER_ID);
        sqLiteHelper = new SQLiteHelper(this);
    }

    @Override
    protected void initUI() {
        titleView = findViewById(R.id.tvTitleView);
        etMeterId = findViewById(R.id.et_meter_address);
        etFirmCode = findViewById(R.id.editTextFirmCode);
        etChannel = findViewById(R.id.etChannel);
        etUserId = findViewById(R.id.etUserId);
        btnQuery = findViewById(R.id.btnQuery);
        btnModify = findViewById(R.id.btnModify);
        tvLocation = findViewById(R.id.tv_location);
        tvUserName = findViewById(R.id.tvUserName);
        btnQuery.setOnClickListener(this);
        btnModify.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        titleView.setTitle("修改的文件:" + mFileName);
        if (mFileName != null) {
            selectUserInfo = getSqOperator().queryByUserId(mFileName, meterId);
            editBeanArrayList = (ArrayList<FileEditInfo>) LitePal.findAll(FileEditInfo.class);
            if (selectUserInfo != null) {
                originalUserId = selectUserInfo.accountnum;
                originalChannel = selectUserInfo.channelNumber;
                originalFirmCode = selectUserInfo.firmCode;
                originalMeterId = selectUserInfo.meteraddr;
                filePath = selectUserInfo.filePath;
                LogUtil.d("加载的文件路径：" + filePath);
                currentFileDirName = selectUserInfo.dirname;
                userAddress = EmiStringUtil.formatNull(selectUserInfo.useraddr);
                if (!TextUtils.isEmpty(selectUserInfo.loadStrategyJson) && EmiStringUtil.isJSONValid(selectUserInfo.loadStrategyJson)) {
                    mLoadStrategy = JSON.parseObject(selectUserInfo.loadStrategyJson, LoadStrategy.class);
                }
                showInfoDetail(selectUserInfo);
            } else {
                ToastUtil.showShortToast("未查到对应数据");
            }
        }
        LogUtil.d("mLoadStrategy:" + mLoadStrategy.toString());
        meterIdIndex = mLoadStrategy.getMeterIdIndex();
        channelIndex = mLoadStrategy.getChannelIndex();
        firmCodeIndex = mLoadStrategy.getFirmCodeIndex();
        mergeEnable = mLoadStrategy.isMerge();
        /**
         * todo：暂时未处理excel中水表信息合并在一起的情况
         */
        if (mergeEnable) {
            mergeInfoIndex = mLoadStrategy.getMergeInfoIndex();
            splitChar = mLoadStrategy.getSplitChar();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnQuery:
                break;
            case R.id.btnModify:
                mModifyMeterIdEnable = isNeedModify(originalMeterId, etMeterId);
                mModifyChannelEnable = isNeedModify(originalChannel, etChannel);
                mModifyFirmCodeEnable = isNeedModify(originalFirmCode, etFirmCode);
                if (!mModifyMeterIdEnable && !mModifyChannelEnable && !mModifyFirmCodeEnable) {
                    ToastUtil.showShortToast("文件信息无改动");
                    return;
                }
                if (!TextUtils.isEmpty(filePath) && selectUserInfo != null) {
                    showDialog("正在修改数据库...");
                    doModify(filePath);
                } else {
                    ToastUtils.showToastNormal("原始文件可能被删除，文件修改失败");
                }
                break;
            default:
                break;
        }
    }


    private void doQuery() {
        if (TextUtils.isEmpty(etMeterId.getText().toString())) {
            ToastUtil.showShortToast("请输入表地址");
        } else {
            getResultList();
            showResult();
        }
    }

    private void getResultList() {
        myOperator = new MyOperator(sqLiteHelper.getWritableDatabase());
        if (!TextUtils.isEmpty(mFileName)) {
            userInfoArrayList = (ArrayList<UserInfo>) myOperator.queryByMeterId(mFileName, etMeterId.getText().toString());
        } else {
            ToastUtil.showShortToast("找不到文件位置");
        }

    }

    private void showResult() {
        if (userInfoArrayList.size() > 1) {
            ArrayList<String> stringArrayList = new ArrayList<>();
            UserInfo userInfo;
            StringBuilder sb = new StringBuilder("");
            for (int i = 0; i < userInfoArrayList.size(); i++) {
                userInfo = userInfoArrayList.get(i);
                sb.append("编号：");
                sb.append(userInfo.accountnum);
                sb.append(" 表地址：");
                sb.append(userInfo.meteraddr);
                stringArrayList.add(sb.toString());
                sb.delete(0, sb.length());
            }
            showSelectDialog(stringArrayList);
        } else if (userInfoArrayList.size() == 1) {
            selectUserInfo = userInfoArrayList.get(0);
            showInfoDetail(selectUserInfo);
        } else {
            ToastUtil.showShortToast("未找到该表地址相关信息");
        }
    }


    private void showInfoDetail(UserInfo userInfo) {
        if (userInfo != null) {
            tvLocation.setText(EmiStringUtil.formatNull(userInfo.useraddr));
            etFirmCode.setText(EmiStringUtil.formatNull(userInfo.firmCode));
            etChannel.setText(EmiStringUtil.formatNull(userInfo.channelNumber));
            etUserId.setText(EmiStringUtil.formatNull(userInfo.accountnum));
            etMeterId.setText(EmiStringUtil.formatNull(userInfo.meteraddr));
            tvUserName.setText(EmiStringUtil.formatNull(userInfo.username));
        } else {
            ToastUtil.showShortToast("未找到相关信息");
        }
        LogUtil.d(TAG, "originalMeterId=" + originalMeterId);
        LogUtil.d(TAG, "originalMeterId=" + etMeterId.getText().toString());
    }


    private void showSelectDialog(final ArrayList<String> userList) {
        CommonSelectEmiAdapter adapter = new CommonSelectEmiAdapter(userList);
        CommonSelectDialog.Builder builder = new CommonSelectDialog.Builder(ModifyFileActivity.this);
        builder.setTitle("找到" + userList.size() + "个结果，请选择修改");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final CommonSelectDialog deviceDialog = builder.create();
        deviceDialog.setCancelable(false);
        deviceDialog.setCanceledOnTouchOutside(false);
        adapter.bindToRecyclerView(builder.recyclerView);
        adapter.setOnItemClickListener(new BaseEmiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
                selectUserInfo = userInfoArrayList.get(position);
                showInfoDetail(selectUserInfo);
                deviceDialog.dismiss();
            }
        });
        builder.setAdapter(adapter);
        deviceDialog.show();
    }


    private void createExcelFile() {
        if (selectUserInfo != null && !TextUtils.isEmpty(selectUserInfo.filePath)) {
            createFile(selectUserInfo);
        }
    }


    private void createFile(UserInfo userInfo) {
        String suffix = FileUtil.getFileSuffix(userInfo.getFilePath());
        switch (suffix) {
            case SUFFIX_TXT:
                break;
            case SUFFIX_EXCEL:
                ThreadPoolManager.EXECUTOR.execute(new CreateFileRunnable(userInfo.getFilePath()));
                break;
            case SUFFIX_EXCEL_2007:
                ThreadPoolManager.EXECUTOR.execute(new CreateFileRunnable(userInfo.getFilePath()));
                break;
            case SUFFIX_DBF:
                break;
            default:
                break;
        }
    }


    private class CreateFileRunnable implements Runnable {
        private String filePath;

        public CreateFileRunnable(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void run() {
            getSqOperator().updateUserInfo(mFileName, originalUserId, getValue(etUserId), EmiStringUtil.clearFirstZero(getValue(etMeterId)), EmiStringUtil.clearFirstZero(getValue(etChannel)), getValue(etFirmCode));
            createNewExcelByOldExcelPath(filePath);
        }
    }


    private void createNewExcelByOldExcelPath(String filePath) {
        InputStream inputStream;
        OutputStream outputStream;
        try {
            inputStream = new FileInputStream(filePath);
            org.apache.poi.ss.usermodel.Workbook workbook;
            if (EmiUtils.isExcel2003(filePath)) {
                workbook = new HSSFWorkbook(inputStream);
            } else {
                System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
                System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
                System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");
                workbook = new XSSFWorkbook(inputStream);
            }
            int sheetCount = workbook.getNumberOfSheets();
            org.apache.poi.ss.usermodel.Sheet sheet;
            for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
                sheet = workbook.getSheetAt(sheetIndex);
                if (mModifyMeterIdEnable) {
                    for (Row row : sheet) {
                        if (isEditSuccess(row, meterIdIndex, originalMeterId, etMeterId.getText().toString(), false)) {
                            break;
                        }
                    }
                }
                if (mModifyChannelEnable) {
                    for (Row row : sheet) {
                        if (isEditSuccess(row, channelIndex, originalChannel, etChannel.getText().toString(), false)) {
                            break;
                        }
                    }
                }
                if (mModifyFirmCodeEnable) {
                    for (Row row : sheet) {
                        if (isEditSuccess(row, firmCodeIndex, originalFirmCode, etFirmCode.getText().toString(), true)) {
                            break;
                        }
                    }
                }
            }
            FileEditInfo editInfo = makeFileEditInfo();
            originalUserId = getValue(etUserId);
            originalMeterId = getValue(etMeterId);
            originalChannel = getValue(etChannel);
            originalFirmCode = getValue(etFirmCode);
            saveModifyInfo(editInfo);
            outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);
            notificationFile(mContext, filePath);
            sendMsg(SUCCESS_CODE);
        } catch (Exception e) {
            sendMsg(ERROR_CODE);
            LogUtil.e(TAG, e.toString());
        }
    }


    private void modifyExcelDataSplit(Cell cell, String oldValue, String needModifyValue, EditText editView) {
        if (!TextUtils.isEmpty(editView.getText().toString())) {
            LogUtil.d(TAG, "当前行：" + cell.getRowIndex() + "----" + "当前列:" + cell.getColumnIndex());
            Row editRow = cell.getRow();
            for (Cell editCell : editRow) {
                if (editCell.getCellType() == CELL_TYPE_STRING && editCell.getStringCellValue().contains(oldValue)) {
                    if (editCell.getColumnIndex() == EmiConfig.MERGE_INFO_INDEX) {
                        editCell.setCellValue(editCell.getStringCellValue().replace(oldValue, needModifyValue));
                    }
                }
            }
        }
    }


    /**
     * 是否需要修改
     *
     * @param editText
     * @return
     */
    private boolean isNeedModify(EditText editText) {
        return !TextUtils.isEmpty(editText.getText().toString());
    }

    private static class MyHandler extends Handler {
        WeakReference<Activity> mWeakReference;

        private MyHandler(Activity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final ModifyFileActivity activity = (ModifyFileActivity) mWeakReference.get();
            switch (msg.what) {
                case MSG_DATA_NOT_FOUND:
                    activity.closeDialog();
                    break;
                case SUCCESS_CODE:
                    ToastUtils.showToastNormal("修改成功");
                    activity.closeDialog();
                    break;
                case MSG_TOAST:
                    ToastUtils.showToastNormal((String) msg.obj);
                    break;
                default:
                    break;
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void sendMsg(int what) {
        mHandler.sendEmptyMessage(what);
    }

    private void sendToastMsg(String msg) {
        Message message = mHandler.obtainMessage();
        message.what = MSG_TOAST;
        message.obj = msg;
        mHandler.sendMessage(message);
    }

    private void doModify(final String filePath) {
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                switch (FileUtil.getFileSuffix(filePath)) {
                    case SUFFIX_TXT:
                        String path555 = get555FilePath(filePath);
                        LogUtil.i("555文件路径:" + path555);
                        doModifyTxt(path555);
                        break;
                    case SUFFIX_EXCEL:
                    case SUFFIX_EXCEL_2007:
                        createExcelFile();
                        break;
                    default:
                        break;
                }
            }
        });
    }


    /**
     * 解析文本数据并添加到集合
     *
     * @param filePath
     * @param
     */
    private ArrayList<String> getTxtDataFromTxtFile(String filePath) {
        ArrayList<String> resultList = new ArrayList<>();
        synchronized (this) {
            long startTime = System.currentTimeMillis();
            FileInputStream fis;
            InputStreamReader inputReader;
            BufferedReader bufferedReader;
            try {
                fis = new FileInputStream(filePath);
                inputReader = new InputStreamReader(fis, "gbk");
                bufferedReader = new BufferedReader(inputReader);
                String lineTxt;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    resultList.add(lineTxt);
                    LogUtil.d(TAG, "lineTxt=" + lineTxt);
                    long endTime = System.currentTimeMillis();
                    long time = endTime - startTime;
                    LogUtil.d("当前线程id：" + Thread.currentThread().getId() + "已经执行结束" + "，消耗时间：" + time);
                    LogUtil.d(TAG, "当前线程id：" + Thread.currentThread().getId() + "的txtList长度：" + resultList.size());
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendToastMsg("修改失败，原因:" + e.toString());
            }
        }
        return resultList;
    }


    private boolean isNeedModify(String originalData, EditText editText) {
        return !originalData.equals(editText.getText().toString());
    }


    private String getValue(EditText editText) {
        return editText.getText().toString();
    }


    public void writeToFile(ArrayList<String> txtDataList, String filePath) {
        try {
            String encode;
            if (IS_GBK) {
                encode = GBK;
            } else {
                encode = UTF;
            }
            Writer writer = new OutputStreamWriter(new FileOutputStream(filePath), encode);
            BufferedWriter bw = new BufferedWriter(writer);
            StringBuilder stringBuilder = new StringBuilder();
            for (String content : txtDataList) {
                stringBuilder.append(content);
                stringBuilder.append(NEW_LINE);
                bw.write(stringBuilder.toString());
                stringBuilder.delete(0, stringBuilder.length());
            }
            bw.close();
            LogUtil.i(TAG, "写入成功！");
        } catch (Exception e) {
            LogUtil.e(TAG, "错误：" + e.toString());
            sendToastMsg("写入失败:原因：" + e.toString());
        }
    }


    private void saveModifyInfo(FileEditInfo currentFilEditBean) {
        FileEditInfo fileEditInfo;
        boolean isSame = false;
        LogUtil.e("数据库中数据长度：" + editBeanArrayList.size());
        if (editBeanArrayList.isEmpty()) {
            editBeanArrayList.add(currentFilEditBean);
            LogUtil.d("数据库中没有该记录，直接添加");
        } else {
            for (int i = 0; i < editBeanArrayList.size(); i++) {
                fileEditInfo = editBeanArrayList.get(i);
                LogUtil.e("数据库中用户名:" + fileEditInfo.getUserName());
                LogUtil.i("当前修改中用户名:" + currentFilEditBean.getUserName());
                if (fileEditInfo.userAddress.equals(currentFilEditBean.userAddress)) {
                    editBeanArrayList.set(i, currentFilEditBean);
                    LogUtil.w("数据库中有该记录，需要替换");
                    isSame = true;
                    break;
                }
            }
            if (!isSame) {
                LogUtil.i("数据库中无该记录，直接添加");
                editBeanArrayList.add(currentFilEditBean);
            }
        }
        LogUtil.i(TAG, "数据库中数据长度：" + editBeanArrayList.size());
        for (FileEditInfo info : editBeanArrayList) {
            LogUtil.w(TAG, "用户地址------>" + info.userAddress);
        }
        LitePal.saveAll(editBeanArrayList);
        String path = LogTool.LogPath + FileUtil.clearFileSuffix(mFileName) + "(修改记录)" + SUFFIX_EXCEL;
        LogUtil.i("保存路径：" + path);
        exportModifyInfo(path, getEditFileList(editBeanArrayList));
        notificationFile(mContext, path);
    }


    private void exportModifyInfo(String exportFilePath, List<FileEditInfo> fileEditInfoArrayList) {
        //创建HSSFWorkbook对象(excel的文档对象)
        HSSFWorkbook wb = new HSSFWorkbook();
        //建立新的sheet对象（excel的表单）
        HSSFSheet sheet = wb.createSheet(SHEET_NAME);
        //在sheet里创建第一行，参数为行索引(excel的行)，可以是0～65535之间的任何一个
        //创建表头
        try {
            HSSFRow tableRow = sheet.createRow(0);
            //每一列宽度集合
            HSSFRow row;
            HSSFCell cell;
            String[] tableNameArray = {"旧用户编号", "新用户编号", "用户名", "住户地址", "旧通道号", "新通道号", "旧表地址", "新表地址", "旧厂商代码", "新厂商代码"};
            for (int i = 0; i < tableNameArray.length; i++) {
                HSSFCell tableCell = tableRow.createCell(i);
                tableCell.setCellType(Cell.CELL_TYPE_STRING);
                tableCell.setCellValue(tableNameArray[i]);
            }
            for (int i = 0; i < fileEditInfoArrayList.size(); i++) {
                FileEditInfo editInfo = fileEditInfoArrayList.get(i);
                LogUtil.i("-------->" + editInfo.userAddress);
                row = sheet.createRow(i + 1);
                ArrayList<String> stringArrayList = loadValueList(fileEditInfoArrayList.get(i));
                for (int j = 0; j < stringArrayList.size(); j++) {
                    cell = row.createCell(j);
                    cell.setCellValue(stringArrayList.get(j));
                    cell.setCellType(CELL_TYPE_STRING);
                    if (j == 2) {
                        sheet.setColumnWidth(j, lineWidth * 2);
                    } else if (j == 3) {
                        sheet.setColumnWidth(j, lineWidth * 5);
                    } else {
                        sheet.setColumnWidth(j, lineWidth);
                    }
                }
            }
            FileOutputStream fos = new FileOutputStream(exportFilePath);
            wb.write(fos);
            fos.close();
        } catch (final Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG, "exportModifyInfo()异常--->" + e.toString());
        }
    }


    private ArrayList<String> loadValueList(FileEditInfo editBean) {
        ArrayList<String> stringArrayList = new ArrayList<>();
        if (editBean.originalUserId.equals(editBean.newUserId)) {
            editBean.newUserId = "";
        }
        if (editBean.originalMeterId.equals(editBean.newMeterId)) {
            editBean.newMeterId = "";
        }
        if (editBean.originalFirmCode.equals(editBean.newFirmCode)) {
            editBean.newFirmCode = "";
        }
        if (editBean.originalChannelNumber.equals(editBean.newChannelNumber)) {
            editBean.newChannelNumber = "";
        }
        stringArrayList.add(editBean.originalUserId);
        stringArrayList.add(editBean.newUserId);
        stringArrayList.add(editBean.userName);
        stringArrayList.add(editBean.userAddress);
        stringArrayList.add(editBean.originalChannelNumber);
        stringArrayList.add(editBean.newChannelNumber);
        stringArrayList.add(editBean.originalMeterId);
        stringArrayList.add(editBean.newMeterId);
        stringArrayList.add(editBean.originalFirmCode);
        stringArrayList.add(editBean.newFirmCode);
        return stringArrayList;
    }

    /**
     * 修改TXT文件
     *
     * @param file555Path 555文件路径
     */
    private void doModifyTxt(String file555Path) {
        //先读取文本中的数据文件
        boolean isFind = false;
        ArrayList<String> txtList = getTxtDataFromTxtFile(file555Path);
        String[] lineArray;
        String currentUserId;
        //遍历每一行数据
        for (int i = 0; i < txtList.size(); i++) {
            lineArray = txtList.get(i).split(EmiConfig.SPLIT_CHAR);
            //防止数组越界
            if (lineArray.length >= EmiConfig.FIELD_COUNT) {
                currentUserId = lineArray[EmiConfig.USER_ID_INDEX];
                if (currentUserId.equals(originalUserId)) {
                    lineArray[EmiConfig.USER_ID_INDEX] = getValue(etUserId);
                    lineArray[EmiConfig.METER_ID_INDEX] = getValue(etMeterId);
                    lineArray[EmiConfig.CHANNEL_INDEX] = getValue(etChannel);
                    lineArray[EmiConfig.FIRM_CODE_INDEX] = getValue(etFirmCode);
                    txtList.set(i, StringUtils.join(lineArray, "$"));
                    getSqOperator().updateUserInfo(mFileName, originalUserId, getValue(etUserId), getValue(etMeterId), getValue(etChannel), getValue(etFirmCode));
                    isFind = true;
                    break;
                }
            }
        }
        if (isFind) {
            FileEditInfo editInfo = makeFileEditInfo();
            originalUserId = getValue(etUserId);
            originalMeterId = getValue(etMeterId);
            originalChannel = getValue(etChannel);
            originalFirmCode = getValue(etFirmCode);
            saveModifyInfo(editInfo);
            writeToFile(txtList, file555Path);
            sendMsg(SUCCESS_CODE);
        } else {
            sendMsg(MSG_DATA_NOT_FOUND);
        }
    }


    private ArrayList<FileEditInfo> getEditFileList(ArrayList<FileEditInfo> editInfoList) {
        ArrayList<FileEditInfo> fileEditInfoArrayList = new ArrayList<>();
        fileEditInfoArrayList.addAll(editInfoList);
        for (int i = fileEditInfoArrayList.size() - 1; i >= 0; i--) {
            if (!(fileEditInfoArrayList.get(i).fileName.equals(mFileName))) {
                fileEditInfoArrayList.remove(i);
            }
        }
        LogUtil.w(TAG, "fileEditInfoArrayList长度：" + fileEditInfoArrayList.size());
        return removeSameUser(fileEditInfoArrayList);
    }


    protected ArrayList<FileEditInfo> removeSameUser(ArrayList<FileEditInfo> fileEditInfoList) {
        for (int i = 0; i < fileEditInfoList.size(); i++) {
            for (int j = i + 1; j < fileEditInfoList.size(); j++) {
                if (fileEditInfoList.get(i).equals(fileEditInfoList.get(j))) {
                    LogUtil.i(TAG, "已经移除数据：" + fileEditInfoList.get(j).userAddress);
                    fileEditInfoList.remove(j);
                    j--;
                }
            }
        }
        LogUtil.i(TAG, "fileEditInfoArrayList长度：(移除)" + fileEditInfoList.size());
        return fileEditInfoList;
    }


    private FileEditInfo makeFileEditInfo() {
        FileEditInfo editInfo = new FileEditInfo();
        editInfo.fileName = mFileName;
        editInfo.userName = mUserName;
        editInfo.userAddress = userAddress;
        editInfo.originalUserId = originalUserId;
        editInfo.originalMeterId = originalMeterId;
        editInfo.originalChannelNumber = originalChannel;
        editInfo.originalFirmCode = originalFirmCode;
        editInfo.newMeterId = getValue(etMeterId);
        editInfo.newUserId = getValue(etUserId);
        editInfo.newFirmCode = getValue(etFirmCode);
        editInfo.newChannelNumber = getValue(etChannel);
        LogUtil.d(TAG, "旧的表地址：" + originalMeterId);
        LogUtil.i(TAG, "新的表地址：" + editInfo.newMeterId);
        return editInfo;
    }


    private boolean isEditSuccess(Row row, int columnIndex, String needModifyValue, String newValue, boolean isModifyFirmCode) {
        if (columnIndex < 0 || columnIndex >= row.getPhysicalNumberOfCells()) {
            LogUtil.e("索引超出范围!");
            return false;
        }
        Cell cell = row.getCell(columnIndex);
        String value = "";
        switch (cell.getCellTypeEnum()) {
            case NUMERIC:
                value = EmiStringUtil.subZeroAndDot(cell.getNumericCellValue() + "");
                break;
            case STRING:
                value = cell.getStringCellValue();
                break;
            default:
                break;
        }
        if (isModifyFirmCode && TextUtils.isEmpty(value)) {
            value = EmiConstants.FIRM_CODE_7833;
        }
        if (EmiStringUtil.clearFirstZero(EmiStringUtil.handleString(value)).equalsIgnoreCase(needModifyValue)) {
            cell.setCellValue(newValue);
            return true;
        }
        return false;
    }


    private void showDialog(String text) {
        dialog = EmiMultipleProgressDialog.create(mContext)
                .setLabel(text)
                .setCancellable(false)
                .show();
    }


    private void notifyDialogText(String text) {
        if (dialog == null) {
            showDialog(text);
        } else {
            dialog.setLabel(text);
            dialog.show();
        }
    }

    private void closeDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    /**
     * 获取555文件路径
     */
    private String get555FilePath(String currentSelectFilePath) {
        try {
            //先尝试从表册文件目录中获取555文件路径
            int txt555FileIndex = currentSelectFilePath.lastIndexOf(File.separator) + 1;
            String mergePath = EmiConfig.EMI_MERGE_PATH + File.separator + currentFileDirName + File.separator + EMI_MERGE_FILE;
            String file555Path = currentSelectFilePath.substring(0, txt555FileIndex) + EMI_MERGE_FILE;
            if (FileUtil.checkFileExsit(mergePath)) {
                return mergePath;
            } else if (FileUtil.checkFileExsit(file555Path)) {
                return file555Path;
            }
            return "";
        } catch (Exception e) {
            LogUtil.e("get555FilePath()异常----->" + e.toString());
            return "";
        }
    }
}
