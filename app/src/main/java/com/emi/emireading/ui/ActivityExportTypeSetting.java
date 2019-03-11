package com.emi.emireading.ui;

import android.view.View;
import android.widget.ImageView;

import com.emi.emireading.R;
import com.emi.emireading.common.EmiUtils;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.config.EmiConfig;
import com.emi.emireading.core.config.EmiConstants;

/**
 * @author :zhoujian
 * @description : 导出文件类型设置
 * @company :翼迈科技
 * @date 2018年04月20日上午 09:35
 * @Email: 971613168@qq.com
 */

public class ActivityExportTypeSetting extends BaseActivity implements View.OnClickListener {
    /**
     * 导出全部
     */
    private ImageView ivAll;
    private ImageView ivHasRead;
    private ImageView ivNoRead;
    private ImageView ivSuccess;
    private ImageView ivWarning;
    private ImageView ivFailed;
    private ImageView ivNormal;
    private ImageView ivPeopleRecording;

    @Override
    protected int getContentLayout() {
        return R.layout.emi_activity_export_type_setting;
    }

    @Override
    protected void initIntent() {

    }

    @Override
    protected void initUI() {
        initView();
        initConfig();
    }

    @Override
    protected void initData() {

    }

    private void initView() {
        ivAll = findViewById(R.id.ivAll);
        ivHasRead = findViewById(R.id.ivHasRead);
        ivNoRead = findViewById(R.id.ivNoRead);
        ivHasRead = findViewById(R.id.ivHasRead);
        ivSuccess = findViewById(R.id.ivSuccess);
        ivFailed = findViewById(R.id.ivFailed);
        ivWarning = findViewById(R.id.ivWarning);
        ivNormal = findViewById(R.id.ivNormal);
        ivPeopleRecording = findViewById(R.id.ivPeopleRecording);
        findViewById(R.id.relayExportAll).setOnClickListener(this);
        findViewById(R.id.relayExportNoRead).setOnClickListener(this);
        findViewById(R.id.relayExportHasRead).setOnClickListener(this);
        findViewById(R.id.relayExportSuccess).setOnClickListener(this);
        findViewById(R.id.relayExportFailed).setOnClickListener(this);
        findViewById(R.id.relayExportWarning).setOnClickListener(this);
        findViewById(R.id.relayExportNormal).setOnClickListener(this);
        findViewById(R.id.relayExportPeopleRecording).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.relayExportAll:
                doSave(EmiConstants.STATE_ALL);
                showExportAll();
                break;
            case R.id.relayExportHasRead:
                doSave(EmiConstants.STATE_HAS_READ);
                showExportHasRead();
                break;
            case R.id.relayExportNoRead:
                doSave(EmiConstants.STATE_NO_READ);
                showExportNoRead();
                break;
            case R.id.relayExportSuccess:
                doSave(EmiConstants.STATE_SUCCESS);
                showExportSuccess();
                break;
            case R.id.relayExportFailed:
                doSave(EmiConstants.STATE_FAILED);
                showExportFailed();
                break;
            case R.id.relayExportWarning:
                doSave(EmiConstants.STATE_WARNING);
                showExportWarning();
                break;
            case R.id.relayExportNormal:
                //正常、异常、补录
                doSave(EmiConstants.STATE_NORMAL);
                showExportNormal();
                break;
            case R.id.relayExportPeopleRecording:
                doSave(EmiConstants.STATE_PEOPLE_RECORDING);
                showExportPeopleRecord();
                break;
            default:
                break;
        }
    }


    private void doSave(int state) {
        EmiConfig.EXPORT_TYPE = state;
        EmiUtils.saveExportType(state);
        finish();
    }


    private void showExportAll() {
        ivAll.setVisibility(View.VISIBLE);
        ivHasRead.setVisibility(View.INVISIBLE);
        ivNoRead.setVisibility(View.INVISIBLE);
        ivSuccess.setVisibility(View.INVISIBLE);
        ivFailed.setVisibility(View.INVISIBLE);
        ivWarning.setVisibility(View.INVISIBLE);
        ivNormal.setVisibility(View.INVISIBLE);
        ivPeopleRecording.setVisibility(View.INVISIBLE);
    }

    private void showExportHasRead() {
        ivAll.setVisibility(View.INVISIBLE);
        ivHasRead.setVisibility(View.VISIBLE);
        ivNoRead.setVisibility(View.INVISIBLE);
        ivSuccess.setVisibility(View.INVISIBLE);
        ivFailed.setVisibility(View.INVISIBLE);
        ivWarning.setVisibility(View.INVISIBLE);
        ivNormal.setVisibility(View.INVISIBLE);
        ivPeopleRecording.setVisibility(View.INVISIBLE);
    }

    private void showExportNoRead() {
        ivAll.setVisibility(View.INVISIBLE);
        ivHasRead.setVisibility(View.INVISIBLE);
        ivNoRead.setVisibility(View.VISIBLE);
        ivSuccess.setVisibility(View.INVISIBLE);
        ivFailed.setVisibility(View.INVISIBLE);
        ivWarning.setVisibility(View.INVISIBLE);
        ivNormal.setVisibility(View.INVISIBLE);
        ivPeopleRecording.setVisibility(View.INVISIBLE);
    }

    private void showExportSuccess() {
        ivAll.setVisibility(View.INVISIBLE);
        ivHasRead.setVisibility(View.INVISIBLE);
        ivNoRead.setVisibility(View.INVISIBLE);
        ivSuccess.setVisibility(View.VISIBLE);
        ivFailed.setVisibility(View.INVISIBLE);
        ivWarning.setVisibility(View.INVISIBLE);
        ivNormal.setVisibility(View.INVISIBLE);
        ivPeopleRecording.setVisibility(View.INVISIBLE);
    }

    private void showExportFailed() {
        ivAll.setVisibility(View.INVISIBLE);
        ivHasRead.setVisibility(View.INVISIBLE);
        ivNoRead.setVisibility(View.INVISIBLE);
        ivSuccess.setVisibility(View.INVISIBLE);
        ivFailed.setVisibility(View.VISIBLE);
        ivWarning.setVisibility(View.INVISIBLE);
        ivNormal.setVisibility(View.INVISIBLE);
        ivPeopleRecording.setVisibility(View.INVISIBLE);
    }

    private void showExportWarning() {
        ivAll.setVisibility(View.INVISIBLE);
        ivHasRead.setVisibility(View.INVISIBLE);
        ivNoRead.setVisibility(View.INVISIBLE);
        ivSuccess.setVisibility(View.INVISIBLE);
        ivFailed.setVisibility(View.INVISIBLE);
        ivWarning.setVisibility(View.VISIBLE);
        ivNormal.setVisibility(View.INVISIBLE);
        ivPeopleRecording.setVisibility(View.INVISIBLE);
    }

    private void showExportNormal() {
        ivAll.setVisibility(View.INVISIBLE);
        ivHasRead.setVisibility(View.INVISIBLE);
        ivNoRead.setVisibility(View.INVISIBLE);
        ivSuccess.setVisibility(View.INVISIBLE);
        ivFailed.setVisibility(View.INVISIBLE);
        ivWarning.setVisibility(View.INVISIBLE);
        ivNormal.setVisibility(View.VISIBLE);
        ivPeopleRecording.setVisibility(View.INVISIBLE);
    }
    private void showExportPeopleRecord() {
        ivAll.setVisibility(View.INVISIBLE);
        ivHasRead.setVisibility(View.INVISIBLE);
        ivNoRead.setVisibility(View.INVISIBLE);
        ivSuccess.setVisibility(View.INVISIBLE);
        ivFailed.setVisibility(View.INVISIBLE);
        ivWarning.setVisibility(View.INVISIBLE);
        ivNormal.setVisibility(View.INVISIBLE);
        ivPeopleRecording.setVisibility(View.VISIBLE);
    }

    private void initConfig() {
        EmiConfig.EXPORT_TYPE = EmiUtils.getExportType();
        switch (EmiConfig.EXPORT_TYPE) {
            case EmiConstants.STATE_ALL:
                showExportAll();
                break;
            case EmiConstants.STATE_HAS_READ:
                showExportHasRead();
                break;
            case EmiConstants.STATE_NO_READ:
                showExportNoRead();
                break;
            case EmiConstants.STATE_NORMAL:
                showExportNormal();
                break;
            case EmiConstants.STATE_FAILED:
                showExportFailed();
                break;
            case EmiConstants.STATE_WARNING:
                showExportWarning();
                break;
            case EmiConstants.STATE_SUCCESS:
                showExportSuccess();
                break;
            case EmiConstants.STATE_PEOPLE_RECORDING:
                showExportPeopleRecord();
                break;
            default:
                break;
        }
    }
}
