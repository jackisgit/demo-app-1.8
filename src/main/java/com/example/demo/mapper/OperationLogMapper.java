package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.OperationLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper接口
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogEntity> {
    
    /**
     * 插入操作日志
     * @param log 操作日志实体
     * @return 影响行数
     */
    int insertLog(OperationLogEntity log);
}
