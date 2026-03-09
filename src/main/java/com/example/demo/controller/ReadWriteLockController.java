package com.example.demo.controller;

import com.example.demo.service.ReadWriteLockProblemDemo;
import com.example.demo.service.RedisReadWriteLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis 读写锁演示控制器
 * 
 * 测试步骤：
 * 1. 先调用 /help 查看说明
 * 2. 调用 /concurrent-read 观察读锁共享特性
 * 3. 调用 /mixed-demo 观察读写互斥特性
 */
@RestController
@RequestMapping("/rwlock")
public class ReadWriteLockController {

    @Autowired
    private RedisReadWriteLockService readWriteLockService;

    @Autowired
    private ReadWriteLockProblemDemo problemDemo;

    /**
     * 【接口1】单个读操作
     * 
     * 测试命令：curl "http://localhost:8080/rwlock/read?key=mykey"
     */
    @GetMapping("/read")
    public Map<String, Object> read(@RequestParam(defaultValue = "test-key") String key) {
        Map<String, Object> result = new HashMap<>();
        String data = readWriteLockService.readWithLock(key);
        
        result.put("success", data != null);
        result.put("data", data);
        result.put("operation", "read");
        result.put("key", key);
        
        return result;
    }

    /**
     * 【接口2】单个写操作
     * 
     * 测试命令：curl -X POST "http://localhost:8080/rwlock/write" -d "key=mykey" -d "value=newdata"
     */
    @PostMapping("/write")
    public Map<String, Object> write(
            @RequestParam(defaultValue = "test-key") String key,
            @RequestParam String value) {
        Map<String, Object> result = new HashMap<>();
        boolean success = readWriteLockService.writeWithLock(key, value);
        
        result.put("success", success);
        result.put("operation", "write");
        result.put("key", key);
        result.put("value", value);
        
        return result;
    }

    /**
     * 【接口3】并发读演示 - 展示读锁的共享特性
     * 
     * 测试命令：curl "http://localhost:8080/rwlock/concurrent-read?key=mykey&count=5"
     * 
     * 观察日志：5个读线程应该几乎同时获取锁、同时完成
     */
    @GetMapping("/concurrent-read")
    public Map<String, Object> concurrentRead(
            @RequestParam(defaultValue = "test-key") String key,
            @RequestParam(defaultValue = "5") int count) {
        Map<String, Object> result = new HashMap<>();
        
        readWriteLockService.concurrentReadDemo(key, count);
        
        result.put("success", true);
        result.put("message", "已启动 " + count + " 个并发读线程，请查看控制台日志观察执行过程");
        result.put("key", key);
        result.put("readerCount", count);
        result.put("预期现象", "所有读线程几乎同时获取锁、同时完成（读锁是共享的）");
        
        return result;
    }

    /**
     * 【接口4】读写混合演示 - 展示读写互斥特性
     * 
     * 测试命令：curl "http://localhost:8080/rwlock/mixed-demo?key=mykey"
     * 
     * 观察日志：写线程必须等待读线程完成后才能执行
     */
    @GetMapping("/mixed-demo")
    public Map<String, Object> mixedDemo(
            @RequestParam(defaultValue = "test-key") String key) {
        Map<String, Object> result = new HashMap<>();
        
        readWriteLockService.readWriteMixedDemo(key);
        
        result.put("success", true);
        result.put("message", "已启动读写混合演示，请查看控制台日志观察执行顺序");
        result.put("key", key);
        result.put("预期现象", "写线程必须等待读线程完成后才能执行（读写互斥）");
        
        return result;
    }

    /**
     * 【接口5】获取当前数据
     */
    @GetMapping("/current-data")
    public Map<String, Object> getCurrentData() {
        Map<String, Object> result = new HashMap<>();
        String data = readWriteLockService.getCurrentData();
        
        result.put("success", true);
        result.put("data", data);
        
        return result;
    }

    /**
     * 【接口6】问题演示：读操作加锁，写操作不加锁
     * 
     * 测试命令：curl "http://localhost:8080/rwlock/problem-demo?key=mykey"
     * 
     * 观察日志：读操作两次读取结果不一致（数据被无锁写操作修改了）
     */
    @GetMapping("/problem-demo")
    public Map<String, Object> problemDemo(@RequestParam(defaultValue = "test-key") String key) {
        Map<String, Object> result = new HashMap<>();
        
        problemDemo.demonstrateProblem(key);
        
        result.put("success", true);
        result.put("message", "问题演示已启动，请查看控制台日志");
        result.put("场景", "读操作加了读锁，写操作没加锁");
        result.put("预期问题", "写操作不会被阻塞，导致读操作两次读取结果不一致");
        
        return result;
    }

    /**
     * 【接口7】正确演示：读写都加锁
     * 
     * 测试命令：curl "http://localhost:8080/rwlock/correct-demo?key=mykey"
     * 
     * 观察日志：写操作会等待读操作完成后才执行
     */
    @GetMapping("/correct-demo")
    public Map<String, Object> correctDemo(@RequestParam(defaultValue = "test-key") String key) {
        Map<String, Object> result = new HashMap<>();
        
        problemDemo.demonstrateCorrect(key);
        
        result.put("success", true);
        result.put("message", "正确演示已启动，请查看控制台日志");
        result.put("场景", "读操作加读锁，写操作加写锁");
        result.put("预期结果", "写操作会等待读操作完成后才执行，数据保持一致");
        
        return result;
    }

    /**
     * 【接口8】API 帮助说明
     */
    @GetMapping("/help")
    public Map<String, Object> help() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("说明", "Redis 读写锁演示 API - 基于 Redisson 实现");
        
        result.put("核心概念", Map.of(
            "读锁（共享锁）", "多个线程可以同时获取，适合并发读场景",
            "写锁（排他锁）", "同一时间只有一个线程能获取，保证写操作独占",
            "互斥规则", "读锁和写锁不能同时存在"
        ));
        
        result.put("重要提醒", Map.of(
            "问题", "如果读操作加锁但写操作不加锁，写操作不会被阻塞",
            "后果", "会导致数据不一致（脏读、不可重复读）",
            "解决方案", "读写操作都必须加对应的锁"
        ));
        
        result.put("API列表", Map.of(
            "GET /rwlock/read?key=xxx", "单个读操作（使用读锁）",
            "POST /rwlock/write?key=xxx&value=yyy", "单个写操作（使用写锁）",
            "GET /rwlock/concurrent-read?key=xxx&count=5", "并发读演示（观察读锁共享）",
            "GET /rwlock/mixed-demo?key=xxx", "读写混合演示（观察读写互斥）",
            "GET /rwlock/problem-demo?key=xxx", "问题演示（写操作不加锁的后果）",
            "GET /rwlock/correct-demo?key=xxx", "正确演示（读写都加锁）",
            "GET /rwlock/current-data", "获取当前数据（无锁）",
            "GET /rwlock/help", "查看此帮助"
        ));
        
        result.put("测试建议", Map.of(
            "步骤1", "调用 /problem-demo 观察写操作不加锁的问题",
            "步骤2", "调用 /correct-demo 观察读写都加锁的正确效果",
            "观察", "对比两次演示的日志差异"
        ));
        
        return result;
    }
}
