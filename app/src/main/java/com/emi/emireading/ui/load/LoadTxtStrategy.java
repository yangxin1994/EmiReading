package com.emi.emireading.ui.load;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.emi.emireading.core.common.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.core.utils.FileUtil;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.listener.OnProgressListener;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.emi.emireading.core.config.EmiConfig.CHANNEL_INDEX;
import static com.emi.emireading.core.config.EmiConfig.FIRM_CODE_INDEX;
import static com.emi.emireading.core.config.EmiConfig.IS_MATCH_SAME;
import static com.emi.emireading.core.config.EmiConfig.LAST_READING_INDEX;
import static com.emi.emireading.core.config.EmiConfig.LAST_USAGE_INDEX;
import static com.emi.emireading.core.config.EmiConfig.METER_ID_INDEX;
import static com.emi.emireading.core.config.EmiConfig.USER_ADDRESS_INDEX;
import static com.emi.emireading.core.config.EmiConfig.USER_ID_INDEX;
import static com.emi.emireading.core.config.EmiConfig.USER_NAME_INDEX;
import static com.emi.emireading.core.config.EmiConfig.isDebug;

/**
 * @author :zhoujian
 * @description : 加载txt文件
 * @company :翼迈科技
 * @date 2018年05月07日下午 03:12
 * @Email: 971613168@qq.com
 */

public class LoadTxtStrategy {
    private static final String TAG = "LoadTxtStrategy";
    private ArrayList<UserInfo> mUserInfoArrayList = new ArrayList<>();
    private ArrayList<String> txtDataBookList = new ArrayList<>();
    private ArrayList<String> txtData555List = new ArrayList<>();
    /**
     * 555文件路径
     */
    private String emiFilePath;
    /**
     * 表册文件路径
     */
    private String bookFilePath;
    /**
     * 表册文件信息实体集合
     */
    private ArrayList<UserInfo> userListBook = new ArrayList<>();
    /**
     * 555文件信息实体集合
     */
    private ArrayList<UserInfo> userList555 = new ArrayList<>();
    private Handler handler;
    private OnProgressListener onProgressListener;
    private String mFileName;
    private String dirName;
    private boolean isLoadFinish555;
    private boolean isLoadFinishBook;
    private String mLoadStrategyJson = "";

    public LoadTxtStrategy() {

    }

    public LoadTxtStrategy(String emiFilePath, String bookFilePath, String dirName, OnProgressListener onProgressListener) {
        this.emiFilePath = emiFilePath;
        this.bookFilePath = bookFilePath;
        this.onProgressListener = onProgressListener;
        handler = new Handler(Looper.getMainLooper());
        mFileName = FileUtil.getFileName(bookFilePath, false) + EmiConstants.SUFFIX_TXT;
        this.dirName = dirName;
    }

    public LoadTxtStrategy(String emiFilePath, String bookFilePath, String dirName) {
        this.emiFilePath = emiFilePath;
        this.bookFilePath = bookFilePath;
        handler = new Handler(Looper.getMainLooper());
        mFileName = FileUtil.getFileName(bookFilePath, false) + EmiConstants.SUFFIX_TXT;
        this.dirName = dirName;
        if (EmiConfig.loadStrategy != null && !TextUtils.isEmpty(EmiConfig.loadStrategy.getCity())) {
            mLoadStrategyJson = JSON.toJSONString(EmiConfig.loadStrategy);
        }
    }


