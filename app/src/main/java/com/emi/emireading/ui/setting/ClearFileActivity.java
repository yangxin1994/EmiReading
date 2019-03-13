package com.emi.emireading.ui.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.View;
import android.widget.Toast;

import com.emi.emireading.R;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.db.MyOperator;
import com.emi.emireading.core.db.SQLiteHelper;
import com.emi.emireading.core.utils.FileUtil;
import com.emi.emireading.entities.DataFileBean;
import com.emi.emireading.entities.FileEditInfo;
import com.emi.emireading.entities.SavedFileInfo;
import com.emi.emireading.ui.PathSelectActivity;
import com.emi.emireading.widget.view.MenuItemView;
import com.emi.emireading.widget.view.dialog.CustomDialog;

import org.litepal.LitePal;

import static com.emi.emireading.core.config.EmiConfig.APK_PATH_DOWNLOAD;


/**
 * @author :zhoujian
 * @description : 管理数据文件
 * @company :翼迈科技
 * @date: 2017年09月12日上午 11:02
 * @Email: 971613168@qq.com
 */

public class ClearFileActivity extends BaseActivity implements View.OnClickListener {
    private MenuItemView miv_clearDataBase;
    private MenuItemView miv_clearResultFile;
    private MenuItemView miv_clearReadingFile;
    private Context mContext;
    private MyOperator mytab = null;
    private SQLiteOpenHelper helper = null;


    @Override
    protected int getContentLayout() {
        return R.layout.activity_clear_data;
    }

    @Override
    protected void initIntent() {
        mContext = this;
        //数据库操作辅助类
        this.helper = new SQLiteHelper(mContext);
    }

    @Override
    protected void initUI() {
        miv_clearDataBase = findViewById(R.id.miv_clearDataBase);
        miv_clearResultFile = findViewById(R.id.miv_clearResultFile);
        miv_clearReadingFile = findViewById(R.id.miv_clearReadingFile);
        findViewById(R.id.mivDataList).setOnClickListener(this);


    }

    @Override
    protected void initData() {
        miv_clearDataBase.setOnClickListener(this);
        miv_clearResultFile.setOnClickListener(this);
        miv_clearReadingFile.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.miv_clearReadingFile:
                FileUtil.deleteDirectory(APK_PATH_DOWNLOAD);
                notifyFileExplore(APK_PATH_DOWNLOAD);
                deleteNeedDirFile();
                break;
            case R.id.miv_clearDataBase:
                FileUtil.deleteDirectory(APK_PATH_DOWNLOAD);
                notifyFileExplore(APK_PATH_DOWNLOAD);
                deleteFile();
                break;
            case R.id.miv_clearResultFile:
             FileUtil.deleteDirectory(APK_PATH_DOWNLOAD);
                notifyFileExplore(APK_PATH_DOWNLOAD);
                deleteCreateDirFile();

                break;
            case R.id.mivDataList:
                Intent intent = new Intent();
                intent.setClass(ClearFileActivity.this, PathSelectActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }


    public void deleteFile() {
        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        builder.setTitle("提示");
        builder.setMessage("确保抄表数据已下载导出且已过期,清空数据库将不可恢复!确定清空吗?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LitePal.deleteAll(SavedFileInfo.class);
                if (FileUtil.deleteDirectory(EmiConfig.TempPath)) {
                    Toast.makeText(mContext, "删除成功！", Toast.LENGTH_SHORT).show();
                    notifyFileExplore(EmiConfig.TempPath);
                } else {
                    Toast.makeText(mContext, "删除失败！", Toast.LENGTH_SHORT).show();
                }
                //清除数据库
                mytab = new MyOperator(helper.getWritableDatabase());
                mytab.deleteAll();
                LitePal.deleteAll(DataFileBean.class);
                LitePal.deleteAll(FileEditInfo.class);
                dialog.dismiss();
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
     * 删除翼迈所需文件文件夹
     */
    public void deleteCreateDirFile() {
        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        builder.setTitle("提示");
        builder.setMessage("将要删除EMI抄表“翼迈生成文件”目录下的所有文件,确定删除吗?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (FileUtil.deleteDirectory(EmiConfig.GeneratePath)) {
                    Toast.makeText(mContext, "删除成功！", Toast.LENGTH_SHORT).show();
                    notifyFileExplore(EmiConfig.GeneratePath);
                } else {
                    Toast.makeText(mContext, "删除失败！", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
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
     * 删除翼迈所需文件文件夹
     */
    public void deleteNeedDirFile() {
        CustomDialog.Builder builder = new CustomDialog.Builder(mContext);
        builder.setTitle("提示");
        builder.setMessage("将要删除EMI抄表“翼迈所需文件”目录下的所有文件,确定删除吗?");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (FileUtil.deleteDirectory(EmiConfig.NeedFilePath)) {
                    Toast.makeText(mContext, "删除成功！", Toast.LENGTH_SHORT).show();
                    notifyFileExplore(EmiConfig.NeedFilePath);
                } else {
                    Toast.makeText(mContext, "删除失败！", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
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





}
