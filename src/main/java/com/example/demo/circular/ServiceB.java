package com.example.demo.circular;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * B 服务 - 与 ServiceA 形成循环依赖
 */
@Component
public class ServiceB {
    
    private static final Logger log = LoggerFactory.getLogger(ServiceB.class);
    
    private ServiceA serviceA;
    
    public ServiceB() {
        log.info("【ServiceB】实例化完成");
    }
    
    /**
     * Setter 注入 - Spring 可以解决这种循环依赖
     */
    @Autowired
    public void setServiceA(ServiceA serviceA) {
        log.info("【ServiceB】通过 setter 注入 ServiceA: {}", serviceA.getClass().getSimpleName());
        this.serviceA = serviceA;
    }
    
    /**
     * 调用 ServiceA 的方法
     */
    public void doSomethingInB() {
        log.info("【ServiceB】doSomethingInB() 被调用");
        if (serviceA != null) {
            serviceA.doSomethingInA();
        } else {
            log.warn("【ServiceB】serviceA 为 null!");
        }
    }
    
    /**
     * 获取 ServiceA（用于演示）
     */
    public ServiceA getServiceA() {
        return serviceA;
    }
}
