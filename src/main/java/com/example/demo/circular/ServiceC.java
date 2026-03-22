package com.example.demo.circular;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * C 服务 - 演示使用@Lazy 注解解决循环依赖
 * 
 * @Lazy 注解原理：
 * 1. Spring 会注入一个代理对象（Proxy）
 * 2. 实际调用时才获取真实的 Bean
 * 3. 这样可以避免循环依赖问题
 */
@Component
public class ServiceC {
    
    private static final Logger log = LoggerFactory.getLogger(ServiceC.class);
    
    /**
     * 使用@Lazy 注解延迟加载 ServiceD
     * 这样即使 ServiceD 也依赖 ServiceC，也不会产生循环依赖问题
     */
    @Autowired
    @Lazy
    private ServiceD serviceD;
    
    public ServiceC() {
        log.info("【ServiceC】实例化完成");
    }
    
    /**
     * 调用 ServiceD 的方法
     */
    public void doSomethingInC() {
        log.info("【ServiceC】doSomethingInC() 被调用");
        if (serviceD != null) {
            log.info("【ServiceC】准备调用 ServiceD 的方法...");
            serviceD.doSomethingInD();
        } else {
            log.warn("【ServiceC】serviceD 为 null!");
        }
    }
    
    /**
     * 获取 ServiceD（用于演示）
     */
    public ServiceD getServiceD() {
        return serviceD;
    }
}
