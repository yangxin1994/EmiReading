

package com.emi.emireading.widget.filepicker.controller.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.emi.emireading.R;
import com.emi.emireading.widget.filepicker.controller.NotifyItemChecked;
import com.emi.emireading.widget.filepicker.controller.module.BaseDialogConfig;
import com.emi.emireading.widget.filepicker.controller.module.DialogProperties;
import com.emi.emireading.widget.filepicker.controller.module.FileItem;
import com.emi.emireading.widget.filepicker.controller.module.MarkedItem;
import com.emi.emireading.widget.filepicker.view.MaterialCheckbox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


/**
 * @author :zhoujian
 * @description : 文件列表适配器
 * @company :翼迈科技
 * @date 2019年01月29日上午 11:09
 * @Email: 971613168@qq.com
 */

public class FileListAdapter extends BaseAdapter {
    private static final String TAG = "FileListAdapter";
    private ArrayList<FileItem> listItem;
    private Context context;
    private DialogProperties properties;
    private NotifyItemChecked notifyItemChecked;

    public FileListAdapter(ArrayList<FileItem> listItem, Context context, DialogProperties properties) {
        this.listItem = listItem;
        this.context = context;
        this.properties = properties;
    }

    @Override
    public int getCount() {
        return listItem.size();
    }

    @Override
    public FileItem getItem(int i) {
        return listItem.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    @SuppressWarnings("deprecation")
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.dialog_file_list_item, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        final FileItem item = listItem.get(i);
        if (MarkedItem.hasItem(item.getLocation())) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.marked_item_animation);
            view.setAnimation(animation);
        } else {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.unmarked_item_animation);
            view.setAnimation(animation);
        }
        if (item.isDirectory()) {
            holder.type_icon.setImageResource(R.mipmap.ic_type_folder);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.type_icon.setColorFilter(context.getResources().getColor(R.color.colorPrimary, context.getTheme()));
            } else {
                holder.type_icon.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
            }
            if (properties.selectType == BaseDialogConfig.SELECT_FILE) {
                holder.fmark.setVisibility(View.INVISIBLE);
            } else {
                holder.fmark.setVisibility(View.VISIBLE);
            }
        } else {
            holder.type_icon.setImageResource(R.mipmap.ic_type_file);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.type_icon.setColorFilter(context.getResources().getColor(R.color.colorAccent, context.getTheme()));
            } else {
                holder.type_icon.setColorFilter(context.getResources().getColor(R.color.colorAccent));
            }
            if (properties.selectType == BaseDialogConfig.SELECT_DIR) {
                holder.fmark.setVisibility(View.INVISIBLE);
            } else {
                holder.fmark.setVisibility(View.VISIBLE);
            }
        }
        holder.type_icon.setContentDescription(item.getFilename());
        holder.name.setText(item.getFilename());
        SimpleDateFormat sdate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat stime = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
        Date date = new Date(item.getTime());
        if (i == 0 && item.getFilename().startsWith(context.getString(R.string.label_parent_dir))) {
            holder.type.setText(R.string.label_parent_directory);
        } else {
            holder.type.setText(context.getString(R.string.last_edit) + sdate.format(date) + ", " + stime.format(date));
        }
        if (holder.fmark.getVisibility() == View.VISIBLE) {
            if (i == 0 && item.getFilename().startsWith(context.getString(R.string.label_parent_dir))) {
                holder.fmark.setVisibility(View.INVISIBLE);
            }
            if (MarkedItem.hasItem(item.getLocation())) {
                holder.fmark.setChecked(true);
            } else {
                holder.fmark.setChecked(false);
            }
        }
        holder.fmark.setOnCheckedChangedListener(new MaterialCheckbox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(MaterialCheckbox checkbox, boolean isChecked) {
                item.setMarked(isChecked);
                if (item.isMarked()) {
                    if (properties.selectMode == BaseDialogConfig.MULTI_MODE) {
                        MarkedItem.addSelectedItem(item);
                    } else {
                        MarkedItem.addSingleFile(item);
                    }
                } else {
                    MarkedItem.removeSelectedItem(item.getLocation());
                }
                notifyItemChecked.notifyCheckBoxIsClicked();
            }
        });
        return view;
    }

    private class ViewHolder {
        ImageView type_icon;
        TextView name, type;
        MaterialCheckbox fmark;

        ViewHolder(View itemView) {
            name = itemView.findViewById(R.id.tvFileName);
            type = itemView.findViewById(R.id.tvFileType);
            type_icon = itemView.findViewById(R.id.image_type);
            fmark = itemView.findViewById(R.id.file_mark);
        }
    }

    public void setNotifyItemCheckedListener(NotifyItemChecked notifyItemChecked) {
        this.notifyItemChecked = notifyItemChecked;
    }
}
