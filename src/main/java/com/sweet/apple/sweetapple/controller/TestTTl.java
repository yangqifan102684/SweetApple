package com.sweet.apple.sweetapple.controller;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.threadpool.TtlExecutors;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 描述
 *
 * @author yangqifan004
 * @date 2021/12/22 17:28
 */
public class TestTTl {

    public static void main(String[] args) throws Exception {
        // 1. threadLocal测试
        // -- 输出结果：线程1null
        //            线程2null
        threadLocalTest();
        System.out.println("============================");
        System.out.println();

        // 2. ITL测试
        // -- 输出结果：子线程1我是主线程
        itlTest();
        // -- 输出结果：子线程1我是主线程
        //            子线程2我是主线程
        // -- 结论：InheritableThreadLocal只会在线程初始化的时候将父线程的值拷贝到子线程（仅拷贝一次）
        itlTestThreadPoolTest();
        System.out.println("============================");
        System.out.println();

        // 3. TTL测试
        // 输出结果：我是线程1：我是主线程
        //         修改主线程
        //         我是线程2：修改主线程
        // -- 结论：TTL能在线程池中传递
        ttlTest();
    }
    // TTL测试
    private static void ttlTest() throws InterruptedException {
        TransmittableThreadLocal<String> local = new TransmittableThreadLocal<>();
        local.set("我是主线程");
        //生成额外的代理
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        //**核心装饰代码！！！！！！！！！**
        executorService = TtlExecutors.getTtlExecutorService(executorService);
        CountDownLatch c1 = new CountDownLatch(1);
        CountDownLatch c2 = new CountDownLatch(1);
        executorService.submit(() -> {
            System.out.println("我是线程1：" + local.get());
            c1.countDown();
        });
        c1.await();
        local.set("修改主线程");
        System.out.println(local.get());
        executorService.submit(() -> {
            System.out.println("我是线程2：" + local.get());
            c2.countDown();
        });
        c2.await();
    }
    // ITL测试
    private static void itlTestThreadPoolTest() {
        ThreadLocal<String> local = new InheritableThreadLocal<>();
        try {
            local.set("我是主线程");
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            CountDownLatch c1 = new CountDownLatch(1);
            CountDownLatch c2 = new CountDownLatch(1);
            //初始化init的时候，赋予了父线程的ThreadLocal的值
            executorService.execute(() -> {
                System.out.println("线程1" + local.get());
                c1.countDown();
            });
            c1.await();
            //主线程修改值
            local.set("修改主线程");
            //再次调用，查看效果
            executorService.execute(() -> {
                System.out.println("线程2" + local.get());
                c2.countDown();
            });
            c2.await();
            executorService.shutdownNow();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //使用完毕，清除线程中ThreadLocalMap中的key。
            local.remove();
        }
    }
    private static void itlTest() throws InterruptedException {
        ThreadLocal<String> local = new InheritableThreadLocal<>();
        local.set("我是主线程");
        new Thread(() -> {
            System.out.println("子线程1" + local.get());
        }).start();
        Thread.sleep(2000);
    }

    // ThreadLocal测试
    private static void threadLocalTest() {
        ThreadLocal<String> local = new ThreadLocal<>();
        try {
            local.set("我是主线程");
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            CountDownLatch c1 = new CountDownLatch(1);
            CountDownLatch c2 = new CountDownLatch(1);
            executorService.execute(() -> {
                System.out.println("线程1" + local.get());
                c1.countDown();
            });
            c1.await();
            executorService.execute(() -> {
                System.out.println("线程2" + local.get());
                c2.countDown();
            });
            c2.await();
            executorService.shutdownNow();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //使用完毕，清除线程中ThreadLocalMap中的key。
            local.remove();
        }
    }

}
