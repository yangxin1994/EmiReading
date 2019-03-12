package com.emi.emireading.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.view.View;
import android.widget.RelativeLayout;

import com.emi.emireading.R;
import com.emi.emireading.core.BaseActivity;

import java.io.File;

import static com.emi.emireading.core.config.EmiConstants.EMI_ROOT_PATH;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_PATH;
import static com.emi.emireading.core.config.EmiConstants.EXTRA_READING_PATH_FLAG;
import static com.emi.emireading.core.config.EmiConstants.MICRO_MSG;
import static com.emi.emireading.core.config.EmiConstants.QQ_PATH;
import static com.emi.emireading.core.config.EmiConstants.TENCENT;

/**
 * @author :zhoujian
 * @description : 路径选择页面
 * @company :翼迈科技
 * @date 2019年01月25日上午 11:04
 * @Email: 971613168@qq.com
 */

public class PathSelectActivity extends BaseActivity implements View.OnClickListener {
    private Context mContext;
    private String path;
    private RelativeLayout rlQQ;
    private RelativeLayout rlMicroMsg;
    private RelativeLayout rlEmi;
    private String qq = TENCENT + File.separator + QQ_PATH;
    private String microMsg = TENCENT + File.separator + MICRO_MSG;
    private String emi = EMI_ROOT_PATH;
    /**
     * 是否是抄表路径
     */
    private boolean readingPathFlag;


    @Override
    protected int getContentLayout() {
        return R.layout.activity_select_path;
    }

    @Override
    protected void initIntent() {
        mContext = this;
    }

    @Override
    protected void initUI() {
        rlQQ = findViewById(R.id.rlQQ);
        rlMicroMsg = findViewById(R.id.rlMicroMsg);
        rlEmi = findViewById(R.id.rlEmi);
        rlQQ.setOnClickListener(this);
        rlMicroMsg.setOnClickListener(this);
        rlEmi.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        showDir();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlQQ:
                path = Environment.getExternalStorageDirectory() + File.separator + qq;
                readingPathFlag = false;
                skip();
                break;
            case R.id.rlMicroMsg:
                path = Environment.getExternalStorageDirectory() + File.separator + microMsg;
                readingPathFlag = false;
                skip();
                break;
            case R.id.rlEmi:
                path = Environment.getExternalStorageDirectory() + File.separator + emi;
                readingPathFlag = true;
                skip();
                break;
            default:
                break;
        }
    }

    private boolean checkFile(String path) {
        String rootPath = Environment.getExternalStorageDirectory() + File.separator + path;
        File file = new File(rootPath);
        return file.exists();
    }


    private void showDir() {
        setVisibility(rlQQ, checkFile(qq));
        setVisibility(rlMicroMsg, checkFile(microMsg));
        setVisibility(rlEmi, checkFile(emi));
    }


    private void setVisibility(View v, boolean b) {
        if (b) {
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
    }


    private void skip() {
        Intent intent = new Intent();
        intent.setClass(mContext, EmiFileManageActivity.class);
        intent.putExtra(EXTRA_PATH, path);
        intent.putExtra(EXTRA_READING_PATH_FLAG, readingPathFlag);
        startActivity(intent);
    }
}
