package com.example.demo.service;

import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis 读写锁服务 - 使用 Redisson 实现
 * 
 * 【核心概念】
 * 1. 读锁（共享锁）：多个线程可以同时获取，适合读多写少的场景
 * 2. 写锁（排他锁）：同一时间只有一个线程能获取，其他线程（读/写）都要等待
 * 3. 互斥规则：读锁和写锁互相排斥，不能同时存在
 * 
 * 【使用场景】
 * - 缓存读取：多个线程同时读缓存
 * - 数据更新：写数据时禁止其他线程读写，保证数据一致性
 */
@Service
public class RedisReadWriteLockService {

    private static final Logger logger = LoggerFactory.getLogger(RedisReadWriteLockService.class);

    @Autowired
    private RedissonClient redissonClient;

    // 模拟共享数据（实际项目中可能是数据库或缓存）
    private volatile String sharedData = "初始数据";

    /**
     * 【读操作示例】使用读锁
     * 
     * 执行流程：
     * 1. 从 Redisson 获取读写锁对象
     * 2. 获取其中的读锁
     * 3. 尝试加锁（最多等待10秒，锁持有30秒自动释放）
     * 4. 执行业务逻辑（读取数据）
     * 5.  finally 中释放锁
     * 
     * 特点：多个读线程可以同时获取读锁，并发执行
     */
    public String readWithLock(String lockKey) {
        // 步骤1：获取读写锁对象（Redis 中的 key 为 "rwlock:xxx"）
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwlock:" + lockKey);
        
        // 步骤2：获取读锁
        RLock readLock = rwLock.readLock();
        
        try {
            // 步骤3：尝试获取锁
            // 参数1：等待时间（10秒内没获取到就放弃）
            // 参数2：锁的过期时间（30秒后自动释放，防止死锁）
            boolean isLocked = readLock.tryLock(10, 30, TimeUnit.SECONDS);
            
            if (!isLocked) {
                logger.warn("【读锁】获取失败，key={}", lockKey);
                return null;
            }
            
            logger.info("【读锁】获取成功 ✓ key={}, 线程={}", lockKey, Thread.currentThread().getName());
            
            // 步骤4：执行业务逻辑（模拟读取耗时1秒）
            Thread.sleep(1000);
            String result = sharedData;
            
            logger.info("【读锁】读取完成，数据=[{}]", result);
            return result;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("【读锁】操作被中断", e);
            return null;
            
        } finally {
            // 步骤5：释放锁（必须放在 finally 中确保一定会执行）
            // 检查是否是当前线程持有锁，避免误释放其他线程的锁
            if (readLock.isHeldByCurrentThread()) {
                readLock.unlock();
                logger.info("【读锁】释放 ✓ key={}", lockKey);
            }
        }
    }

    /**
     * 【写操作示例】使用写锁
     * 
     * 执行流程与读锁相同，但锁的特性不同：
     * - 写锁是排他的，同一时间只有一个线程能获取
     * - 当有读锁被持有时，写锁会等待
     * - 当有写锁被持有时，其他读写操作都会等待
     */
    public boolean writeWithLock(String lockKey, String newData) {
        // 步骤1：获取读写锁对象
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwlock:" + lockKey);
        
        // 步骤2：获取写锁（注意这里是 writeLock）
        RLock writeLock = rwLock.writeLock();
        
        try {
            // 步骤3：尝试获取锁
            boolean isLocked = writeLock.tryLock(10, 30, TimeUnit.SECONDS);
            
            if (!isLocked) {
                logger.warn("【写锁】获取失败，key={}", lockKey);
                return false;
            }
            
            logger.info("【写锁】获取成功 ✓ key={}, 线程={}", lockKey, Thread.currentThread().getName());
            
            // 步骤4：执行业务逻辑（模拟写入耗时2秒）
            Thread.sleep(2000);
            sharedData = newData;
            
            logger.info("【写锁】写入完成，新数据=[{}]", newData);
            return true;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("【写锁】操作被中断", e);
            return false;
            
        } finally {
            // 步骤5：释放锁
            if (writeLock.isHeldByCurrentThread()) {
                writeLock.unlock();
                logger.info("【写锁】释放 ✓ key={}", lockKey);
            }
        }
    }

    // ==================== 演示方法 ====================

    /**
     * 【演示1】多个读线程并发执行
     * 
     * 预期结果：
     * - 所有读线程几乎同时获取读锁
     * - 所有读线程几乎同时完成（因为读锁是共享的）
     */
    public void concurrentReadDemo(String lockKey, int readerCount) {
        logger.info("\n========== 并发读演示（{}个线程）==========", readerCount);
        logger.info("【预期】所有读线程应该几乎同时获取锁、同时完成\n");
        
        for (int i = 0; i < readerCount; i++) {
            final int index = i;
            new Thread(() -> {
                logger.info("读线程-{} 启动", index);
                String data = readWithLock(lockKey);
                logger.info("读线程-{} 结束，读到=[{}]", index, data);
            }, "Reader-" + i).start();
        }
    }

    /**
     * 【演示2】读写混合 - 展示互斥特性
     * 
     * 执行顺序：
     * 1. 读线程先启动，获取读锁
     * 2. 写线程启动，尝试获取写锁（被阻塞，等待读锁释放）
     * 3. 读线程完成，释放读锁
     * 4. 写线程获取写锁，开始执行
     * 5. 写线程完成，释放写锁
     * 
     * 预期结果：写线程必须等待读线程完成后才能执行
     */
    public void readWriteMixedDemo(String lockKey) {
        logger.info("\n========== 读写混合演示 ==========");
        logger.info("【预期】写线程必须等待读线程完成后才能执行\n");
        
        // 先启动读线程
        new Thread(() -> {
            logger.info("【读线程】启动");
            String data = readWithLock(lockKey);
            logger.info("【读线程】结束，读到=[{}]", data);
        }, "Mixed-Reader").start();
        
        // 延迟100毫秒后启动写线程
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        new Thread(() -> {
            logger.info("【写线程】启动（应该被阻塞，等待读线程）");
            boolean success = writeWithLock(lockKey, "新数据-" + System.currentTimeMillis());
            logger.info("【写线程】结束，结果={}", success);
        }, "Mixed-Writer").start();
    }

    /**
     * 获取当前数据（无锁，仅用于查看）
     */
    public String getCurrentData() {
        return sharedData;
    }
}
