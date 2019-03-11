package com.emi.emireading.core.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.EmiStringUtil;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.listener.OnProgressListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.emi.emireading.core.config.EmiConstants.STATE_TAG;
import static com.emi.emireading.core.config.EmiConstants.UPLOAD_STATE_KEY;


public class MyOperator {
    /**
     * 要操作的数据表的名称
     */
    private static final String TABLE_NAME = "emi_data";
    /**
     * 数据库操作
     */
    private SQLiteDatabase db = null;
    private static final String TAG = "MyOperator";
    private Handler handler = new Handler(Looper.getMainLooper());
    private OnProgressListener<ArrayList<UserInfo>> onProgressListener;

    /**
     * 构造函数
     *
     * @param db
     */
    public MyOperator(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * 插入重载操作
     *
     * @param yhxx
     */
    public void insert(UserInfo yhxx) {
        if (yhxx != null) {
            String sql = "INSERT INTO " + TABLE_NAME + " (filename,accountnum,meteraddr,curdata,lastdata,curyl,readmonth,useraddr,state,username,lastyl,curreaddate,channel,rereadflag,fileType,uploadState,channelAddress,channelNumber,dirname,filePath,firmCode,waterId,hasExport,loadStrategyJson,meterTag)" + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            Object[] args = new Object[]{yhxx.getFilename(), yhxx.getAccountnum(), yhxx.getMeteraddr(), yhxx.getCurdata(), yhxx.getLastdata(), yhxx.getCuryl(),
                    yhxx.getReadmonth(), yhxx.getUseraddr(), yhxx.getState(), yhxx.getUsername(), yhxx.getLastyl(), yhxx.getCurreaddate(), yhxx.getChannel(), yhxx.getRereadflag(), yhxx.getFileType(), yhxx.getUploadState(), yhxx.getChannelAddress(), yhxx.getChannelNumber(), yhxx.getDirname(), yhxx.getFilePath(), yhxx.getFirmCode(), yhxx.waterId, yhxx.hasExport, yhxx.loadStrategyJson, yhxx.meterTag};
            this.db.execSQL(sql, args);
            this.db.close();
        }
    }

    public void insert(final ArrayList<UserInfo> userInfoList) {
        if (userInfoList != null && db != null) {
            removeEmptyUserInfo(userInfoList);
            DecimalFormat decimalFormat = new DecimalFormat("######0");
            int progress;
            double allCount;
            double currentCount;
            db.beginTransaction();
            String sql;
            String fileName = "";
            UserInfo yhxx;
            Object[] args;
            try {
                allCount = userInfoList.size();
                if (allCount > 0) {
                    fileName = userInfoList.get(0).filename;
                }
                for (int i = 0; i < allCount; i++) {
                    yhxx = userInfoList.get(i);
                    currentCount = i;
                    if (yhxx != null) {
                        sql = "INSERT INTO " + TABLE_NAME + " (filename,accountnum,meteraddr,curdata,lastdata,curyl,readmonth,useraddr,state,username,lastyl,curreaddate,channel,rereadflag,fileType,uploadState,channelAddress,channelNumber,dirname,filePath,firmCode,waterId,hasExport,loadStrategyJson,meterTag)" + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                        args = new Object[]{yhxx.getFilename(), yhxx.getAccountnum(), yhxx.getMeteraddr(), yhxx.getCurdata(), yhxx.getLastdata(), yhxx.getCuryl(),
                                yhxx.getReadmonth(), yhxx.getUseraddr(), yhxx.getState(), yhxx.getUsername(), yhxx.getLastyl(), yhxx.getCurreaddate(), yhxx.getChannel(), yhxx.getRereadflag(), yhxx.getFileType(), yhxx.getUploadState(), yhxx.getChannelAddress(), yhxx.getChannelNumber(), yhxx.getDirname(), yhxx.getFilePath(), yhxx.getFirmCode(), yhxx.waterId, yhxx.hasExport, yhxx.loadStrategyJson, yhxx.meterTag};
                        this.db.execSQL(sql, args);
                        if (onProgressListener != null) {
                            progress = Integer.parseInt(decimalFormat.format((currentCount / allCount) * 100));
                            onProgressListener.onProgress(progress);
                        }
                    }
                }
                db.setTransactionSuccessful();
                if (onProgressListener != null) {
                    final String finalFileName = fileName;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onProgressListener.onFinish(userInfoList, finalFileName);
                        }
                    });
                }
            } catch (final Exception e) {
                LogUtil.e("数据库操作异常:" + e.toString());
                if (onProgressListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onProgressListener.onError(e.toString());
                        }
                    });
                }
                throw e;
            } finally {
                db.endTransaction();
            }
        }
    }

    /**
     * 更新抄表数据
     *
     * @param accountnum
     * @param a
     * @param b
     * @param c
     * @param d
     */
    public void Update1(String accountnum, int a, int b, int c, String d) {
        String sql = "UPDATE " + TABLE_NAME + " SET curdata=?,curyl=?,state=?,curreaddate=? WHERE accountnum=?";
        Object[] args = new Object[]{a, b, c, d, accountnum};
        this.db.execSQL(sql, args);
        this.db.close();
    }


    /**
     * 更新抄表数据
     *
     * @param useId
     * @param currentData
     * @param currentYl
     * @param state
     * @param readDate
     */
    public void updateData(String fileName, String useId, int currentData, int currentYl, int state, String readDate, String channel) {
        LogUtil.d(TAG, "保存的状态：" + state);
        LogUtil.d(TAG, "保存的文件名：" + fileName);
        String sql = "UPDATE " + TABLE_NAME + " SET curdata=?,curyl=?,state=?,curreaddate=?,channel=? WHERE accountnum=? and filename =?";
        Object[] args = new Object[]{currentData, currentYl, state, readDate, channel, useId, fileName};
        this.db.execSQL(sql, args);
        this.db.close();
    }


    public void updateData(List<UserInfo> userInfoList) {
        String sql = "UPDATE " + TABLE_NAME + " SET curdata=?,curyl=?,state=?,curreaddate=?,channel=?,hasExport=? WHERE accountnum=? and meteraddr =?";
        Object[] args;
        if (userInfoList != null && !userInfoList.isEmpty()) {
            db.beginTransaction();
            for (UserInfo userInfo : userInfoList) {
                if (userInfo != null) {
                    args = new Object[]{userInfo.curdata, userInfo.curyl, userInfo.state, userInfo.curreaddate, userInfo.channel, userInfo.hasExport, userInfo.accountnum, userInfo.meteraddr};
                    this.db.execSQL(sql, args);
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    /**
     * 更新抄表数据
     *
     * @param useId
     * @param currentData
     * @param currentYl
     * @param state
     * @param readDate
     */
    public void updateData(String useId, int currentData, int currentYl, int state, String readDate) {
        String sql = "UPDATE " + TABLE_NAME + " SET curdata=?,curyl=?,state=?,curreaddate=? WHERE accountnum=?";
        Object[] args = new Object[]{currentData, currentYl, state, readDate, useId};
        this.db.execSQL(sql, args);
        this.db.close();
    }


    public void updateData(UserInfo userInfo) {
        String sql = "UPDATE " + TABLE_NAME + " SET curdata=?,curyl=?,state=?,curreaddate=? WHERE meteraddr=?";
        int lastYL = userInfo.curdata - userInfo.lastdata;
        userInfo.meteraddr = EmiStringUtil.clearFirstZero(userInfo.meteraddr);
        Object[] args = new Object[]{userInfo.curdata, lastYL, userInfo.state, userInfo.curreaddate, userInfo.meteraddr};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    /**
     * 更新任务
     *
     * @param accountnum
     * @param curdata
     * @param curyl
     * @param state
     * @param date
     * @param chanel
     */
    public void UpdateRw(String accountnum, int curdata, int curyl, int state, String date, String chanel) {
        String sql = "UPDATE " + TABLE_NAME + " SET curdata=?,curyl=?,state=?,curreaddate=?,channel=? WHERE accountnum=?";
        Object[] args = new Object[]{curdata, curyl, state, date, chanel, accountnum};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    public void Update(String accountnum, int data) {
        String sql = "UPDATE " + TABLE_NAME + " SET curdata=? WHERE accountnum=?";
        Object[] args = new Object[]{data, accountnum};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    public void Updatecuryl(String accountnum, int data) {
        String sql = "UPDATE " + TABLE_NAME + " SET curyl=? WHERE accountnum=?";
        Object[] args = new Object[]{data, accountnum};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    public void UpdateState(String accountnum, int state) {
        String sql = "UPDATE " + TABLE_NAME + " SET state=? WHERE accountnum=?";
        Object[] args = new Object[]{state, accountnum};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    public void updateState(List<UserInfo> userInfoList, int state) {
        String sql = "UPDATE " + TABLE_NAME + " SET state=?,curreaddate =?,channel =? WHERE meteraddr =?";
        Object[] args;
        if (userInfoList != null && !userInfoList.isEmpty()) {
            db.beginTransaction();
            for (UserInfo userInfo : userInfoList) {
                if (userInfo != null) {
                    args = new Object[]{state, userInfo.curreaddate, userInfo.channel, userInfo.meteraddr};
                    this.db.execSQL(sql, args);
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        }

    }

    public void updateMeterInfo(String fileName, String useId, String meterId, String channel, String firmCode) {
        //		" SET curdata=?,curyl=?,state=?,curreaddate=?,channel=? WHERE accountnum=?";
        String sql = "UPDATE " + TABLE_NAME + " SET meteraddr=?,channelNumber=?,firmCode=? WHERE filename=? and accountnum =?";
        Object[] args = new Object[]{meterId, channel, firmCode, fileName, useId};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    public void updateUploadState(String accountnum, int uploadState) {
        String sql = "UPDATE " + TABLE_NAME + " SET uploadState=? WHERE accountnum=?";
        Object[] args = new Object[]{uploadState, accountnum};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    public void Updatereaddate(String accountnum, String date) {
        String sql = "UPDATE " + TABLE_NAME + " SET curreaddate=? WHERE accountnum=?";
        Object[] args = new Object[]{date, accountnum};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    public void Updaterereadflag(String accountnum, int data) {
        String sql = "UPDATE " + TABLE_NAME + " SET rereadflag=? WHERE accountnum=?";
        Object[] args = new Object[]{data, accountnum};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    //	    //更新读数操作
    public void updateCurdata(String accountid, int data) {
        String sql = "UPDATE " + TABLE_NAME + " SET curdata=? WHERE accountnum=?";
        Object[] args = new Object[]{data, accountid};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    /**
     * 删除操作,删除
     *
     * @param fileName
     */
    public void delete(String fileName) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE filename like  '%" + fileName + "%'";
        Object[] args = new Object[]{};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    public void deleteAll() {

        // String sql = "SELECT * FROM " + TABLE_NAME +" where useraddr like  '%" +uptownname+"%'";
        String sql = "DELETE FROM " + TABLE_NAME + "";

        Object[] args = new Object[]{};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    /**
     * //查询操作,查询表中所有的记录返回列表
     *
     * @return
     */
    public List<UserInfo> find() {
        List<UserInfo> all = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME;
        Cursor result = this.db.rawQuery(sql, null);
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            UserInfo yhxx = new UserInfo();
            yhxx.accountnum = result.getString(result.getColumnIndex("accountnum"));
            yhxx.meteraddr = result.getString(result.getColumnIndex("meteraddr"));
            yhxx.curdata = result.getInt(result.getColumnIndex("curdata"));
            yhxx.lastdata = result.getInt(result.getColumnIndex("lastdata"));
            yhxx.curyl = result.getInt(result.getColumnIndex("curyl"));
            yhxx.readmonth = result.getInt(result.getColumnIndex("readmonth"));
            yhxx.useraddr = result.getString(result.getColumnIndex("useraddr"));
            yhxx.state = result.getInt(result.getColumnIndex("state"));
            yhxx.username = result.getString(result.getColumnIndex("username"));
            yhxx.lastyl = result.getInt(result.getColumnIndex("lastyl"));
            yhxx.curreaddate = result.getString(result.getColumnIndex("curreaddate"));
            yhxx.channel = result.getString(result.getColumnIndex("channel"));
            yhxx.firmCode = result.getString(result.getColumnIndex("firmCode"));
            yhxx.fileType = result.getString(result.getColumnIndex("fileType"));
            yhxx.channelAddress = result.getString(result.getColumnIndex("channelAddress"));
            yhxx.uploadState = result.getInt(result.getColumnIndex(UPLOAD_STATE_KEY));
            yhxx.loadStrategyJson = result.getString(result.getColumnIndex("loadStrategyJson"));
            yhxx.meterTag = result.getInt(result.getColumnIndex("meterTag"));
            all.add(yhxx);
        }
        result.close();
        this.db.close();
        return all;
    }

    /**
     * 查询操作,查询表中所有的记录返回列表
     *
     * @param filename
     * @return
     */
    public String findDirName(String filename) {
        String sql = "SELECT dirname from " + TABLE_NAME + " where filename = ?";
        String[] args = new String[]{filename};
        String dirname = "";
        //执行查询语句
        Cursor result = this.db.rawQuery(sql, args);
        //采用循环的方式查询数据
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            dirname = result.getString(result.getColumnIndex("dirname"));
            break;
        }
        result.close();
        this.db.close();
        return dirname;
    }

    /**
     * 查询操作,查询表中所有的记录返回列表
     *
     * @param filename
     * @return
     */
    public List<UserInfo> find(String filename) {
        //此时只是String
        List<UserInfo> all = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " where filename = ?";
        String[] args = new String[]{filename};
        //执行查询语句
        Cursor result = this.db.rawQuery(sql, args);
        //采用循环的方式查询数据
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            UserInfo yhxx = new UserInfo();
            yhxx.accountnum = result.getString(result.getColumnIndex("accountnum"));
            yhxx.meteraddr = result.getString(result.getColumnIndex("meteraddr"));
            yhxx.curdata = result.getInt(result.getColumnIndex("curdata"));
            yhxx.lastdata = result.getInt(result.getColumnIndex("lastdata"));
            yhxx.curyl = result.getInt(result.getColumnIndex("curyl"));
            yhxx.readmonth = result.getInt(result.getColumnIndex("readmonth"));
            yhxx.useraddr = result.getString(result.getColumnIndex("useraddr"));
            yhxx.state = result.getInt(result.getColumnIndex("state"));
            yhxx.username = result.getString(result.getColumnIndex("username"));
            yhxx.lastyl = result.getInt(result.getColumnIndex("lastyl"));
            yhxx.curreaddate = result.getString(result.getColumnIndex("curreaddate"));
            yhxx.channel = result.getString(result.getColumnIndex("channel"));
            yhxx.fileType = result.getString(result.getColumnIndex("fileType"));
            yhxx.dirname = result.getString(result.getColumnIndex("dirname"));
            yhxx.filename = result.getString(result.getColumnIndex("filename"));
            yhxx.uploadState = result.getInt(result.getColumnIndex(UPLOAD_STATE_KEY));
            yhxx.channelAddress = result.getString(result.getColumnIndex("channelAddress"));
            yhxx.channelNumber = result.getString(result.getColumnIndex("channelNumber"));
            yhxx.filePath = result.getString(result.getColumnIndex("filePath"));
            yhxx.firmCode = result.getString(result.getColumnIndex("firmCode"));
            yhxx.waterId = result.getString(result.getColumnIndex("waterId"));
            yhxx.hasExport = result.getInt(result.getColumnIndex("hasExport"));
            yhxx.loadStrategyJson = result.getString(result.getColumnIndex("loadStrategyJson"));
            yhxx.meterTag = result.getInt(result.getColumnIndex("meterTag"));
            all.add(yhxx);
        }
        result.close();
        this.db.close();
        return all;
    }


    /**
     * 查询操作,查询表中所有的记录返回列表
     *
     * @param filename
     * @return
     */
    public List<UserInfo> findByChannel(String filename, String channelNumber) {
        //此时只是String
        List<UserInfo> all = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " where filename = ? and channelNumber = ?";
        String[] args = new String[]{filename, channelNumber};
        //执行查询语句
        Cursor result = this.db.rawQuery(sql, args);
        //采用循环的方式查询数据
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            UserInfo yhxx = new UserInfo();
            yhxx.accountnum = result.getString(result.getColumnIndex("accountnum"));
            yhxx.meteraddr = result.getString(result.getColumnIndex("meteraddr"));
            yhxx.curdata = result.getInt(result.getColumnIndex("curdata"));
            yhxx.lastdata = result.getInt(result.getColumnIndex("lastdata"));
            yhxx.curyl = result.getInt(result.getColumnIndex("curyl"));
            yhxx.readmonth = result.getInt(result.getColumnIndex("readmonth"));
            yhxx.useraddr = result.getString(result.getColumnIndex("useraddr"));
            yhxx.state = result.getInt(result.getColumnIndex("state"));
            yhxx.username = result.getString(result.getColumnIndex("username"));
            yhxx.lastyl = result.getInt(result.getColumnIndex("lastyl"));
            yhxx.curreaddate = result.getString(result.getColumnIndex("curreaddate"));
            yhxx.channel = result.getString(result.getColumnIndex("channel"));
            yhxx.fileType = result.getString(result.getColumnIndex("fileType"));
            yhxx.dirname = result.getString(result.getColumnIndex("dirname"));
            yhxx.uploadState = result.getInt(result.getColumnIndex(UPLOAD_STATE_KEY));
            yhxx.channelAddress = result.getString(result.getColumnIndex("channelAddress"));
            yhxx.channelNumber = result.getString(result.getColumnIndex("channelNumber"));
            yhxx.filePath = result.getString(result.getColumnIndex("filePath"));
            yhxx.firmCode = result.getString(result.getColumnIndex("firmCode"));
            yhxx.filename = result.getString(result.getColumnIndex("filename"));
            yhxx.waterId = result.getString(result.getColumnIndex("waterId"));
            yhxx.loadStrategyJson = result.getString(result.getColumnIndex("loadStrategyJson"));
            yhxx.meterTag = result.getInt(result.getColumnIndex("meterTag"));
            all.add(yhxx);
        }
        result.close();
        this.db.close();
        return all;
    }

    public List<UserInfo> findByChannel(String channelNumber) {
        //此时只是String
        List<UserInfo> all = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " where channelNumber = ?";
        String[] args = new String[]{channelNumber};
        //执行查询语句
        Cursor result = this.db.rawQuery(sql, args);
        //采用循环的方式查询数据
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            UserInfo yhxx = new UserInfo();
            yhxx.accountnum = result.getString(result.getColumnIndex("accountnum"));
            yhxx.meteraddr = result.getString(result.getColumnIndex("meteraddr"));
            yhxx.curdata = result.getInt(result.getColumnIndex("curdata"));
            yhxx.lastdata = result.getInt(result.getColumnIndex("lastdata"));
            yhxx.curyl = result.getInt(result.getColumnIndex("curyl"));
            yhxx.readmonth = result.getInt(result.getColumnIndex("readmonth"));
            yhxx.useraddr = result.getString(result.getColumnIndex("useraddr"));
            yhxx.state = result.getInt(result.getColumnIndex("state"));
            yhxx.username = result.getString(result.getColumnIndex("username"));
            yhxx.lastyl = result.getInt(result.getColumnIndex("lastyl"));
            yhxx.curreaddate = result.getString(result.getColumnIndex("curreaddate"));
            yhxx.channel = result.getString(result.getColumnIndex("channel"));
            yhxx.fileType = result.getString(result.getColumnIndex("fileType"));
            yhxx.dirname = result.getString(result.getColumnIndex("dirname"));
            yhxx.filename = result.getString(result.getColumnIndex("filename"));
            yhxx.uploadState = result.getInt(result.getColumnIndex(UPLOAD_STATE_KEY));
            yhxx.channelAddress = result.getString(result.getColumnIndex("channelAddress"));
            yhxx.channelNumber = result.getString(result.getColumnIndex("channelNumber"));
            yhxx.filePath = result.getString(result.getColumnIndex("filePath"));
            yhxx.firmCode = result.getString(result.getColumnIndex("firmCode"));
            yhxx.waterId = result.getString(result.getColumnIndex("waterId"));
            yhxx.loadStrategyJson = result.getString(result.getColumnIndex("loadStrategyJson"));
            yhxx.meterTag = result.getInt(result.getColumnIndex("meterTag"));
            all.add(yhxx);
        }
        result.close();
        this.db.close();
        return all;
    }

    public UserInfo queryByUserId(String filename, String userId) {
        UserInfo yhxx = null;
        String sql = "SELECT * FROM " + TABLE_NAME + " where filename = ? and accountnum = ? ";
        String[] args = new String[]{filename, userId};
        //执行查询语句
        Cursor result = this.db.rawQuery(sql, args);
        //采用循环的方式查询数据
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            yhxx = new UserInfo();
            yhxx.accountnum = result.getString(result.getColumnIndex("accountnum"));
            yhxx.meteraddr = result.getString(result.getColumnIndex("meteraddr"));
            yhxx.curdata = result.getInt(result.getColumnIndex("curdata"));
            yhxx.lastdata = result.getInt(result.getColumnIndex("lastdata"));
            yhxx.curyl = result.getInt(result.getColumnIndex("curyl"));
            yhxx.readmonth = result.getInt(result.getColumnIndex("readmonth"));
            yhxx.useraddr = result.getString(result.getColumnIndex("useraddr"));
            yhxx.state = result.getInt(result.getColumnIndex("state"));
            yhxx.username = result.getString(result.getColumnIndex("username"));
            yhxx.lastyl = result.getInt(result.getColumnIndex("lastyl"));
            yhxx.curreaddate = result.getString(result.getColumnIndex("curreaddate"));
            yhxx.channel = result.getString(result.getColumnIndex("channel"));
            yhxx.fileType = result.getString(result.getColumnIndex("fileType"));
            yhxx.channelAddress = result.getString(result.getColumnIndex("channelAddress"));
            yhxx.uploadState = result.getInt(result.getColumnIndex(UPLOAD_STATE_KEY));
            yhxx.channelNumber = result.getString(result.getColumnIndex("channelNumber"));
            yhxx.filePath = result.getString(result.getColumnIndex("filePath"));
            yhxx.firmCode = result.getString(result.getColumnIndex("firmCode"));
            yhxx.waterId = result.getString(result.getColumnIndex("waterId"));
            yhxx.loadStrategyJson = result.getString(result.getColumnIndex("loadStrategyJson"));
            yhxx.meterTag = result.getInt(result.getColumnIndex("meterTag"));
            break;
        }
        result.close();
        this.db.close();
        return yhxx;
    }


    public List<UserInfo> queryByMeterId(String filename, String meterId) {
        List<UserInfo> dataList = new ArrayList<>();
        UserInfo yhxx;
        String sql = "SELECT * FROM " + TABLE_NAME + " where filename = ? and meteraddr = ? ";
        String[] args = new String[]{filename, meterId};
        //执行查询语句
        Cursor result = this.db.rawQuery(sql, args);
        //采用循环的方式查询数据
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            yhxx = new UserInfo();
            yhxx.accountnum = result.getString(result.getColumnIndex("accountnum"));
            yhxx.filename = result.getString(result.getColumnIndex("filename"));
            yhxx.meteraddr = result.getString(result.getColumnIndex("meteraddr"));
            yhxx.curdata = result.getInt(result.getColumnIndex("curdata"));
            yhxx.lastdata = result.getInt(result.getColumnIndex("lastdata"));
            yhxx.curyl = result.getInt(result.getColumnIndex("curyl"));
            yhxx.readmonth = result.getInt(result.getColumnIndex("readmonth"));
            yhxx.useraddr = result.getString(result.getColumnIndex("useraddr"));
            yhxx.state = result.getInt(result.getColumnIndex("state"));
            yhxx.username = result.getString(result.getColumnIndex("username"));
            yhxx.lastyl = result.getInt(result.getColumnIndex("lastyl"));
            yhxx.curreaddate = result.getString(result.getColumnIndex("curreaddate"));
            yhxx.channel = result.getString(result.getColumnIndex("channel"));
            yhxx.fileType = result.getString(result.getColumnIndex("fileType"));
            yhxx.channelNumber = result.getString(result.getColumnIndex("channelNumber"));
            yhxx.channelAddress = result.getString(result.getColumnIndex("channelAddress"));
            yhxx.uploadState = result.getInt(result.getColumnIndex(UPLOAD_STATE_KEY));
            yhxx.filePath = result.getString(result.getColumnIndex("filePath"));
            yhxx.firmCode = result.getString(result.getColumnIndex("firmCode"));
            yhxx.waterId = result.getString(result.getColumnIndex("waterId"));
            yhxx.loadStrategyJson = result.getString(result.getColumnIndex("loadStrategyJson"));
            yhxx.meterTag = result.getInt(result.getColumnIndex("meterTag"));
            dataList.add(yhxx);
        }
        result.close();
        this.db.close();
        return dataList;
    }


    //查询操作,查询表中数据状态
    public int findState(String filename) {
        int state = 0;
        String sql = "SELECT state FROM " + TABLE_NAME + " where filename = ?";
        String[] args = new String[]{filename};
        //执行查询语句
        Cursor result = this.db.rawQuery(sql, args);
        //采用循环的方式查询数据
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            state = result.getInt(result.getColumnIndex("state"));
        }
        this.db.close();
        return state;
    }

    /**
     * 查询操作,查询表中数据状态
     *
     * @param acountnum
     * @return
     */
    public int findStateWithUserNO(String fileName, String acountnum) {
        int state = 0;
        String sql = "SELECT state FROM " + TABLE_NAME + " where filename = ?  and accountnum = ?";
        String[] args = new String[]{fileName, acountnum};
        //执行查询语句
        Cursor result = this.db.rawQuery(sql, args);
        //采用循环的方式查询数据
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            state = result.getInt(result.getColumnIndex("state"));
        }
        result.close();
        //this.db.close();
        return state;
    }


    /**
     * 根据用户编号和水表地址查询用户状态
     *
     * @param userId
     * @param meterId
     * @return
     */
    public int queryStateByUserInfo(String userId, String meterId) {
        int state = 0;
        String sql = "SELECT state FROM " + TABLE_NAME + " where accountnum = ?  and meteraddr = ?";
        String[] args = new String[]{userId, meterId};
        //执行查询语句
        Cursor result = this.db.rawQuery(sql, args);
        //采用循环的方式查询数据
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            state = result.getInt(result.getColumnIndex("state"));
        }
        result.close();
        //this.db.close();
        return state;
    }

    public List<UserInfo> findchannel(String channel) {
        //此时只是String
        List<UserInfo> all = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " where channel = ?";
        String[] args = new String[]{channel};
        //执行查询语句
        Cursor result = this.db.rawQuery(sql, args);
        //采用循环的方式查询数据
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            UserInfo yhxx = new UserInfo();
            yhxx.accountnum = result.getString(result.getColumnIndex("accountnum"));
            yhxx.meteraddr = result.getString(result.getColumnIndex("meteraddr"));
            yhxx.curdata = result.getInt(result.getColumnIndex("curdata"));
            yhxx.lastdata = result.getInt(result.getColumnIndex("lastdata"));
            yhxx.curyl = result.getInt(result.getColumnIndex("curyl"));
            yhxx.readmonth = result.getInt(result.getColumnIndex("readmonth"));
            yhxx.useraddr = result.getString(result.getColumnIndex("useraddr"));
            yhxx.state = result.getInt(result.getColumnIndex("state"));
            yhxx.uploadState = result.getInt(result.getColumnIndex(UPLOAD_STATE_KEY));
            yhxx.username = result.getString(result.getColumnIndex("username"));
            yhxx.lastyl = result.getInt(result.getColumnIndex("lastyl"));
            yhxx.curreaddate = result.getString(result.getColumnIndex("curreaddate"));
            yhxx.channelAddress = result.getString(result.getColumnIndex("channelAddress"));
            yhxx.channel = result.getString(result.getColumnIndex("channel"));
            yhxx.loadStrategyJson = result.getString(result.getColumnIndex("loadStrategyJson"));
            yhxx.meterTag = result.getInt(result.getColumnIndex("meterTag"));
            all.add(yhxx);
        }
        result.close();
        this.db.close();
        return all;
    }

    public String findsinglemeter(String meteraddr) {
        String sql = "SELECT useraddr FROM " + TABLE_NAME + " where meteraddr = ?";
        String[] args = new String[]{meteraddr};
        //执行查询语句
        Cursor result = this.db.rawQuery(sql, args);
        String value = null;
        if (result.moveToFirst()) {
            do {
                value = result.getString(result.getColumnIndex("useraddr"));
            } while (result.moveToNext());
        }
        result.close();
        this.db.close();
        return value;
    }

    public List<UserInfo> findchannel1(String channel, int RE) {
        //此时只是String
        List<UserInfo> all = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " where channel = ? and rereadflag = ?";
        String[] args = new String[]{channel, String.valueOf(RE)};
        //执行查询语句
        Cursor result = this.db.rawQuery(sql, args);
        //采用循环的方式查询数据
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            UserInfo yhxx = new UserInfo();
            yhxx.accountnum = result.getString(result.getColumnIndex("accountnum"));
            yhxx.meteraddr = result.getString(result.getColumnIndex("meteraddr"));
            yhxx.curdata = result.getInt(result.getColumnIndex("curdata"));
            yhxx.lastdata = result.getInt(result.getColumnIndex("lastdata"));
            yhxx.curyl = result.getInt(result.getColumnIndex("curyl"));
            yhxx.readmonth = result.getInt(result.getColumnIndex("readmonth"));
            yhxx.useraddr = result.getString(result.getColumnIndex("useraddr"));
            yhxx.state = result.getInt(result.getColumnIndex("state"));
            yhxx.username = result.getString(result.getColumnIndex("username"));
            yhxx.lastyl = result.getInt(result.getColumnIndex("lastyl"));
            yhxx.curreaddate = result.getString(result.getColumnIndex("curreaddate"));
            yhxx.channel = result.getString(result.getColumnIndex("channel"));
            yhxx.channelAddress = result.getString(result.getColumnIndex("channelAddress"));
            yhxx.fileType = result.getString(result.getColumnIndex("fileType"));
            yhxx.meterTag = result.getInt(result.getColumnIndex("meterTag"));
            all.add(yhxx);
        }
        result.close();
        this.db.close();
        return all;
    }


    /**
     * 查询操作,查询表中所有的记录返回列表
     *
     * @param filename
     * @param state
     * @return
     */
    public List<UserInfo> find(String filename, int state) {
        if (state == 0) {
            return find(filename);
        } else {
            //此时只是String
            List<UserInfo> all = new ArrayList<>();
            String sql = "SELECT * FROM " + TABLE_NAME + " where filename like  '%" + filename + "%'  and state =" + String.valueOf(state);
            //执行查询语句
            Cursor result = this.db.rawQuery(sql, null);
            //采用循环的方式查询数据
            for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
                UserInfo yhxx = new UserInfo();
                yhxx.accountnum = result.getString(result.getColumnIndex("accountnum"));
                yhxx.meteraddr = result.getString(result.getColumnIndex("meteraddr"));
                yhxx.curdata = result.getInt(result.getColumnIndex("curdata"));
                yhxx.lastdata = result.getInt(result.getColumnIndex("lastdata"));
                yhxx.curyl = result.getInt(result.getColumnIndex("curyl"));
                yhxx.readmonth = result.getInt(result.getColumnIndex("readmonth"));
                yhxx.useraddr = result.getString(result.getColumnIndex("useraddr"));
                yhxx.state = result.getInt(result.getColumnIndex("state"));
                yhxx.username = result.getString(result.getColumnIndex("username"));
                yhxx.lastyl = result.getInt(result.getColumnIndex("lastyl"));
                yhxx.curreaddate = result.getString(result.getColumnIndex("curreaddate"));
                yhxx.channel = result.getString(result.getColumnIndex("channel"));
                yhxx.fileType = result.getString(result.getColumnIndex("fileType"));
                yhxx.channelAddress = result.getString(result.getColumnIndex("channelAddress"));
                yhxx.uploadState = result.getInt(result.getColumnIndex("uploadState"));
                yhxx.meterTag = result.getInt(result.getColumnIndex("meterTag"));
                all.add(yhxx);
            }
            result.close();
            this.db.close();
            return all;
        }

    }

    public List<UserInfo> findDataByAccountId(String filename, String accountId) {
        //此时只是String
        List<UserInfo> all = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " where filename like  '%" + filename + "%'  and accountnum =" + accountId;
        //执行查询语句
        Cursor result = this.db.rawQuery(sql, null);
        //采用循环的方式查询数据
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            UserInfo yhxx = new UserInfo();
            yhxx.accountnum = result.getString(result.getColumnIndex("accountnum"));
            yhxx.meteraddr = result.getString(result.getColumnIndex("meteraddr"));
            yhxx.curdata = result.getInt(result.getColumnIndex("curdata"));
            yhxx.lastdata = result.getInt(result.getColumnIndex("lastdata"));
            yhxx.curyl = result.getInt(result.getColumnIndex("curyl"));
            yhxx.readmonth = result.getInt(result.getColumnIndex("readmonth"));
            yhxx.useraddr = result.getString(result.getColumnIndex("useraddr"));
            yhxx.state = result.getInt(result.getColumnIndex("state"));
            yhxx.username = result.getString(result.getColumnIndex("username"));
            yhxx.lastyl = result.getInt(result.getColumnIndex("lastyl"));
            yhxx.curreaddate = result.getString(result.getColumnIndex("curreaddate"));
            yhxx.channel = result.getString(result.getColumnIndex("channel"));
            yhxx.fileType = result.getString(result.getColumnIndex("fileType"));
            yhxx.channelAddress = result.getString(result.getColumnIndex("channelAddress"));
            yhxx.uploadState = result.getInt(result.getColumnIndex("uploadState"));
            yhxx.meterTag = result.getInt(result.getColumnIndex("meterTag"));
            all.add(yhxx);
        }
        result.close();
        this.db.close();
        return all;
    }

    /**
     * 查询操作,查询表中所有的记录返回列表
     *
     * @param filename
     * @param uploadState
     * @return
     */
    public List<UserInfo> findUploadData(String filename, int uploadState) {
        if (uploadState == 0) {
            return find(filename);
        } else {
            //此时只是String
            List<UserInfo> all = new ArrayList<>();
            String sql = "SELECT * FROM " + TABLE_NAME + " where filename like  '%" + filename + "%'  and uploadState =" + String.valueOf(uploadState);
            //执行查询语句
            Cursor result = this.db.rawQuery(sql, null);
            //采用循环的方式查询数据
            for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
                UserInfo yhxx = new UserInfo();
                yhxx.accountnum = result.getString(result.getColumnIndex("accountnum"));
                yhxx.meteraddr = result.getString(result.getColumnIndex("meteraddr"));
                yhxx.curdata = result.getInt(result.getColumnIndex("curdata"));
                yhxx.lastdata = result.getInt(result.getColumnIndex("lastdata"));
                yhxx.curyl = result.getInt(result.getColumnIndex("curyl"));
                yhxx.readmonth = result.getInt(result.getColumnIndex("readmonth"));
                yhxx.useraddr = result.getString(result.getColumnIndex("useraddr"));
                yhxx.state = result.getInt(result.getColumnIndex("state"));
                yhxx.username = result.getString(result.getColumnIndex("username"));
                yhxx.lastyl = result.getInt(result.getColumnIndex("lastyl"));
                yhxx.curreaddate = result.getString(result.getColumnIndex("curreaddate"));
                yhxx.channel = result.getString(result.getColumnIndex("channel"));
                yhxx.fileType = result.getString(result.getColumnIndex("fileType"));
                yhxx.channelAddress = result.getString(result.getColumnIndex("channelAddress"));
                yhxx.uploadState = result.getInt(result.getColumnIndex("uploadState"));
                yhxx.filePath = result.getString(result.getColumnIndex("filePath"));
                yhxx.meterTag = result.getInt(result.getColumnIndex("meterTag"));
                all.add(yhxx);
            }
            result.close();
            this.db.close();
            return all;
        }

    }

    /**
     * 查询操作重载函数，返回指定ID的列表
     *
     * @param id
     * @return
     */
    public int getstatebyID(int id) {
        //错误状态-1
        int num = -1;
        String sql = "SELECT state FROM " + TABLE_NAME + " where id=?";
        String[] args = new String[]{String.valueOf(id)};
        Cursor result = this.db.rawQuery(sql, args);
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            num = result.getInt(0);
        }
        result.close();
        //    Log.e("database", "图片状态state"+ String.valueOf(num));
        this.db.close();
        return num;
    }

    public String getlocationbyaddr(String addr) {
        //错误状态-1
        String location = "没有地址信息";
        String sql = "SELECT useraddr FROM " + TABLE_NAME + " where meteraddr=?";
        String args[] = new String[]{String.valueOf(addr)};
        Cursor result = this.db.rawQuery(sql, args);
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            location = result.getString(result.getColumnIndex("useraddr"));
        }
        result.close();
        this.db.close();
        return location;
    }

    /**
     * 根据表地址获取用户信息
     *
     * @param meterId
     * @return
     */
    public UserInfo queryMeterInfoByMeterId(String meterId) {
        UserInfo userInfo = new UserInfo();
        //错误状态-1
        String location = "没有地址信息";
        String channelNumber = "无";
        String userId = "";
        int lastData = 0;
        String sql = "SELECT *  FROM " + TABLE_NAME + " where meteraddr=?";
        String[] args = new String[]{String.valueOf(meterId)};
        Cursor result = this.db.rawQuery(sql, args);
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            location = result.getString(result.getColumnIndex("useraddr"));
            channelNumber = result.getString(result.getColumnIndex("channelNumber"));
            userId = result.getString(result.getColumnIndex("accountnum"));
            lastData = result.getInt(result.getColumnIndex("lastdata"));
        }
        userInfo.useraddr = location;
        userInfo.accountnum = userId;
        userInfo.lastdata = lastData;
        userInfo.channelNumber = channelNumber;
        result.close();
        this.db.close();
        return userInfo;
    }


    /**
     * 根据表地址获取用户信息
     *
     * @param meterId
     * @return
     */
    public String queryUserAddress(String meterId) {
        String location = "未找到地址信息";
        String sql = "SELECT *  FROM " + TABLE_NAME + " where meteraddr=?";
        String[] args = new String[]{String.valueOf(meterId)};
        Cursor result = this.db.rawQuery(sql, args);
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            location = result.getString(result.getColumnIndex("useraddr"));
        }
        result.close();
        this.db.close();
        return location;
    }

    /**
     * 判断插入数据的ID是否已经存在数据库中。
     *
     * @param id
     * @return
     */
    public boolean check_same(int id) {
        String sql = "SELECT id from " + TABLE_NAME + " where id = ?";
        String[] args = new String[]{String.valueOf(id)};
        Cursor result = this.db.rawQuery(sql, args);
        //判断得到的返回数据是否为空
        if (result.getCount() == 0) {
            this.db.close();
            return false;
        } else {
            this.db.close();
            return true;
        }
    }

    /**
     * 账户存在返回true,不存在返回false
     *
     * @param accountnum
     * @return
     */
    public boolean check_same(String fileName, String accountnum) {
        int count;
        String sql = "SELECT accountnum from " + TABLE_NAME + " where filename = ? and accountnum = ?";
        String[] args = new String[]{fileName, accountnum};
        Cursor result = this.db.rawQuery(sql, args);
        count = result.getCount();
        result.close();
        return count != 0;
    }


    /**
     * 判断水表是否存在 ，存在返回true,不存在返回false
     *
     * @param meterId
     * @return
     */
    public boolean checkMeterIdExist(String meterId) {
        if (TextUtils.isEmpty(meterId)) {
            return false;
        }
        int count;
        meterId = EmiStringUtil.clearFirstZero(meterId);
        String sql = "SELECT meteraddr from " + TABLE_NAME + " where meteraddr = ?";
        String[] args = new String[]{meterId};
        Cursor result = this.db.rawQuery(sql, args);
        count = result.getCount();
        result.close();
        return count != 0;
    }

    /**
     * 判断水表是否存在 ，存在返回true,不存在返回false
     *
     * @param meterId
     * @return
     */
    public boolean checkMeterIdExist(String channelNumber, String meterId) {
        if (TextUtils.isEmpty(meterId)) {
            return false;
        }
        int count;
        meterId = EmiStringUtil.clearFirstZero(meterId);
        String sql = "SELECT meteraddr from " + TABLE_NAME + " where channelNumber = ? and meteraddr = ?";
        String[] args = new String[]{channelNumber, meterId};
        Cursor result = this.db.rawQuery(sql, args);
        count = result.getCount();
        result.close();
        return count != 0;
    }


    /**
     * 检测数据库中是否有该文件,存在返回true,不存在返回false
     *
     * @param fileName
     */
    public boolean checkFileIsExist(String fileName) {
        int count;
        String sql = "SELECT filename from " + TABLE_NAME + " where filename = ?";
        String[] args = new String[]{fileName};
        Cursor result = this.db.rawQuery(sql, args);
        count = result.getCount();
        result.close();
        return count != 0;
    }

    public boolean checkMeterTag(String fileName, String userId, String meterId) {
        int meterTag = 0;
        String sql = "SELECT meterTag from " + TABLE_NAME + " where filename = ? and accountnum = ? and meteraddr = ? ";
        String[] args = new String[]{fileName, userId, meterId};
        Cursor result = this.db.rawQuery(sql, args);
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            meterTag = result.getInt(result.getColumnIndex("meterTag"));
            break;
        }
        result.close();
        return meterTag != 0;
    }

    public void updateMeterTag(String fileName, String userId, String meterId, boolean meterTag) {
        int tag = 0;
        if (meterTag) {
            tag = STATE_TAG;
        }
        String sql = "UPDATE " + TABLE_NAME + " SET meterTag=? WHERE filename = ? and accountnum = ? and meteraddr = ? ";
        Object[] args = new Object[]{tag, fileName, userId, meterId};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    public boolean checkIsExist(String filePath) {
        int count;
        String sql = "SELECT filePath from " + TABLE_NAME + " where filePath = ?";
        String[] args = new String[]{filePath};
        Cursor result = this.db.rawQuery(sql, args);
        count = result.getCount();
        result.close();
        return count != 0;
    }

    /**
     * 根据文件名查询目录名
     *
     * @param fileName
     * @return
     */
    public String queryDirName(String fileName) {
        String name = "";
        String sql = "SELECT dirname FROM " + TABLE_NAME + " where filename=?";
        String[] args = new String[]{fileName};
        Cursor result = this.db.rawQuery(sql, args);
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            name = result.getString(result.getColumnIndex("dirname"));
            if (name != null) {
                return name;
            }
        }
        result.close();
        this.db.close();
        return name;
    }


    public String queryFileName(String dirName) {
        String name = "";
        String sql = "SELECT filename FROM " + TABLE_NAME + " where dirname=?";
        String[] args = new String[]{dirName};
        Cursor result = this.db.rawQuery(sql, args);
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            name = result.getString(result.getColumnIndex("filename"));
            if (name != null) {
                return name;
            }
        }
        result.close();
        this.db.close();
        return name;
    }

    /**
     * 根据dirName查找fileName集合(该查询方法比较耗时 不推荐)
     *
     * @param dirName
     * @return
     */
    @Deprecated
    public List<String> queryFileNameList(String dirName) {
        ArrayList<String> fileNameList = new ArrayList<>();
        String name;
        String sql = "SELECT filename FROM " + TABLE_NAME + " where dirname=?";
        String[] args = new String[]{String.valueOf(dirName)};
        Cursor result = this.db.rawQuery(sql, args);
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            name = result.getString(result.getColumnIndex("filename"));
            if (name != null) {
                fileNameList.add(name);
            }
        }
        result.close();
        this.db.close();
        removeSameData(fileNameList);
        return fileNameList;
    }


    public boolean check_channel(String filename, String channel) {
        int count;
        String sql = "SELECT accountnum from " + TABLE_NAME + " where filename =? and channel = ?";
        String[] args = new String[]{filename, channel};
        Cursor result = this.db.rawQuery(sql, args);
        count = result.getCount();
        result.close();
        return count != 0;
    }


    private void removeSameData(ArrayList<String> dataList) {
        for (int i = 0; i < dataList.size(); i++) {
            for (int j = i + 1; j < dataList.size(); j++) {
                if (dataList.get(i).equals(dataList.get(j))) {
                    dataList.remove(j);
                    j--;
                }
            }
        }
    }


    public void updateUserInfo(String fileName, String oldUserId, String newUserId, String newMeterId, String newChannelNumber, String newFirmCode) {
        String sql = "UPDATE " + TABLE_NAME + " SET accountnum=?,meteraddr=?,channelNumber=?,firmCode=? WHERE filename=? and accountnum = ?";
        Object[] args = new Object[]{newUserId, newMeterId, newChannelNumber, newFirmCode, fileName, oldUserId};
        this.db.execSQL(sql, args);
        this.db.close();
    }

    public MyOperator setOnInsertListener(OnProgressListener<ArrayList<UserInfo>> onProgressListener) {
        this.onProgressListener = onProgressListener;
        return this;
    }


    public void updateExportStatus(List<UserInfo> userInfoList) {
        if (userInfoList != null && db != null) {
            String sql = "UPDATE " + TABLE_NAME + " SET hasExport = ? WHERE filename= ? and accountnum = ?";
            Object[] args;
            db.beginTransaction();
            for (UserInfo userInfo : userInfoList) {
                if (userInfo != null) {
                    args = new Object[]{userInfo.hasExport, userInfo.filename, userInfo.accountnum};
                    this.db.execSQL(sql, args);
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }


    public boolean checkHasExport(String filename) {
        int count;
        String exportStatus = "1";
        String[] args = new String[]{filename, exportStatus};
        String sql = "SELECT hasExport from " + TABLE_NAME + " where filename = ? and hasExport = ?";
        Cursor result = this.db.rawQuery(sql, args);
        count = result.getCount();
        result.close();
        LogUtil.w(TAG, "已导出数量：" + count);
        return count != 0;
    }


    public UserInfo findUserByMeterInfo(String meterId, String firmCode) {
        UserInfo yhxx = null;
        String sql;
        String[] args;
        sql = "SELECT * FROM " + TABLE_NAME + " where meteraddr = ? and firmCode = ? ";
        args = new String[]{meterId, firmCode};
        //执行查询语句
        Cursor result = this.db.rawQuery(sql, args);
        //采用循环的方式查询数据
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            yhxx = new UserInfo();
            yhxx.accountnum = result.getString(result.getColumnIndex("accountnum"));
            yhxx.meteraddr = result.getString(result.getColumnIndex("meteraddr"));
            yhxx.curdata = result.getInt(result.getColumnIndex("curdata"));
            yhxx.lastdata = result.getInt(result.getColumnIndex("lastdata"));
            yhxx.curyl = result.getInt(result.getColumnIndex("curyl"));
            yhxx.readmonth = result.getInt(result.getColumnIndex("readmonth"));
            yhxx.useraddr = result.getString(result.getColumnIndex("useraddr"));
            yhxx.state = result.getInt(result.getColumnIndex("state"));
            yhxx.username = result.getString(result.getColumnIndex("username"));
            yhxx.lastyl = result.getInt(result.getColumnIndex("lastyl"));
            yhxx.curreaddate = result.getString(result.getColumnIndex("curreaddate"));
            yhxx.channel = result.getString(result.getColumnIndex("channel"));
            yhxx.fileType = result.getString(result.getColumnIndex("fileType"));
            yhxx.channelAddress = result.getString(result.getColumnIndex("channelAddress"));
            yhxx.uploadState = result.getInt(result.getColumnIndex(UPLOAD_STATE_KEY));
            yhxx.channelNumber = result.getString(result.getColumnIndex("channelNumber"));
            yhxx.filePath = result.getString(result.getColumnIndex("filePath"));
            yhxx.firmCode = result.getString(result.getColumnIndex("firmCode"));
            yhxx.waterId = result.getString(result.getColumnIndex("waterId"));
            yhxx.filename = result.getString(result.getColumnIndex("filename"));
            yhxx.dirname = result.getString(result.getColumnIndex("dirname"));
            yhxx.meterTag = result.getInt(result.getColumnIndex("meterTag"));
            break;
        }
        result.close();
        this.db.close();
        return yhxx;
    }


    public UserInfo findUserByMeterInfo(String meterId) {
        UserInfo yhxx = null;
        String[] args;
        String sql = "SELECT * FROM " + TABLE_NAME + " where meteraddr = ? ";
        args = new String[]{meterId};
        //执行查询语句
        Cursor result = this.db.rawQuery(sql, args);
        //采用循环的方式查询数据
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            yhxx = new UserInfo();
            yhxx.accountnum = result.getString(result.getColumnIndex("accountnum"));
            yhxx.meteraddr = result.getString(result.getColumnIndex("meteraddr"));
            yhxx.curdata = result.getInt(result.getColumnIndex("curdata"));
            yhxx.lastdata = result.getInt(result.getColumnIndex("lastdata"));
            yhxx.curyl = result.getInt(result.getColumnIndex("curyl"));
            yhxx.readmonth = result.getInt(result.getColumnIndex("readmonth"));
            yhxx.useraddr = result.getString(result.getColumnIndex("useraddr"));
            yhxx.state = result.getInt(result.getColumnIndex("state"));
            yhxx.username = result.getString(result.getColumnIndex("username"));
            yhxx.lastyl = result.getInt(result.getColumnIndex("lastyl"));
            yhxx.curreaddate = result.getString(result.getColumnIndex("curreaddate"));
            yhxx.channel = result.getString(result.getColumnIndex("channel"));
            yhxx.fileType = result.getString(result.getColumnIndex("fileType"));
            yhxx.channelAddress = result.getString(result.getColumnIndex("channelAddress"));
            yhxx.uploadState = result.getInt(result.getColumnIndex(UPLOAD_STATE_KEY));
            yhxx.channelNumber = result.getString(result.getColumnIndex("channelNumber"));
            yhxx.filePath = result.getString(result.getColumnIndex("filePath"));
            yhxx.firmCode = result.getString(result.getColumnIndex("firmCode"));
            yhxx.waterId = result.getString(result.getColumnIndex("waterId"));
            yhxx.meterTag = result.getInt(result.getColumnIndex("meterTag"));
            break;
        }
        result.close();
        this.db.close();
        return yhxx;
    }

    private void removeEmptyUserInfo(List<UserInfo> userInfoList) {
        UserInfo userInfo;
        for (int i = userInfoList.size() - 1; i >= 0; i--) {
            userInfo = userInfoList.get(i);
            boolean isNull = userInfo == null;
            if (isNull) {
                userInfoList.remove(i);
            } else {
                boolean isEmpty = TextUtils.isEmpty(userInfo.accountnum) && TextUtils.isEmpty(userInfo.meteraddr);
                if (isEmpty) {
                    userInfoList.remove(i);
                }
            }
        }
    }
}
