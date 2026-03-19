package com.example.demo.annotation;

/**
 * 操作类型枚举
 */
public enum OperationType {
    
    /**
     * 新增
     */
    INSERT("新增"),
    
    /**
     * 修改
     */
    UPDATE("修改"),
    
    /**
     * 删除
     */
    DELETE("删除"),
    
    /**
     * 查询
     */
    SELECT("查询"),
    
    /**
     * 其他
     */
    OTHER("其他");
    
    private final String description;
    
    OperationType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
