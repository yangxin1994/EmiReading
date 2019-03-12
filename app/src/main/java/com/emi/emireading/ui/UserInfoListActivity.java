package com.emi.emireading.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.emi.emireading.R;
import com.emi.emireading.adpter.UserListEmiAdapter;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.adapter.BaseEmiAdapter;
import com.emi.emireading.core.threadpool.ThreadPoolManager;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.config.EmiConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.entities.UserInfo;
import com.emi.emireading.ui.debug.SingleMeterDebugActivity;
import com.emi.emireading.widget.view.TitleView;
import com.emi.emireading.widget.view.dialog.LoadingDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.emi.emireading.core.config.EmiConstants.CURRENT_ITEM_TAG_KEY;
import static com.emi.emireading.core.config.EmiConstants.EDIT_DATA_KEY;
import static com.emi.emireading.core.config.EmiConstants.EDIT_LIST_KEY;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_CHANNEL_NUMBER;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_CURRENT_TAG;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_FILE_NAME;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_METER_FIRM_CODE;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_METER_ID;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_POSITION;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_SKIP_TAG;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_USER_LIST;
import static com.emi.emireading.core.config.EmiConstants.STATE_FAILED;
import static com.emi.emireading.core.config.EmiConstants.STATE_PEOPLE_RECORDING;
import static com.emi.emireading.core.config.EmiConstants.STATE_WARNING;
import static com.emi.emireading.ui.ChannelListActivity.EXTRA_CHANNEL;
import static com.emi.emireading.ui.ChannelListActivity.SKIP_TAG_AUTO_READ_METER;
import static com.emi.emireading.ui.MeterQueryActivity.REQUEST_CODE_DETAIL_INFO;
import static com.emi.emireading.ui.UserInfoDetailActivity.EXTRA_BUNDLE;
import static com.emi.emireading.ui.UserInfoDetailActivity.EXTRA_EDIT_DATA_LIST;


/**
 * @author zhoujian
 * @description 用户信息列表
 * @company 翼迈科技
 * @date 2017年10月20日下午 04:02
 * @Email: 971613168@qq.com
 */

public class UserInfoListActivity extends BaseActivity implements View.OnClickListener {
    private static final int REQUEST_CODE = 1;
    private static final int REQUEST_CODE_SINGLE_DEBUG = 5;
    private UserListEmiAdapter mUserListAdapter;
    private static final int LOAD_FINISH = 100;
    private RecyclerView rvUserInfo;
    private ArrayList<UserInfo> tempList = new ArrayList<>();
    private ArrayList<UserInfo> mUserInfoArrayList;
    private Context mContext;
    private Button btnAll;
    private Button btnWarning;
    private Button btnReRead;
    private Button btnTotal;
    private Button btnFail;
    private int currentTag;
    private final int TAG_ALL = 0;
    private TitleView titleView;
    private String fileName;
    private LoadingDialog mDialog;
    public static final int REQUEST_CODE_USER_DETAIL = 2;
    public static final int RESULT_CODE_CHANNEL_DATA = 3;
    private String channelNumber;
    private int skipTag;
    private MyHandler mHandler = new MyHandler(this);
    private ArrayList<UserInfo> editUserList = new ArrayList<>();

    private static class MyHandler extends Handler {
        WeakReference<Activity> mWeakReference;

        private MyHandler(Activity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final UserInfoListActivity activity = (UserInfoListActivity) mWeakReference.get();
            switch (msg.what) {
                case LOAD_FINISH:
                    LogUtil.d(TAG, "已执行");
                    activity.loadData();
                    activity.closeDialog();
                    break;
                default:
                    activity.closeDialog();
                    break;
            }
        }
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_channel_query;
    }

    @Override
    protected void initIntent() {
        mContext = this;
        channelNumber = getIntent().getStringExtra(EXTRA_CHANNEL);
        skipTag = getIntent().getIntExtra(EXTRA_SKIP_TAG, 0);
        fileName = getIntent().getStringExtra(EXTRA_FILE_NAME);
        if (channelNumber == null) {
            channelNumber = "";
        }
        LogUtil.d(TAG, "fileName：" + fileName);
        LogUtil.d(TAG, "mChannelStr：" + channelNumber);
    }

