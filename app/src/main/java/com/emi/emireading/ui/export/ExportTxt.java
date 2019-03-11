package com.emi.emireading.ui.export;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.emi.emireading.EmiReadingApplication;
import com.emi.emireading.R;
import com.emi.emireading.core.common.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.config.EmiExportConstants;
import com.emi.emireading.core.db.MyOperator;
import com.emi.emireading.core.db.SQLiteHelper;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.TimeUtil;
import com.emi.emireading.entities.ExportStrategy;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.listener.OnProgressListener;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.emi.emireading.core.config.EmiConstants.KEY_CHANNEL_ADDRESS;
import static com.emi.emireading.core.config.EmiConstants.KEY_CHANNEL_NUMBER;
import static com.emi.emireading.core.config.EmiConstants.KEY_CURRENT_DATA;
import static com.emi.emireading.core.config.EmiConstants.KEY_CURRENT_USAGE;
import static com.emi.emireading.core.config.EmiConstants.KEY_FIRM_CODE;
import static com.emi.emireading.core.config.EmiConstants.KEY_LAST_DATA;
import static com.emi.emireading.core.config.EmiConstants.KEY_LAST_USAGE;
import static com.emi.emireading.core.config.EmiConstants.KEY_METER_ADDRESS;
import static com.emi.emireading.core.config.EmiConstants.KEY_READ_DATE;
import static com.emi.emireading.core.config.EmiConstants.KEY_READ_STATE;
import static com.emi.emireading.core.config.EmiConstants.KEY_USER_ADDRESS;
import static com.emi.emireading.core.config.EmiConstants.KEY_USER_ID;
import static com.emi.emireading.core.config.EmiConstants.KEY_USER_NAME;
import static com.emi.emireading.core.config.EmiConstants.KEY_WATER_ID;
import static com.emi.emireading.core.config.EmiConstants.STATE_FAILED;
import static com.emi.emireading.core.config.EmiConstants.STATE_NO_READ;
import static com.emi.emireading.core.config.EmiConstants.STATE_PEOPLE_RECORDING;
import static com.emi.emireading.core.config.EmiConstants.STATE_SUCCESS;
import static com.emi.emireading.core.config.EmiConstants.STATE_WARNING;
import static com.emi.emireading.core.utils.TimeUtil.getCurrentTime;

/**
 * @author :zhoujian
 * @description : 导出TXT文件
 * @company :翼迈科技
 * @date 2018年05月29日上午 11:37
 * @Email: 971613168@qq.com
 */

public class ExportTxt {
    private OnProgressListener mOnProgressListener;
    private ExportStrategy exportStrategy;
    private String exportFilePath;
    private Context context;
    private ArrayList<String> valueList = new ArrayList<>(13);

    public List<UserInfo> getDataList() {
        return mDataList;
    }


    public void setDataList(List<UserInfo> mDataList) {
        this.mDataList = mDataList;
    }

    private List<UserInfo> mDataList;
    private Handler handler;
    private ArrayList<String> sortList = new ArrayList<>();

    public ExportTxt(ExportStrategy exportStrategy, String exportFilePath, List<UserInfo> dataList) {
        this.exportFilePath = exportFilePath;
        this.exportStrategy = exportStrategy;
        this.handler = new Handler(Looper.getMainLooper());
        this.mDataList = dataList;
        context = EmiReadingApplication.getAppContext();
    }


    /**
     * 获取模板
     */
    private void getTxtMould() {
        JSONObject jsonObject = JSONObject.parseObject(exportStrategy.getExportFormateJson());
        sortList.clear();
        String key;
        for (int i = 0; i < jsonObject.size(); i++) {
            key = jsonObject.getString(i + 1 + "");
            sortList.add(key);
        }
    }


    public void setOnProgressListener(OnProgressListener onProgressListener) {
        this.mOnProgressListener = onProgressListener;
    }


    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    private String convertDecimal(StringBuilder sb, String value) {
        sb.setLength(0);
        sb.append("");
        sb.append(value);
        sb.append(".0");
        return sb.toString();
    }


