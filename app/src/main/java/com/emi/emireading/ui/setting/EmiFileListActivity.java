package com.emi.emireading.ui.setting;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.emi.emireading.R;
import com.emi.emireading.adpter.MultipleItemQuickAdapter;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.listener.OnItemClickListener;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.FileUtil;
import com.emi.emireading.entities.FileInfo;
import com.emi.emireading.entities.MultiItem;
import com.emi.emireading.log.EmiLog;
import com.emi.emireading.widget.view.EmiCheckBox;
import com.emi.emireading.widget.view.EmiRecycleViewDivider;
import com.emi.emireading.widget.view.emimenu.SystemUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.emi.emireading.core.config.EmiConstants.EXTRA_PATH;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_EXCEL;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_EXCEL_2007;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_TXT;
import static com.emi.emireading.core.utils.FileUtil.fileFilter;
import static com.emi.emireading.entities.MultiItem.FILE;
import static com.emi.emireading.entities.MultiItem.FOLD;


/**
 * @author :zhoujian
 * @description : 翼迈文件管理
 * @company :翼迈科技
 * @date 2019年01月23日下午 05:11
 * @Email: 971613168@qq.com
 */

public class EmiFileListActivity extends BaseActivity {
    private List<MultiItem> mMultiItemList = new ArrayList<>();
    private MultipleItemQuickAdapter mItemQuickAdapter;
    private List<FileInfo> mFileInfoList = new ArrayList<>();
    private File currentPathFile;
    private File sdCardFile;
    private RecyclerView rvSdCard;
    private Context mContext;
    private static final String TAG = "EmiFileListActivity";
    private TextView mTvPath;
    private String mRootPath;
    private TextView mTvSelect;

    @Override
    public int getContentLayout() {
        return R.layout.activity_sdcard;
    }

    @Override
    protected void initIntent() {
        mContext = this;
        mRootPath = getIntent().getStringExtra(EXTRA_PATH);
    }

    @Override
    protected void initUI() {
        rvSdCard = findViewById(R.id.rvSdCard);
        mTvPath = findViewById(R.id.tv_path);
        mTvSelect = findViewById(R.id.tv_all_size);
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

        });
    }

    @Override
    protected void initData() {
        mTvSelect.setText(getString(R.string.select_count, "" + 0));
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
        int count = 0;
        for (MultiItem multiItem : mMultiItemList) {
            if (multiItem.getData().select) {
                count++;
            }
            mTvSelect.setText(getString(R.string.select_count, "" + count));
        }
    }
}
