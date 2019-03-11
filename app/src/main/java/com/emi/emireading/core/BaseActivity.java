package com.emi.emireading.core;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.emi.emireading.EmiReadingApplication;
import com.emi.emireading.common.DigitalTrans;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.db.MyOperator;
import com.emi.emireading.core.db.SQLiteHelper;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.ActivityCollector;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.FileUtil;
import com.emi.emireading.entities.ExportStrategy;
import com.emi.emireading.entities.FileEditInfo;
import com.emi.emireading.entities.FileEntity;
import com.emi.emireading.entities.SavedFileInfo;
import com.emi.emireading.entities.UserInfo;

import org.apache.commons.lang.StringUtils;
import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.emi.emireading.core.config.EmiConfig.exportStrategy;
import static com.emi.emireading.core.config.EmiConstants.EMI_CALLBACK_CODE_BEGIN;
import static com.emi.emireading.core.config.EmiConstants.EMI_CALLBACK_CODE_END;
import static com.emi.emireading.core.config.EmiConstants.EMI_MERGE_FILE;
import static com.emi.emireading.core.config.EmiConstants.JSON_EXPORT_MOULD_NAME;
import static com.emi.emireading.core.utils.EmiStringUtil.splitStrToList;
import static java.lang.Integer.parseInt;

/**
 * @author :zhoujian
 * @description : 基类Activity
 * @company :翼迈科技
 * @date: 2017年12月25日上午 11:53
 * @Email: 971613168@qq.com
 */

