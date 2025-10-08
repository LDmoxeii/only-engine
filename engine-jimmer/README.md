# Engine Jimmer 模块

Jimmer 集成模块，为 engine-web 提供 Jimmer 不可变对象的 JSON 序列化支持。

## 功能特性

- **自动装配**: 当 Jimmer 存在于 classpath 时自动启用
- **条件配置**: 支持通过 `only.engine.web.enabled` 属性控制启用/禁用
- **解耦设计**: engine-web 模块不再强依赖 Jimmer
- **扩展性**: 基于 ObjectMapperCustomizer 接口，便于扩展

## 使用方式

### 1. 添加依赖

```kotlin
dependencies {
    implementation(project(":engine-web"))
    implementation(project(":engine-jimmer"))  // 可选依赖
}
```

### 2. 自动配置

当 `engine-jimmer` 模块在 classpath 中时，会自动：

- 注册 `JimmerObjectMapperCustomizer` Bean
- 为 ObjectMapper 添加 `ImmutableModule` 支持

### 3. 配置属性

```yaml
only:
  jimmer:
    enabled: true  # 默认为 true，设置为 false 可禁用 Jimmer 支持
```

### 4. 使用示例

```kotlin
// 创建支持 Jimmer 的 ObjectMapper
val customizers = applicationContext.getBeansOfType(ObjectMapperCustomizer::class.java).values.toList()
val objectMapper = JsonMessageConverterUtils.createObjectMapper(customizers)

// 或使用默认实例（如果 engine-jimmer 在 classpath 中，会自动支持 Jimmer）
val json = JsonMessageConverterUtils.toJsonString(jimmerEntity)
```

## 工作原理

1. `JimmerAutoConfiguration` 检测到 `ImmutableModule` 类存在
2. 创建 `JimmerObjectMapperCustomizer` Bean
3. 当创建 ObjectMapper 时，自动应用所有定制器
4. Jimmer 不可变对象获得完整的序列化/反序列化支持

## 条件装配

- `@ConditionalOnClass(ImmutableModule::class)`: 只有当 Jimmer 存在时才生效
- `@ConditionalOnProperty`: 支持通过配置禁用
- `@ConditionalOnMissingBean`: 允许自定义实现覆盖默认配置
