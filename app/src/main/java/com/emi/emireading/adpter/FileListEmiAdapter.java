package com.emi.emireading.adpter;

import android.support.annotation.Nullable;

import com.emi.emireading.R;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.adapter.BaseViewHolder;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.entities.FileEntity;

import java.util.List;

import static com.emi.emireading.core.config.EmiConstants.SUFFIX_DBF;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_EXCEL;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_EXCEL_2007;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_FOLDER;
import static com.emi.emireading.core.config.EmiConstants.SUFFIX_TXT;

/**
 * @author :zhoujian
 * @description : zj
 * @company :翼迈科技
 * @date 2018年06月09日上午 10:12
 * @Email: 971613168@qq.com
 */

public class FileListEmiAdapter extends BaseEmiAdapter<FileEntity, BaseViewHolder> {
    public FileListEmiAdapter(@Nullable List<FileEntity> data) {
        super(R.layout.item_file_list_layout, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, FileEntity fileEntity) {
        helper.setText(R.id.tvFileName, fileEntity.getFileName());
        LogUtil.d("文件后缀名：" + fileEntity.fileSuffix);
        switch (fileEntity.fileSuffix) {
            case SUFFIX_EXCEL_2007:
                helper.setImageResource(R.id.ivFileType, R.mipmap.icon_file_type_excel_2007);
                break;
            case SUFFIX_EXCEL:
                helper.setImageResource(R.id.ivFileType, R.mipmap.icon_file_type_excel);
                break;
            case SUFFIX_TXT:
                helper.setImageResource(R.id.ivFileType, R.mipmap.icon_file_type_txt);
                break;
            case SUFFIX_DBF:
                helper.setImageResource(R.id.ivFileType, R.mipmap.icon_file_type_dbf);
                break;
            case SUFFIX_FOLDER:
                helper.setImageResource(R.id.ivFileType, R.mipmap.icon_file_explorer);
                break;
            default:
                helper.setImageResource(R.id.ivFileType, R.mipmap.icon_unkonw);
                break;
        }
    }

}
