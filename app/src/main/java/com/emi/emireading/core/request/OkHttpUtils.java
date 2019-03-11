package com.emi.emireading.core.request;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.emi.emireading.core.log.LogUtil;
import com.emi.emireading.core.request.body.ProgressRequestBody;
import com.emi.emireading.core.request.body.ResponseProgressBody;
import com.emi.emireading.core.request.response.BaseJsonResponseHandler;
import com.emi.emireading.core.request.response.BaseResponseHandler;
import com.emi.emireading.core.request.response.DownloadResponseListener;
import com.emi.emireading.core.request.response.IResponseListener;
import com.emi.emireading.core.request.response.RawResponseHandler;
import com.emi.emireading.core.request.response.GsonResponseHandler;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author :zhoujian
 * @description : 请求工具类
 * @company :翼迈科技
 * @date: 2017年07月17日下午 05:30
 * @Email: 971613168@qq.com
 */
public class OkHttpUtils {
    private static OkHttpClient client;

    private static class SingletonHolder {
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static OkHttpUtils instance = new OkHttpUtils();
    }

    private OkHttpUtils() {
    }

    public static OkHttpUtils getInstance() {
        if (client == null) {
            client = new OkHttpClient();
        }
        return SingletonHolder.instance;
    }

    public OkHttpClient getOkHttpClient() {
        return client;
    }

    /**
     * post 请求
     *
     * @param url              url
     * @param params           参数
     * @param responseListener 回调
     */
    public void post(final String url, final Map<String, String> params, final IResponseListener responseListener) {
        post(null, url, params, responseListener);
    }

    /**
     * post 请求
     *
     * @param context          发起请求的context
     * @param url              url
     * @param params           参数
     * @param responseListener 回调
     */
    public void post(Context context, final String url, final Map<String, String> params, final IResponseListener responseListener) {
        //post builder 参数
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        Request request;
        //发起request
        if (context == null) {
            request = new Request.Builder()
                    .url(url)
                    .post(builder.build())
                    .build();
        } else {
            request = new Request.Builder()
                    .url(url)
                    .post(builder.build())
                    .tag(context)
                    .build();
        }
        client.newCall(request).enqueue(new MyCallback(responseListener));
    }

    /**
     * get 请求
     *
     * @param url             url
     * @param params          参数
     * @param responseHandler 回调
     */
    public void get(final String url, final Map<String, String> params, final IResponseListener responseHandler) {
        get(null, url, params, responseHandler);
    }

    /**
     * get 请求
     *
     * @param context         发起请求的context
     * @param url             url
     * @param params          参数
     * @param responseHandler 回调
     */
    public void get(Context context, final String url, final Map<String, String> params, final IResponseListener responseHandler) {
        //拼接url
        String get_url = url;
        if (params != null && params.size() > 0) {
            int i = 0;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (i++ == 0) {
                    get_url = get_url + "?" + entry.getKey() + "=" + entry.getValue();
                } else {
                    get_url = get_url + "&" + entry.getKey() + "=" + entry.getValue();
                }
            }
        }

        Request request;

