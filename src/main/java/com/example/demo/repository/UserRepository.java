package com.example.demo.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 用户数据访问层 - MyBatis-Plus
 */
@Mapper
public interface UserRepository extends BaseMapper<User> {
    
    @Select("SELECT * FROM t_user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);
    
    @Select("SELECT COUNT(*) > 0 FROM t_user WHERE username = #{username}")
    boolean existsByUsername(@Param("username") String username);
}
