
package com.emi.emireading.core.crash;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.emi.emireading.R;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.log.imp.CrashWriter;
import com.emi.emireading.core.request.NetUtils;
import com.emi.emireading.core.request.OkHttpUtils;
import com.emi.emireading.core.request.response.BaseJsonResponseHandler;
import com.emi.emireading.core.utils.ToastUtil;

import org.json.JSONObject;

import java.util.HashMap;

import static com.emi.emireading.core.config.EmiConstants.APP_NAME;
import static com.emi.emireading.core.config.UrlConstants.UPLOAD_CRASH_URL;


/**
 * @author :zhoujian
 * @description : 异常页面
 * @company :翼迈科技
 * @date: 2017年07月04日下午 03:41
 * @Email: 971613168@qq.com
 */
public class ErrorActivity extends Activity {
    private static final String TAG = "ErrorActivity";
    private static final String CODE_SUCCESS = "0000";
    private static final String CODE_KEY = "code";
    private static final String PARAMS_VERSION_NAME = "versionName";
    private static final String PARAMS_CRASH_MSG = "crashMsg";
    private static final String PARAMS_DEVICE_NAME = "deviceName";
    private static final String PARAMS_CRASH_DATE = "crashDate";

    public static final String PARAMETER_APP_NAME = "appName";
    public static final String PARAMETER_VERSION_CODE = "versionCode";
    String errorInformation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.default_error_activity);
        errorInformation = CrashManager.getAllErrorDetailsFromIntent(ErrorActivity.this, getIntent());
        LogUtil.e(TAG, "errorInformation:" + errorInformation);
        //查看
        Button btn_check = (Button) findViewById(R.id.btn_check);
        btn_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ErrorActivity.this)
                        .setTitle("错误详情")
                        .setCancelable(true)
                        .setMessage(errorInformation)
                        .setPositiveButton("返回", null)
                        .setNeutralButton("复制", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                copyErrorToClipboard();
                                Toast.makeText(ErrorActivity.this, "已复制到剪切板", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
        });
        //上传
        Button moreInfoButton = (Button) findViewById(R.id.customactivityoncrash_error_activity_more_info_button);
        if (CrashManager.isShowErrorDetailsFromIntent(getIntent())) {
            moreInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //上传信息
                    if (NetUtils.isNetworkAvailable()) {
                        uploadErrorMessage();
                    } else {
                        ToastUtil.showShortToast("网络未连接");
                    }
                }
            });
        } else {
            moreInfoButton.setVisibility(View.GONE);
        }

        int defaultErrorActivityDrawableId = CrashManager.getDefaultErrorActivityDrawableIdFromIntent(getIntent());
        ImageView errorImageView = ((ImageView) findViewById(R.id.customactivityoncrash_error_activity_image));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            errorImageView.setImageDrawable(getResources().getDrawable(defaultErrorActivityDrawableId, getTheme()));
        } else {
            errorImageView.setImageDrawable(getResources().getDrawable(defaultErrorActivityDrawableId));
        }
        CrashWriter crashWriter = new CrashWriter(this);
        //保存崩溃日志到本地
        crashWriter.saveCrashLog(errorInformation);
        if (NetUtils.isNetworkAvailable()) {
            requestServer(null);
        }
    }


    /**
     * 复制到剪切板
     */
    private void copyErrorToClipboard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getString(R.string.customactivityoncrash_error_activity_error_details_clipboard_label), errorInformation);
            clipboard.setPrimaryClip(clip);
        } else {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setText(errorInformation);
        }
    }


    /**
     * 上传错误信息
     */
    void uploadErrorMessage() {
        final ProgressDialog md = new ProgressDialog(this);
        md.setMessage("正在上传，请稍后...");
        md.setCancelable(false);
        md.show();
        LogUtil.i(TAG, "错误详情" + errorInformation);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestServer(md);
            }
        }, 1000);
    }


    private void requestServer(final ProgressDialog dialog) {
        CrashInfo crashInfo = CrashManager.getCrashInfo(ErrorActivity.this, getIntent());
        HashMap<String, String> params = new HashMap<>(5);
        LogUtil.e(TAG, "崩溃信息：" + crashInfo.getCrashMsg());
        LogUtil.e(TAG, "崩溃日期：" + crashInfo.getCrashDate());
        LogUtil.e(TAG, "崩溃版本号：" + crashInfo.getVersionName());
        LogUtil.e(TAG, "设备名称：" + crashInfo.getDeviceName());
        params.put(PARAMETER_APP_NAME, APP_NAME);
        params.put(PARAMS_DEVICE_NAME, crashInfo.getDeviceName());
        params.put(PARAMS_CRASH_DATE, crashInfo.getCrashDate());
        params.put(PARAMS_VERSION_NAME, crashInfo.getVersionName());
        params.put(PARAMS_CRASH_MSG, crashInfo.getCrashMsg());
        OkHttpUtils.getInstance().post(ErrorActivity.this, UPLOAD_CRASH_URL, params, new BaseJsonResponseHandler() {
            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                closeDialog(dialog);
            }

            @Override
            public void onError(int statusCode, String errorMsg) {
                LogUtil.e(TAG, "onError：" + errorMsg);
                closeDialog(dialog);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(ErrorActivity.this)
                    .setTitle("提示")
                    .setCancelable(true)
                    .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNeutralButton("重新启动", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final Class<? extends Activity> restartActivityClass = CrashManager.getRestartActivityClassFromIntent(getIntent());
                            if (restartActivityClass != null) {
                                Intent intent = new Intent(ErrorActivity.this, restartActivityClass);
                                CrashManager.restartApplicationWithIntent(ErrorActivity.this, intent);
                            } else {
                                CrashManager.closeApplication(ErrorActivity.this);
                            }
                        }
                    })
                    .show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void closeDialog(ProgressDialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


}
