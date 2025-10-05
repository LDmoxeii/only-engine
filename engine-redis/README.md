# Engine Redis

基于 Redisson 的 Redis 集成模块,提供分布式缓存、分布式锁、消息发布订阅、幂等性等功能。

## 功能特性

- ✅ **分布式缓存**: 基于 Redisson 的 Redis 缓存
- ✅ **两级缓存**: Redis (L2) + Caffeine (L1) 二级缓存架构
- ✅ **动态 TTL**: 支持通过缓存名称动态配置过期时间
- ✅ **分布式锁**: 集成 Lock4j 提供注解式分布式锁
- ✅ **幂等性**: 基于 Redis 的接口幂等性保护（防重复提交）
- ✅ **消息发布订阅**: Redis Pub/Sub 支持
- ✅ **分布式工具**: ID 生成器、分布式队列、限流器
- ✅ **Key 前缀管理**: 统一的 Redis key 前缀处理
- ✅ **Spring Cache 集成**: 标准 Spring Cache 抽象支持

## 依赖

```kotlin
dependencies {
    implementation("com.only.engine:engine-redis:1.0.0")
}
```

## 核心组件

### 配置类

- **RedisAutoConfiguration**: Redis 主配置类,配置 Redisson 客户端
- **CacheAutoConfiguration**: 缓存配置类,配置 Caffeine 和二级缓存
- **IdempotentAutoConfiguration**: 幂等性配置类,配置防重复提交切面
- **RedissonProperties**: Redisson 配置属性类

### 工具类

- **RedisUtils**: Redis 操作工具类,提供缓存、发布订阅、原子操作等
- **CacheUtils**: Spring Cache 简化操作工具类
- **SequenceUtils**: 分布式 ID 生成器
- **QueueUtils**: 分布式队列工具类

### 管理器

- **PlusSpringCacheManager**: 自定义 Spring Cache 管理器,支持动态 TTL
- **CaffeineCacheDecorator**: 二级缓存装饰器 (Caffeine L1 + Redis L2)

### 处理器

- **KeyPrefixHandler**: Redis key 前缀处理器
- **RedisExceptionHandler**: Lock4j 异常处理器

### 幂等性组件

- **TokenProvider**: Token 提供者接口（依赖倒置抽象）
- **DefaultTokenProvider**: 默认 Token 提供者（从请求头获取）
- **RepeatSubmit**: 防重复提交注解
- **RepeatSubmitAspect**: 防重复提交切面

## 配置示例

### 单机模式

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0

redisson:
  key-prefix: "my-app"
  threads: 16
  netty-threads: 32
  single-server-config:
    client-name: ${spring.application.name}
    connection-minimum-idle-size: 8
    connection-pool-size: 32
    idle-connection-timeout: 10000
    timeout: 3000
    subscription-connection-pool-size: 50
```

### 集群模式

```yaml
spring:
  data:
    redis:
      cluster:
        nodes:
          - 192.168.0.100:6379
          - 192.168.0.101:6379
          - 192.168.0.102:6379
      password:
      timeout: 10s

redisson:
  key-prefix: "my-app"
  threads: 16
  netty-threads: 32
  cluster-servers-config:
    client-name: ${spring.application.name}
    master-connection-minimum-idle-size: 32
    master-connection-pool-size: 64
    slave-connection-minimum-idle-size: 32
    slave-connection-pool-size: 64
    idle-connection-timeout: 10000
    timeout: 3000
    subscription-connection-pool-size: 50
    read-mode: "SLAVE"
    subscription-mode: "MASTER"
```

## 使用示例

### RedisUtils - 基本操作

```kotlin
// 缓存对象
RedisUtils.setCacheObject("user:1", user)
RedisUtils.setCacheObject("user:1", user, Duration.ofMinutes(30))

// 获取对象
val user = RedisUtils.getCacheObject<User>("user:1")

// 删除对象
RedisUtils.deleteObject("user:1")

// 检查是否存在
val exists = RedisUtils.isExistsObject("user:1")

// 设置过期时间
RedisUtils.expire("user:1", Duration.ofHours(1))
```

### RedisUtils - 集合操作

```kotlin
// List 操作
RedisUtils.setCacheList("list:key", listOf("a", "b", "c"))
RedisUtils.addCacheList("list:key", "d")
val list = RedisUtils.getCacheList<String>("list:key")

