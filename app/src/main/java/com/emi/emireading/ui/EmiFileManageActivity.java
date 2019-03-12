package com.emi.emireading.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.emi.emireading.R;
import com.emi.emireading.adpter.CommonSelectDeviceEmiAdapter;
import com.emi.emireading.adpter.MultipleItemQuickAdapter;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.threadpool.ThreadPoolManager;
import com.emi.emireading.core.listener.OnItemClickListener;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.request.response.ToastUtils;
import com.emi.emireading.core.utils.FileUtil;
import com.emi.emireading.entities.CommonSelect;
import com.emi.emireading.entities.FileInfo;
import com.emi.emireading.entities.MultiItem;
import com.emi.emireading.log.EmiLog;
import com.emi.emireading.widget.filepicker.controller.DialogSelectListener;
import com.emi.emireading.widget.filepicker.controller.module.DialogProperties;
import com.emi.emireading.widget.filepicker.view.FilePickerDialog;
import com.emi.emireading.widget.view.EmiCheckBox;
import com.emi.emireading.widget.view.EmiRecycleViewDivider;
import com.emi.emireading.widget.view.dialog.CommonSelectDialog;
import com.emi.emireading.widget.view.dialog.InputDialog;
import com.emi.emireading.widget.view.dialog.sweetalert.SweetAlertDialog;
import com.emi.emireading.widget.view.emimenu.SystemUtil;
import com.emi.emireading.widget.view.popup.EmiPopupList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.emi.emireading.core.config.EmiConfig.NeedFilePath;
import static com.emi.emireading.core.config.EmiConstants.EMI_ROOT_PATH;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_PATH;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_READING_PATH_FLAG;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_EXCEL;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_EXCEL_2007;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_TXT;
import static com.emi.emireading.core.utils.FileUtil.fileFilter;
import static com.emi.emireading.entities.MultiItem.FILE;
import static com.emi.emireading.entities.MultiItem.FOLD;
import static com.emi.emireading.widget.filepicker.controller.module.BaseDialogConfig.SELECT_DIR;
import static com.emi.emireading.widget.filepicker.controller.module.BaseDialogConfig.SINGLE_MODE;


/**
 * @author :zhoujian
 * @description : 翼迈文件管理
 * @company :翼迈科技
 * @date 2019年01月23日下午 05:11
 * @Email: 971613168@qq.com
 */

public class EmiFileManageActivity extends BaseActivity implements View.OnClickListener {
    private List<MultiItem> mMultiItemList = new ArrayList<>();
    private MultipleItemQuickAdapter mItemQuickAdapter;
    private List<FileInfo> mFileInfoList = new ArrayList<>();
    private File currentPathFile;
    private File sdCardFile;
    private RecyclerView rvSdCard;
    private Context mContext;
    private static final String TAG = "EmiFileManageActivity";
    private TextView mTvPath;
    private String mRootPath;
    private TextView mTvSelect;
    private TextView tvSend;
    private List<FileInfo> selectedList = new ArrayList<>();
    private boolean readingPathFlag = false;
    private TextView tvCopy;
    private FilePickerDialog mFilePickerDialog;
    private Handler mHandler = new Handler();
    private String createdPath;
    private float mRawX;
    private float mRawY;
    private static final int CODE_OPEN = 0;
    private static final int CODE_DELETE = 1;

    @Override
    public int getContentLayout() {
        return R.layout.activity_sdcard;
    }

    @Override
    protected void initIntent() {
        mContext = this;
        mRootPath = getIntent().getStringExtra(EXTRA_PATH);
        readingPathFlag = getIntent().getBooleanExtra(EXTRA_READING_PATH_FLAG, false);
    }