public abstract class BaseActivity extends AppCompatActivity {
    protected Disposable baseDisposable;
    protected static String TAG;
    protected SQLiteHelper sqLiteHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = getClass().getSimpleName();
        setContentView(getContentLayout());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        sqLiteHelper = new SQLiteHelper(EmiReadingApplication.getAppContext());
        initIntent();
        initUI();
        initData();
        if (isCollect()) {
            ActivityCollector.addActivity(this);
        }
    }


    protected abstract int getContentLayout();

    protected abstract void initIntent();

    protected abstract void initUI();

    protected abstract void initData();

    protected boolean isCollect() {
        return true;
    }


    @Override
    protected void onDestroy() {
        LogUtil.w(TAG, "==================== onDestroy =====================");
        if (isCollect()) {
            ActivityCollector.removeActivity(this);
        }
        System.gc();
        super.onDestroy();
    }


    protected void notifyFileExplore(String filePath) {
        File file = new File(filePath);
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        sendBroadcast(intent);
    }


    /**
     * 通知媒体库更新文件
     *
     * @param context
     * @param filePath 文件全路径
     */
    protected void notificationFile(Context context, String filePath) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(Uri.fromFile(new File(filePath)));
        context.sendBroadcast(scanIntent);
    }

    protected void openActivity(Context context, Class activity) {
        Intent intent = new Intent();
        intent.setClass(context, activity);
        startActivity(intent);
    }

    protected MyOperator getSqOperator() {
        return new MyOperator(sqLiteHelper.getWritableDatabase());
    }

    /**
     * 发送蓝牙指令
     *
     * @param cmd
     */
    protected void sendBTCmd(byte[] cmd) {
        if (EmiConstants.bluetoothSocket == null) {
            return;
        }
        try {
            OutputStream os = EmiConstants.bluetoothSocket.getOutputStream();
            os.write(cmd);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 延时
     *
     * @param time
     */
    protected void delays(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    protected List<UserInfo> removeSameUser(List<UserInfo> userInfoList) {
        for (int i = 0; i < userInfoList.size() - 1; i++) {
            for (int j = userInfoList.size() - 1; j > i; j--) {
                if (userInfoList.get(j).equals(userInfoList.get(i))) {
                    userInfoList.remove(j);
                }
            }
        }
        return userInfoList;
    }

    /**
     * 移除重复元素
     *
     * @param stringList
     * @return
     */
    protected List<String> removeDuplicateStringElement(List<String> stringList) {
        for (int i = 0; i < stringList.size() - 1; i++) {
            for (int j = stringList.size() - 1; j > i; j--) {
                if (stringList.get(j).equals(stringList.get(i))) {
                    stringList.remove(j);
                }
            }
        }
        return stringList;
    }

    protected List<UserInfo> selectUserByState(List<UserInfo> userInfoList, int state) {
        List<UserInfo> userInfoArrayList = new ArrayList<>();
        userInfoArrayList.addAll(userInfoList);
        for (int i = userInfoArrayList.size() - 1; i >= 0; i--) {
            if (userInfoArrayList.get(i).state != state) {
                userInfoArrayList.remove(i);
            }
        }
        return userInfoArrayList;
    }


    protected List<UserInfo> selectUserByChannel(List<UserInfo> userInfoList, String channelNumber, int uploadState) {
        List<UserInfo> userInfoArrayList = new ArrayList<>();
        for (int i = 0; i < userInfoList.size(); i++) {
            if (userInfoList.get(i).channelNumber.equals(channelNumber)) {
                userInfoList.get(i).uploadState = uploadState;
                userInfoArrayList.add(userInfoList.get(i));
            }
        }
        return userInfoArrayList;
    }

    /**
     * 判断路径是否为文件夹
     *
     * @param filePath
     * @return
     */
    protected boolean isFolder(String filePath) {
        if (filePath == null) {
            return false;
        } else {
            File file = new File(filePath);
            return file.exists() && file.isDirectory();
        }
    }

    protected boolean isFile(String filePath) {
        if (filePath == null) {
            return false;
        } else {
            File file = new File(filePath);
            return file.exists() && file.isFile();
        }
    }

    /**
     * 判断文件夹下是否有555文件
     *
     * @param filePath
     * @return
     */
    protected boolean isMergeFileExist(String filePath) {
        if (isFolder(filePath)) {
            ArrayList<FileEntity> fileList = FileUtil.getFilePathListFromDir(filePath);
            for (FileEntity entity : fileList) {
                if (EMI_MERGE_FILE.equals(entity.fileName)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }

    }


    protected byte[] stringToBytes(String s) {
        byte[] buf = new byte[s.length() / 2];
        for (int i = 0; i < buf.length; i++) {
            try {
                buf[i] = (byte) parseInt(s.substring(i * 2, i * 2 + 2), 16);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return buf;
    }

    protected byte[] stringToBytes(String s, int radix) {
        byte[] buf = new byte[s.length() / 2];
        for (int i = 0; i < buf.length; i++) {
            try {
                buf[i] = (byte) parseInt(s.substring(i * 2, i * 2 + 2), radix);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return buf;
    }


    protected String getHexString(String buf) {
        String s = buf;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean flag = ('0' <= c && c <= '9') || ('a' <= c && c <= 'f')
                    || ('A' <= c && c <= 'F');
            if (flag) {
                sb.append(c);
            }
        }
        if ((sb.length() % 2) != 0) {
            sb.deleteCharAt(sb.length());
        }
        return sb.toString();
    }

    /**
     * 追加字符串
     *
     * @param data：数据源
     * @param character：需要追加的字符
     * @param length：需要的字符串长度
     * @return
     */
    protected String appendString(String data, String character, int length) {
        if (data == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder("");
        if (data.length() >= length) {
            sb.append(data);
            return sb.toString();
        } else {
            for (int i = 0; i < length - data.length(); i++) {
                sb.append(character);
            }
            sb.append(data);
            return sb.toString();
        }
    }

    protected int byte2Int(byte b) {
        return b & 0xFF;
    }


    /**
     * 计时器
     *
     * @param milliseconds
     * @param observable
     */
    protected void doEventByInterval(long milliseconds, Observer<Long> observable) {
        Observable.interval(milliseconds, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observable);
    }

    /**
     * milliseconds毫秒后执行next操作(倒计时)
     *
     * @param milliseconds
     * @param
     */
    protected void doEventCountDown(long milliseconds, Observer<Long> observer) {
        Observable.timer(milliseconds, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    /**
     * 取消计时
     */
    protected void cancelTimers() {
        if (baseDisposable != null && !baseDisposable.isDisposed()) {
            baseDisposable.dispose();
            LogUtil.w("====定时器终止======(base)");
        }
    }

    /**
     * 取消计时
     *
     * @param disposable
     */
    protected void stopTimer(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    protected void stopTimer(List<Disposable> disposableList) {
        for (int i = 0; i < disposableList.size(); i++) {
            stopTimer(disposableList.get(i));
        }
    }

    protected View getViewByResource(Context context, int resource) {
        return LayoutInflater.from(context).inflate(resource, null);
    }

    /**
     * 检验校验位是否正确
     *
     * @param inf
     * @param csSum
     * @return
     */
    protected boolean checkCs(byte[] inf, int csSum) {
        //校验位
        int checkNumber = 0;
        for (int i = 0; i < inf.length; i++) {
            checkNumber += byte2Int(inf[i]);
            checkNumber = checkNumber & 0xFF;
        }
        LogUtil.d("校验位：csSum" + csSum);
        LogUtil.i("校验位：checkNumber" + checkNumber);
        return csSum == checkNumber;
    }


    /**
     * 获取当前蓝牙设备索引
     *
     * @param deviceArrayList
     * @return
     */
    protected int getEmiDeviceIndex(ArrayList<BluetoothDevice> deviceArrayList) {
        int index = -1;
        for (int i = 0; i < deviceArrayList.size(); i++) {
            if (EmiConstants.EMI_DEVICE_NAME.equals(deviceArrayList.get(i).getName())) {
                index = i;
            }
        }
        return index;
    }


    /**
     * 移除已经销毁的计时器
     */
    protected void removeInvalidTimer(List<Disposable> disposableList) {
        for (int i = disposableList.size() - 1; i >= 0; i--) {
            if (disposableList.get(i).isDisposed()) {
                disposableList.remove(i);
            }
        }
    }

    protected void removeEmptyUserInfo(List<UserInfo> userInfoList) {
        for (int i = userInfoList.size() - 1; i >= 0; i--) {
            if (userInfoList.get(i) == null || TextUtils.isEmpty(userInfoList.get(i).meteraddr)) {
                userInfoList.remove(i);
            }
        }
    }

    protected void setUserInfoUploadState(List<UserInfo> userInfoList, int state) {
        for (UserInfo userInfo : userInfoList) {
            userInfo.uploadState = state;
        }
    }


    protected void doDelete(String fileName) {
        getSqOperator().delete(fileName);
        int count = LitePal.deleteAll(FileEditInfo.class, "fileName = ?", fileName);
        LogUtil.d("删除的数量：" + count);
    }

    /**
     * 解析assets目录下的导出配置
     *
     * @return
     */
    protected List<ExportStrategy> getExportStrategyFromAssets() {
        List<ExportStrategy> exportStrategyList = new ArrayList<>();
        try {
            String exportMouldString = FileUtil.getAssetsJson(this, JSON_EXPORT_MOULD_NAME);
            JSONArray mouldArray = JSONObject.parseArray(exportMouldString);
            exportStrategyList.addAll(JSONArray.parseArray(exportMouldString, ExportStrategy.class));
            String cityName;
            for (ExportStrategy exportStrategy : exportStrategyList) {
                for (int i = 0; i < mouldArray.size(); i++) {
                    JSONObject jsonObject = mouldArray.getJSONObject(i);
                    cityName = jsonObject.getString("cityName");
                    if (!TextUtils.isEmpty(cityName)) {
                        if (cityName.equals(exportStrategy.cityName)) {
                            exportStrategy.setExportFormatJson(jsonObject.toString());
                            LogUtil.i(TAG, "已经匹配到数据:" + exportStrategy.cityName);
                            LogUtil.i(TAG, "已经匹配到数据:文件类型:" + exportStrategy.exportFileType);
                            LogUtil.i(TAG, "已经匹配到数据:" + exportStrategy.exportFormatJson);
                            break;
                        }
                    }
                }
            }
            for (int i = exportStrategyList.size() - 1; i >= 0; i--) {
                if (TextUtils.isEmpty(exportStrategyList.get(i).exportFormatJson)) {
                    exportStrategyList.remove(i);
                }
            }
            LitePal.deleteAll(ExportStrategy.class);
            LitePal.saveAll(exportStrategyList);
        } catch (Exception e) {
            EmiConfig.exportStrategy = null;
        }
        return exportStrategyList;
    }


    /**
     * 解析配置文件中的导出配置
     *
     * @return
     */
    protected List<ExportStrategy> getExportStrategyFromConfigFile(String jsonString) {
        List<ExportStrategy> exportStrategyList = new ArrayList<>();
        try {
            //            String exportStrategyString = FileUtil.getAssetsJson(this, JSON_EXPORT_FILE_NAME);
            //            String exportMouldString = FileUtil.getAssetsJson(this, JSON_EXPORT_MOULD_NAME);
            JSONArray mouldArray = JSONObject.parseArray(jsonString);
            exportStrategyList.addAll(JSONArray.parseArray(jsonString, ExportStrategy.class));
            String cityName;
            for (ExportStrategy exportStrategy : exportStrategyList) {
              /*  LogUtil.w(TAG, "解析的结果：" + exportStrategy.cityName);
                LogUtil.w(TAG, "找到对应结果：" + exportStrategy.cityName);
                exportStrategy.setExportFormatJson(mouldArray.getJSONObject(i).toString());
                LogUtil.w(TAG, "找到对应结果：" + exportStrategy.exportFormatJson);*/
                for (int i = 0; i < mouldArray.size(); i++) {
                    JSONObject jsonObject = mouldArray.getJSONObject(i);
                    cityName = jsonObject.getString("cityName");
                    if (!TextUtils.isEmpty(cityName)) {
                        if (cityName.equals(exportStrategy.cityName)) {
                            exportStrategy.setExportFormatJson(jsonObject.toString());
                            LogUtil.i(TAG, "已经匹配到数据:" + exportStrategy.cityName);
                            LogUtil.i(TAG, "已经匹配到数据:文件类型:" + exportStrategy.exportFileType);
                            LogUtil.i(TAG, "已经匹配到数据:" + exportStrategy.exportFormatJson);
                            break;
                        }
                    }
                }
            }
            for (int i = exportStrategyList.size() - 1; i >= 0; i--) {
                if (TextUtils.isEmpty(exportStrategyList.get(i).exportFormatJson)) {
                    exportStrategyList.remove(i);
                }
            }
        } catch (Exception e) {
            LogUtil.e("解析异常：" + e.toString());
        }
        return exportStrategyList;
    }

    /**
     * 初始化导出策略
     */
    private void initExportExportStrategy() {
        LitePal.deleteAll(ExportStrategy.class);
        List<ExportStrategy> list = LitePal.findAll(ExportStrategy.class);
        if (list.isEmpty()) {
            list = getExportStrategyFromAssets();
            LogUtil.w("数据库策略为空");
        } else {
            LogUtil.d("数据库策略不为空");
        }
        if (exportStrategy == null && list.size() > 0) {
            exportStrategy = list.get(0);
            LogUtil.d("解析的导出策略：" + exportStrategy.cityName);
            LogUtil.d("解析的导出策略：" + exportStrategy.exportFormatJson);
            LogUtil.d("解析的导出策略：" + exportStrategy.exportFileType);
        }
    }

    protected void loadExportStrategy() {
        if (EmiConfig.exportStrategy == null) {
            EmiConfig.exportStrategy = EmiUtils.getExportStrategy();
            if (EmiConfig.exportStrategy == null) {
                initExportExportStrategy();
            }
        }
    }


    /**
     * 检查回调的数据是否正确
     *
     * @param callbackStr
     * @return
     */
    protected boolean checkCallbackDataCorrect(String callbackStr) {
        if (TextUtils.isEmpty(callbackStr) || !callbackStr.endsWith(EmiConstants.EMI_CALLBACK_CODE_END)) {
            return false;
        }
        String endCode = EmiStringUtil.getLastStr(callbackStr, 2);
        if (callbackStr.contains(EMI_CALLBACK_CODE_BEGIN) && endCode.contains(EMI_CALLBACK_CODE_END)) {
            int beginIndex = callbackStr.indexOf("68");
            int csIndex = callbackStr.lastIndexOf(EMI_CALLBACK_CODE_END) - 2;
            String csValue = callbackStr.substring(csIndex, callbackStr.length() - 2);
            int csNumber = DigitalTrans.stringConvertInt(csValue, 16);
            String checkString = callbackStr.substring(beginIndex, csIndex);
            LogUtil.d(TAG, "解析的字符串:" + checkString);
            byte[] checkArray = DigitalTrans.stringConvertBytes(checkString);
            return checkCs(checkArray, csNumber);
        }
        return false;
    }


    protected String convertMeterInfoByList(List<String> stringList) {
        if (stringList != null && !stringList.isEmpty()) {
            StringBuilder sb = new StringBuilder("");
            for (int i = stringList.size() - 1; i >= 0; i--) {
                sb.append(stringList.get(i));
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * 解析水表读数和水表地址及厂商代码
     *
     * @param callbackStr
     * @return
     */
    protected UserInfo parseMeterDataCallback(String callbackStr) {
        if (TextUtils.isEmpty(callbackStr) || (!checkCallbackDataCorrect(callbackStr))) {
            return null;
        }
        String meterInfo = "";
        int meterInfoLength = 14;
        String meterId;
        String meterFirmCode;
        String meterData = "";
        int beginIndex = callbackStr.indexOf(EMI_CALLBACK_CODE_BEGIN) + EMI_CALLBACK_CODE_BEGIN.length() + 2;
        if (beginIndex > -1) {
            if (callbackStr.length() > beginIndex + meterInfoLength) {
                meterInfo = callbackStr.substring(beginIndex, beginIndex + meterInfoLength);
                meterData = callbackStr.substring(beginIndex + meterInfoLength + 12, beginIndex + meterInfoLength + 12 + 4);
            }
            List<String> meterInfoList = splitStrToList(meterInfo);
            List<String> meterDataList = splitStrToList(meterData);
            meterInfo = convertMeterInfoByList(meterInfoList);
            meterData = convertMeterInfoByList(meterDataList);
            if (meterInfo.length() == meterInfoLength) {
                meterFirmCode = meterInfo.substring(0, 4);
                meterId = meterInfo.substring(4, meterInfoLength);
                //抄表成功
                LogUtil.w(TAG, "水表信息:" + meterInfo);
                LogUtil.i(TAG, "解析结果:" + meterId + "---" + meterFirmCode);
                LogUtil.i(TAG, "水表读数:" + meterData);
                UserInfo userInfo = new UserInfo();
                userInfo.meteraddr = meterId;
                userInfo.firmCode = meterFirmCode;
                if (TextUtils.isDigitsOnly(meterData)) {
                    userInfo.curdata = Integer.parseInt(meterData);
                } else {
                    userInfo.curdata = -1;
                }
                return userInfo;
            }
        }
        return null;
    }


    /**
     * 获取加载过的文件名
     *
     * @return
     */
    protected ArrayList<String> getTempFileNameList() {
        //        String currentFileName;
        ArrayList<String> tempFileList = new ArrayList<>();
        List<SavedFileInfo> fileInfoList = LitePal.findAll(SavedFileInfo.class);
        for (SavedFileInfo savedFileInfo : fileInfoList) {
            if (FileUtil.getFileSuffix(savedFileInfo.savedFileName).contains(EmiConfig.CURRENT_SUFFIX)) {
                tempFileList.add(savedFileInfo.savedFileName);
            }
            LogUtil.w("找到的文件名：" + savedFileInfo.savedFileName);
        }
        return tempFileList;
    }

    /**
     * 中断当前线程
     */
    protected void stopCurrentThread() {
        try {
            throw new InterruptedException("线程中断");
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * 判断设备(采集器)返回的数据是否正确
     */

    protected boolean checkCallbackDeviceDataCorrect(String value) {
        //将字符串中i替换为空,创建新的字符串
        if (TextUtils.isEmpty(value) || !value.endsWith(EMI_CALLBACK_CODE_END) || !value.contains(EMI_CALLBACK_CODE_BEGIN)) {
            return false;
        }
        String beginCode = "68";
        int index = StringUtils.ordinalIndexOf(value, beginCode, 2);
        int endIndex = value.lastIndexOf(EMI_CALLBACK_CODE_END);
        if (index == -1) {
            return false;
        }
        index = index + beginCode.length();
        String checkString = value.substring(index, endIndex - 2);
        String csString = value.substring(endIndex - 2, endIndex);
        int csNumber = DigitalTrans.stringConvertInt(csString, 16);
        byte[] checkArray = DigitalTrans.stringConvertBytes(checkString);
        return checkCs(checkArray, csNumber);
    }


}
