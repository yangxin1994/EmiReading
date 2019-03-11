package com.emi.emireading.adpter;

import android.widget.ImageView;

import com.emi.emireading.R;
import com.emi.emireading.core.adapter.BaseMultiItemQuickEmiAdapter;
import com.emi.emireading.core.adapter.BaseViewHolder;
import com.emi.emireading.core.utils.FileUtil;
import com.emi.emireading.core.utils.GlideManager;
import com.emi.emireading.entities.MultiItem;
import com.emi.emireading.widget.view.EmiCheckBox;

import java.util.List;

/**
 * @author :zhoujian
 * @description : 文件管理适配器
 * @company :翼迈科技
 * @date 2019年01月24日上午 10:51
 * @Email: 971613168@qq.com
 */

public class MultipleItemQuickAdapter extends BaseMultiItemQuickEmiAdapter<MultiItem, BaseViewHolder> {
    private static final String TAG = "MultipleItemQuickAdapter";

    public MultipleItemQuickAdapter(List<MultiItem> data) {
        super(data);
        addItemType(MultiItem.FOLD, R.layout.item_fold);
        addItemType(MultiItem.FILE, R.layout.item_file);
    }

    @Override
    protected void convert(BaseViewHolder helper, MultiItem item) {
        helper.setText(R.id.tv_file_name, item.getData().getFileName());
        if (item.getItemType() == MultiItem.FOLD) {
            GlideManager.loadImg(R.drawable.rc_ad_list_folder_icon, (ImageView) helper.getView(R.id.iv_file));
        } else {
            helper.setText(R.id.tv_file_size, FileUtil.formatFileSize(item.getData().fileSize));
            helper.setText(R.id.tv_file_time, item.getData().date);
            if (item.getData().select) {
                ((EmiCheckBox) helper.getView(R.id.cb_file)).setChecked(true, false);
            } else {
                ((EmiCheckBox) helper.getView(R.id.cb_file)).setChecked(false, false);
            }
            GlideManager.loadImg(FileUtil.getFileTypeImageId(item.getData().getFileName()), (ImageView) helper.getView(R.id.iv_file));
        }
    }

}
