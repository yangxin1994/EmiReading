package com.emi.emireading.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.emi.emireading.R;
import com.emi.emireading.adpter.ChannelInfoEmiAdapter;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.threadpool.ThreadPoolManager;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.ui.debug.WriteDataToDeviceActivity;
import com.emi.emireading.ui.debug.WriteDataToDeviceActivitySplitPackage68;
import com.emi.emireading.widget.view.EmiRecycleViewDivider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.emi.emireading.core.config.EmiConstants.EXTRA_CHANNEL_NUMBER;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_FILE_NAME;
import static com.emi.emireading.core.config.EmiConstants.PREF_SKIP_TAG;
import static com.emi.emireading.core.config.EmiConstants.userInfoArrayList;
import static com.emi.emireading.ui.CollectorCommunicationActivity.SKIP_TAG_COLLECTOR_COMMUNICATION;
import static com.emi.emireading.ui.ConcentratorActivity.SKIP_TAG_CONCENTRATOR_COMMUNICATION;
import static com.emi.emireading.ui.debug.WriteDataToDeviceActivity.SKIP_TAG_CONCENTRATOR_COMMUNICATION_68;
import static com.emi.emireading.ui.debug.WriteDataToDeviceActivitySplitPackage68.SKIP_TAG_CONCENTRATOR_COMMUNICATION_SPLIT_PACKAGE_68;

/**
 * @author :zhoujian
 * @description : 通道号列表
 * @company :翼迈科技
 * @date 2018年03月15日上午 09:20
 * @Email: 971613168@qq.com
 */

