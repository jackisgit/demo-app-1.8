package com.example.demo.config;

import com.example.demo.bean.LifecycleDemoBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bean 生命周期演示配置 - 使用@Bean 注解定义带自定义初始化和销毁方法的 Bean
 */
@Configuration
public class LifecycleDemoConfig {
    
    private static final Logger log = LoggerFactory.getLogger(LifecycleDemoConfig.class);
    
    /**
     * 使用@Bean 注解创建 LifecycleDemoBean，并指定 init-method 和 destroy-method
     * 
     * @return LifecycleDemoBean 实例
     */
    @Bean(initMethod = "customInitMethod", destroyMethod = "customDestroyMethod")
    public LifecycleDemoBean lifecycleDemoBean() {
        log.info("【@Bean】通过@Bean 注解创建 LifecycleDemoBean");
        return new LifecycleDemoBean();
    }
}
