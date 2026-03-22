package com.example.demo.circular;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * D 服务 - 与 ServiceC 形成循环依赖（使用@Lazy 解决）
 */
@Component
public class ServiceD {
    
    private static final Logger log = LoggerFactory.getLogger(ServiceD.class);
    
    @Autowired
    private ServiceC serviceC;
    
    public ServiceD() {
        log.info("【ServiceD】实例化完成");
    }
    
    /**
     * Setter 注入
     */
    @Autowired
    public void setServiceC(ServiceC serviceC) {
        log.info("【ServiceD】通过 setter 注入 ServiceC: {}", serviceC.getClass().getSimpleName());
        this.serviceC = serviceC;
    }
    
    /**
     * 调用 ServiceC 的方法
     */
    public void doSomethingInD() {
        log.info("【ServiceD】doSomethingInD() 被调用");
        if (serviceC != null) {
            log.info("【ServiceD】准备调用 ServiceC 的方法...");
            serviceC.doSomethingInC();
        } else {
            log.warn("【ServiceD】serviceC 为 null!");
        }
    }
    
    /**
     * 获取 ServiceC（用于演示）
     */
    public ServiceC getServiceC() {
        return serviceC;
    }
}
