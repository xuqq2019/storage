package com.xqq.oss.core.model;

import com.xqq.oss.core.exception.ExecutorException;
import com.xqq.oss.core.warn.ExceptionStatus;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定制属于自己的阻塞线程池
 * @author Zach
 * @date 2020-10-29
 * @version v1.0
 */
public class CustomUnblockThreadPoolExecutor {
    private ThreadPoolExecutor pool = null;

    /**
     * 线程池初始化方法
     *
     * corePoolSize 核心线程池大小----1
     * maximumPoolSize 最大线程池大小----3
     * keepAliveTime 线程池中超过corePoolSize数目的空闲线程最大存活时间----30+单位TimeUnit
     * TimeUnit keepAliveTime时间单位----TimeUnit.MINUTES
     * workQueue 阻塞队列----new ArrayBlockingQueue<Runnable>(5)==== 5容量的阻塞队列
     * threadFactory 新建线程工厂----new CustomThreadFactory()====定制的线程工厂
     * rejectedExecutionHandler 当提交任务数超过maxmumPoolSize+workQueue之和时,
     * 							即当提交第9个任务时(前面线程都没有执行完,此测试方法中用sleep(100)),
     * 						          任务会交给RejectedExecutionHandler来处理
     */

    public void init(int corePoolSize, int maximumPoolSize, long keepAliveTime,int workQueueCapacity) {
        pool = new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,
                TimeUnit.MINUTES, new ArrayBlockingQueue<>(workQueueCapacity), new CustomThreadFactory(), new CustomRejectedExecutionHandler());
    }

    public void destory() {
        if(pool !=null) {
            pool.shutdownNow();
        }
    }

    public ExecutorService getCustomThreadPoolExecutor() {
        return this.pool;
    }


    private static class CustomRejectedExecutionHandler implements  RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            //核心改造点,由blockingqueue的offer改成put阻塞方法
            try {
                System.out.println("新增队列线程");
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                throw new ExecutorException(ExceptionStatus.QUEUE_PUT_EXCEPTION);
            }
        }
    }

    private static class CustomThreadFactory implements ThreadFactory {

        private final AtomicInteger count = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable runnable) {
            Thread t = new Thread(runnable);
            String threadName =  CustomUnblockThreadPoolExecutor.class.getSimpleName()+count.addAndGet(1);
            System.out.println(threadName);
            t.setName(threadName);
            return t;
        }
    }
}