    @Override
    protected void initUI() {
        titleView = (TitleView) findViewById(R.id.titleView);
        rvUserInfo = (RecyclerView) findViewById(R.id.rv_userInfoList);
        btnAll = (Button) findViewById(R.id.bt_normal);
        btnWarning = (Button) findViewById(R.id.bt_unormal);
        btnReRead = (Button) findViewById(R.id.bt_rewrite);
        btnTotal = (Button) findViewById(R.id.count);
        btnFail = (Button) findViewById(R.id.bt_fail);
        btnAll.setOnClickListener(this);
        btnWarning.setOnClickListener(this);
        btnReRead.setOnClickListener(this);
        btnTotal.setOnClickListener(this);
        btnFail.setOnClickListener(this);
        mUserInfoArrayList = new ArrayList<>();
        String title = "通道号：" + channelNumber;
        titleView.setTitle(title);
    }

    @Override
    protected void initData() {
        mUserListAdapter = new UserListEmiAdapter(mUserInfoArrayList);
        rvUserInfo.setLayoutManager(new LinearLayoutManager(this));
        mDialog = new LoadingDialog(UserInfoListActivity.this, "正在查询...");
        mDialog.show();
        ThreadPoolManager.EXECUTOR.execute(new QueryUserInfoRunnable());
        mUserListAdapter.setOnItemClickListener(new BaseEmiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseEmiAdapter adapter, View view, int position) {
                skipToUserDetailActivity(position);
            }
        });
        if (EmiUtils.isDebugMode()) {
            mUserListAdapter.setOnItemLongClickListener(new BaseEmiAdapter.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(BaseEmiAdapter adapter, View view, int position) {
                    doSkipSingleDebugActivity(mUserInfoArrayList, position);
                    return true;
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_normal:
                currentTag = TAG_ALL;
                showAllData();
                break;
            case R.id.bt_unormal:
                currentTag = STATE_WARNING;
                showDataByState(STATE_WARNING);
                break;
            case R.id.bt_fail:
                currentTag = STATE_FAILED;
                showDataByState(STATE_FAILED);
                break;
            case R.id.bt_rewrite:
                currentTag = STATE_PEOPLE_RECORDING;
                showDataByState(STATE_PEOPLE_RECORDING);
                break;
            case R.id.count:
                skipToCountActivity();
                break;
            default:
                break;
        }
    }

    /**
     * 查询通道号下对应的所有用户
     */
    private class QueryUserInfoRunnable implements Runnable {
        @Override
        public void run() {
            mUserInfoArrayList.clear();
            if (!TextUtils.isEmpty(fileName) && !TextUtils.isEmpty(channelNumber)) {
                mUserInfoArrayList.addAll(getSqOperator().findByChannel(fileName, channelNumber));
                EmiConstants.userInfoArrayList = mUserInfoArrayList;
                sendMsg(LOAD_FINISH);
            } else {
                sendMsg(LOAD_FINISH);
            }

          /*  if (EmiConstants.userInfoArrayList != null) {
                mUserInfoArrayList.addAll(EmiConstants.userInfoArrayList);
                for (int i = mUserInfoArrayList.size() - 1; i >= 0; i--) {
                    if (!channelNumber.equals(mUserInfoArrayList.get(i).channelNumber)) {
                        mUserInfoArrayList.remove(i);
                    }
                }
                sendMsg(LOAD_FINISH);
            } else {
                //无数据
                sendMsg(LOAD_FINISH);
            }*/
        }

    }


    private void sendMsg(int what) {
        mHandler.sendEmptyMessage(what);
    }


    private class MyItemDecoration extends RecyclerView.ItemDecoration {
        /**
         * @param outRect 边界
         * @param view    recyclerView ItemView
         * @param parent  recyclerView
         * @param state   recycler 内部数据管理
         */
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            //设定底部边距为1px
            outRect.set(0, 0, 0, 1);
        }
    }

    private void loadData() {
        rvUserInfo.addItemDecoration(new MyItemDecoration());
        rvUserInfo.setAdapter(mUserListAdapter);
        if (mUserInfoArrayList.isEmpty()) {
            rvUserInfo.setBackgroundColor(ContextCompat.getColor(UserInfoListActivity.this, R.color.white));
        }
        backUpDataList(mUserInfoArrayList);
        mUserListAdapter.bindToRecyclerView(rvUserInfo);
        mUserListAdapter.setEmptyView(R.layout.layout_empty_view);
    }


    /**
     * 跳转至统计查询
     */
    private void skipToUserDetailActivity(int position) {
        Intent intent = new Intent();
        intent.setClass(mContext, UserInfoDetailActivity.class);
        intent.putExtra(EXTRA_POSITION, position);
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        intent.putExtra(EXTRA_CURRENT_TAG, currentTag);
        intent.putExtra(EXTRA_CHANNEL_NUMBER, channelNumber);
        EmiConfig.userInfoArrayList = tempList;
        startActivityForResult(intent, REQUEST_CODE_USER_DETAIL);
    }

    /**
     * 跳转至统计查询
     */
    private void skipToCountActivity() {
        Intent intent = new Intent();
        intent.setClass(mContext, ChannelCountActivity.class);
        EmiConfig.userInfoArrayList = tempList;
        startActivity(intent);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getBundleExtra(EDIT_DATA_KEY);
                    ArrayList<UserInfo> callBackList = (ArrayList<UserInfo>) bundle.getSerializable(EDIT_LIST_KEY);
                    if (callBackList != null && !callBackList.isEmpty()) {
                        updateList(callBackList);
                    }
                    mUserInfoArrayList.clear();
                    mUserInfoArrayList.addAll(tempList);
                    editUserList.clear();
                    editUserList.addAll(callBackList);
                    LogUtil.i(TAG, "CURRENT_ITEM_TAG_KEY：" + bundle.getInt(CURRENT_ITEM_TAG_KEY));
                    showDataByState(bundle.getInt(CURRENT_ITEM_TAG_KEY, 0));
                }
                break;

            case REQUEST_CODE_USER_DETAIL:
                LogUtil.w("数据长度：" + tempList.size());
                LogUtil.w("返回码：" + resultCode);
                if (data != null) {
                    showDataByState(currentTag);
                }
                if (REQUEST_CODE_DETAIL_INFO == resultCode) {
                    if (data != null) {
                        Bundle bundle = data.getBundleExtra(EXTRA_BUNDLE);
                        ArrayList<UserInfo> editCallbackList = (ArrayList<UserInfo>) bundle.getSerializable(EXTRA_EDIT_DATA_LIST);
                        if (editCallbackList != null) {
                            editUserList.clear();
                            editUserList.addAll(editCallbackList);
                        }
                    }
                }
                break;
            default:
                break;
        }

    }

    /**
     * 更新数据源
     *
     * @param dataList
     */
    private void updateList(ArrayList<UserInfo> dataList) {
        for (int i = 0; i < dataList.size(); i++) {
            for (int j = 0; j < tempList.size(); j++) {
                String editUserId = dataList.get(i).accountnum;
                String userId = tempList.get(j).accountnum;
                if (userId.equals(editUserId)) {
                    int yl = dataList.get(i).curyl;
                    int state = dataList.get(i).state;
                    String readDate = dataList.get(i).curreaddate;
                    UserInfo editUserInfo = tempList.get(j);
                    editUserInfo.setCuryl(yl);
                    editUserInfo.state = state;
                    editUserInfo.curreaddate = readDate;
                    tempList.set(j, editUserInfo);
                    break;
                }
            }
        }
    }

    private void backUpDataList(List<UserInfo> list) {
        tempList.clear();
        tempList.addAll(list);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showAllData() {
        mUserInfoArrayList.clear();
        mUserInfoArrayList.addAll(tempList);
        mUserListAdapter.notifyDataSetChanged();
    }

    private void showDataByState(int state) {
        mUserInfoArrayList.clear();
        mUserInfoArrayList.addAll(tempList);
        if (currentTag != TAG_ALL) {
            for (int i = mUserInfoArrayList.size() - 1; i >= 0; i--) {
                if ((mUserInfoArrayList.get(i).state != state)) {
                    mUserInfoArrayList.remove(i);
                }
            }
        }
        mUserListAdapter.notifyDataSetChanged();
    }


    private void closeDialog() {
        if (mDialog != null && (mDialog.isShowing())) {
            mDialog.close();
        }
    }


    @Override
    public void finish() {
        Intent data = new Intent();
        if (skipTag == SKIP_TAG_AUTO_READ_METER) {
            LogUtil.d(TAG, "修改的集合长度：" + editUserList.size());
            data.putExtra(EXTRA_USER_LIST, editUserList);
        } else {
            LogUtil.e(TAG, "修改的集合长度：" + tempList.size());
            data.putExtra(EXTRA_USER_LIST, tempList);
        }
        setResult(RESULT_CODE_CHANNEL_DATA, data);
        super.finish();
    }

    private void doSkipSingleDebugActivity(List<UserInfo> userInfoList, int position) {
        if (userInfoList != null && !userInfoList.isEmpty()) {
            UserInfo userInfo = userInfoList.get(position);
            if (userInfo.meteraddr != null && userInfo.firmCode != null) {
                Intent intent = new Intent();
                intent.setClass(mContext, SingleMeterDebugActivity.class);
                intent.putExtra(EXTRA_METER_ID, userInfo.meteraddr);
                intent.putExtra(EXTRA_METER_FIRM_CODE, userInfo.firmCode);
                startActivityForResult(intent, REQUEST_CODE_SINGLE_DEBUG);
            }
        }
    }

}
