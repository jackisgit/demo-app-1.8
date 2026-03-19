package com.example.demo.controller;

import com.example.demo.annotation.OperationLog;
import com.example.demo.annotation.OperationType;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 根据 ID 获取用户(演示缓存读取)
     * GET /api/users/1
     */
    @GetMapping("/{id}")
    @OperationLog(module = "用户管理", type = OperationType.SELECT, description = "根据ID查询用户")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
    
    /**
     * 根据用户名获取用户(演示缓存读取)
     * GET /api/users/username/admin
     */
    @GetMapping("/username/{username}")
    @OperationLog(module = "用户管理", type = OperationType.SELECT, description = "根据用户名查询用户")
    public User getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }
    
    /**
     * 创建新用户
     * POST /api/users
     */
    @PostMapping
    @OperationLog(module = "用户管理", type = OperationType.INSERT, description = "创建新用户")
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }
    
    /**
     * 更新用户信息
     * PUT /api/users
     */
    @PutMapping
    @OperationLog(module = "用户管理", type = OperationType.UPDATE, description = "更新用户信息")
    public User updateUser(@RequestBody User user) {
        return userService.saveUser(user);
    }
    
    /**
     * 删除用户
     * DELETE /api/users/1
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "用户管理", type = OperationType.DELETE, description = "删除用户")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
    
    /**
     * 获取所有用户(不缓存)
     * GET /api/users/all
     */
    @GetMapping("/all")
    @OperationLog(module = "用户管理", type = OperationType.SELECT, description = "查询所有用户")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
    
    /**
     * 清空缓存
     * POST /api/users/cache/clear
     */
    @PostMapping("/cache/clear")
    public String clearCache() {
        userService.clearAllCache();
        return "缓存已清空";
    }
    
    /**
     * 重新初始化布隆过滤器
     * POST /api/users/bloom-filter/init
     */
    @PostMapping("/bloom-filter/init")
    public String initBloomFilter() {
        userService.initBloomFilter();
        return "布隆过滤器已重新初始化";
    }
}
