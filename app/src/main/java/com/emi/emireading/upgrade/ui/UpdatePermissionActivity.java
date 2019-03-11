package com.emi.emireading.upgrade.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.emi.emireading.R;
import com.emi.emireading.upgrade.event.CommonEvent;
import com.emi.emireading.upgrade.event.UpdateEventType;

import org.greenrobot.eventbus.EventBus;

/**
 * @author :zhoujian
 * @description : 权限相关
 * @company :翼迈科技
 * @date :2018/8/19
 * @Email: 971613168@qq.com
 */
public class UpdatePermissionActivity extends BaseUpdateActivity {
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0x123;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            sendBroadcast(true);
        }
    }

    @Override
    public void showDefaultDialog() {

    }

    @Override
    public void showCustomDialog() {

    }


    @SuppressWarnings("unchecked")
    private void sendBroadcast(boolean result) {
        //post event
        CommonEvent commonEvent = new CommonEvent();
        commonEvent.setEventType(UpdateEventType.EVENT_ACTION_REQUEST_PERMISSION);
        commonEvent.setSuccessful(true);
        commonEvent.setData(result);
        EventBus.getDefault().post(commonEvent);
        finish();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    sendBroadcast(true);
                } else {
                    Toast.makeText(this, getString(R.string.version_check_write_permission_deny), Toast.LENGTH_LONG).show();
                    sendBroadcast(false);
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            default:
                break;
        }
    }
}
