package com.example.demo.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 演示 Spring Bean 生命周期的示例 Bean
 * 
 * Spring Bean 生命周期完整流程：
 * 1. 实例化 Bean（构造函数）
 * 2. 注入依赖（@Autowired）
 * 3. BeanNameAware.setBeanName()
 * 4. BeanFactoryAware.setBeanFactory()
 * 5. ApplicationContextAware.setApplicationContext()
 * 6. BeanPostProcessor.postProcessBeforeInitialization()
 * 7. @PostConstruct 注解方法
 * 8. InitializingBean.afterPropertiesSet()
 * 9. 自定义 init-method
 * 10. BeanPostProcessor.postProcessAfterInitialization()
 * 11. Bean 就绪，可以使用
 * 12. --- 容器关闭时 ---
 * 13. @PreDestroy 注解方法
 * 14. DisposableBean.destroy()
 * 15. 自定义 destroy-method
 */
@Component
public class LifecycleDemoBean implements BeanNameAware, BeanFactoryAware, 
        ApplicationContextAware, InitializingBean, DisposableBean {
    
    private static final Logger log = LoggerFactory.getLogger(LifecycleDemoBean.class);
    
    private String beanName;
    private BeanFactory beanFactory;
    private ApplicationContext applicationContext;
    
    /**
     * 1. 构造函数 - Bean 实例化
     */
    public LifecycleDemoBean() {
        log.info("【1】构造函数 - Bean 实例化");
    }
    
    /**
     * 2. 依赖注入 - @Autowired 注入（如果有依赖的话）
     */
    // @Autowired
    // public void setDependency(SomeService service) {
    //     log.info("【2】依赖注入 - @Autowired");
    // }
    
    /**
     * 3. BeanNameAware.setBeanName() - 设置 Bean 名称
     */
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        log.info("【3】BeanNameAware.setBeanName() - Bean 名称：{}", name);
    }
    
    /**
     * 4. BeanFactoryAware.setBeanFactory() - 设置 Bean 工厂
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        log.info("【4】BeanFactoryAware.setBeanFactory() - 获取 BeanFactory");
    }
    
    /**
     * 5. ApplicationContextAware.setApplicationContext() - 设置应用上下文
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        log.info("【5】ApplicationContextAware.setApplicationContext() - 获取 ApplicationContext");
    }
    
    /**
     * 6. BeanPostProcessor.postProcessBeforeInitialization() - 初始化前处理
     * 注意：这个方法在 BeanPostProcessor 中实现，不在 Bean 本身
     */
    // 需要在配置类中定义 BeanPostProcessor 来演示这一步
    
    /**
     * 7. @PostConstruct 注解方法 - JSR-250 初始化回调
     */
    @PostConstruct
    public void postConstruct() {
        log.info("【7】@PostConstruct - JSR-250 初始化回调");
    }
    
    /**
     * 8. InitializingBean.afterPropertiesSet() - 属性设置完成后初始化
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("【8】InitializingBean.afterPropertiesSet() - 属性设置完成后初始化");
    }
    
    /**
     * 9. 自定义 init-method - 通过@Bean 注解或 XML 配置的初始化方法
     * 这个方法会在 XML 或配置类中指定的 init-method 被调用
     */
    public void customInitMethod() {
        log.info("【9】自定义 init-method - 初始化方法");
    }
    
    /**
     * 10. BeanPostProcessor.postProcessAfterInitialization() - 初始化后处理
     * 注意：这个方法在 BeanPostProcessor 中实现，不在 Bean 本身
     */
    // 需要在配置类中定义 BeanPostProcessor 来演示这一步
    
    /**
     * 11. Bean 就绪，可以使用
     * 此时 Bean 已经完全初始化，可以正常提供服务
     */
    public void doWork() {
        log.info("【11】Bean 正在工作中... Bean 名称：{}", beanName);
        log.info("ApplicationContext: {}", applicationContext != null ? "已注入" : "未注入");
        log.info("BeanFactory: {}", beanFactory != null ? "已注入" : "未注入");
    }
    
    /**
     * 13. @PreDestroy 注解方法 - JSR-250 销毁前回调
     */
    @PreDestroy
    public void preDestroy() {
        log.info("【13】@PreDestroy - JSR-250 销毁前回调");
    }
    
    /**
     * 14. DisposableBean.destroy() - 销毁 Bean
     */
    @Override
    public void destroy() throws Exception {
        log.info("【14】DisposableBean.destroy() - 销毁 Bean");
    }
    
    /**
     * 15. 自定义 destroy-method - 通过@Bean 注解或 XML 配置的销毁方法
     */
    public void customDestroyMethod() {
        log.info("【15】自定义 destroy-method - 销毁方法");
    }
    
    /**
     * 获取 Bean 名称
     */
    public String getBeanName() {
        return beanName;
    }
}