public class ChannelListActivity extends BaseActivity implements View.OnClickListener {
    public static final int SKIP_TAG_AUTO_READ_METER = 1;
    public static final int SKIP_TAG_TASK_QUERY = 2;
    public static final String EXTRA_CHANNEL_LIST_ALL = "EXTRA_CHANNEL_LIST_ALL";
    public static final String EXTRA_CHANNEL_BUNDLE = "EXTRA_CHANNEL_BUNDLE";
    private Context mContext;
    private String mFileName;
    private TextView tvFileName;
    private TextView tvCurrentChannel;
    private List<UserInfo> userList = new ArrayList<>();
    private Handler handler = new Handler();
    private ArrayList<UserInfo> finishedList = new ArrayList<>();
    private ArrayList<UserInfo> notFinishedList = new ArrayList<>();
    private ArrayList<UserInfo> allList = new ArrayList<>();
    private ChannelInfoEmiAdapter channelInfoAdapter;
    private RecyclerView rvChannel;
    public static final String EXTRA_CHANNEL = "EXTRA_CHANNEL";
    private ArrayList<UserInfo> uiList = new ArrayList<>();
    public static final int INTENT_REFRESH_LIST = 1;
    private List<UserInfo> hasReadChannelList = new ArrayList<>();
    /**
     * 跳转的TAG
     */
    private int skipTag;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_task_list;
    }

    @Override
    protected void initIntent() {
        mContext = this;
        mFileName = getIntent().getStringExtra(EXTRA_FILE_NAME);
        skipTag = getIntent().getIntExtra(PREF_SKIP_TAG, 0);
        LogUtil.d("传过来的fileName =" + mFileName);
    }

    @Override
    protected void initUI() {
        initView();
    }

    private void initView() {
        tvFileName = findViewById(R.id.tvFileName);
        tvCurrentChannel = findViewById(R.id.tvCurrentChannel);
        rvChannel = findViewById(R.id.rvChannel);
        findViewById(R.id.btnAllChannel).setOnClickListener(this);
        findViewById(R.id.btnNoReadChannel).setOnClickListener(this);
        findViewById(R.id.btnFinishChannel).setOnClickListener(this);
    }

    @Override
    protected void initData() {
        tvCurrentChannel.setText("全部通道");
        tvFileName.setText(mFileName);
        channelInfoAdapter = new ChannelInfoEmiAdapter(uiList);
        rvChannel.setLayoutManager(new LinearLayoutManager(ChannelListActivity.this));
        rvChannel.addItemDecoration(new EmiRecycleViewDivider(this, LinearLayout.HORIZONTAL));
        channelInfoAdapter.bindToRecyclerView(rvChannel);
        rvChannel.setAdapter(channelInfoAdapter);
        channelInfoAdapter.setOnItemClickListener(new BaseEmiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
                Intent intent = new Intent();
                UserInfo userInfo = uiList.get(position);
                intent.putExtra(EXTRA_FILE_NAME, mFileName);
                intent.putExtra(EXTRA_CHANNEL, userInfo.channelNumber);
                intent.putExtra(EXTRA_CHANNEL_NUMBER, userInfo.channelNumber);
                switch (skipTag) {
                    case SKIP_TAG_AUTO_READ_METER:
                        intent.setClass(mContext, AutoReadMeterActivity.class);
                        startActivityForResult(intent, INTENT_REFRESH_LIST);
                        break;
                    case SKIP_TAG_TASK_QUERY:
                        intent.setClass(mContext, UserInfoListActivity.class);
                        startActivity(intent);
                        break;
                    case SKIP_TAG_COLLECTOR_COMMUNICATION:
                        //集中器通讯A
                        intent.setClass(mContext, CollectorCommunicationActivity.class);
                        startActivity(intent);
                        break;
                    case SKIP_TAG_CONCENTRATOR_COMMUNICATION:
                        intent.setClass(mContext, ConcentratorActivity.class);
                        //全部通道号（采集器编号）
                        Bundle channelBundle = new Bundle();
                        List<String> allChannelList = new ArrayList<>();
                        for (UserInfo info : allList) {
                            allChannelList.add(info.channelNumber);
                        }
                        channelBundle.putSerializable(EXTRA_CHANNEL_LIST_ALL, (Serializable) allChannelList);
                        intent.putExtra(EXTRA_CHANNEL_BUNDLE, channelBundle);
                        startActivity(intent);
                        break;
                    case SKIP_TAG_CONCENTRATOR_COMMUNICATION_68:
                        intent.setClass(mContext, WriteDataToDeviceActivity.class);
                        //全部通道号（采集器编号）
                        Bundle channelBundle68 = new Bundle();
                        List<String> allChannelList68 = new ArrayList<>();
                        for (UserInfo info : allList) {
                            allChannelList68.add(info.channelNumber);
                        }
                        channelBundle68.putSerializable(EXTRA_CHANNEL_LIST_ALL, (Serializable) allChannelList68);
                        intent.putExtra(EXTRA_CHANNEL_BUNDLE, channelBundle68);
                        startActivity(intent);
                        break;
                    case SKIP_TAG_CONCENTRATOR_COMMUNICATION_SPLIT_PACKAGE_68:
                        intent.setClass(mContext, WriteDataToDeviceActivitySplitPackage68.class);
                        //全部通道号（采集器编号）
                        Bundle channelBundleSplit68 = new Bundle();
                        List<String> allChannelListSplit68 = new ArrayList<>();
                        for (UserInfo info : allList) {
                            allChannelListSplit68.add(info.channelNumber);
                        }
                        channelBundleSplit68.putSerializable(EXTRA_CHANNEL_LIST_ALL, (Serializable) allChannelListSplit68);
                        intent.putExtra(EXTRA_CHANNEL_BUNDLE, channelBundleSplit68);
                        startActivity(intent);
                        break;
                    default:
                        ToastUtil.showShortToast("未获取到跳转类型");
                        break;
                }
                uiList.get(position);
            }
        });
        ThreadPoolManager.EXECUTOR.execute(new LoadChannelRunnable());
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAllChannel:
                showAllChannelList();
                break;
            case R.id.btnNoReadChannel:
                showNoFinishList();
                break;
            case R.id.btnFinishChannel:
                showFinishList();
                break;
            default:
                break;
        }
    }


    private class LoadChannelRunnable implements Runnable {
        @Override
        public void run() {
            getAllData();
            addData();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    channelInfoAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void getAllData() {
        if (mFileName != null) {
            allList.clear();
            uiList.clear();
            hasReadChannelList.clear();
            userList = getSqOperator().find(mFileName);
            userInfoArrayList = (ArrayList<UserInfo>) userList;
            allList.clear();
            allList.addAll(removeSameChannelNumber(userList));
            List<UserInfo> hasReadList = removeSameChannel(allList);
            hasReadChannelList.addAll(removeEmptyChannel(hasReadList));
            //去除重复的通道号
            uiList.addAll(allList);
        }
    }

    private void addData() {
        notFinishedList.clear();
        finishedList.clear();
        for (UserInfo userInfo : uiList) {
            boolean isFindHasRead = false;
            for (UserInfo hasReadUserInfo : hasReadChannelList) {
                if (userInfo.channelNumber.equals(hasReadUserInfo.channelNumber)) {
                    userInfo.channel = userInfo.channelNumber;
                    finishedList.add(userInfo);
                    isFindHasRead = true;
                    break;
                }
            }
            if (isFindHasRead) {
                finishedList.add(userInfo);
            } else {
                notFinishedList.add(userInfo);
            }

        }
       /* for (UserInfo userInfo : tempList) {
            LogUtil.d(TAG, "回调回来的通道板：" + userInfo.channel);
            if (EmiStringUtil.isEmpty(userInfo.channel)) {
                notFinishedList.add(userInfo);
            } else {
                finishedList.add(userInfo);
            }
        }*/
    }


    private void showAllChannelList() {
        uiList.clear();
        tvCurrentChannel.setText("全部通道");
        uiList.addAll(allList);
        channelInfoAdapter.notifyDataSetChanged();
    }

    private void showNoFinishList() {
        uiList.clear();
        tvCurrentChannel.setText("未完成通道");
        uiList.addAll(notFinishedList);
        channelInfoAdapter.notifyDataSetChanged();
    }

    private void showFinishList() {
        uiList.clear();
        tvCurrentChannel.setText("已完成通道");
        uiList.addAll(removeSameChannel(finishedList));
        channelInfoAdapter.notifyDataSetChanged();
    }

    public List<UserInfo> removeSameChannelNumber(List<UserInfo> dataList) {
        //从左向右循环
        List<UserInfo> userInfoList = new ArrayList<>();
        userInfoList.addAll(dataList);
        for (int i = 0; i < userInfoList.size() - 1; i++) {
            //从右往左内循环
            for (int j = userInfoList.size() - 1; j > i; j--) {
                if (userInfoList.get(j).channelNumber.equals(userInfoList.get(i).channelNumber)) {
                    //相等则移除
                    userInfoList.remove(j);
                }
            }
        }
        return userInfoList;
    }


    public List<UserInfo> removeSameChannel(List<UserInfo> dataList) {
        //从左向右循环
        List<UserInfo> userInfoList = new ArrayList<>();
        userInfoList.addAll(dataList);
        for (int i = 0; i < userInfoList.size() - 1; i++) {
            //从右往左内循环
            for (int j = userInfoList.size() - 1; j > i; j--) {
                if (userInfoList.get(j).channel.equals(userInfoList.get(i).channel)) {
                    //相等则移除
                    userInfoList.remove(j);
                }
            }
        }
        if (userInfoList.size() == 1 && TextUtils.isEmpty(userInfoList.get(0).channel)) {
            userInfoList.remove(0);
        }
        return userInfoList;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ThreadPoolManager.EXECUTOR.execute(new LoadChannelRunnable());
    }

    /**
     * 移除空的通道板号
     *
     * @param userList
     * @return
     */
    private List<UserInfo> removeEmptyChannel(List<UserInfo> userList) {
        for (int i = 0; i < userList.size(); i++) {
            if (TextUtils.isEmpty(userList.get(i).channel)) {
                userList.remove(i);
            }
        }
        return userList;
    }
}
