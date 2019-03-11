package com.emi.emireading.core.config;

/**
 * @author :zhoujian
 * @description : URL常量类
 * @company :翼迈科技
 * @date: 2017年08月23日下午 03:25
 * @Email: 971613168@qq.com
 */

public class UrlConstants {
//            public static final String HOST = "http://192.168.0.111:8089/";
    /**
     * 测试服务器IP
     */
    //    public static final String HOST = "http://192.168.0.168:8888/";

    /**
     * 正式服务器IP
     */
    public static final String HOST = "http://220.178.85.242:2030/";
    public static final String URL_SERVICE_HOST = HOST+"newemi/mobile/";
    public static final String CHECK_VERSION = "newemi/mobile/checkVersion";
    //    public static final String URL_SERVICE_HOST = "newemi/mobile/";
    /**
     * 上传水表读数到服务器
     */
    public static final String UPLOAD_METER_DATA = "modifyMeterReading";
    public static final String UPDATE_URL = HOST + "/mobile/app/lastversion.do";
    public static final String UPLOAD_CRASH_URL = HOST + "newemi/mobile/receiveMobileLog";
    public static final String LOGIN = "mobileLogin";
    public static final String SERVICE_SPACE = "http://service.k.cc.com";
    public static final String GET_YM_DATA = "getYmData";
    public static final String WebService_URL = "http://cccb.thiscc.com:8086/webServiceCBJ2/services/CbjService";
    //    public static final String WebService_URL = "http://223.241.224.160:8088/services/CbjService";
    /**
     * 真实环境：http://223.241.224.160:8088/services/CbjService
     */
    /**
     * 测试环境：http://cccb.thiscc.com:8086/webServiceCBJ2/services/CbjService
     */
    public static final String DownLoad_URL = "http://cccb.thiscc.com:8086/webServiceCBJ2/downYm?fileName=";
    //    public static final String DownLoad_URL = "http://223.241.224.160:8088/downYm?fileName=";
    public static final String REQUEST_SUCCESS = "0000";
    public static final String REQUEST_KEY = "code";
    public static final String REQUEST_DATA = "message";

}
