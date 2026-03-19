package com.example.demo.aspect;

import com.example.demo.annotation.OperationLog;
import com.example.demo.entity.OperationLogEntity;
import com.example.demo.mapper.OperationLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 操作日志切面
 * 使用AOP拦截带有@OperationLog注解的方法,记录操作日志
 */
@Aspect
@Component
public class OperationLogAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(OperationLogAspect.class);
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private OperationLogMapper operationLogMapper;
    
    /**
     * 环绕通知,拦截带有@OperationLog注解的方法
     */
    @Around("@annotation(com.example.demo.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);
        
        // 创建日志实体
        OperationLogEntity logEntity = new OperationLogEntity();
        
        try {
            // 设置基本信息
            logEntity.setModule(operationLog.module());
            logEntity.setOperationType(operationLog.type().getDescription());
            logEntity.setDescription(operationLog.description());
            
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                logEntity.setMethod(request.getMethod());
                logEntity.setUrl(request.getRequestURL().toString());
                logEntity.setIp(getIpAddress(request));
            }
            
            // 设置操作用户(这里简化处理,实际项目中可以从SecurityContext或Session中获取)
            logEntity.setOperator("system");
            
            // 保存请求参数
            if (operationLog.isSaveRequestData()) {
                String requestParams = getRequestParams(joinPoint);
                logEntity.setRequestParams(requestParams);
            }
            
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            logEntity.setExecutionTime(executionTime);
            logEntity.setSuccess(true);
            
            // 保存响应结果
            if (operationLog.isSaveResponseData() && result != null) {
                String responseResult = objectMapper.writeValueAsString(result);
                logEntity.setResponseResult(responseResult);
            }
            
            // 记录日志
            logger.info("操作日志: {}", logEntity);
            
            // 保存日志到数据库
            saveLogToDatabase(logEntity);
            
            return result;
            
        } catch (Throwable e) {
            // 记录异常信息
            long executionTime = System.currentTimeMillis() - startTime;
            logEntity.setExecutionTime(executionTime);
            logEntity.setSuccess(false);
            logEntity.setErrorMsg(e.getMessage());
            
            logger.error("操作异常: {}", logEntity, e);
            
            // 保存异常日志到数据库
            saveLogToDatabase(logEntity);
            
            throw e;
        }
    }
    
    /**
     * 获取请求参数
     */
    private String getRequestParams(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return "";
            }
            
            // 过滤掉HttpServletRequest和HttpServletResponse等对象
            StringBuilder params = new StringBuilder();
            for (Object arg : args) {
                if (arg != null && !isServletObject(arg)) {
                    params.append(objectMapper.writeValueAsString(arg)).append(";");
                }
            }
            return params.toString();
        } catch (Exception e) {
            logger.warn("获取请求参数失败: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * 判断是否是Servlet相关对象
     */
    private boolean isServletObject(Object obj) {
        String className = obj.getClass().getName();
        return className.contains("HttpServletRequest") ||
               className.contains("HttpServletResponse") ||
               className.contains("HttpSession");
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 对于通过多个代理的情况,第一个IP才是客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
    
    /**
     * 保存日志到数据库
     */
    private void saveLogToDatabase(OperationLogEntity logEntity) {
        try {
            operationLogMapper.insertLog(logEntity);
        } catch (Exception e) {
            logger.error("保存操作日志到数据库失败: {}", e.getMessage(), e);
        }
    }
}
