# CLAUDE_CN.md

本文件为 Claude Code (claude.ai/code) 在此代码库中工作提供中文指导。

## 构建命令

这是一个基于 Gradle 的 Kotlin/Java 多模块项目，使用 Gradle Wrapper：

- `./gradlew build` - 构建所有模块
- `./gradlew test` 或 `./gradlew check` - 运行所有模块的测试和检查
- `./gradlew clean` - 清理所有构建输出
- `./gradlew :engine-common:test` - 运行特定模块的测试
- `./gradlew :engine-web-starter:build` - 构建特定模块
- `./gradlew publish` - 发布构件到配置的仓库（需要凭证）

## 架构概览

这是一个 Spring Boot 框架库，由两个主要模块组成：

### engine-common
核心工具和通用类：
- `Result<T>` - 标准 API 响应包装器，支持成功/错误处理
- 异常层次结构（`ErrorException`、`KnownException`、`WarnException`）
- 基于枚举的状态码和常量
- 用于请求上下文的 ThreadLocal 工具

### engine-web-starter
Spring Boot 自动配置启动器，提供：
- **响应包装**：通过 `ResponseAdvice` 自动将控制器响应包装为 `Result<T>` 格式
- **全局异常处理**：将异常转换为标准化错误响应
- **国际化支持**：带消息解析的国际化
- **请求过滤**：ThreadLocal 上下文管理、健康检查、请求体包装
- **身份验证**：基本的身份验证检查工具

关键自动配置类：
- `WebAutoConfiguration` - 主配置入口点
- `ResponseAdvice` - 包装响应，除非使用 `@IgnoreResultWrapper` 注解
- `GlobalExceptionHandlerAdvice` - 全局处理异常

### 模块依赖关系
- `engine-web-starter` 依赖于 `engine-common`
- 使用 Spring Boot 3.1.12、Kotlin 2.1.20、JDK 17
- 发布配置为阿里云 Maven 仓库

### 约定插件
`buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts` 包含共享构建逻辑：
- 带 Spring 和 JPA 插件的 Kotlin/JVM
- Maven 发布设置
- JUnit 5 测试配置
- 面向 Java 17 的 JVM 工具链

自动配置默认启用，但可通过 `only.engine.web.enable=false` 属性控制。
