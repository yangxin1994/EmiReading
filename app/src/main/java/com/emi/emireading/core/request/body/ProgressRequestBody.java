package com.emi.emireading.core.request.body;


import com.emi.emireading.core.request.response.IResponseListener;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;


/**
 * @author :zhoujian
 * @description : 请求进度实体类
 * @company :翼迈科技
 * @date: 2017年07月17日下午 05:30
 * @Email: 971613168@qq.com
 */
public class ProgressRequestBody extends RequestBody {
    /**
     * 回调监听
     */
    private IResponseListener mResponseListener;
    private RequestBody mRequestBody;
    private BufferedSink mBufferedSink;

    public ProgressRequestBody(RequestBody requestBody, IResponseListener responseHandler) {
        this.mResponseListener = responseHandler;
        this.mRequestBody = requestBody;
    }

    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return mRequestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (mBufferedSink == null) {
            mBufferedSink = Okio.buffer(sink(sink));
        }

        //写入
        mRequestBody.writeTo(mBufferedSink);
        //必须调用flush，否则最后一部分数据可能不会被写入
        mBufferedSink.flush();
    }

    /**
     * 写入，回调进度接口
     *
     * @param sink Sink
     * @return Sink
     */
    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            //当前写入字节数
            long bytesWritten = 0L;
            //总字节长度，避免多次调用contentLength()方法
            long contentLength = 0L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    //获得contentLength的值，后续不再调用
                    contentLength = contentLength();
                }
                //增加当前写入的字节数
                bytesWritten += byteCount;
                //回调
                mResponseListener.onProgress(bytesWritten, contentLength);
            }
        };
    }
}
