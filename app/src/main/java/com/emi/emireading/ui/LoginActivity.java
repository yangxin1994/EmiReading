package com.emi.emireading.ui;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.emi.emireading.R;
import com.emi.emireading.core.BaseActivity;
import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.request.OkHttpUtils;
import com.emi.emireading.core.request.response.BaseJsonResponseHandler;
import com.emi.emireading.core.request.response.ToastUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.emi.emireading.core.config.UrlConstants.LOGIN;
import static com.emi.emireading.core.config.UrlConstants.URL_SERVICE_HOST;

/**
 * @author :zhoujian
 * @description : 登录
 * @company :翼迈科技
 * @date 2018年09月26日上午 11:24
 * @Email: 971613168@qq.com
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";
    private EditText mEtAccount;
    private EditText mEtPassword;
    private Button mBtnLogin;
    private static final String PARAM_ACCOUNT = "userName";
    private static final String PARAM_PASSWORD = "passWord";
    private static final String SUCCESS_CODE = "1";
    @Override
    protected int getContentLayout() {
        return R.layout.activity_login_act;
    }

    @Override
    protected void initIntent() {

    }

    @Override
    protected void initUI() {
        mEtAccount = findViewById(R.id.etAccount);
        mBtnLogin = findViewById(R.id.btnLogin);
        mEtPassword = findViewById(R.id.etPassword);
        mBtnLogin.setOnClickListener(this);
    }

    @Override
    protected void initData() {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogin:
                if (mEtAccount.getText().toString().isEmpty()) {
                    ToastUtils.showToastFailed("请输入账号");
                    return;
                }
                if (mEtPassword.getText().toString().isEmpty()) {
                    ToastUtils.showToastFailed("请输入密码");
                    return;
                }
                login(mEtAccount.getText().toString(), mEtPassword.getText().toString());
                break;
            default:
                break;
        }
    }

    private void login(String account, String password) {
        String loginUrl = URL_SERVICE_HOST + LOGIN;
        LogUtil.w(TAG, "请求结果:" + loginUrl);
        Map<String, String> params = new HashMap<>(2);
        params.put(PARAM_ACCOUNT, account);
        params.put(PARAM_PASSWORD, password);
        OkHttpUtils.getInstance().post(loginUrl, params, new BaseJsonResponseHandler() {
            @Override
            public void onSuccess(int statusCode, JSONObject response) {
                LogUtil.i(TAG, "请求结果:" + response.toString());
                try {
                    ToastUtils.showToastSuccess(response.getString("msg"));
                }catch (org.json.JSONException e){
                    ToastUtils.showToastFailed(e.toString());
                }
            }

            @Override
            public void onError(int statusCode, String errorMsg) {
                LogUtil.e(TAG, "请求结果:" + errorMsg);
            }
        });

    }


}
