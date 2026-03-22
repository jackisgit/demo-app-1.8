package com.example.demo.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Bean 后置处理器 - 演示 Bean 生命周期的第 6 步和第 10 步
 */
@Component
public class LifecycleDemoBeanPostProcessor implements BeanPostProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(LifecycleDemoBeanPostProcessor.class);
    
    /**
     * 6. 初始化前回调 - 在 Bean 的初始化方法之前执行
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof LifecycleDemoBean) {
            log.info("【6】BeanPostProcessor.postProcessBeforeInitialization() - Bean 名称：{}", beanName);
        }
        return bean;
    }
    
    /**
     * 10. 初始化后回调 - 在 Bean 的初始化方法之后执行
     * 这一步完成后，Bean 就完全就绪了
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof LifecycleDemoBean) {
            log.info("【10】BeanPostProcessor.postProcessAfterInitialization() - Bean 名称：{}", beanName);
        }
        return bean;
    }
}
