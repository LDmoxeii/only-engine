# Engine Doc Module

基于 SpringDoc OpenAPI 3.0 的 API 文档自动配置模块,提供开箱即用的 Swagger UI 和 OpenAPI 文档生成能力。

## 功能特性

- **自动配置 OpenAPI 文档**: 基于 SpringDoc 自动生成 OpenAPI 3.0 规范文档
- **Swagger UI 集成**: 提供可视化的 API 文档界面
- **Javadoc 支持**: 集成 Therapi 运行时 Javadoc,自动读取 Java 注释作为 API 描述
- **上下文路径支持**: 自动为所有 API 路径添加应用上下文路径前缀
- **自定义标签处理**: 支持使用类注释作为 API 分组标签名称
- **安全认证配置**: 支持配置 API 安全认证方案

## 依赖说明

### 核心依赖

- `springdoc-openapi-starter-webmvc-api`: SpringDoc OpenAPI 核心库
- `therapi-runtime-javadoc`: 运行时 Javadoc 读取支持
- `hutool-core`: 工具类库(用于 IO 操作)
- `commons-lang3`: Apache Commons 工具类
- `jackson-module-kotlin`: Jackson Kotlin 模块支持

### 模块依赖

- `engine-common`: Engine 通用模块

## 使用方式

### 1. 添加依赖

在你的 `build.gradle.kts` 中添加依赖:

```kotlin
dependencies {
    implementation("com.only.engine:engine-doc:${version}")
}
```

### 2. 配置属性

在 `application.yml` 中配置文档信息:

```yaml
springdoc:
  api-docs:
    enabled: true  # 是否启用 API 文档,默认为 true
  info:
    title: "My API"
    description: "API 文档描述"
    version: "1.0.0"
    contact:
      name: "开发者"
      email: "dev@example.com"
      url: "https://example.com"
    license:
      name: "Apache 2.0"
      url: "https://www.apache.org/licenses/LICENSE-2.0"
  components:
    security-schemes:
      bearer-jwt:
        type: http
        scheme: bearer
        bearer-format: JWT
```

### 3. 访问文档

启动应用后,可以通过以下 URL 访问:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 配置说明

### 启用/禁用文档

```yaml
springdoc:
  api-docs:
    enabled: false  # 禁用 API 文档
```

### 基本信息配置

```yaml
springdoc:
  info:
    title: "API 标题"           # API 文档标题
    description: "API 描述"      # API 文档描述
    version: "1.0.0"            # API 版本号
```

### 联系人信息

```yaml
springdoc:
  info:
    contact:
      name: "联系人名称"
      email: "email@example.com"
      url: "https://example.com"
```

### 许可证信息

```yaml
springdoc:
  info:
    license:
      name: "许可证名称"
      url: "https://license-url.com"
```

### 安全认证配置

```yaml
springdoc:
  components:
    security-schemes:
      # Bearer Token 认证
      bearer-jwt:
        type: http
        scheme: bearer
        bearer-format: JWT
      # API Key 认证
      api-key:
        type: apiKey
        in: header
        name: X-API-Key
      # OAuth2 认证
      oauth2:
        type: oauth2
        flows:
          authorizationCode:
            authorization-url: https://auth.example.com/oauth/authorize
            token-url: https://auth.example.com/oauth/token
            scopes:
              read: 读取权限
              write: 写入权限
```

## 代码示例

### 基本控制器

```kotlin
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户相关接口")
class UserController {

    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    fun getUser(
        @Parameter(description = "用户ID") @PathVariable id: Long
    ): User {
        // 实现逻辑
    }

    @PostMapping
    @Operation(summary = "创建用户")
    fun createUser(@RequestBody user: User): User {
        // 实现逻辑
    }
}
```

### 使用 Javadoc 作为标签

当启用 `therapi-runtime-javadoc` 时,可以直接使用类的 Javadoc 注释作为标签名:

```kotlin
/**
 * 用户管理
 *
 * 提供用户的增删改查功能
 */
@RestController
@RequestMapping("/api/users")
class UserController {
    // 控制器方法
}
```

生成的文档会自动使用 "用户管理" 作为标签名称,"提供用户的增删改查功能" 作为标签描述。

### 安全认证注解

```kotlin
@RestController
@RequestMapping("/api/secure")
@SecurityRequirement(name = "bearer-jwt")  // 应用于整个控制器
class SecureController {

    @GetMapping("/data")
    fun getData(): Data {
        // 需要认证的接口
    }

    @GetMapping("/public")
    @SecurityRequirements  // 移除安全要求
    fun getPublicData(): Data {
        // 公开接口
    }
}
```

