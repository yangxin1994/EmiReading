package com.emi.emireading.ui.load;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.emi.emireading.EmiReadingApplication;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.FileUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.listener.OnProgressListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import static com.emi.emireading.core.config.EmiConstants.FIRM_CODE_1001;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_TXT;

/**
 * @author :zhoujian
 * @description : 生成固定格式的抄表需要的txt文件
 * @company :翼迈科技
 * @date 2018年03月06日下午 01:07
 * @Email: 971613168@qq.com
 */

public class CreateTxtFile {
    private OnProgressListener mOnProgressListener;

    /**
     * 数据源集合
     */
    private ArrayList<UserInfo> userInfoArrayList;

    private ArrayList<String> txtDataList;

    private Handler handler = new Handler(Looper.getMainLooper());
    /**
     * 需要生成的文件名
     */
    private String fileName;

    public CreateTxtFile(ArrayList<UserInfo> userInfoArrayList, String fileName) {
        this.userInfoArrayList = userInfoArrayList;
        this.fileName = fileName;
        txtDataList = new ArrayList<>();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public ArrayList<UserInfo> getUserInfoArrayList() {
        return userInfoArrayList;
    }


    public void setUserInfoArrayList(ArrayList<UserInfo> userInfoArrayList) {
        this.userInfoArrayList = userInfoArrayList;
    }


    private void addDataToList() {
        if (userInfoArrayList != null && !userInfoArrayList.isEmpty()) {
            txtDataList.clear();
            //只取一条数据，方便排查问题
//            txtDataList.add(convertString(userInfoArrayList.get(0)));
            txtDataList.add(fileName);
        }
    }

    private String convertString(UserInfo userInfo) {
        if (userInfo != null) {
            StringBuilder sb = new StringBuilder("");
            sb.append(userInfo.accountnum);
            sb.append("$");
            sb.append(userInfo.username);
            sb.append("$");
            sb.append(userInfo.useraddr);
            sb.append("$");
            sb.append(userInfo.lastdata);
            sb.append("$");
            //由于未抄表所以为0
            sb.append(userInfo.curdata);
            sb.append("$");
            sb.append(userInfo.state);
            sb.append("$");
            if (userInfo.channel != null) {
                sb.append(userInfo.channel.trim());
            } else {
                sb.append("");
            }
            sb.append("$");
            if (userInfo.meteraddr != null) {
                sb.append(userInfo.meteraddr.trim());
            } else {
                sb.append("");
            }
            sb.append("$");
            if (TextUtils.isEmpty(userInfo.firmCode)) {
                userInfo.firmCode = EmiConstants.FIRM_CODE_7833;
            } else if (EmiConstants.FIRM_CODE_0110.equals(userInfo.firmCode)) {
                userInfo.firmCode = FIRM_CODE_1001;
            }
            LogUtil.d("厂商代码：" + userInfo.firmCode.trim());
            sb.append(userInfo.firmCode.trim());
            sb.append("$");
            sb.append(userInfo.filename);
            sb.append("$");
            sb.append(userInfo.fileType);
            sb.append("$");
            sb.append(userInfo.lastyl);
            sb.append("$");
            sb.append(userInfo.dirname);
            sb.append("$");
            if (EmiStringUtil.isEmpty(userInfo.channelAddress)) {
                sb.append(userInfo.useraddr);
            } else {
                sb.append(userInfo.channelAddress);
            }
            sb.append("$");
            sb.append(userInfo.filePath);
            sb.append("$");
            LogUtil.i("convertString--->" + sb.toString());
            return sb.append(userInfo.waterId).toString();
        } else {
            LogUtil.e("UserInfo为null");
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    public boolean createTxtFile() {
        addDataToList();
        fileName = FileUtil.clearFileSuffix(fileName);
        String filePath = EmiConfig.TempPath + "/" + fileName + SUFFIX_TXT;
        File file = new File(filePath);
        try {
            /**
             * todo:此处改为只生成一条数据,用来排查数据问题，因为数据源并不是从该文件中获取
             */
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            if (!txtDataList.isEmpty()) {
                bw.write(txtDataList.get(0));
                bw.newLine();
            }
            bw.close();
            notifyFileExplore(filePath);
            txtDataList.clear();
            if (mOnProgressListener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mOnProgressListener.onFinish(txtDataList, fileName);
                    }
                });
            }
            return true;
        } catch (final Exception e) {
            if (mOnProgressListener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mOnProgressListener.onError(e.toString());
                    }
                });
            }
            LogUtil.e("createTxtFile()-->" + e.toString());
            return false;
        }
    }

    private void notifyFileExplore(String filePath) {
        File file = new File(filePath);
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        EmiReadingApplication.getContext().sendBroadcast(intent);
    }


    public void setOnProgressListener(OnProgressListener onProgressListener) {
        this.mOnProgressListener = onProgressListener;
    }
}