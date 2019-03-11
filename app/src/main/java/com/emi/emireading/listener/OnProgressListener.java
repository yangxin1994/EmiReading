package com.emi.emireading.listener;

/**
 * @author :zhoujian
 * @description : 进度监听
 * @company :翼迈科技
 * @date 2018年03月02日下午 05:00
 * @Email: 971613168@qq.com
 */

public interface OnProgressListener<T> {

    /**
     * 进度
     *
     * @param progress:进度
     */
    void onProgress(int progress);

    /**
     * 完成
     *
     * @param dataList：数据源
     * @param fileName：对应的文件名
     */
    void onFinish(T dataList, String fileName);


    /**
     * 错误回调
     *
     * @param errorMsg：错误信息
     */
    void onError(String errorMsg);
}
