package com.example.demo.circular;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A 服务 - 演示循环依赖
 * 
 * 循环依赖场景：
 * A 依赖 B，B 依赖 A，形成循环依赖
 * 
 * Spring 解决循环依赖的方式：
 * 1. 使用三级缓存（singletonFactories）
 * 2. 仅支持 setter 注入的循环依赖
 * 3. 构造器注入的循环依赖无法解决，会抛出 BeanCurrentlyInCreationException
 */
@Component
public class ServiceA {
    
    private static final Logger log = LoggerFactory.getLogger(ServiceA.class);
    
    // 使用 @Lazy 注解解决循环依赖的另一种方式
    // @Autowired
    // @Lazy
    // private ServiceB serviceB;

    private ServiceB serviceB;

    public ServiceA() {
        log.info("【ServiceA】实例化完成");
    }
    
    /**
     * Setter 注入 - Spring 可以解决这种循环依赖
     */
    @Autowired
    public void setServiceB(ServiceB serviceB) {
        log.info("【ServiceA】通过 setter 注入 ServiceB: {}", serviceB.getClass().getSimpleName());
        this.serviceB = serviceB;
    }
    
    /**
     * 调用 ServiceB 的方法
     */
    public void doSomethingInA() {
        log.info("【ServiceA】doSomethingInA() 被调用");
        if (serviceB != null) {
            serviceB.doSomethingInB();
        } else {
            log.warn("【ServiceA】serviceB 为 null!");
        }
    }
    
    /**
     * 获取 ServiceB（用于演示）
     */
    public ServiceB getServiceB() {
        return serviceB;
    }
}