    public void exportTxtFile(final boolean isCreateDate) {
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    getTxtMould();
                    if (exportFilePath != null) {
                        createTxt(isCreateDate);
                    } else {
                        sendErrorToast("导出路径不正确");
                    }
                } catch (Exception e) {
                    sendErrorToast(e.toString());
                }
            }
        });

    }

    @SuppressWarnings("unchecked")
    private void createTxt(boolean isCreateDate) {
        DecimalFormat decimalFormat = new DecimalFormat("######0");
        int progress;
        double allCount;
        double currentCount;
        ArrayList<String> lineTxtList = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder("");
        String date = TimeUtil.getDateString(getCurrentTime());
        UserInfo userInfo;
        allCount = mDataList.size();
        for (int i = 0; i < mDataList.size(); i++) {
            currentCount = i + 1;
            progress = Integer.parseInt(decimalFormat.format((currentCount / allCount) * 100));
            userInfo = mDataList.get(i);
            userInfo.hasExport = 1;
            loadData(userInfo);
            for (String value : valueList) {
                stringBuilder.append(value);
            }
            //合肥抄表要求最后追加01状态位表示已抄
            stringBuilder.append("01");
            if (isCreateDate) {
                stringBuilder.append("$");
                stringBuilder.append(date);
            }
            stringBuilder.append(EmiConstants.NEW_LINE);
            lineTxtList.add(stringBuilder.toString());
            stringBuilder.delete(0, stringBuilder.length());
            if (mOnProgressListener != null) {
                mOnProgressListener.onProgress(progress);
            }
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(exportFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fileWriter != null) {
            try {
                for (String currentLine : lineTxtList) {
                    fileWriter.write(currentLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        SQLiteHelper sqLiteHelper = new SQLiteHelper(EmiReadingApplication.getAppContext());
        MyOperator operator = new MyOperator(sqLiteHelper.getWritableDatabase());
        operator.updateExportStatus(mDataList);
        if (mOnProgressListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mOnProgressListener.onFinish(mDataList, exportFilePath);
                }
            });
        }
    }

    /**
     * 将对象数据按模板放入集合
     *
     * @param userInfo
     */
    private void loadData(UserInfo userInfo) {
        valueList.clear();
        String filedName;
        StringBuilder sb = new StringBuilder("");
        String splitChar = EmiStringUtil.formatNull("$");
        boolean isLast;
        for (int i = 0; i < sortList.size(); i++) {
            isLast = i == sortList.size() - 1;
            filedName = sortList.get(i);
            //用户编号
            if (KEY_USER_ID.equals(filedName)) {
                valueList.add(userInfo.accountnum);
                appendSplitChar(isLast, splitChar);
            }
            //用户名
            if (KEY_USER_NAME.equals(filedName)) {
                valueList.add(userInfo.username);
                appendSplitChar(isLast, splitChar);
            }
            //用户地址
            if (KEY_USER_ADDRESS.equals(filedName)) {
                valueList.add(String.valueOf(userInfo.useraddr));
                appendSplitChar(isLast, splitChar);
            }
            //用水id
            if (KEY_WATER_ID.equals(filedName)) {
                valueList.add(userInfo.waterId);
                appendSplitChar(isLast, splitChar);
            }
            //上次读数
            if (KEY_LAST_DATA.equals(filedName)) {
                if (EmiExportConstants.IS_DOUBLE) {
                    valueList.add(convertDecimal(sb, String.valueOf(userInfo.lastdata)));
                } else {
                    valueList.add(String.valueOf(userInfo.lastdata));
                }
                appendSplitChar(isLast, splitChar);
            }
            //上次用水量
            if (KEY_LAST_USAGE.equals(filedName)) {
                if (EmiExportConstants.IS_DOUBLE) {
                    valueList.add(convertDecimal(sb, String.valueOf(userInfo.lastyl)));
                } else {
                    valueList.add(String.valueOf(userInfo.lastyl));
                }
                appendSplitChar(isLast, splitChar);
            }
            //本次读数
            if (KEY_CURRENT_DATA.equals(filedName)) {
                if (EmiExportConstants.IS_DOUBLE) {
                    valueList.add(convertDecimal(sb, String.valueOf(userInfo.curdata)));
                } else {
                    valueList.add(String.valueOf(userInfo.curdata));
                }
                appendSplitChar(isLast, splitChar);
            }
            //本次用水量
            if (KEY_CURRENT_USAGE.equals(filedName)) {
                if (EmiExportConstants.IS_DOUBLE) {
                    valueList.add(convertDecimal(sb, String.valueOf(userInfo.curyl)));
                } else {
                    valueList.add(String.valueOf(userInfo.curyl));
                }
                appendSplitChar(isLast, splitChar);
            }
            //抄表日期
            if (KEY_READ_DATE.equals(filedName)) {
                if (!TextUtils.isEmpty(userInfo.curreaddate)) {
                    valueList.add(TimeUtil.getDateString(userInfo.curreaddate));
                    LogUtil.i("当前日期：" + TimeUtil.getDateString(userInfo.curreaddate));
                } else {
                    valueList.add("");
                }
                appendSplitChar(isLast, splitChar);
            }
            //通道板号
            if (KEY_CHANNEL_NUMBER.equals(filedName)) {
                valueList.add(userInfo.channelNumber);
                appendSplitChar(isLast, splitChar);
            }
            //通道板地址
            if (KEY_CHANNEL_ADDRESS.equals(filedName)) {
                valueList.add(userInfo.channelAddress);
                appendSplitChar(isLast, splitChar);
            }
            //表地址
            if (KEY_METER_ADDRESS.equals(filedName)) {
                valueList.add(userInfo.meteraddr);
                appendSplitChar(isLast, splitChar);
            }
            //厂商代码
            if (KEY_FIRM_CODE.equals(filedName)) {
                valueList.add(userInfo.firmCode);
                appendSplitChar(isLast, splitChar);
            }
            if (KEY_READ_STATE.equals(filedName)) {
                switch (userInfo.state) {
                    case STATE_SUCCESS:
                        valueList.add(context.getString(R.string.state_success));
                        break;
                    case STATE_NO_READ:
                        valueList.add(context.getString(R.string.state_no_read));
                        break;
                    case STATE_PEOPLE_RECORDING:
                        valueList.add(context.getString(R.string.state_people_record));
                        break;
                    case STATE_WARNING:
                        valueList.add(context.getString(R.string.state_warning));
                        break;
                    case STATE_FAILED:
                        valueList.add(context.getString(R.string.state_failed));
                        break;
                    default:
                        valueList.add(context.getString(R.string.state_unknown));
                        break;
                }
                appendSplitChar(isLast, splitChar);
            }
        }
    }

    private void appendSplitChar(boolean isLast, String splitChar) {
        if (!isLast) {
            valueList.add(splitChar);
        }
    }

    private void sendErrorToast(final String errorMessage) {
        if (mOnProgressListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (EmiConfig.isDebug) {
                        mOnProgressListener.onError(errorMessage);
                    } else {
                        mOnProgressListener.onError("导出异常");
                    }
                }
            });
        }
    }
}
