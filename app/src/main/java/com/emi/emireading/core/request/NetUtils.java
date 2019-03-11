package com.emi.emireading.core.request;

import android.content.Context;
import android.net.ConnectivityManager;

import com.emi.emireading.EmiReadingApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author :zhoujian
 * @description : 网络监听类
 * @company :翼迈科技
 * @date: 2017年09月27日上午 11:53
 * @Email: 971613168@qq.com
 */

public class NetUtils {
    /**
     * 检测网络是否连接
     *
     * @return
     */
    public static boolean isNetworkAvailable() {
        // 得到网络连接信息
        ConnectivityManager manager = (ConnectivityManager) EmiReadingApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        // 去进行判断网络是否连接
        if (manager.getActiveNetworkInfo() != null) {
            return manager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }


    public static  boolean ping() {
        String result = null;
        try {
            String ip = "www.baidu.com";
            // ping网址3次
            Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            // ping的状态
            int status = p.waitFor();
            if (status == 0) {
                result = "success";
                return true;
            } else {
                result = "failed";
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
        }
        return false;
    }

}
