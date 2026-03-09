package com.example.demo.listener;

import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 应用启动监听器 - 初始化布隆过滤器
 */
@Component
public class BloomFilterInitListener {
    
    @Autowired
    private UserService userService;
    
    /**
     * 应用启动完成后初始化布隆过滤器
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        // 异步初始化布隆过滤器，避免阻塞启动
        new Thread(() -> {
            try {
                Thread.sleep(2000); // 等待数据库连接完全建立
                userService.initBloomFilter();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("【布隆过滤器】初始化失败：" + e.getMessage());
            }
        }).start();
    }
}
