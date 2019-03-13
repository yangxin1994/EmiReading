package com.emi.emireading.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.emi.emireading.R;
import com.emi.emireading.core.config.UrlConstants;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.request.NetUtils;
import com.emi.emireading.core.request.response.ToastUtils;
import com.emi.emireading.core.utils.AppUtils;
import com.emi.emireading.core.utils.ToastUtil;
import com.emi.emireading.upgrade.callback.CustomVersionDialogListener;
import com.emi.emireading.upgrade.callback.ForceUpdateListener;
import com.emi.emireading.upgrade.callback.OnCancelListener;
import com.emi.emireading.upgrade.callback.RequestVersionListener;
import com.emi.emireading.upgrade.core.BaseDialog;
import com.emi.emireading.upgrade.core.HttpRequestMethod;
import com.emi.emireading.upgrade.core.RequestParams;
import com.emi.emireading.upgrade.core.VersionChecker;
import com.emi.emireading.upgrade.entity.AppVersionInfo;
import com.emi.emireading.upgrade.helper.DownloadHelper;
import com.emi.emireading.widget.view.dialog.multidialog.EmiMultipleProgressDialog;

import org.json.JSONException;
import org.json.JSONObject;

import static com.emi.emireading.core.config.EmiConstants.APP_NAME;
import static com.emi.emireading.core.config.UrlConstants.CHECK_VERSION;
import static com.emi.emireading.core.crash.ErrorActivity.PARAMETER_APP_NAME;
import static com.emi.emireading.core.crash.ErrorActivity.PARAMETER_VERSION_CODE;

/**
 * @author chx
 * @modify by zhoujian
 * 2018年09月20日上午 10:34
 */
public class AboutActivity extends AppCompatActivity {
    private Button downButton;
    private ImageView iv_goback;
    private Context context;
    private TextView tvCurrentVersion;
    private static final String TAG = "AboutActivity";
    private DownloadHelper downloadHelper;
    private static final String EXTRA_FORCE_UPGRADE = "0";
    private EmiMultipleProgressDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        context = this;
        initView();
        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetUtils.isNetworkAvailable()) {
                    showDialog("正在检查更新...");
                    sendVersionRequest();
                } else {
                    ToastUtils.showToastNormal("请检查网络");
                }
            }
        });
    }


    private void showDialog(String text) {
        closeDialog();
        dialog = EmiMultipleProgressDialog.create(this)
                .setLabel(text)
                .setCancellable(false)
                .show();
    }

    private void closeDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void initView() {
        downButton = (Button) findViewById(R.id.btn_downVer);
        iv_goback = (ImageView) findViewById(R.id.iv_goback);
        tvCurrentVersion = (TextView) findViewById(R.id.tvCurrentVersion);
        PackageManager manager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = manager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String versionName = "当前版本：未知";
        if (packageInfo != null) {
            versionName = packageInfo.versionName;
        }
        versionName = "当前版本：" + versionName;
        tvCurrentVersion.setText(versionName);
        iv_goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void sendVersionRequest() {
        RequestParams requestParams = new RequestParams();
        String url = UrlConstants.HOST + CHECK_VERSION;
        requestParams.put(PARAMETER_VERSION_CODE, AppUtils.getVersionCode(getApplicationContext()) + "");
        requestParams.put(PARAMETER_APP_NAME, APP_NAME);
        downloadHelper = VersionChecker
                .getInstance()
                .requestVersion()
                .setRequestMethod(HttpRequestMethod.POST)
                .setRequestParams(requestParams)
                .setRequestUrl(url)
                .request(new RequestVersionListener() {
                    @Nullable
                    @Override
                    public AppVersionInfo onRequestVersionSuccess(String result) {
                        closeDialog();
                        try {
                            LogUtil.d("解析的内容：" + result);
                            JSONObject jsonObject = new JSONObject(result);
                            boolean flag = jsonObject.getBoolean("status");
                            String apkPath = jsonObject.getString("apkPath");
                            String apkTitle = jsonObject.getString("versionTitle");
                            String forceUpgradeMsg = jsonObject.getString("ismustUpgrade");
                            boolean forceUpgrade;
                            if (EXTRA_FORCE_UPGRADE.equals(forceUpgradeMsg)) {
                                forceUpgrade = true;
                                downloadHelper.setForceUpdateListener(new ForceUpdateListener() {
                                    @Override
                                    public void onShouldForceUpdate() {

                                    }
                                });
                            } else {
                                forceUpgrade = false;
                            }
                            String apkContent = jsonObject.getString("upgradeDesc");
                            if (flag) {
                                return crateAppInfo(apkTitle, apkContent, apkPath, forceUpgrade);
                            } else {
                                LogUtil.e(TAG, "onRequestVersionFailed");
                                return null;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            LogUtil.e(TAG, "onRequestVersionFailed=" + e.toString());
                            ToastUtils.showToastNormal("服务器数据异常");
                            closeDialog();
                            return null;
                        }
                    }

                    @Override
                    public void onRequestVersionFailed(String message) {
                        LogUtil.e(TAG, "onRequestVersionFailed=" + message);
                        ToastUtils.showToastNormal("服务器请求失败");
                        closeDialog();
                    }
                });
        downloadHelper.setSilentDownload(false);
        downloadHelper.setShowDownloadingDialog(true);
        downloadHelper.setShowNotification(true);
        downloadHelper.setDirectDownload(true);
        downloadHelper.setShowNotification(true);
        downloadHelper.setDirectDownload(false);
        downloadHelper.setShowDownloadingDialog(true)
                .setCustomVersionDialogListener(createCustomDialogOne())
                .setForceReDownload(false)
                .setShowDownloadFailDialog(true)
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        ToastUtil.showShortToast("您取消了操作");
                    }
                });
        downloadHelper.executeTask(this);
    }

    /**
     * @return
     * @important 使用请求版本功能，可以在这里设置downloadUrl
     * 这里可以构造UI需要显示的数据
     * UIData 内部是一个Bundle
     */
    private AppVersionInfo crateAppInfo(String title, String content, String apkUrl, boolean flag) {
        AppVersionInfo uiData = AppVersionInfo.create();
        uiData.setTitle(title);
        uiData.setDownloadUrl(apkUrl);
        uiData.setContent(content);
        uiData.setForceUpdate(flag);
        return uiData;
    }


    private CustomVersionDialogListener createCustomDialogOne() {
        return new CustomVersionDialogListener() {
            @Override
            public Dialog getCustomVersionDialog(Context context, AppVersionInfo appVersionInfo) {
                BaseDialog baseDialog = new BaseDialog(context, R.style.BaseDialog, R.layout.custom_dialog_two_layout);
                Button btnCancel = baseDialog.findViewById(R.id.version_check__failed_dialog_cancel);
                if (appVersionInfo.isForceUpdate()) {
                    btnCancel.setVisibility(View.GONE);
                } else {
                    btnCancel.setVisibility(View.VISIBLE);
                }
                TextView tvTitle = baseDialog.findViewById(R.id.tv_title);
                TextView textView = baseDialog.findViewById(R.id.tv_msg);
                tvTitle.setText(appVersionInfo.getTitle());
                textView.setText(appVersionInfo.getContent());
                return baseDialog;
            }
        };
    }
}