## 高级特性

### 自定义 OpenAPI 配置

```kotlin
@Configuration
class CustomOpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("自定义 API 文档")
                    .version("1.0")
            )
            .addSecurityItem(
                SecurityRequirement().addList("bearer-jwt")
            )
    }
}
```

### 自定义 OpenAPI 定制器

```kotlin
@Component
class CustomOpenApiCustomizer : OpenApiCustomizer {
    override fun customise(openApi: OpenAPI) {
        // 自定义 OpenAPI 对象
        openApi.info.description = "添加自定义描述"
    }
}
```

### 分组 API 文档

```kotlin
@Configuration
class ApiGroupConfig {

    @Bean
    fun publicApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("public")
            .pathsToMatch("/api/public/**")
            .build()
    }

    @Bean
    fun adminApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("admin")
            .pathsToMatch("/api/admin/**")
            .build()
    }
}
```

## 技术实现

### 核心组件

1. **SpringDocAutoConfiguration**: 自动配置类,负责初始化 OpenAPI 相关 Bean
2. **SpringDocProperties**: 配置属性类,映射 `springdoc` 前缀的配置
3. **OpenApiHandler**: 自定义 OpenAPI 处理器,扩展 SpringDoc 默认行为
4. **DocInitPrinter**: 初始化日志打印接口

### 关键特性实现

#### 上下文路径处理

模块会自动读取 `server.servlet.context-path` 配置,为所有 API 路径添加前缀:

```kotlin
@Bean
fun openApiCustomizer(): OpenApiCustomizer {
    val contextPath = serverProperties.servlet.contextPath
    return OpenApiCustomizer { openApi ->
        val newPaths = PlusPaths()
        openApi.paths?.forEach { (path, pathItem) ->
            newPaths.addPathItem(contextPath + path, pathItem)
        }
        openApi.paths = newPaths
    }
}
```

#### Javadoc 标签支持

通过 `therapi-runtime-javadoc` 读取类注释,提取第一行作为标签名:

```kotlin
override fun buildTags(handlerMethod: HandlerMethod, ...): Operation {
    if (javadocProvider.isPresent) {
        val description = javadocProvider.get().getClassJavadoc(handlerMethod.beanType)
        if (StringUtils.isNotBlank(description)) {
            val lines = IoUtil.readLines(StringReader(description), ArrayList())
            val tag = Tag()
            tag.name = lines[0]  // 使用第一行作为标签名
            tag.description = description
            openAPI.addTagsItem(tag)
        }
    }
}
```

## 常见问题

### 1. 如何隐藏某些接口不在文档中显示?

使用 `@Hidden` 注解:

```kotlin
@Hidden
@GetMapping("/internal")
fun internalApi() { }
```

### 2. 如何自定义 Swagger UI 路径?

```yaml
springdoc:
  swagger-ui:
    path: /api-docs  # 自定义 Swagger UI 路径
```

### 3. 如何在生产环境禁用文档?

```yaml
springdoc:
  api-docs:
    enabled: false
```

或者使用 Profile:

```yaml
spring:
  profiles: prod
springdoc:
  api-docs:
    enabled: false
```

### 4. 如何配置请求/响应示例?

```kotlin
@PostMapping
@Operation(
    summary = "创建用户",
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = @Content(
            examples = @ExampleObject(
                name = "示例用户",
                value = """{"name":"张三","age":25}"""
            )
        )
    )
)
fun createUser(@RequestBody user: User): User { }
```

## 依赖版本

- SpringDoc OpenAPI: 2.6.0
- Therapi Javadoc: 0.15.0
- Hutool: 5.8.39
- Apache Commons Lang3: (Spring Boot 管理版本)

## 注意事项

1. **性能考虑**: 在生产环境建议禁用 API 文档或限制访问权限
2. **安全配置**: 确保 Swagger UI 不暴露敏感信息
3. **Javadoc 配置**: 使用 Therapi Javadoc 需要在编译时包含 Javadoc 信息
4. **上下文路径**: 模块会自动处理上下文路径,无需手动配置

## 相关链接

- [SpringDoc OpenAPI 官方文档](https://springdoc.org/)
- [OpenAPI 3.0 规范](https://swagger.io/specification/)
- [Swagger UI 使用指南](https://swagger.io/tools/swagger-ui/)
