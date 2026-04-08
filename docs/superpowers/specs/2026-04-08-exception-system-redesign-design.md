## 背景

`only-engine` 当前异常体系以 `KnownException / WarnException / ErrorException` 为核心，但实际使用中混合了承载业务语义、日志级别、HTTP 状态和错误码四类职责，导致以下问题：

- 业务失败、参数错误、系统故障经常共用 `KnownException`
- 异常调用点大量使用字符串消息，缺少稳定错误码语义
- Web 层协议映射依赖默认状态和零散 `@RespStatus`，规则不稳定
- 前端只能依靠经验处理 `HTTP status` 与 `body.code` 的组合
- 后续新增异常时难以判断“该抛哪种异常、该返回什么协议”

本次重构目标是为前端页面接口建立一套清晰的 Hybrid 异常协议：业务失败使用 `HTTP 200 + body.code`，认证、权限、限流、系统故障仍使用真实 HTTP 状态。

## 目标

- 建立语义清晰的新异常模型，替代 `KnownException / WarnException / ErrorException`
- 所有业务错误都绑定稳定 `ErrorCode`，禁止字符串直抛
- 在 `engine-web` 统一完成异常到 HTTP/响应体的映射
- 统一前端页面接口的失败处理约定
- 支持一次性迁移现有 `KnownException` 用法，不保留长期兼容负担

## 非目标

- 不为 RPC/第三方开放平台单独设计协议
- 不保留“新旧异常体系长期并存”的兼容模式
- 不在本次设计中引入多语言文案平台，仅为未来 i18n 保留结构

## 目标协议

页面接口统一遵循以下返回规则：

- `BusinessException`：`HTTP 200`
- `RequestException`：`HTTP 400`
- `AuthenticationException`：`HTTP 401`
- `AuthorizationException`：`HTTP 403`
- `RateLimitException`：`HTTP 429`
- `SystemException`：`HTTP 500`
- `DependencyException`：`HTTP 503`，必要时可扩展为 `502`

典型示例：

- 验证码错误、余额不足、状态不允许、资源业务上不存在：`200 + body.code`
- JSON 解析失败、请求字段缺失、参数格式错误：`400`
- 未登录：`401`
- 无权限：`403`
- 重复提交、限流：`429`
- 数据库、Redis、OSS、磁盘、未知异常：`5xx`

## 新异常模型

### 1. 基础结构

新增统一基类：

```kotlin
abstract class AppException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.message,
    val context: Map<String, Any?> = emptyMap(),
    cause: Throwable? = null
) : RuntimeException(message, cause)
```

新增异常子类：

- `BusinessException`
- `RequestException`
- `AuthenticationException`
- `AuthorizationException`
- `RateLimitException`
- `SystemException`
- `DependencyException`

这些子类的职责只在于表达错误类别，不承载日志级别，不直接决定响应体结构。

### 2. ErrorCategory

新增 `ErrorCategory`：

- `BUSINESS`
- `REQUEST`
- `AUTHENTICATION`
- `AUTHORIZATION`
- `RATE_LIMIT`
- `SYSTEM`
- `DEPENDENCY`

异常类别用于统一决定：

- HTTP 状态
- 日志级别
- 默认告警策略

### 3. ErrorCode

新增 `ErrorCode` 协议：

```kotlin
interface ErrorCode {
    val code: Int
    val name: String
    val message: String
    val category: ErrorCategory
}
```

错误码按领域拆分，例如：

- `CommonErrors`
- `AuthErrors`
- `UserErrors`
- `VideoErrors`
- `StorageErrors`

调用方式统一为：

```kotlin
throw BusinessException(AuthErrors.CAPTCHA_INVALID)
throw RequestException(CommonErrors.PARAM_INVALID, context = mapOf("field" to "quality"))
throw SystemException(StorageErrors.FILE_WRITE_FAILED, cause = e)
```

禁止新增以下写法：

- `throw KnownException("xxx")`
- `throw BusinessException("xxx")`
- 业务代码中直接拼 `Result.error(...)`

## 响应结构

保留 `Result<T>` 作为统一响应包装，但扩展为更适合问题排查的结构：

- `code`
- `message`
- `data`
- `timestamp`
- `requestId`
- `path`

其中：

- `code` 是前端稳定判断业务失败的主键
- `requestId` 用于日志联查
- `path` 用于快速识别出错接口

`code=20000` 继续代表成功，其他错误码按 `ErrorCode` 输出。

## Web 层映射

`engine-web` 中的全局异常处理器重写为两层职责：

### 1. 领域异常映射层

`@ExceptionHandler(AppException::class)` 统一处理新模型异常：

- 写日志
- 计算 HTTP 状态
- 输出标准 `Result`

### 2. 框架异常翻译层