        //发起request
        if (context == null) {
            request = new Request.Builder()
                    .url(get_url)
                    .build();
        } else {
            request = new Request.Builder()
                    .url(get_url)
                    .tag(context)
                    .build();
        }
        client.newCall(request).enqueue(new MyCallback(new Handler(Looper.getMainLooper()), responseHandler));
    }

    /**
     * 上传文件
     *
     * @param url             url
     * @param files           上传的文件files
     * @param responseHandler 回调
     */
    public void upload(String url, Map<String, File> files, final IResponseListener responseHandler) {
        upload(null, url, null, files, responseHandler);
    }

    /**
     * 上传文件
     *
     * @param url             url
     * @param params          参数
     * @param files           上传的文件files
     * @param responseHandler 回调
     */
    public void upload(String url, Map<String, String> params, Map<String, File> files, final IResponseListener responseHandler) {
        upload(null, url, params, files, responseHandler);
    }

    /**
     * 上传文件
     *
     * @param context         发起请求的context
     * @param url             url
     * @param files           上传的文件files
     * @param responseHandler 回调
     */
    public void upload(Context context, String url, Map<String, File> files, final IResponseListener responseHandler) {
        upload(context, url, null, files, responseHandler);
    }

    /**
     * 上传文件
     *
     * @param context         发起请求的context
     * @param url             url
     * @param params          参数
     * @param files           上传的文件files
     * @param responseHandler 回调
     */
    public void upload(Context context, String url, Map<String, String> params, Map<String, File> files, final IResponseListener responseHandler) {
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        //添加参数
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                multipartBuilder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + key + "\""),
                        RequestBody.create(null, params.get(key)));
            }
        }

        //添加上传文件
        if (files != null && !files.isEmpty()) {
            RequestBody fileBody;
            for (String key : files.keySet()) {
                File file = files.get(key);
                String fileName = file.getName();
                fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), file);
                multipartBuilder.addPart(Headers.of("Content-Disposition",
                        "form-data; name=\"" + key + "\"; filename=\"" + fileName + "\""),
                        fileBody);
            }
        }

        Request request;
        if (context == null) {
            request = new Request.Builder()
                    .url(url)
                    .post(new ProgressRequestBody(multipartBuilder.build(), responseHandler))
                    .build();
        } else {
            request = new Request.Builder()
                    .url(url)
                    .post(new ProgressRequestBody(multipartBuilder.build(), responseHandler))
                    .tag(context)
                    .build();
        }

        client.newCall(request).enqueue(new MyCallback(responseHandler));
    }

    /**
     * 下载文件
     *
     * @param url                     下载地址
     * @param fileDir                 下载目的目录
     * @param filename                下载目的文件名
     * @param downloadResponseHandler 下载回调
     */
    public void download(String url, String fileDir, String filename, final DownloadResponseListener downloadResponseHandler) {
        download(null, url, fileDir, filename, downloadResponseHandler);
    }

    /**
     * 下载文件
     *
     * @param context                 发起请求的context
     * @param url                     下载地址
     * @param fileDir                 下载目的目录
     * @param filename                下载目的文件名
     * @param downloadResponseHandler 下载回调
     */
    public void download(Context context, String url, String fileDir, String filename, final DownloadResponseListener downloadResponseHandler) {
        LogUtil.d("下载的文件路径："+url);
        Request request;
        if (context == null) {
            request = new Request.Builder()
                    .url(url)
                    .build();
        } else {
            request = new Request.Builder()
                    .url(url)
                    .tag(context)
                    .build();
        }

        client.newBuilder()
                //设置拦截器
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());
                        return originalResponse.newBuilder()
                                .body(new ResponseProgressBody(originalResponse.body(), downloadResponseHandler))
                                .build();
                    }
                })
                .build()
                .newCall(request)
                .enqueue(new MyDownloadCallback(downloadResponseHandler, fileDir, filename));
    }

    /**
     * 取消当前context的所有请求
     *
     * @param context
     */
    public void cancel(Context context) {
        if (client != null) {
            for (Call call : client.dispatcher().queuedCalls()) {
                if (call.request().tag().equals(context)) {
                    call.cancel();
                }
            }
            for (Call call : client.dispatcher().runningCalls()) {
                if (call.request().tag().equals(context)) {
                    call.cancel();
                }
            }
        }
    }


    /**
     * 下载回调
     */
    private class MyDownloadCallback implements Callback {
        private Handler mHandler;
        private DownloadResponseListener mDownloadResponseHandler;
        private String mFileDir;
        private String mFilename;

        public MyDownloadCallback(Handler handler, DownloadResponseListener downloadResponseHandler,
                                  String fileDir, String filename) {
            mHandler = handler;
            mDownloadResponseHandler = downloadResponseHandler;
            mFileDir = fileDir;
            mFilename = filename;
        }

        public MyDownloadCallback(DownloadResponseListener downloadResponseHandler,
                                  String fileDir, String filename) {
            mHandler = new Handler(Looper.getMainLooper());
            mDownloadResponseHandler = downloadResponseHandler;
            mFileDir = fileDir;
            mFilename = filename;
        }

        @Override
        public void onFailure(Call call, final IOException e) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDownloadResponseHandler.onError(e.toString());
                }
            });
        }

        @Override
        public void onResponse(Call call, final Response response) throws IOException {
            if (response.isSuccessful()) {
                File file = null;
                try {
                    file = saveFile(response, mFileDir, mFilename);
                } catch (final IOException e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mDownloadResponseHandler.onError("onResponse saveFile fail." + e.toString());
                        }
                    });
                }

                final File newFile = file;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadResponseHandler.onFinish(newFile);
                    }
                });
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadResponseHandler.onError("fail status=" + response.code());
                    }
                });
            }
        }
    }


    private class MyCallback implements Callback {

        private Handler mHandler;
        private IResponseListener mResponseListener;

        public MyCallback(Handler handler, IResponseListener responseListener) {
            mHandler = handler;
            mResponseListener = responseListener;
        }

        public MyCallback(IResponseListener responseListener) {
            mHandler = new Handler(Looper.getMainLooper());
            mResponseListener = responseListener;
        }

        @Override
        public void onFailure(Call call, final IOException e) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mResponseListener.onError(0, e.toString());
                }
            });
        }

        @Override
        public void onResponse(Call call, final Response response) throws IOException {
            if (response.isSuccessful()) {
                final String responseBody = response.body().string();
                //json回调
                if (mResponseListener instanceof BaseJsonResponseHandler) {
                    try {
                        final JSONObject jsonBody = new JSONObject(responseBody);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ((BaseJsonResponseHandler) mResponseListener).onSuccess(response.code(), jsonBody);
                            }
                        });
                    } catch (JSONException e) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mResponseListener.onError(response.code(), "fail parse JsonObject, body=" + responseBody);
                            }
                        });
                    }
                    //Gson回调
                } else if (mResponseListener instanceof GsonResponseHandler) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Gson gson = new Gson();
                                ((GsonResponseHandler) mResponseListener).onSuccess(response.code(),
                                        gson.fromJson(responseBody, ((GsonResponseHandler) mResponseListener).getType()));
                            } catch (Exception e) {
                                mResponseListener.onError(response.code(), "fail parse gson, body=" + responseBody);
                            }

                        }
                    });
                } else if (mResponseListener instanceof RawResponseHandler) {
                    //raw字符串回调
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((RawResponseHandler) mResponseListener).onSuccess(response.code(), responseBody);
                        }
                    });
                } else if (mResponseListener instanceof BaseResponseHandler) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((BaseResponseHandler) mResponseListener).onSuccess(response.code(), responseBody);
                        }
                    });
                }
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mResponseListener.onError(0, "fail status=" + response.code());
                    }
                });
            }
        }
    }

    /**
     * 保存文件
     *
     * @param response
     * @param fileDir
     * @param filename
     * @return
     * @throws IOException
     */
    private File saveFile(Response response, String fileDir, String filename) throws IOException {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len;
        FileOutputStream fos = null;
        try {
            is = response.body().byteStream();
            File dir = new File(fileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, filename);
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
            return file;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
            }
        }
    }


    /**
     * 获取mime type
     *
     * @param path
     * @return
     */
    private String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }


}