    @Override
    protected void initUI() {
        rvSdCard = findViewById(R.id.rvSdCard);
        mTvPath = findViewById(R.id.tv_path);
        mTvSelect = findViewById(R.id.tv_all_size);
        tvSend = findViewById(R.id.tv_send);
        tvCopy = findViewById(R.id.tvCopy);
        tvSend.setOnClickListener(this);
        tvCopy.setOnClickListener(this);
        SystemUtil.init(mContext);
        LogUtil.d(TAG, mRootPath);
        sdCardFile = new File(mRootPath);
        rvSdCard.setLayoutManager(new LinearLayoutManager(this));
        EmiRecycleViewDivider divider = new EmiRecycleViewDivider(
                mContext, LinearLayoutManager.HORIZONTAL, 2, ContextCompat.getColor(mContext, R.color.colorLineGray));
        rvSdCard.addItemDecoration(divider);
        mItemQuickAdapter = new MultipleItemQuickAdapter(mMultiItemList);
        rvSdCard.setAdapter(mItemQuickAdapter);
        mItemQuickAdapter.setEmptyView(getEmptyView());
        EmiLog.d(TAG, "路径:" + mRootPath);
        showFiles(sdCardFile);
        rvSdCard.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseEmiAdapter adapter, View view, int position) {
                if (adapter.getItemViewType(position) == FILE) {
                    boolean isCheck = mFileInfoList.get(position).select;
                    mFileInfoList.get(position).select = (!isCheck);
                    ((EmiCheckBox) view.findViewById(R.id.cb_file)).setChecked(mFileInfoList.get(position).select, true);
                } else {
                    showFiles(new File(mFileInfoList.get(position).filePath));
                }
                showSelect();
            }