框架/三方异常不直接暴露给业务方，而是先翻译为新模型：

- `MethodArgumentNotValidException` / `BindException` / `HttpMessageNotReadableException`
  -> `RequestException`
- `NotLoginException`
  -> `AuthenticationException`
- `NotPermissionException` / `NotRoleException`
  -> `AuthorizationException`
- `LockFailureException` / 重复提交 / 限流异常
  -> `RateLimitException`
- 其余 `Throwable`
  -> `SystemException`

这样应用层与领域层只关心“错误是什么”，只有 Web 层关心“如何对外表现”。

## 日志策略

日志级别不再由异常对象字段控制，统一按 `ErrorCategory` 处理：

- `BUSINESS`：`INFO`
- `REQUEST`：`WARN`
- `AUTHENTICATION`：`INFO`
- `AUTHORIZATION`：`WARN`
- `RATE_LIMIT`：`WARN`
- `SYSTEM`：`ERROR`
- `DEPENDENCY`：`ERROR`

当异常存在 `cause` 时，`SYSTEM` 与 `DEPENDENCY` 输出堆栈；其余类别默认不打印长堆栈，避免日志噪音。

## 迁移策略

本次迁移不考虑长期兼容，采用一次性切换。

### 1. 基础设施先落地

先在 `only-engine` 完成：

- `ErrorCategory`
- `ErrorCode`
- `AppException` 及子类
- 新版 `GlobalExceptionHandlerAdvice`
- 扩展后的 `Result`

### 2. 规则迁移旧代码

全量替换旧异常调用：

- `throw KnownException("...")`
  -> 默认替换为 `BusinessException(...)`
- `KnownException.illegalArgument(...)`
  -> 替换为 `RequestException(...)`
- `KnownException.systemError(...)`
  -> 替换为 `SystemException(...)`

### 3. 人工校正高风险区域

以下目录必须二次审查，因为这里最容易把系统错误误判为业务错误：

- `only-engine/engine-common`
- `only-engine/engine-oss`
- `only-danmuku-adapter` 下的 distributed clients
- `only-danmuku-adapter` 下的 portal api

### 4. 彻底删除旧模型

迁移完成后删除：

- `KnownException`
- `WarnException`
- `ErrorException`

并移除依赖这些类型的处理分支、工厂方法和文档说明。

## 迁移判断规则

为保证迁移有一致标准，统一使用以下判定：

- 用户操作不满足业务规则：`BusinessException`
- 请求体缺字段、格式错、枚举值非法、参数越界：`RequestException`
- 需要登录但未登录：`AuthenticationException`
- 已登录但无权执行：`AuthorizationException`
- 限流、重复提交、锁竞争失败：`RateLimitException`
- 文件系统、JVM、数据库、Redis、未知运行时错误：`SystemException`
- 下游服务、三方组件、OSS、短信、HTTP 依赖异常：`DependencyException`

“资源不存在”不按技术视角判断，而按产品语义判断：

- 业务上“视频不存在/评论不存在/系列不存在”
  -> `BusinessException`
- 协议上“路径不存在/接口不存在”
  -> `RequestException`

## 实施顺序

建议按以下顺序执行，降低爆炸半径：

1. `only-engine` 新模型与处理器
2. `only-engine` 自身模块迁移
3. `only-danmuku-domain`
4. `only-danmuku-application`
5. `only-danmuku-adapter`
6. `only-danmuku-web-ui` 前端拦截器与约定清理

## 测试与验证

至少补充以下验证：

- `BusinessException` 返回 `HTTP 200`
- `RequestException` 返回 `HTTP 400`
- `AuthenticationException` 返回 `401`
- `AuthorizationException` 返回 `403`
- `RateLimitException` 返回 `429`
- `SystemException` 返回 `500`
- `DependencyException` 返回 `503`
- 响应体包含 `code/message/timestamp/requestId/path`
- 前端拦截器能够正确区分业务失败与协议失败

## 风险

- 旧代码中有大量 `throw KnownException("...")`，默认迁移为 `BusinessException` 后可能掩盖部分系统错误
- 前端如果目前把所有非 200 都当“网络异常”，需要同步按新协议清理拦截器
- 删除旧模型后，任何遗漏调用点都会直接编译失败，需要一次性清理彻底

## 结论

本次重构采用 `Hybrid` 协议，核心策略是：

- 用 `ErrorCode + AppException 子类` 取代旧的模糊异常模型
- 让异常只表达业务/系统语义，不再直接携带 HTTP 与日志策略
- 由 `engine-web` 统一完成协议映射
- 采用“规则迁移 + 高风险目录人工校正”的方式一次性完成切换

该方案适合当前以页面接口为主、允许调整协议且不追求旧模型兼容的代码库现状。
