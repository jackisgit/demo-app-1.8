package com.example.demo.config;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 布隆过滤器配置类
 */
@Configuration
public class BloomFilterConfig {
    
    /**
     * 用户 ID 布隆过滤器
     * 
     * @param expectedInsertions 预期插入的元素数量
     * @param fpp 误判率（0.01 = 1%）
     * @return BloomFilter<Long>
     */
    @Bean
    public BloomFilter<Long> userBloomFilter() {
        // 预期有 10000 个用户，误判率 1%
        long expectedInsertions = 10000L;
        double fpp = 0.01;
        
        return BloomFilter.create(
            Funnels.longFunnel(),
            expectedInsertions,
            fpp
        );
    }
    
    /**
     * 用户名布隆过滤器
     */
    @Bean
    public BloomFilter<String> usernameBloomFilter() {
        // 预期有 10000 个用户名，误判率 1%
        long expectedInsertions = 10000L;
        double fpp = 0.01;
        
        return BloomFilter.create(
            Funnels.stringFunnel(java.nio.charset.StandardCharsets.UTF_8),
            expectedInsertions,
            fpp
        );
    }
}
