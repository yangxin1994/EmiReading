package com.emi.emireading.core.request.body;


import com.emi.emireading.core.request.response.DownloadResponseListener;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * @author :zhoujian
 * @description :重写responsebody 设置下载进度监听
 * @company :翼迈科技
 * @date: 2017年8月6日下午 04:08
 * @Email: 971613168@qq.com
 */
public class ResponseProgressBody extends ResponseBody {
    private ResponseBody mResponseBody;
    private DownloadResponseListener mDownloadResponseListener;
    private BufferedSource bufferedSource;

    public ResponseProgressBody(ResponseBody responseBody, DownloadResponseListener downloadResponseHandler) {
        this.mResponseBody = responseBody;
        this.mDownloadResponseListener = downloadResponseHandler;
    }

    @Override
    public MediaType contentType() {
        return mResponseBody.contentType();
    }

    @Override
    public long contentLength() {
        return mResponseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(mResponseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                totalBytesRead += ((bytesRead != -1) ? bytesRead : 0);
                if (mDownloadResponseListener != null) {
                    mDownloadResponseListener.onProgress(totalBytesRead, mResponseBody.contentLength());
                }
                return bytesRead;
            }
        };
    }
}
