package com.example.demo.controller;

import com.example.demo.circular.ServiceA;
import com.example.demo.circular.ServiceB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 循环依赖演示 Controller
 */
@RestController
@RequestMapping("/api/circular")
public class CircularDependencyController {
    
    private static final Logger log = LoggerFactory.getLogger(CircularDependencyController.class);
    
    private final ServiceA serviceA;
    private final ServiceB serviceB;
    
    public CircularDependencyController(ServiceA serviceA, ServiceB serviceB) {
        this.serviceA = serviceA;
        this.serviceB = serviceB;
        log.info("CircularDependencyController 初始化完成");
    }
    
    /**
     * 测试 ServiceA
     */
    @GetMapping("/test-a")
    public String testServiceA() {
        log.info("========== 开始测试 ServiceA ==========");
        serviceA.doSomethingInA();
        log.info("========== ServiceA 测试完成 ==========");
        return "ServiceA 测试完成，请查看日志输出";
    }
    
    /**
     * 测试 ServiceB
     */
    @GetMapping("/test-b")
    public String testServiceB() {
        log.info("========== 开始测试 ServiceB ==========");
        serviceB.doSomethingInB();
        log.info("========== ServiceB 测试完成 ==========");
        return "ServiceB 测试完成，请查看日志输出";
    }
    
    /**
     * 验证 Bean 是否完全注入
     */
    @GetMapping("/check")
    public String checkInjection() {
        log.info("检查依赖注入情况:");
        log.info("ServiceA 中的 ServiceB: {}", serviceA.getServiceB() != null ? "已注入" : "未注入");
        log.info("ServiceB 中的 ServiceA: {}", serviceB.getServiceA() != null ? "已注入" : "未注入");
        
        StringBuilder result = new StringBuilder();
        result.append("依赖注入检查结果:\n");
        result.append("1. ServiceA 中的 ServiceB: ").append(serviceA.getServiceB() != null ? "✓ 已注入" : "✗ 未注入").append("\n");
        result.append("2. ServiceB 中的 ServiceA: ").append(serviceB.getServiceA() != null ? "✓ 已注入" : "✗ 未注入").append("\n");
        result.append("\nSpring 成功解决了 setter 注入的循环依赖！");
        
        return result.toString();
    }
}
