package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.google.common.hash.BloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务层 - 演示 Redis 缓存使用
 */
@Service
public class UserService {

    @Autowired
    private com.example.demo.bean.LifecycleDemoBean lifecycleDemoBean;
    
    @Autowired
    private UserRepository repository;
    
    @Autowired
    private BloomFilter<Long> userBloomFilter;
    
    @Autowired
    private BloomFilter<String> usernameBloomFilter;
    
    /**
     * 根据 ID 查询用户
     * 使用 @Cacheable 注解，如果缓存中存在则直接从缓存读取，否则查询数据库并缓存
     * value: 缓存名称
     * key: 缓存键（使用 SpEL 表达式）
     */
    @Cacheable(value = "user", key = "#id")
    public User getUserById(Long id) {
        System.out.println("【数据库查询】从数据库查询用户 ID: " + id);
        
        // 先通过布隆过滤器判断用户是否存在
        if (!userBloomFilter.mightContain(id)) {
            System.out.println("【布隆过滤器】用户 ID " + id + " 一定不存在，直接返回 null");
            return null;
        }
        System.out.println("【布隆过滤器】用户 ID " + id + " 可能存在，继续查询数据库");
        
        return repository.selectById(id);
    }
    
    /**
     * 根据用户名查询用户
     */
    @Cacheable(value = "user:username", key = "#username")
    public User getUserByUsername(String username) {
        System.out.println("【数据库查询】从数据库查询用户名：" + username);
        
        // 先通过布隆过滤器判断用户名是否存在
        if (!usernameBloomFilter.mightContain(username)) {
            System.out.println("【布隆过滤器】用户名 " + username + " 一定不存在，直接返回 null");
            return null;
        }
        System.out.println("【布隆过滤器】用户名 " + username + " 可能存在，继续查询数据库");
        
        return repository.findByUsername(username);
    }
    
    /**
     * 保存或更新用户
     * 使用 @CachePut 注解，总是执行方法并更新缓存
     */
    @CachePut(value = "user", key = "#result.id")
    public User saveUser(User user) {
        System.out.println("【保存用户】保存用户到数据库：" + user.getUsername());
        if (user.getId() == null) {
            // 新用户，设置创建时间
            user.setCreateTime(java.time.LocalDateTime.now());
        }
        user.setUpdateTime(java.time.LocalDateTime.now());
        
        User savedUser;
        if (user.getId() != null && user.getId() > 0) {
            // 更新操作
            repository.updateById(user);
            savedUser = repository.selectById(user.getId());
        } else {
            // 插入操作
            repository.insert(user);
            savedUser = user;
        }
        
        // 将用户 ID 和用户名添加到布隆过滤器
        if (savedUser != null && savedUser.getId() != null) {
            userBloomFilter.put(savedUser.getId());
            if (savedUser.getUsername() != null) {
                usernameBloomFilter.put(savedUser.getUsername());
            }
            System.out.println("【布隆过滤器】已将用户 ID=" + savedUser.getId() + ", username=" + savedUser.getUsername() + " 添加到布隆过滤器");
        }
        
        return savedUser;
    }
    
    /**
     * 删除用户
     * 使用 @CacheEvict 注解，删除缓存
     */
    @CacheEvict(value = "user", key = "#id")
    public void deleteUser(Long id) {
        System.out.println("【删除用户】删除用户 ID: " + id);
        repository.deleteById(id);
    }
    
    /**
     * 查询所有用户
     * 不使用缓存，每次都查询数据库
     */
    public List<User> getAllUsers() {
        System.out.println("【查询所有用户】从数据库查询所有用户");
        return repository.selectList(new LambdaQueryWrapper<>());
    }
    
    /**
     * 清空所有用户缓存
     */
    @CacheEvict(value = {"user", "user:username"}, allEntries = true)
    public void clearAllCache() {
        System.out.println("【清空缓存】清空所有用户缓存");
    }
    
    /**
     * 初始化布隆过滤器 - 从数据库加载所有用户 ID 和用户名
     * 在应用启动时调用
     */
    public void initBloomFilter() {
        System.out.println("【布隆过滤器】开始初始化，从数据库加载用户数据...");
        List<User> allUsers = repository.selectList(new LambdaQueryWrapper<>());
        for (User user : allUsers) {
            if (user.getId() != null) {
                userBloomFilter.put(user.getId());
            }
            if (user.getUsername() != null) {
                usernameBloomFilter.put(user.getUsername());
            }
        }
        System.out.println("【布隆过滤器】初始化完成，共加载 " + allUsers.size() + " 个用户");
        System.out.println("【布隆过滤器】用户 ID 布隆过滤器预计元素数量：10000, 误判率：1%");
        System.out.println("【布隆过滤器】用户名布隆过滤器预计元素数量：10000, 误判率：1%");

    }
}
