package com.emi.emireading.adpter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.emi.emireading.R;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.CommonViewHolder;
import com.emi.emireading.entities.UserInfo;

import java.util.ArrayList;
import java.util.List;

import static com.emi.emireading.core.config.EmiConstants.EMPTY_METER_DATA;
import static com.emi.emireading.core.config.EmiConstants.ERROR_METER_DATA;
import static com.emi.emireading.core.config.EmiConstants.STATE_ALL;
import static com.emi.emireading.core.config.EmiConstants.STATE_FAILED;
import static com.emi.emireading.core.config.EmiConstants.STATE_NO_READ;
import static com.emi.emireading.core.config.EmiConstants.STATE_PEOPLE_RECORDING;
import static com.emi.emireading.core.config.EmiConstants.STATE_SUCCESS;
import static com.emi.emireading.core.config.EmiConstants.STATE_WARNING;


/**
 * 描述:
 *
 * @author chx
 *         Created by chx on 2017/4/6.
 *         邮箱:snake_chenhx@163.com
 * @author chx
 */

public class DataAdapter extends BaseAdapter implements Filterable {
     private Context mContext;
    private ArrayList<UserInfo> list;
    /**
     * 备份的原始数据
     */
    private ArrayList<UserInfo> backUpList = new ArrayList<>();
    private SelectFilter selectFilter;
    private OnFilterListener onFilterListener;

    public DataAdapter(Context mContext, ArrayList<UserInfo> list) {
        this.mContext = mContext;
        this.list = list;
        this.backUpList.addAll(list);
        LogUtil.d("数据源长度为：" + backUpList.size());
    }

    @Override
    public int getCount() {
        return list.size();
    }

    public List<UserInfo> getDataList() {
        return list;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_query, parent, false);
        }
        UserInfo userInfo = list.get(position);
        TextView tv_num = CommonViewHolder.get(convertView, R.id.tvUserId);
        TextView tv_address = CommonViewHolder.get(convertView, R.id.tvAddress);
        TextView tv_meter_address = CommonViewHolder.get(convertView, R.id.tvMeterAddress);
        TextView tv_lastdata = CommonViewHolder.get(convertView, R.id.tvCurrentUseAge);
        ImageView ivState = CommonViewHolder.get(convertView, R.id.ivState);
        tv_num.setText(list.get(position).getAccountnum());
        tv_address.setText(list.get(position).getUseraddr());
        String meterAddress = list.get(position).getMeteraddr();
        int state = list.get(position).getState();
        if (TextUtils.isEmpty(meterAddress)) {
            tv_meter_address.setText("空");

        } else {
            tv_meter_address.setText(meterAddress);
        }
        if (userInfo.state == STATE_NO_READ || userInfo.state == STATE_ALL) {
            tv_lastdata.setText(EMPTY_METER_DATA);
        } else if (userInfo.state == STATE_FAILED) {
            tv_lastdata.setText(ERROR_METER_DATA);
        } else {
            tv_lastdata.setText(list.get(position).getCuryl() + "");
        }
        switch (state) {
            case STATE_NO_READ:
                //未抄
                ivState.setImageResource(R.mipmap.red);
                break;
            case STATE_FAILED:
                //失败
                ivState.setImageResource(R.mipmap.red);
                break;
            case STATE_SUCCESS:
                //正常
                ivState.setImageResource(R.mipmap.star);
                break;
            case STATE_WARNING:
                //用数量异常
                ivState.setImageResource(R.mipmap.know);
                break;
            case STATE_PEOPLE_RECORDING:
                //人工补录
                ivState.setImageResource(R.mipmap.star);
                break;
            default:
                break;
        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (selectFilter == null) {
            selectFilter = new SelectFilter();
        }
        return selectFilter;
    }

    private class SelectFilter extends Filter {


        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            String constraintStr = constraint.toString();
            List<UserInfo> userList;
            if (constraintStr.length() > 0) {
                userList = new ArrayList<>();
                for (UserInfo userInfo : backUpList) {
                    if (userInfo.username.contains(constraintStr) || userInfo.meteraddr.contains(constraintStr) || userInfo.getUseraddr().contains(constraintStr)
                            || userInfo.accountnum.contains(constraintStr)) {
                        userList.add(userInfo);
                    }
                }
            } else {
                userList = backUpList;
            }
            results.values = userList;
            results.count = userList.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results == null) {
                list = backUpList;
            } else {
                if (results.values != null) {
                    list = (ArrayList<UserInfo>) results.values;
                } else {
                    list = backUpList;
                }
            }
       /*     LogUtil.w(TAG, "constraint：" + constraint.toString());
            LogUtil.w(TAG, "用户集合长度：" + list.size());*/
            if (results != null && results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
            if (onFilterListener != null) {
                if (constraint.toString().length() > 0) {
                    onFilterListener.filterResult(list);
                } else {
                    onFilterListener.filterResult(backUpList);
                }
            }
        }
    }


    public interface OnFilterListener {
        /**
         * 过滤的结果回调
         *
         * @param filterData
         */
        void filterResult(ArrayList<UserInfo> filterData);
    }

    public void setOnFilterListener(OnFilterListener onFilterListener) {
        this.onFilterListener = onFilterListener;
    }

}
