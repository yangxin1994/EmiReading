package com.emi.emireading.core.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author :zhoujian
 * @description : 线程池管理类
 * @company :翼迈科技
 * @date: 2018年3月25日下午 01:38
 * @Email: 971613168@qq.com
 */

public class ThreadPoolManager {
    /**
     * 线程池中最少的线程的数量
     */
    private static final int CORE_POOL_SIZE = 10;

    /**
     * 线程池中最多的线程的数量
     */
    private static final int MAXIMUM_POOL_SIZE = 15;
    /**
     * 空闲线程的最长存活时间
     */
    private static final int KEEP_ALIVE = 10;
    /**
     * 工作队列-阻塞队列
     */
    private static final BlockingQueue<Runnable> WORK_QUEUE = new LinkedBlockingQueue<>(MAXIMUM_POOL_SIZE);
    /**
     * 线程池中线程的创建工厂
     */
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        /**
         *  原子操作级别的整数，初始值是1
         */
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            //线程名，前缀固定，后缀自增长
            Thread thread = new Thread(r, "Task #" + mCount.getAndIncrement());
            //正常优先级
            thread.setPriority(Thread.NORM_PRIORITY);
            return thread;
        }
    };

    /**
     * 线程池中线程的执行的管理器
     */
    public static final ExecutorService EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            KEEP_ALIVE, TimeUnit.SECONDS,
            WORK_QUEUE, THREAD_FACTORY,
            new ThreadPoolExecutor.DiscardPolicy());
}