            @Override
            public void onItemLongClick(BaseEmiAdapter adapter, View view, int position) {
                showPop(view, position);
            }

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                mRawX = e.getRawX();
                mRawY = e.getRawY();
                return super.onInterceptTouchEvent(rv, e);
            }
        });
        showFunction();
    }

    @Override
    protected void initData() {
        mTvSelect.setText(getString(R.string.select_count, "" + 0));
        initFilePickerDialog();
    }


    @Override
    public void onBackPressed() {
        goBack();
    }


    private void showFiles(File folder) {
        loadAdapter();
        mMultiItemList.clear();
        File[] files = fileFilter(folder);
        currentPathFile = folder;
        mTvPath.setText(currentPathFile.getPath());
        if (null == files || files.length == 0) {
            mItemQuickAdapter.setEmptyView(getEmptyView());
        } else {
            mFileInfoList = FileUtil.getFileInfoListFromFileArray(files);
            filterFile(mFileInfoList);
            for (FileInfo fileInfo : mFileInfoList) {
                if (fileInfo.directory) {
                    mMultiItemList.add(new MultiItem(FOLD, fileInfo));
                } else {
                    mMultiItemList.add(new MultiItem(FILE, fileInfo));
                }
            }
        }
        mItemQuickAdapter.notifyDataSetChanged();
    }


    private void loadAdapter() {
        if (mItemQuickAdapter == null) {
            if (mMultiItemList == null) {
                mMultiItemList = new ArrayList<>();
            }
            mItemQuickAdapter = new MultipleItemQuickAdapter(mMultiItemList);
        }
    }


    private View getEmptyView() {
        return LayoutInflater.from(mContext).inflate(R.layout.empty_view, null);
    }

    /**
     * 回退
     */
    private void goBack() {
        if (sdCardFile.getAbsolutePath().equals(currentPathFile.getAbsolutePath())) {
            finish();
        } else {
            currentPathFile = currentPathFile.getParentFile();
            showFiles(currentPathFile);
        }
    }

    /**
     * 保留指定文件路径文件
     */
    private void filterFile(List<FileInfo> fileInfoList) {
        boolean match;
        for (int i = fileInfoList.size() - 1; i >= 0; i--) {
            match = SUFFIX_TXT.contains(fileInfoList.get(i).suffix.toLowerCase()) || SUFFIX_EXCEL_2007.contains(fileInfoList.get(i).suffix.toLowerCase()) ||
                    SUFFIX_EXCEL.contains(fileInfoList.get(i).suffix.toLowerCase());
            if (!match) {
                fileInfoList.remove(i);
            }
        }
    }

    private void showSelect() {
        selectedList.clear();
        for (MultiItem multiItem : mMultiItemList) {
            if (multiItem.getData().select) {
                selectedList.add(multiItem.getData());
            }
        }
        mTvSelect.setText(getString(R.string.select_count, selectedList.size() + ""));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_send:
                shareSelectFile();
                break;
            case R.id.tvCopy:
                if (selectedList.isEmpty()) {
                    ToastUtils.showToastNormal("请先选择文件");
                    return;
                }
                selectPath();
                break;
            default:
                break;
        }
    }

    private void showFunction() {
        if (readingPathFlag) {
            tvCopy.setVisibility(View.GONE);
        } else {
            tvCopy.setVisibility(View.VISIBLE);
        }
    }


    private void showCreateDialog() {
        final InputDialog.Builder builder = new InputDialog.Builder(mContext);
        builder.setTitle("创建文件夹");
        builder.setMessage("");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (builder.editText.getText().toString().isEmpty()) {
                    ToastUtils.showToastNormal("请输入要创建的文件夹名称");
                    return;
                }
                createdPath = builder.editText.getText().toString();
                File file = new File(NeedFilePath + File.separator + createdPath);
                file.mkdirs();
                createdPath = file.getPath();
                copy(getSelectPathList(), createdPath);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        InputDialog inputDialog = builder.create();
        builder.editText.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setInputMaxLength(15);
        inputDialog.show();
    }


    private void shareSelectFile() {
        if (selectedList.isEmpty()) {
            ToastUtils.showToastNormal("请选择文件");
            return;
        }
        if (selectedList.size() > 1) {
            ToastUtils.showToastNormal("目前只支持单个发送");
            return;
        }
        File file = new File(selectedList.get(0).filePath);
        sendFile(mContext, file);
    }


    /**
     * 調用系統方法分享文件
     *
     * @param context
     * @param file
     */
    public void sendFile(Context context, File file) {
        if (null != file && file.exists()) {
            Intent share = new Intent(Intent.ACTION_SEND);
            Uri uri;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                uri = Uri.fromFile(file);
            } else {
                uri = FileProvider.getUriForFile(context, context.getResources().getString(R.string.provider_authorities), file);
            }
            share.putExtra(Intent.EXTRA_STREAM, uri);
            //此处可发送多种文件
            share.setType(getMimeType(file.getAbsolutePath()));
            share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(share, "发送文件"));
        } else {
            ToastUtils.showToastNormal("发送的文件不存在");
        }
    }

    /**
     * 根据文件后缀名获得对应的MIME类型
     *
     * @param filePath
     * @return
     */
    private String getMimeType(String filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        String mime = "*/*";
        if (filePath != null) {
            try {
                mmr.setDataSource(filePath);
                mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            } catch (IllegalStateException e) {
                return mime;
            } catch (IllegalArgumentException e) {
                return mime;
            } catch (RuntimeException e) {
                return mime;
            }
        }
        return mime;
    }


    /**
     * 选择路径
     */
    private void selectPath() {
        final List<CommonSelect> commonSelectList = new ArrayList<>();
        CommonSelect commonSelect = new CommonSelect("在抄表目录下新建文件夹", true);
        CommonSelect commonSelect1 = new CommonSelect("选择已存在的文件夹", false);
        commonSelectList.add(commonSelect);
        commonSelectList.add(commonSelect1);
        final CommonSelectDeviceEmiAdapter commonSelectAdapter = new CommonSelectDeviceEmiAdapter(commonSelectList);
        commonSelectAdapter.setCheckBoxVisibility(false);
        CommonSelectDialog.Builder builder = new CommonSelectDialog.Builder(mContext);
        builder.setTitle("复制文件");
        final CommonSelectDialog commonSelectDialog = builder.create();
        commonSelectDialog.setCancelable(true);
        commonSelectDialog.setCanceledOnTouchOutside(true);
        commonSelectAdapter.bindToRecyclerView(builder.recyclerView);
        commonSelectAdapter.setOnItemClickListener(new BaseEmiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
                commonSelectAdapter.select(commonSelectList, position);
                switch (position) {
                    case 0:
                        showCreateDialog();
                        break;
                    case 1:
                        mFilePickerDialog.show();
                        break;
                    default:
                        break;
                }
                commonSelectDialog.dismiss();
            }
        });
        builder.setAdapter(commonSelectAdapter);
        commonSelectDialog.show();
    }


    private void initFilePickerDialog() {
        final DialogProperties properties = new DialogProperties();
        //Instantiate FilePickerDialog with Context and DialogProperties.
        properties.selectType = SELECT_DIR;
        properties.selectMode = SINGLE_MODE;
        properties.root = new File(Environment.getExternalStorageDirectory() + File.separator + EMI_ROOT_PATH);
        mFilePickerDialog = new FilePickerDialog(EmiFileManageActivity.this, properties);
        mFilePickerDialog.setTitle("复制到");
        mFilePickerDialog.setPositiveBtnName("选择");
        mFilePickerDialog.setNegativeBtnName("取消");
        mFilePickerDialog.setDialogSelectionListener(new DialogSelectListener() {
            @Override
            public void onSelectFilePaths(String[] files) {
                copy(getSelectPathList(), files[0]);
            }
        });
    }


    private void copy(final List<String> pathList, final String toFile) {
        if (pathList == null || pathList.isEmpty()) {
            return;
        }
        ThreadPoolManager.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                String fromPath;
                String toFilePath;
                try {
                    fromPath = pathList.get(0);
                    toFilePath = toFile + File.separator + FileUtil.getFileName(fromPath, true);
                    FileUtil.copySdcardFile(fromPath, toFilePath);
                    notifyFileExplore(toFile);
                    pathList.remove(fromPath);
                    if (pathList.isEmpty()) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showToastNormal("复制成功");
                            }
                        });
                    } else {
                        copy(pathList, toFile);
                    }
                } catch (Exception e) {
                    e.getStackTrace();
                }
            }
        });

    }


    private List<String> getSelectPathList() {
        List<String> selectFilePathList = new ArrayList<>();
        for (FileInfo fileInfo : selectedList) {
            selectFilePathList.add(fileInfo.filePath);
            LogUtil.i(TAG, "选择的文件信息:" + fileInfo.filePath);
        }
        return selectFilePathList;
    }


    private void showPop(View view, int position) {
        final EmiPopupList normalViewPopupList = new EmiPopupList(mContext);
        List<String> titleList = new ArrayList<>();
        titleList.add("查看");
        titleList.add("删除");
        final MultiItem multiItem = mMultiItemList.get(position);
        final FileInfo fileInfo = multiItem.getData();
        normalViewPopupList.showPopupListWindow(view, position, mRawX, mRawY, titleList, new EmiPopupList.PopupListListener() {
            @Override
            public boolean showPopupList(View adapterView, View contextView, int contextPosition) {
                return true;
            }

            @Override
            public void onPopupListClick(View contextView, int contextPosition, int position) {
                switch (position) {
                    case CODE_OPEN:
                        switch ("." + fileInfo.suffix) {
                            case SUFFIX_TXT:
                                startActivity(FileUtil.getTextFileIntent(fileInfo.filePath));
                                break;
                            case SUFFIX_EXCEL:
                                startActivity(FileUtil.getExcelFileIntent(fileInfo.filePath));
                                break;
                            case SUFFIX_EXCEL_2007:
                                startActivity(FileUtil.getExcelFileIntent(fileInfo.filePath));
                                break;
                            default:
                                ToastUtils.showToastNormal("无法打开此文件");
                                break;
                        }
                        break;
                    case CODE_DELETE:
                        showDeleteConfirmAlertDialog(fileInfo.filePath, multiItem);
                        break;
                    default:
                        break;
                }
            }
        });
    }


    /**
     * 确认删除文件
     */
    private void showDeleteConfirmAlertDialog(final String filePath, final MultiItem multiItem) {
        String fileName = FileUtil.getFileName(filePath, true);
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("删除文件")
                .setContentText("将要删除文件:" + fileName)
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
                        deleteFile(filePath, multiItem);
                        sDialog.dismiss();
                    }
                })
                .show();
    }


    private void deleteFile(String path, MultiItem multiItem) {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            boolean delete = file.delete();
            if (delete) {
                ToastUtils.showToastNormal("该文件已删除");
                mMultiItemList.remove(multiItem);
                mItemQuickAdapter.notifyDataSetChanged();
            } else {
                ToastUtils.showToastNormal("文件可能被占用,删除失败");
            }
        } else {
            ToastUtils.showToastNormal("无效文件:删除失败");
        }
    }
}


