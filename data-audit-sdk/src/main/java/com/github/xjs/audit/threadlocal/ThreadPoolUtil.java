/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.audit.threadlocal;

import com.alibaba.ttl.threadpool.TtlExecutors;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * transmittable-thread-local线程池<br/>
 * 如果应用中需要并行执行任务，可以引用这个ThreadPool来传递ThreadLocal数据。
 * @author 605162215@qq.com
 * @date 2019/12/5 9:17
 **/
public class ThreadPoolUtil {

    /**线程池*/
    private final ExecutorService executorService;

    private static ThreadPoolUtil instance = new ThreadPoolUtil();

    private ThreadPoolUtil() {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.executorService = TtlExecutors.getTtlExecutorService(executorService);
    }


    public static ThreadPoolUtil getInstance() {
        return instance;
    }

    public static <T> Future<T> execute(final Callable<T> runnable) {
        return getInstance().executorService.submit(runnable);
    }

    public static Future<?> execute(final Runnable runnable) {
        return getInstance().executorService.submit(runnable);
    }

}
