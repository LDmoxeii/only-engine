# Engine Job 模块

SnailJob 定时任务集成模块，为项目提供分布式定时任务和调度功能。

## 功能特性

- **SnailJob 客户端集成**: 自动配置 SnailJob 客户端
- **Spring 调度支持**: 启用 Spring `@Scheduled` 注解支持
- **日志集成**: 自动配置 SnailJob Logback Appender
- **自动装配**: 当 Job 功能存在于 classpath 时自动启用
- **条件配置**: 支持通过 `only.job.enabled` 属性控制启用/禁用

## SnailJob 简介

[SnailJob](https://snailjob.opensnail.com/) 是一个灵活、可靠、快速的分布式任务调度平台，支持：

- 分布式任务调度
- 任务编排
- 工作流引擎
- 任务重试策略
- 任务监控和日志

## 使用方式

### 1. 添加依赖

```kotlin
dependencies {
    implementation(project(":engine-job"))
}
```

### 2. 自动配置

当 `engine-job` 模块在 classpath 中时，会自动：

- 启用 Spring 调度支持 (`@EnableScheduling`)
- 启用 SnailJob 客户端 (`@EnableSnailJob`)
- 配置 SnailJob Logback Appender
- 初始化定时任务功能

### 3. 配置属性

#### 基本配置

```yaml
only:
  job:
    enabled: true  # 默认为 true，设置为 false 可禁用 Job 支持

# SnailJob 客户端配置
snail-job:
  enabled: true
  namespace: default
  group: my-application
  server:
    host: localhost
    port: 1788
```

#### 完整配置示例

```yaml
snail-job:
  # 是否启用
  enabled: true
  # 命名空间
  namespace: default
  # 组名称
  group: my-application-group
  # 服务器配置
  server:
    host: 127.0.0.1
    port: 1788
  # 重试配置
  retry:
    # 最大重试次数
    max-count: 3
    # 重试间隔（毫秒）
    interval: 1000
```

### 4. 使用示例

#### 创建定时任务

```kotlin
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class MyScheduledTask {

    // 使用 Spring @Scheduled 注解
    @Scheduled(cron = "0 0 * * * ?")
    fun hourlyTask() {
        println("每小时执行一次")
    }

    @Scheduled(fixedRate = 60000)
    fun everyMinuteTask() {
        println("每分钟执行一次")
    }
}
```

#### 创建 SnailJob 任务

```kotlin
import com.aizuda.snailjob.client.job.core.annotation.JobExecutor
import com.aizuda.snailjob.client.model.ExecuteResult
import org.springframework.stereotype.Component

@Component
class MySnailJobTask {

    @JobExecutor(name = "myTask")
    fun executeTask(): ExecuteResult {
        try {
            // 执行任务逻辑
            println("执行 SnailJob 任务")

            return ExecuteResult.success("任务执行成功")
        } catch (e: Exception) {
            return ExecuteResult.failure("任务执行失败: ${e.message}")
        }
    }
}
```

## 工作原理

1. `SnailJobAutoConfiguration` 检测配置启用状态
2. 启用 Spring 调度功能
3. 启用 SnailJob 客户端
4. 监听 SnailJob 客户端启动事件
5. 配置 Logback Appender 用于任务日志收集
6. 注册所有 `@JobExecutor` 标记的任务

## 日志集成

SnailJob 会自动配置 Logback Appender，将任务执行日志发送到 SnailJob 服务端：

- **自动收集**: 任务执行期间的所有日志自动收集
- **远程查看**: 可在 SnailJob 管理界面查看任务日志
- **日志上下文**: 自动关联任务执行上下文信息

## 条件装配

- `@ConditionalOnProperty(prefix = "only.job", name = ["enabled"], matchIfMissing = true)`: 默认启用
- 可通过配置 `only.job.enabled=false` 禁用整个 Job 模块

## 与 only4j-job 的差异

| 方面   | only4j-job  | engine-job               |
|------|-------------|--------------------------|
| 语言   | Java        | Kotlin                   |
| 配置前缀 | `snail-job` | `only.job` + `snail-job` |
| 默认启用 | 需显式配置       | 默认启用                     |
| 日志风格 | 无初始化日志      | 统一的 InitPrinter 风格       |
| 代码风格 | Java 传统风格   | Kotlin 习惯用法              |

## 更多资源

- [SnailJob 官方文档](https://snailjob.opensnail.com/)
- [SnailJob GitHub](https://github.com/aizuda/snail-job)
- [Spring Scheduling 文档](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#scheduling)
