package com.emi.emireading.core.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.emi.emireading.core.log.LogUtil;

import static android.content.ContentValues.TAG;

/**
 * 创建数据库
 */
public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "emireading.db";
    /**
     * 数据库版本
     */
    private static final int DATABASE_VERSION = 3;
    private static final String TABLE_NAME = "emi_data";

    public SQLiteHelper(Context context, String name, CursorFactory factory,
                        int version) {
        super(context, name, factory, version);
    }

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtil.i(TAG, "------->onCreate");
        String sql = "CREATE TABLE " + TABLE_NAME + "("
                + "filename VARCHAR(50),"
                + "accountnum VARCHAR(12),"
                + "meteraddr VARCHAR(10),"
                + "curdata INTEGER,"
                + "lastdata INTEGER,"
                + "curyl INTEGER,"
                + "readmonth INTEGER,"
                + "useraddr VARCHAR(50),"
                + "state INTEGER,"
                + "username VARCHAR(50),"
                + "lastyl INTEGER,"
                + "curreaddate VARCHAR(50),"
                + "channel VARCHAR(50),"
                + "rereadflag INTEGER,"
                + "fileType VARCHAR(50),"
                + "uploadState INTEGER,"
                + "channelAddress VARCHAR(80),"
                + "channelNumber VARCHAR(50),"
                + "dirname VARCHAR(50),"
                + "firmCode VARCHAR(20),"
                + "filePath VARCHAR(90),"
                + "waterId VARCHAR(50),"
                + "hasExport INTEGER,"
                + "loadStrategyJson VARCHAR(400),"
                +"meterTag INTEGER"
                + ")";
        LogUtil.i(TAG, "------->onCreate:" + sql);
        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtil.d(TAG, "------->onUpgrade");
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(sql);
        this.onCreate(db);
        // TODO Auto-generated method stub
    }
}
