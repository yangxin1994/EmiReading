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
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.threadpool.ThreadPoolManager;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.FileEntity;
import com.emi.emireading.entities.SavedFileInfo;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;
import com.emi.emireading.widget.view.dialog.multidialog.EmiMultipleProgressDialog;
import com.emi.emireading.widget.view.dialog.sweetalert.SweetAlertDialog;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.emi.emireading.core.config.EmiConstants.EXTRA_FILE_NAME;
import static com.emi.emireading.core.config.EmiConstants.PREF_SKIP_TAG;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_FOLDER;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_TXT;
import static com.emi.emireading.ui.ChannelListActivity.SKIP_TAG_TASK_QUERY;

/**
 * @author :zhoujian
 * @description : 任务查询
 * @company :翼迈科技
 * @date 2018年03月15日下午 02:07
 * @Email: 971613168@qq.com
 */

public class TaskQueryActivity extends BaseActivity {
    private ArrayList<String> dirNameList = new ArrayList<>();
    private ArrayList<String> fileNameList = new ArrayList<>();
    private ArrayList<FileEntity> fileEntityList = new ArrayList<>();
    private String mFileName;
    private EmiMultipleProgressDialog dialog;
    private Handler handler = new Handler();
    private Context context;
    private RecyclerView recyclerView;
    private FileListEmiAdapter fileListAdapter;
    private Context mContext;
    private boolean isLongClick = false;
    /**
     * 选中的文件名列表
     */
    private ArrayList<String> selectFileNameList = new ArrayList<>();

    @Override
    protected int getContentLayout() {
        return R.layout.activity_querytask;
    }

    @Override
    protected void initIntent() {
        mContext = this;
    }

    @Override
    protected void initUI() {
        recyclerView = findViewById(R.id.recyclerView);
        context = this;
    }

    @Override
    protected void initData() {
        showDialog(getString(R.string.loading_data));
        initList();
    }

    private void initList() {
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                fileEntityList.clear();
                fileNameList.clear();
                dirNameList.clear();
                getLoadInfo();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        loadListView();
                        closeDialog();
                    }
                });
            }
        });
    }

    private void getLoadInfo() {
        String fileName;
        String fileSuffix;
        dirNameList.clear();
        FileEntity entity;
        HashMap<String, String> hashMap = new HashMap<>(10);
        List<String> tempNameList = getTempFileNameList();
        for (String tempName : tempNameList) {
            fileName = tempName;
            int end = fileName.lastIndexOf(".");
            if (end != -1) {
                mFileName = fileName;
                String dirname = getSqOperator().findDirName(mFileName);
                if (!TextUtils.isEmpty(dirname)) {
                    fileNameList.add(mFileName);
                    dirNameList.add(dirname);
                    entity = new FileEntity();
                    entity.fileName = dirname;
                    fileSuffix = com.emi.emireading.core.utils.FileUtil.getFileSuffix(dirname);
                    if (hashMap.containsValue(dirname)) {
                        entity.fileSuffix = SUFFIX_FOLDER;
                    } else if (TextUtils.isEmpty(fileSuffix)) {
                        entity.fileSuffix = SUFFIX_TXT;
                    } else {
                        entity.fileSuffix = fileSuffix;
                    }
                    hashMap.put(fileName, dirname);
                    fileEntityList.add(entity);
                }
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

    private void loadListView() {
        fileListAdapter = new FileListEmiAdapter(fileEntityList);
        fileListAdapter.setEmptyView(getViewByResource(mContext, R.layout.layout_empty_view));
        fileListAdapter.bindToRecyclerView(recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        fileListAdapter.setOnItemClickListener(new BaseEmiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
                LogUtil.w("点击的目录名：" + dirNameList.get(position));
                isLongClick = false;
                doSelect(dirNameList.get(position));
            }
        });
        fileListAdapter.setOnItemLongClickListener(new BaseEmiAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseEmiAdapter adapter, View view, int position) {
                LogUtil.w("点击的目录名：" + dirNameList.get(position));
                isLongClick = true;
                doSelect(dirNameList.get(position));
                return true;
            }
        });


    }

    /**
     * 获取选中的文件目录下的所有已加载文件名
     *
     * @param selectDirName
     */
    private void getSelectFileNames(String selectDirName) {
        selectFileNameList.clear();
        for (String tempFileName : fileNameList) {
            if(getSqOperator().queryDirName(tempFileName).equals(selectDirName)){
                selectFileNameList.add(tempFileName);
            }
        }

    }


    private void showSelectTxtFileDialog(final ArrayList<String> fileNameList) {
        CommonSelectEmiAdapter selectFileAdapter = new CommonSelectEmiAdapter(fileNameList);
        CommonSelectDialog.Builder builder = new CommonSelectDialog.Builder(context);
        if (isLongClick) {
            builder.setTitle("请选择要删除的文件");
        } else {
            builder.setTitle("请选择要查询的文件");
        }
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final CommonSelectDialog deviceDialog = builder.create();
        selectFileAdapter.setOnItemClickListener(new BaseEmiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
                deviceDialog.dismiss();
                if (isLongClick) {
                    showDeleteAlert(fileNameList.get(position));
                } else {
                    skip(fileNameList.get(position));
                }
            }
        });
        selectFileAdapter.bindToRecyclerView(builder.recyclerView);
        deviceDialog.setCancelable(false);
        deviceDialog.setCanceledOnTouchOutside(false);
        builder.setAdapter(selectFileAdapter);
        deviceDialog.show();
    }


    private void doSelect(String selectDirName) {
        //获取到该目录下的文件名集合
        getSelectFileNames(selectDirName);
        if (selectFileNameList.size() > 1) {
            showSelectTxtFileDialog(selectFileNameList);
        } else {
            //直接跳转
            if (selectFileNameList.isEmpty()) {
                ToastUtil.showShortToast("未找到对应数据");
            } else {
                if (isLongClick) {
                    showDeleteAlert(selectFileNameList.get(0));
                } else {
                    skip(selectFileNameList.get(0));
                }
            }
        }
    }

    private void skip(String selectFileName) {
        Intent intent = new Intent(context, ChannelListActivity.class);
        intent.putExtra(EXTRA_FILE_NAME, selectFileName);
        intent.putExtra(PREF_SKIP_TAG, SKIP_TAG_TASK_QUERY);
        startActivity(intent);
    }

    private void showDialog(String text) {
        dialog = EmiMultipleProgressDialog.create(context)
                .setLabel(text)
                .setCancellable(false)
                .show();
    }

    private void closeDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
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
                        LitePal.deleteAll(SavedFileInfo.class, "savedFileName = ? ", selectFileName);
                        updateUI();
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
    private void updateUI() {
        isLongClick = true;
        initList();
    }
}
