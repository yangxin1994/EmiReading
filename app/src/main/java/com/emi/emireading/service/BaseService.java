package com.emi.emireading.service;

import android.app.Service;
import android.os.Binder;

import com.emi.emireading.core.log.LogUtil;

/**
 * @author :zhoujian
 * @description : BaseService
 * @company :翼迈科技
 * @date 2018年01月11日上午 10:16
 * @Email: 971613168@qq.com
 */

public  abstract class BaseService  extends Service {
    protected String tag;
    public class ServiceBinder extends Binder {
        private BaseService service;

        protected ServiceBinder(BaseService service) {
            this.service = service;
        }

        public BaseService getService() {
            return service;
        }
    }

    @Override
    public void onCreate() {
        tag = getClass().getSimpleName();
        super.onCreate();
        LogUtil.d(tag,tag+"--->onCreate--->");
        initOnCreate();
    }

    /**
     * 初始化onCreate
     */
    protected abstract void initOnCreate();


}
