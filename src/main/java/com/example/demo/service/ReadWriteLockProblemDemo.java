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
 * 读写锁问题演示 - 展示不加锁的后果
 * 
 * 【核心问题】
 * 读操作加了读锁，但写操作没加写锁，会发生什么？
 * 
 * 【答案】
 * 1. 写操作可以正常执行（因为没加锁，不会被阻塞）
 * 2. 但会导致数据不一致问题（脏读、不可重复读）
 */
@Service
public class ReadWriteLockProblemDemo {

    private static final Logger logger = LoggerFactory.getLogger(ReadWriteLockProblemDemo.class);

    @Autowired
    private RedissonClient redissonClient;

    // 共享数据
    private volatile int balance = 100;

    /**
     * 【场景1】加了读锁的读操作
     * 读取账户余额，需要2秒
     */
    public void readWithLock(String lockKey) {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwlock:" + lockKey);
        RLock readLock = rwLock.readLock();

        try {
            boolean isLocked = readLock.tryLock(10, 30, TimeUnit.SECONDS);
            if (!isLocked) {
                logger.warn("获取读锁失败");
                return;
            }

            logger.info("【读操作】获取读锁 ✓ 开始读取余额...");

            // 模拟读取耗时2秒（分两次读取，检查是否一致）
            int firstRead = balance;
            logger.info("【读操作】第一次读取: 余额={}", firstRead);

            Thread.sleep(1000); // 等待1秒

            int secondRead = balance;
            logger.info("【读操作】第二次读取: 余额={}", secondRead);

            // 检查两次读取是否一致
            if (firstRead != secondRead) {
                logger.error("【读操作】⚠️ 数据不一致！第一次={}, 第二次={}", firstRead, secondRead);
            } else {
                logger.info("【读操作】✓ 数据一致，余额={}", secondRead);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (readLock.isHeldByCurrentThread()) {
                readLock.unlock();
                logger.info("【读操作】释放读锁");
            }
        }
    }

    /**
     * 【场景2】没加锁的写操作（问题所在！）
     * 修改账户余额
     * 
     * 问题：这个写操作不会被读锁阻塞，可以在读操作进行中修改数据
     */
    public void writeWithoutLock(int newBalance) {
        logger.info("【写操作-无锁】准备修改余额为: {}", newBalance);

        // 直接修改，没有任何锁保护！
        balance = newBalance;

        logger.info("【写操作-无锁】✓ 修改完成，余额={}", balance);
    }

    /**
     * 【场景3】加了写锁的写操作（正确做法）
     * 修改账户余额
     */
    public void writeWithLock(String lockKey, int newBalance) {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwlock:" + lockKey);
        RLock writeLock = rwLock.writeLock();

        try {
            boolean isLocked = writeLock.tryLock(10, 30, TimeUnit.SECONDS);
            if (!isLocked) {
                logger.warn("【写操作-有锁】获取写锁失败");
                return;
            }

            logger.info("【写操作-有锁】获取写锁 ✓ 准备修改余额为: {}", newBalance);

            balance = newBalance;

            logger.info("【写操作-有锁】✓ 修改完成，余额={}", balance);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (writeLock.isHeldByCurrentThread()) {
                writeLock.unlock();
                logger.info("【写操作-有锁】释放写锁");
            }
        }
    }

    /**
     * 【演示1】问题演示：读操作加锁，写操作不加锁
     * 
     * 预期结果：读操作进行中，写操作会修改数据，导致读操作两次读取结果不一致
     */
    public void demonstrateProblem(String lockKey) {
        logger.info("\n========== 问题演示：写操作不加锁 ==========");
        logger.info("初始余额: {}\n", balance);

        // 启动读线程（加了读锁）
        new Thread(() -> {
            readWithLock(lockKey);
        }, "ReadThread").start();

        // 等待500毫秒后启动写线程（不加锁）
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 启动写线程（不加锁！）
        new Thread(() -> {
            writeWithoutLock(200); // 把余额改成200
        }, "WriteThread-NoLock").start();
    }

    /**
     * 【演示2】正确做法：读写都加锁
     * 
     * 预期结果：读操作完成前，写操作会等待，数据保持一致
     */
    public void demonstrateCorrect(String lockKey) {
        // 重置数据
        balance = 100;

        logger.info("\n========== 正确演示：读写都加锁 ==========");
        logger.info("初始余额: {}\n", balance);

        // 启动读线程（加了读锁）
        new Thread(() -> {
            readWithLock(lockKey);
        }, "ReadThread").start();

        // 等待500毫秒后启动写线程（加写锁）
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 启动写线程（加写锁！）
        new Thread(() -> {
            writeWithLock(lockKey, 200); // 把余额改成200
        }, "WriteThread-WithLock").start();
    }

    /**
     * 获取当前余额
     */
    public int getBalance() {
        return balance;
    }

    /**
     * 重置余额
     */
    public void resetBalance() {
        balance = 100;
    }
}
