package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CountDownLatch 应用实例服务
 * 
 * CountDownLatch 是一个同步辅助类，允许一个或多个线程等待其他线程完成操作。
 * 
 * 主要特点：
 * 1. 初始化时指定计数次数
 * 2. countDown() 方法将计数减 1
 * 3. await() 方法阻塞等待直到计数为 0
 * 4. 计数器不可重用（与 CyclicBarrier 不同）
 */
@Service
public class CountDownLatchService {

    /**
     * 场景 1：等待多个任务完成后汇总结果
     * 模拟从多个数据源并行获取数据，然后合并结果
     */
    public List<String> getDataFromMultipleSources() throws InterruptedException {
        // 假设有 3 个数据源需要并行查询
        int sourceCount = 3;
        CountDownLatch latch = new CountDownLatch(sourceCount);
        
        List<String> results = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(sourceCount);
        
        // 提交 3 个查询任务
        for (int i = 1; i <= sourceCount; i++) {
            final int sourceId = i;
            executor.submit(() -> {
                try {
                    // 模拟从数据源获取数据，耗时不同
                    Thread.sleep((long) (Math.random() * 1000));
                    String data = "数据源-" + sourceId + ": Data-" + System.currentTimeMillis();
                    synchronized (results) {
                        results.add(data);
                        System.out.println(Thread.currentThread().getName() + " 获取到：" + data);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    // 任务完成，计数器减 1
                    latch.countDown();
                }
            });
        }
        
        // 主线程等待所有任务完成
        System.out.println("主线程等待所有数据源返回...");
        latch.await(); // 阻塞直到所有任务完成
        
        System.out.println("所有数据源已返回，共 " + results.size() + " 条数据");
        executor.shutdown();
        
        return results;
    }

    /**
     * 场景 2：同时启动多个任务（发令枪模式）
     * 模拟多个服务同时启动或同时执行某个操作
     */
    public String startMultipleTasksSimultaneously() throws InterruptedException {
        int taskCount = 5;
        CountDownLatch startLatch = new CountDownLatch(1); // 控制同时开始
        CountDownLatch doneLatch = new CountDownLatch(taskCount); // 等待所有完成
        
        AtomicInteger successCount = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(taskCount);
        
        // 准备 5 个任务
        for (int i = 1; i <= taskCount; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    System.out.println("任务-" + taskId + " 准备就绪，等待开始信号");
                    startLatch.await(); // 等待开始信号
                    
                    // 模拟任务执行
                    Thread.sleep((long) (Math.random() * 500));
                    successCount.incrementAndGet();
                    System.out.println("任务-" + taskId + " 完成");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        
        // 给一点准备时间
        Thread.sleep(100);
        System.out.println("=== 所有任务已准备就绪，开始！===");
        startLatch.countDown(); // 发出开始信号，所有任务同时执行
        
        // 等待所有任务完成
        doneLatch.await();
        System.out.println("所有任务执行完毕，成功：" + successCount.get());
        
        executor.shutdown();
        return "5 个任务同时执行完成，成功数：" + successCount.get();
    }
    
    /**
     * 场景 3：带超时的等待
     * 在规定时间内等待任务完成，超时则继续执行
     */
    public String waitForTasksWithTimeout() throws InterruptedException {
        int taskCount = 3;
        CountDownLatch latch = new CountDownLatch(taskCount);
        AtomicInteger completedTasks = new AtomicInteger(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(taskCount);
        
        for (int i = 1; i <= taskCount; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    // 模拟任务执行，有些可能很慢
                    long sleepTime = taskId == 2 ? 3000 : 500; // 任务 2 比较慢
                    Thread.sleep(sleepTime);
                    completedTasks.incrementAndGet();
                    System.out.println("任务-" + taskId + " 完成");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 等待 2 秒，超时则继续
        boolean finished = latch.await(2, java.util.concurrent.TimeUnit.SECONDS);
        
        executor.shutdown();
        
        if (finished) {
            return "所有任务在 2 秒内完成，完成数：" + completedTasks.get();
        } else {
            return "等待超时（2 秒），实际完成数：" + completedTasks.get() + "，仍有任务在执行";
        }
    }

    /**
     * 场景 4：组装高性能测试工具
     * 模拟并发压力测试场景
     */
    public String performLoadTest() throws InterruptedException {
        int threadCount = 10; // 10 个并发线程
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown(); // 报告准备就绪
                    startLatch.await(); // 等待开始信号
                    
                    // 模拟业务处理
                    boolean success = Math.random() > 0.1; // 90% 成功率
                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                    
                    Thread.sleep(100);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        
        // 等待所有线程准备就绪
        readyLatch.await();
        System.out.println("所有测试线程已准备就绪，开始并发测试...");
        
        // 同时开始
        startLatch.countDown();
        
        // 等待所有测试完成
        doneLatch.await();
        
        executor.shutdown();
        
        return String.format("压力测试完成 - 总数：%d, 成功：%d, 失败：%d, 成功率：%.2f%%",
                threadCount, successCount.get(), failCount.get(),
                successCount.get() * 100.0 / threadCount);
    }
}
