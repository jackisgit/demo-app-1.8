package com.example.demo.controller;

import com.example.demo.service.CountDownLatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CountDownLatch 示例控制器
 */
@RestController
@RequestMapping("/api/countdownlatch")
public class CountDownLatchController {

    @Autowired
    private CountDownLatchService countDownLatchService;

    /**
     * 场景 1：等待多个任务完成后汇总结果
     */
    @GetMapping("/multi-source")
    public Map<String, Object> getMultiSourceData() {
        long startTime = System.currentTimeMillis();
        try {
            List<String> results = countDownLatchService.getDataFromMultipleSources();
            return buildResponse("success", results, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 场景 2：同时启动多个任务（发令枪模式）
     */
    @GetMapping("/simultaneous-start")
    public Map<String, Object> simultaneousStart() {
        long startTime = System.currentTimeMillis();
        try {
            String result = countDownLatchService.startMultipleTasksSimultaneously();
            return buildResponse(result, null, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 场景 3：带超时的等待
     */
    @GetMapping("/with-timeout")
    public Map<String, Object> waitForTasksWithTimeout() {
        long startTime = System.currentTimeMillis();
        try {
            String result = countDownLatchService.waitForTasksWithTimeout();
            return buildResponse(result, null, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 场景 4：并发压力测试
     */
    @GetMapping("/load-test")
    public Map<String, Object> performLoadTest() {
        long startTime = System.currentTimeMillis();
        try {
            String result = countDownLatchService.performLoadTest();
            return buildResponse(result, null, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            return buildErrorResponse(e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    private Map<String, Object> buildResponse(String message, Object data, long duration) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        response.put("data", data);
        response.put("duration_ms", duration);
        return response;
    }

    private Map<String, Object> buildErrorResponse(String message, long duration) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        response.put("duration_ms", duration);
        return response;
    }
}
