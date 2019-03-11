package com.emi.emireading.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.emi.emireading.R;
import com.emi.emireading.adpter.CommonSelectEmiAdapter;
import com.emi.emireading.adpter.FileListEmiAdapter;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.common.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.FileUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.FileEntity;
import com.emi.emireading.entities.SavedFileInfo;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;
import com.emi.emireading.widget.view.dialog.multidialog.EmiMultipleProgressDialog;
import com.emi.emireading.widget.view.dialog.sweetalert.SweetAlertDialog;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.emi.emireading.core.config.EmiConstants.EXTRA_FILE_NAME;
import static com.emi.emireading.core.config.EmiConstants.PREF_SKIP_TAG;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_FOLDER;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_TXT;
import static com.emi.emireading.ui.ChannelListActivity.SKIP_TAG_AUTO_READ_METER;

/**
 * @author :zhoujian
 * @description : 文件列表
 * @company :翼迈科技
 * @date 2018年03月17日上午 11:56
 * @Email: 971613168@qq.com
 */


public class FileListActivity extends BaseActivity {
    private boolean isLongClick = false;
    private ArrayList<String> dirNameList = new ArrayList<>();
    private ArrayList<String> fileNameList = new ArrayList<>();
    private HashMap<String, String> fileNameHashMap = new HashMap<>();
    public static final String EXTRA_READ_TAG = "EXTRA_READ_TAG";
    private RecyclerView fileNameListRecyclerView;
    private FileListEmiAdapter fileListAdapter;
    private List<FileEntity> fileEntityList = new ArrayList<>();
    private Context mContext;
    private EmiMultipleProgressDialog dialog;
    private Handler handler = new Handler();

    @Override
    protected int getContentLayout() {
        return R.layout.activity_select_community;
    }

    @Override
    protected void initIntent() {
        mContext = this;
    }

    @Override
    protected void initUI() {
        fileListAdapter = new FileListEmiAdapter(fileEntityList);
        fileNameListRecyclerView = findViewById(R.id.fileNameListRecyclerView);

    }