// Set 操作
RedisUtils.setCacheSet("set:key", setOf("a", "b", "c"))
RedisUtils.addCacheSet("set:key", "d")
val set = RedisUtils.getCacheSet<String>("set:key")

// Map 操作
RedisUtils.setCacheMap("map:key", mapOf("field1" to "value1"))
RedisUtils.setCacheMapValue("map:key", "field2", "value2")
val value = RedisUtils.getCacheMapValue<String>("map:key", "field1")
val map = RedisUtils.getCacheMap<String>("map:key")
```

### RedisUtils - 发布订阅

```kotlin
// 发布消息
RedisUtils.publish("channel:news", "Hello World")

// 订阅消息
RedisUtils.subscribe("channel:news", String::class.java) { msg ->
    println("Received: $msg")
}
```

### RedisUtils - 限流

```kotlin
// 限流: 每秒最多 10 次请求
val result = RedisUtils.rateLimiter(
    "rate:api:user",
    RateType.OVERALL,
    rate = 10,
    rateInterval = 1
)
if (result == -1L) {
    // 限流了
    throw RuntimeException("Too many requests")
}
```

### CacheUtils - Spring Cache 操作

```kotlin
// 缓存对象
CacheUtils.put("user", "1", user)

// 获取对象
val user = CacheUtils.get<User>("user", "1")

// 删除缓存
CacheUtils.evict("user", "1")

// 清空缓存
CacheUtils.clear("user")
```

### SequenceUtils - 分布式 ID 生成

```kotlin
// 生成日期序列号: ORDER20250102000001
val orderId = SequenceUtils.nextIdDate("ORDER")

// 生成纯序列号: 000001
val seqId = SequenceUtils.nextId("SEQ")
```

### QueueUtils - 分布式队列

```kotlin
// 添加到队列
QueueUtils.addQueueObject("task:queue", task)

// 从队列获取
val task = QueueUtils.getQueueObject<Task>("task:queue")

// 获取队列所有元素
val allTasks = QueueUtils.getAllQueueObject<Task>("task:queue")

// 删除队列
QueueUtils.destroyQueue("task:queue")
```

### Spring Cache 注解

```kotlin
@Service
class UserService {

    // 动态 TTL: cacheName#ttl#maxIdleTime#maxSize
    // 缓存 30 分钟,最大空闲时间 10 分钟,最大 1000 条
    @Cacheable("user#30m#10m#1000")
    fun getUser(id: String): User {
        // 查询数据库
    }

    @CachePut("user#30m#10m#1000", key = "#user.id")
    fun updateUser(user: User): User {
        // 更新数据库
        return user
    }

    @CacheEvict("user#30m#10m#1000", key = "#id")
    fun deleteUser(id: String) {
        // 删除数据库
    }
}
```

### 分布式锁 (Lock4j)

```kotlin
@Service
class OrderService {

    @Lock4j(keys = ["#orderId"], expire = 60000, acquireTimeout = 3000)
    fun processOrder(orderId: String) {
        // 处理订单,同一 orderId 同时只能有一个线程处理
    }
}
```

### 幂等性（防重复提交）

**基本使用**:

```kotlin
@RestController
@RequestMapping("/api/orders")
class OrderController {

    // 默认 5 秒内防止重复提交
    @RepeatSubmit
    @PostMapping("/create")
    fun createOrder(@RequestBody request: CreateOrderRequest): Result<Order> {
        // 创建订单逻辑
    }

    // 自定义间隔时间为 10 秒
    @RepeatSubmit(interval = 10, timeUnit = TimeUnit.SECONDS)
    @PostMapping("/submit")
    fun submitOrder(@RequestBody request: SubmitOrderRequest): Result<Order> {
        // 提交订单逻辑
    }

