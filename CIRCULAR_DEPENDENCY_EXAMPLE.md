# Spring 循环依赖示例

本目录包含 Spring 循环依赖的完整示例，演示了循环依赖的问题和解决方案。

## 📚 什么是循环依赖？

循环依赖是指两个或多个 Bean 之间相互依赖，形成环路。例如：
- Bean A 依赖 Bean B
- Bean B 依赖 Bean A

## 🔄 示例场景

### 场景 1: Setter 注入的循环依赖（可以解决）

**ServiceA ↔ ServiceB**

```java
@Component
public class ServiceA {
    @Autowired
    private ServiceB serviceB;  // A 依赖 B
}

@Component
public class ServiceB {
    @Autowired
    private ServiceA serviceA;  // B 依赖 A
}
```

✅ **Spring 可以解决这个问题！**

### 场景 2: 使用@Lazy 注解的循环依赖

**ServiceC ↔ ServiceD**

```java
@Component
public class ServiceC {
    @Autowired
    @Lazy  // 延迟加载
    private ServiceD serviceD;  // C 依赖 D，但使用@Lazy
}

@Component
public class ServiceD {
    @Autowired
    private ServiceC serviceC;  // D 依赖 C
}
```

✅ **使用@Lazy 注解可以轻松解决循环依赖！**

### 场景 3: 构造器注入的循环依赖（无法解决）❌

```java
@Component
public class ServiceA {
    private final ServiceB serviceB;
    
    public ServiceA(ServiceB serviceB) {  // ❌ 会导致循环依赖错误
        this.serviceB = serviceB;
    }
}

@Component
public class ServiceB {
    private final ServiceA serviceA;
    
    public ServiceB(ServiceA serviceA) {  // ❌ 会导致循环依赖错误
        this.serviceA = serviceA;
    }
}
```

❌ **这种情况会抛出 `BeanCurrentlyInCreationException` 异常！**

## 🔧 Spring 如何解决循环依赖？

Spring 通过**三级缓存**机制解决 setter 注入的循环依赖问题：

### 三级缓存结构

1. **singletonObjects（一级缓存）**
   - 存放完全初始化好的 Bean
   - Bean 已经实例化、属性注入、初始化方法都执行完毕

2. **earlySingletonObjects（二级缓存）**
   - 存放原始的 Bean 对象（尚未填充属性）
   - 用于解决循环依赖

3. **singletonFactories（三级缓存）**
   - 存放 Bean 工厂对象
   - 用于提前暴露 Bean 的引用

### 解决流程（以 A→B→A 为例）

```
1. 创建 ServiceA
   - 实例化 ServiceA
   - 将 ServiceA 的 ObjectFactory 放入三级缓存
   - 准备注入 ServiceB

2. 创建 ServiceB
   - 实例化 ServiceB
   - 将 ServiceB 的 ObjectFactory 放入三级缓存
   - 准备注入 ServiceA

3. ServiceB 需要注入 ServiceA
   - 从一级缓存查找 → 没有
   - 从二级缓存查找 → 没有
   - 从三级缓存查找 → 找到 ServiceA 的 ObjectFactory
   - 通过 ObjectFactory 获取 ServiceA 的早期引用
   - 将 ServiceA 放入二级缓存
   - 完成 ServiceB 的注入

4. ServiceA 需要注入 ServiceB
   - 此时 ServiceB 已经完全创建好
   - 直接从一级缓存获取 ServiceB
   - 完成 ServiceA 的注入
```

## 🎯 三种解决方案对比

| 方案 | 优点 | 缺点 | 适用场景 |
|------|------|------|----------|
| **Setter 注入** | Spring 自动解决 | 代码不够简洁 | 一般不推荐 |
| **@Lazy 注解** | 简单直观 | 需要额外注解 | **推荐使用** |
| **重构设计** | 从根本上解决问题 | 可能需要较大改动 | **最佳实践** |

## 🚀 如何测试

启动应用后，访问以下接口：

### 1. 测试 ServiceA 和 ServiceB 的循环依赖
```bash
curl http://localhost:8080/api/circular/test-a
curl http://localhost:8080/api/circular/test-b
```

### 2. 检查依赖注入情况
```bash
curl http://localhost:8080/api/circular/check
```

## 📝 日志输出示例

成功的循环依赖解决会看到类似日志：

```
【ServiceA】实例化完成
【ServiceB】实例化完成
【ServiceB】通过 setter 注入 ServiceA: ServiceA
【ServiceA】通过 setter 注入 ServiceB: ServiceB
```

## ⚠️ 注意事项

1. **构造器注入的循环依赖无法解决**
   - 会抛出 `BeanCurrentlyInCreationException`
   - 应避免使用构造器注入形成循环依赖

2. **原型 Bean（@Scope("prototype")）的循环依赖**
   - Spring 无法解决
   - 会抛出异常

3. **最佳实践是避免循环依赖**
   - 重新设计类结构
   - 提取公共接口
   - 使用事件机制

## 💡 最佳实践建议

1. **优先使用@Lazy 注解**
   ```java
   @Autowired
   @Lazy
   private OtherService otherService;
   ```

2. **重构代码消除循环依赖**
   - 提取第三方服务
   - 使用接口解耦
   - 引入事件驱动架构

3. **使用构造函数注入（无循环依赖时）**
   ```java
   private final OtherService otherService;
   
   public MyService(OtherService otherService) {
       this.otherService = otherService;
   }
   ```

## 📖 参考资料

- [Spring 官方文档 - Bean 依赖](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-dependencies)
- [Spring 循环依赖源码分析](https://spring.io/blog)
