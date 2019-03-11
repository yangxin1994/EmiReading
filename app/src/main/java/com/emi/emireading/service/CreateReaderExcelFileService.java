package com.emi.emireading.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.emi.emireading.EmiReadingApplication;
import com.emi.emireading.core.common.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.db.MyOperator;
import com.emi.emireading.core.db.SQLiteHelper;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.listener.OnProgressListener;
import com.emi.emireading.ui.export.ExportExcel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author :zhoujian
 * @description : zj
 * @company :翼迈科技
 * @date 2018年10月16日下午 01:51
 * @Email: 971613168@qq.com
 */

public class CreateReaderExcelFileService extends IntentService {
    private static final String TAG = "CreateReaderExcelFileService";
    private SQLiteHelper sqLiteHelper;
    private List<UserInfo> dataList;
    private String mFileName;

    public CreateReaderExcelFileService() {
        super("CreateReaderExcelFileService");
    }

    /**
     * 只要service不执行完，就不会重新创建
     */
    @Override
    public void onCreate() {
        super.onCreate();
        sqLiteHelper = new SQLiteHelper(EmiReadingApplication.getAppContext());
        LogUtil.d(TAG, "onCreate");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null){
            mFileName=intent.getStringExtra(EmiConstants.EXTRA_FILE_NAME);
            LogUtil.d(TAG,"onHandleIntent");
            doCreateExcel();
        }
    }


    private void doCreateExcel() {
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(mFileName)) {
                    return;
                }
                LogUtil.i(TAG,"doCreateExcel："+mFileName);
                dataList = getSqOperator().find(mFileName);
                LogUtil.i(TAG,"doCreateExcel："+dataList.size());
                ExportExcel exportExcel = new ExportExcel(initTableName(), dataList);
                exportExcel.setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(int progress) {

                    }

                    @Override
                    public void onFinish(Object dataList, String fileName) {
                        LogUtil.i(TAG,"生成成功："+fileName);
                    }

                    @Override
                    public void onError(String errorMsg) {
                    LogUtil.e(TAG,errorMsg);
                    }
                });
                notifyFileExplore( exportExcel.exportReaderFile());
            }
        });
    }

    private MyOperator getSqOperator() {
        return new MyOperator(sqLiteHelper.getWritableDatabase());
    }


    private ArrayList<String> initTableName() {
        ArrayList<String> rowNameList = new ArrayList<>();
        rowNameList.add("用户编号");
        rowNameList.add("用户名");
        rowNameList.add("用户地址");
        rowNameList.add("上月抄码");
        rowNameList.add("通道板号");
        rowNameList.add("水表地址");
        rowNameList.add("厂商代码");
        rowNameList.add("人工读数");
        return rowNameList;
    }
    private void notifyFileExplore(String filePath){
        if(TextUtils.isEmpty(filePath)){
            return;
        }
        File file = new File(filePath);
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        sendBroadcast(intent);
    }
}
