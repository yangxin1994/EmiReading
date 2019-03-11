package com.emi.emireading.core.request.response;


import com.google.gson.internal.$Gson$Types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


/**
 * @author :zhoujian
 * @description : Gson类型的回调接口
 * @company :翼迈科技
 * @date: 2017年8月6日下午 04:02
 * @Email: 971613168@qq.com
 */
public abstract class GsonResponseHandler<T> implements IResponseListener {
    Type mType;
    public GsonResponseHandler() {
        //反射获取带泛型的class
        Type myclass = getClass().getGenericSuperclass();
        if (myclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        //获取所有泛型
        ParameterizedType parameter = (ParameterizedType) myclass;
        //将泛型转为type
        mType = $Gson$Types.canonicalize(parameter.getActualTypeArguments()[0]);
    }

    public final Type getType() {
        return mType;
    }

    public abstract void onSuccess(int statusCode, T response);

    @Override
    public void onProgress(long currentBytes, long totalBytes) {

    }
}