    @Override
    protected void initData() {
        showDialog(getResources().getString(R.string.loading_data));
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                fileNameList.addAll(getTempFileNameList());
                loadDirNameList();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        initAdapter();
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                });
            }
        });


    }


  /*  *//**
     * 获取加载过的文件名
     *
     * @return
     *//*
    private ArrayList<String> getTempFileName() {
        //        String currentFileName;
        ArrayList<String> tempFileList = new ArrayList<>();
        //        File file = new File(EmiConfig.TempPath);
        List<SavedFileInfo> fileInfoList = LitePal.findAll(SavedFileInfo.class);
        for (SavedFileInfo savedFileInfo : fileInfoList) {
            if (FileUtil.getFileSuffix(savedFileInfo.savedFileName).contains(EmiConfig.CURRENT_SUFFIX)) {
                tempFileList.add(savedFileInfo.savedFileName);
            }
            LogUtil.w("找到的文件名：" + savedFileInfo.savedFileName);
        }
       *//* File[] files = file.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile() && checkIsTempFile((files[i]))) {
                    LogUtil.i("得到的文件名：" + files[i].getName());
                    currentFileName = files[i].getName();
                    currentFileName = FileUtil.clearFileSuffix(currentFileName) + EmiConfig.CURRENT_SUFFIX;
                    tempFileList.add(currentFileName);
                }
            }
        }*//*
        return tempFileList;
    }*/


    /**
     * 判断是否是缓存文件
     */
    private boolean checkIsTempFile(File file) {
        String fileName = file.getName();
        if (file.isDirectory()) {
            return false;
        }
        LogUtil.d("文件名：" + FileUtil.getFileSuffix(file.getName().toLowerCase()));
        //判断是否是txt文件
        if (!(EmiConstants.SUFFIX_TXT.equals(FileUtil.getFileSuffix(file.getName().toLowerCase())))) {
            return false;
        }
        fileName = FileUtil.clearFileSuffix(fileName) + EmiConfig.CURRENT_SUFFIX;
        LogUtil.i("处理过后的文件名：" + fileName);
        boolean isExist = getSqOperator().checkFileIsExist(fileName);
        LogUtil.d("checkIsTempFile::" + isExist);
        return isExist;
    }


    private void loadDirNameList() {
        dirNameList.clear();
        fileEntityList.clear();
        fileNameHashMap.clear();
        String dirName;
        FileEntity entity;
        String fileSuffix;
        for (String fileName : fileNameList) {
            dirName = getSqOperator().queryDirName(fileName);
            if (!TextUtils.isEmpty(dirName)) {
                LogUtil.d(TAG, "fileName=" + fileName);
                dirNameList.add(dirName);
                entity = new FileEntity();
                entity.fileName = dirName;
                fileSuffix = FileUtil.getFileSuffix(dirName);
                if (fileNameHashMap.containsValue(dirName)) {
                    entity.fileSuffix = SUFFIX_FOLDER;
                } else if (TextUtils.isEmpty(fileSuffix)) {
                    entity.fileSuffix = SUFFIX_TXT;
                } else {
                    entity.fileSuffix = fileSuffix;
                }
                fileNameHashMap.put(fileName, dirName);
                fileEntityList.add(entity);
            }
        }
        resettingFileSuffix(fileEntityList);
        removeSameData(fileEntityList);
        removeDuplicateStringElement(dirNameList);
    }


    private List<FileEntity> removeSameData(List<FileEntity> dataList) {
        for (int i = 0; i < dataList.size(); i++) {
            for (int j = i + 1; j < dataList.size(); j++) {
                if (dataList.get(i).fileName.equals(dataList.get(j).fileName)) {
                    dataList.remove(j);
                    j--;
                }
            }
        }
        return dataList;
    }

    private List<FileEntity> resettingFileSuffix(List<FileEntity> dataList) {
        for (int i = 0; i < dataList.size() - 1; i++) {
            for (int j = dataList.size() - 1; j > i; j--) {
                if (dataList.get(j).fileName.equals(dataList.get(i).fileName)) {
                    dataList.get(i).fileSuffix = SUFFIX_FOLDER;
                }
            }
        }
        return dataList;
    }


    private ArrayList<String> getSelectFileNameList(String dirName) {
        String sqDirName;
        ArrayList<String> selectList = new ArrayList<>();
        for (String fileName : fileNameList) {
            sqDirName = getSqOperator().queryDirName(fileName);
            if (dirName.equals(sqDirName)) {
                LogUtil.d("sqDirName=" + sqDirName);
                LogUtil.i("dirName=" + dirName);
                if (dirName.equals(sqDirName)) {
                    selectList.add(fileName);
                }
            }
        }
        return selectList;
    }

    private void doSelect(String selectDirName) {
        ArrayList<String> selectFileNameList = getSelectFileNameList(selectDirName);
        switch (selectFileNameList.size()) {
            case 0:
                ToastUtil.showShortToast("未找到数据文件");
                break;
            case 1:
                if (isLongClick) {
                    showDeleteAlert(selectFileNameList.get(0));
                } else {
                    doSkip(selectFileNameList.get(0));
                }
                break;
            default:
                showSelectTxtFileDialog(selectFileNameList);
                break;
        }
    }

    private void doSkip(String selectFileName) {
        Intent intent = new Intent();
        if (EmiUtils.isNeedChannel()) {
            intent.setClass(FileListActivity.this, AutoReadMeterActivityNew.class);
        } else {
            intent.setClass(FileListActivity.this, ChannelListActivity.class);
            intent.putExtra(PREF_SKIP_TAG, SKIP_TAG_AUTO_READ_METER);
        }

        intent.putExtra(EXTRA_FILE_NAME, selectFileName);
        startActivity(intent);
    }


    private void showSelectTxtFileDialog(final ArrayList<String> fileNameList) {
        CommonSelectEmiAdapter selectFileAdapter = new CommonSelectEmiAdapter(fileNameList);
        CommonSelectDialog.Builder builder = new CommonSelectDialog.Builder(FileListActivity.this);
        if (isLongClick) {
            builder.setTitle("请选择要删除的文件");
        } else {
            builder.setTitle("请选择要抄表的文件");
        }
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final CommonSelectDialog dialog = builder.create();
        selectFileAdapter.setOnItemClickListener(new BaseEmiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
                dialog.dismiss();
                if (isLongClick) {
                    showDeleteAlert(fileNameList.get(position));
                } else {
                    doSkip(fileNameList.get(position));
                }
            }
        });
        selectFileAdapter.bindToRecyclerView(builder.recyclerView);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        builder.setAdapter(selectFileAdapter);
        dialog.show();
    }

    private void showDeleteAlert(final String selectFileName) {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("确定删除?")
                .setContentText("将要删除该文件对应的数据库数据!")
                .setCancelText("取消")
                .setConfirmText("删除")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismiss();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        doDelete(selectFileName);
                        updateUI(selectFileName);
                        LitePal.deleteAll(SavedFileInfo.class, "savedFileName = ? ", selectFileName);
                        sDialog.setTitleText("删除成功!")
                                .setContentText("该文件对应数据已删除")
                                .setConfirmText("确定")
                                .showCancelButton(false)
                                .setCancelClickListener(null)
                                .setConfirmClickListener(null)
                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    }
                })
                .show();
    }


    /**
     * 删除后更新UI
     */
    private void updateUI(String selectName) {
        fileNameList.remove(selectName);
        loadDirNameList();
        fileListAdapter.notifyDataSetChanged();
    }

    private void initAdapter() {
        fileListAdapter.setEmptyView(getViewByResource(mContext, R.layout.layout_empty_view));
        fileListAdapter.bindToRecyclerView(fileNameListRecyclerView);
        fileNameListRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        fileListAdapter.setOnItemClickListener(new BaseEmiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
                isLongClick = false;
                LogUtil.w("点击的目录名：" + dirNameList.get(position));
                doSelect(dirNameList.get(position));
            }
        });
        fileListAdapter.setOnItemLongClickListener(new BaseEmiAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseEmiAdapter adapter, View view, int position) {
                isLongClick = true;
                LogUtil.w("点击的目录名：" + dirNameList.get(position));
                doSelect(dirNameList.get(position));
                return false;
            }
        });
    }

    private void showDialog(String text) {
        dialog = EmiMultipleProgressDialog.create(mContext)
                .setLabel(text)
                .setCancellable(false)
                .show();
    }
}