    // 自定义错误消息（支持国际化 key）
    @RepeatSubmit(
        interval = 3000,
        timeUnit = TimeUnit.MILLISECONDS,
        message = "{order.submit.duplicate}"
    )
    @PostMapping("/pay")
    fun payOrder(@RequestBody request: PayOrderRequest): Result<Void> {
        // 支付订单逻辑
    }
}
```

**工作原理**:

1. **Token 获取**: 通过 `TokenProvider` 接口获取用户身份标识（如 JWT token、session ID 等）
2. **Key 生成**: 使用 `MD5(token:参数)` 生成唯一提交 key
3. **Redis 检查**: 使用 `SETNX` 命令检查该 key 是否存在
4. **重复拦截**: 如果 key 已存在，抛出 `KnownException` 阻止重复提交
5. **成功放行**: 如果 key 不存在，设置 key 并放行请求
6. **结果处理**:
    - 成功（code=20000）: 保留 Redis key，在过期时间内阻止重复提交
    - 失败或异常: 删除 Redis key，允许重新提交

**扩展性设计（依赖倒置）**:

`engine-redis` 模块提供了 `TokenProvider` 接口抽象，不依赖任何具体的认证框架。你可以：

1. **使用默认实现** (engine-redis):
    - `DefaultTokenProvider`: 从请求头 `Authorization` 获取 token
    - 如果 token 不存在，使用请求 URI 作为标识
    - 优先级最低 (`@Order(Integer.MAX_VALUE)`)

2. **使用 Sa-Token 实现** (engine-satoken):
   ```kotlin
   dependencies {
       implementation("com.only.engine:engine-satoken:1.0.0")
   }
   ```
    - 自动使用 `SaTokenProvider` 获取 Sa-Token 的用户 token
    - 优先级: `@Order(100)`

3. **自定义实现** (推荐用于 Spring Security、OAuth2 等):
   ```kotlin
   @Component
   @Order(50)  // 更高优先级
   class CustomTokenProvider : TokenProvider {
       override fun getToken(): String {
           // 从 Spring Security、OAuth2 等获取用户标识
           return SecurityContextHolder.getContext()
               .authentication?.name ?: ""
       }

       override fun getTokenName(): String = "X-Auth-Token"
   }
   ```

**优先级规则**:

- `@Order` 值越小，优先级越高
- 建议范围:
    - 1-99: 自定义实现（最高优先级）
    - 100: Sa-Token 实现
    - Integer.MAX_VALUE: 默认实现（最低优先级）

## 二级缓存架构

本模块实现了 **Caffeine (L1) + Redis (L2)** 的二级缓存架构:

1. **查询流程**:
    - 先查询 Caffeine 本地缓存 (L1)
    - 如果 L1 未命中,查询 Redis 缓存 (L2)
    - 如果 L2 未命中,执行实际查询并同时写入 L1 和 L2

2. **写入流程**:
    - 同时写入 L1 (Caffeine) 和 L2 (Redis)

3. **删除流程**:
    - 同时删除 L1 和 L2 的缓存

4. **优势**:
    - 本地缓存 (Caffeine) 提供超高性能读取
    - Redis 缓存提供分布式一致性
    - 减少 Redis 网络开销

## 动态 TTL 配置

缓存名称支持以下格式:

```
cacheName#ttl#maxIdleTime#maxSize
```

参数说明:

- `cacheName`: 缓存名称
- `ttl`: 过期时间 (支持单位: ms, s, m, h, d)
- `maxIdleTime`: 最大空闲时间 (可选)
- `maxSize`: 最大缓存数量 (可选)

示例:

```kotlin
@Cacheable("user#30m")           // 30 分钟过期
@Cacheable("user#1h#10m")        // 1 小时过期,10 分钟最大空闲
@Cacheable("user#30m#10m#1000")  // 30 分钟过期,10 分钟最大空闲,最大 1000 条
```

## 依赖项

- **Redisson**: 3.34.1
- **Lock4j**: 2.2.7
- **Caffeine**: 3.1.8
- **Spring Boot**: 3.1.12

## 注意事项

1. **Key 监听器**: 使用 `addObjectListener`、`addListListener` 等监听器需要开启 Redis 的 `notify-keyspace-events` 配置
2. **序列化**: 默认使用 Jackson 进行 JSON 序列化,Key 使用 String 序列化
3. **LocalDateTime**: 自动配置了 `yyyy-MM-dd HH:mm:ss` 格式的序列化/反序列化
4. **Lua 脚本缓存**: 已启用 Lua 脚本缓存以提升性能
5. **线程池**: 可通过 `redisson.threads` 和 `redisson.netty-threads` 配置线程池大小

## License

Apache License 2.0