    private void onError(final String errorMsg) {
        if (onProgressListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onProgressListener.onError(errorMsg);
                }
            });
        }
    }

    private void merge() {
        long startTime = System.currentTimeMillis();
        UserInfo userInfo;
        mUserInfoArrayList.clear();
        boolean isOpenListen = onProgressListener != null;
        DecimalFormat decimalFormat = new DecimalFormat("######0");
        int progress;
        double allCount = userListBook.size();
        LogUtil.d("数据源长度：" + userListBook.size());
        double currentCount;
        for (int i = 0; i < userListBook.size(); i++) {
            userInfo = new UserInfo();
            userInfo.loadStrategyJson = mLoadStrategyJson;
            userInfo.accountnum = userListBook.get(i).accountnum.trim();
            userInfo.username = userListBook.get(i).username;
            userInfo.useraddr = userListBook.get(i).useraddr;
            //默认未抄
            userInfo.state = EmiConstants.STATE_NO_READ;
            userInfo.lastdata = userListBook.get(i).lastdata;
            userInfo.lastyl = userListBook.get(i).lastyl;
            userInfo.meteraddr = "";
            userInfo.firmCode = "";
            userInfo.filename = mFileName;
            userInfo.dirname = dirName;
            userInfo.filePath = emiFilePath;
            userInfo.channel = "";
            for (int j = 0; j < userList555.size(); j++) {
                if (userListBook.get(i).accountnum.trim().equalsIgnoreCase(userList555.get(j).accountnum.trim())) {
                    userInfo.channelNumber = userList555.get(j).channelNumber.trim();
                    userInfo.meteraddr = EmiStringUtil.clearFirstZero(userList555.get(j).meteraddr.trim());
                    userInfo.firmCode = userList555.get(j).firmCode.trim();
                    if (EmiConstants.FIRM_CODE_0110.equals(userInfo.firmCode)) {
                        userInfo.firmCode = EmiConstants.FIRM_CODE_1001;
                    }
                    if (isOpenListen) {
                        currentCount = i;
                        progress = Integer.parseInt(decimalFormat.format((currentCount / allCount) * 100));
                        onProgressListener.onProgress(progress);
                        if (IS_MATCH_SAME) {
                            mUserInfoArrayList.add(userInfo);
                        }
                    }
                }
            }
            if (!IS_MATCH_SAME) {
                mUserInfoArrayList.add(userInfo);
            }
        }
        onFinish();
        long endTime = System.currentTimeMillis();
        LogUtil.w(TAG, "消耗时间:" + (endTime - startTime));
    }

    @SuppressWarnings("unchecked")
    private void onFinish() {
        if (onProgressListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onProgressListener.onFinish(mUserInfoArrayList, mFileName);
                }
            });
        }
    }


    public void setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
    }

    public void loadTxt() {
        isLoadFinish555 = false;
        isLoadFinishBook = false;
        ThreadPoolManager.EXECUTOR.execute(new LoadTextRunnable(emiFilePath, new ReadTextListener() {
            @Override
            public void onReadFinish(ArrayList<String> txtList) {
                isLoadFinish555 = true;
                txtData555List = txtList;
                LogUtil.d(TAG, "解析的555文件长度：" + txtData555List.size());
                if (isLoadFinishBook) {
                    doMerge();
                }
            }
        }));
        ThreadPoolManager.EXECUTOR.execute(new LoadTextRunnable(bookFilePath, new ReadTextListener() {
            @Override
            public void onReadFinish(ArrayList<String> txtList) {
                isLoadFinishBook = true;
                txtDataBookList = txtList;
                LogUtil.d(TAG, "解析的表册文件长度：" + txtDataBookList.size());
                if (isLoadFinish555) {
                    doMerge();
                }
            }
        }));
    }


    /**
     * 解析文本数据并添加到集合
     *
     * @param filePath
     * @param
     */
    private void addTXTtoList(String filePath, ReadTextListener readTextListener) {
        synchronized (this) {
            long startTime = System.currentTimeMillis();
            ArrayList<String> resultList = new ArrayList<>();
            FileInputStream fis;
            InputStreamReader inputReader;
            BufferedReader bufferedReader;
            try {
                fis = new FileInputStream(filePath);
                try {
                    inputReader = new InputStreamReader(fis, "gbk");
                    bufferedReader = new BufferedReader(inputReader);
                    String lineTxt;
                    try {
                        while ((lineTxt = bufferedReader.readLine()) != null) {
                            resultList.add(lineTxt);
                        }
                        long endTime = System.currentTimeMillis();
                        long time = endTime - startTime;
                        LogUtil.d("当前线程id：" + Thread.currentThread().getId() + "已经执行结束" + "，消耗时间：" + time);
                        LogUtil.d(TAG, "当前线程id：" + Thread.currentThread().getId() + "的txtList长度：" + resultList.size());
                        readTextListener.onReadFinish(resultList);
                    } catch (IOException e) {
                        e.printStackTrace();
                        onError(onProgressListener, e.toString());
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    onError(onProgressListener, e.toString());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                onError(onProgressListener, e.toString());
            }
        }
    }


    private void onError(final OnProgressListener onProgressListener, final String errorMsg) {
        if (onProgressListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onProgressListener.onError(errorMsg);
                }
            });
        }

    }

    private void doMerge() {
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                if (isDebug) {
                    loadBean();
                    merge();
                } else {
                    try {
                        loadBean();
                        merge();
                    } catch (NumberFormatException e) {
                        onError("加载异常");
                        LogUtil.e(TAG, e.toString());
                    }
                }

            }
        });

    }


    private interface ReadTextListener {
        /**
         * 读取文本结束
         *
         * @param txtList
         */
        void onReadFinish(ArrayList<String> txtList);
    }


    private class LoadTextRunnable implements Runnable {
        private String filePath;
        private ReadTextListener readTextListener;

        public LoadTextRunnable(String filePath, ReadTextListener readTextListener) {
            this.filePath = filePath;
            this.readTextListener = readTextListener;
        }

        @Override
        public void run() {
            addTXTtoList(filePath, readTextListener);
        }
    }

    private void loadBean() {
        long beginTime = System.currentTimeMillis();
        UserInfo data555Bean;
        String[] data555Array;
        UserInfo customDataBean;
        String[] bookDataArray;
        for (String txt555Msg : txtData555List) {
            data555Array = txt555Msg.split("\\$");
            if (data555Array.length > EmiConfig.FIELD_COUNT - 2) {
                data555Bean = new UserInfo();
                data555Bean.accountnum = data555Array[USER_ID_INDEX].trim();
                data555Bean.channelNumber = data555Array[CHANNEL_INDEX].trim();
                data555Bean.channel = "";
                data555Bean.meteraddr = data555Array[METER_ID_INDEX].trim();
                data555Bean.firmCode = data555Array[FIRM_CODE_INDEX].trim();
                userList555.add(data555Bean);
            }
        }
        for (String txtBookMsg : txtDataBookList) {
            bookDataArray = txtBookMsg.split("\\$");
            if (bookDataArray.length > EmiConfig.FIELD_COUNT) {
                customDataBean = new UserInfo();
                customDataBean.accountnum = bookDataArray[USER_ID_INDEX];
                customDataBean.username = bookDataArray[USER_NAME_INDEX];
                customDataBean.useraddr = bookDataArray[USER_ADDRESS_INDEX];
                //默认未抄
                customDataBean.state = EmiConstants.STATE_NO_READ;
                try {
                    customDataBean.lastdata = Integer.parseInt(bookDataArray[LAST_READING_INDEX]);
                    customDataBean.lastyl = Integer.parseInt(bookDataArray[LAST_USAGE_INDEX]);
                } catch (NumberFormatException e) {
                    customDataBean.lastdata = 0;
                    customDataBean.lastyl = 0;
                }
                userListBook.add(customDataBean);
            }
        }
        long endTime = System.currentTimeMillis();
        LogUtil.d(TAG, "表册文件长度：" + userListBook.size());
        LogUtil.d(TAG, "555文件长度：" + userList555.size());
        LogUtil.i(TAG, "消耗时间:" + (endTime - beginTime));
    }


    public ArrayList<String> getTxtData(String filePath) {
        ArrayList<String> resultList = new ArrayList<>();
        FileInputStream fis;
        InputStreamReader inputReader;
        BufferedReader bufferedReader;
        long startTime = System.currentTimeMillis();
        try {
            fis = new FileInputStream(filePath);
            try {
                inputReader = new InputStreamReader(fis, "gbk");
                bufferedReader = new BufferedReader(inputReader);
                String lineTxt;
                try {
                    while ((lineTxt = bufferedReader.readLine()) != null) {
                        resultList.add(lineTxt);
                    }
                    long endTime = System.currentTimeMillis();
                    long time = endTime - startTime;
                    LogUtil.d("当前线程id：" + Thread.currentThread().getId() + "已经执行结束" + "，消耗时间：" + time);
                } catch (IOException e) {
                    e.printStackTrace();
                    onError(onProgressListener, e.toString());
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                onError(onProgressListener, e.toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            onError(onProgressListener, e.toString());
        }
        return resultList;
    }
}


