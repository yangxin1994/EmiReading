package com.emi.emireading.upgrade.callback;


import com.emi.emireading.upgrade.entity.AppVersionInfo;

/**
 * @author :zhoujian
 * @description : 请求版本信息监听
 * @company :翼迈科技
 * @date 2018年08月17日下午 02:55
 * @Email: 971613168@qq.com
 */

public interface RequestVersionListener {
    /**
     * 版本信息请求成功
     *
     * @param result:结果
     *  @return AppVersionInfo
     */
    AppVersionInfo onRequestVersionSuccess(String result);

    /**
     * 版本信息请求失败
     *
     * @param msg :失败信息
     */
    void onRequestVersionFailed(String msg);

}
