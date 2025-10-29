# Engine JSON 模块

JSON 序列化和反序列化模块，为 engine-web 提供完整的 JSON 处理功能。

## 功能特性

- **完整的 JSON 工具类**: 提供序列化、反序列化、类型转换等功能
- **大数字处理**: 自动处理超出 JS 安全整数范围的数字
- **时间格式化**: 统一的 LocalDateTime 格式化 (yyyy-MM-dd HH:mm:ss)
- **BigDecimal 序列化**: 自动转换为字符串避免精度问题
- **Kotlin 支持**: 完整的 Kotlin 类型支持
- **自动装配**: 当 JSON 功能存在于 classpath 时自动启用
- **条件配置**: 支持通过 `only.engine.json.enabled` 属性控制启用/禁用
- **JSON 校验注解**: 提供 `@JsonPattern` 校验 JSON 字符串格式（对象/数组/任意）

## 使用方式

### 1. 添加依赖

```kotlin
dependencies {
    implementation(project(":engine-json"))
}
```

### 2. 自动配置

当 `engine-json` 模块在 classpath 中时，会自动：

- 注册 `ObjectMapper` Bean
- 配置 BigNumberSerializer 处理大数字
- 设置 LocalDateTime 格式化
- 初始化 JsonMessageConverterUtils

### 3. 配置属性

```yaml
only:
  json:
    enabled: true  # 默认为 true，设置为 false 可禁用 JSON 支持
```

### 4. 使用示例

```kotlin
import com.only.engine.json.misc.JsonMessageConverterUtils

// 对象序列化
val json = JsonMessageConverterUtils.toJsonString(myObject)

// 字符串反序列化
val obj = JsonMessageConverterUtils.parseObject(json, MyClass::class.java)

// 数组反序列化
val list = JsonMessageConverterUtils.parseArray(jsonArray, MyClass::class.java)

// Map 反序列化
val map = JsonMessageConverterUtils.parseMap(jsonString)

// 复杂类型反序列化
val complexObj = JsonMessageConverterUtils.parseObject(json, object : TypeReference<List<MyClass>>() {})

// 字节数组反序列化
val obj = JsonMessageConverterUtils.parseObject(bytes, MyClass::class.java)

// 获取 ObjectMapper 实例
val objectMapper = JsonMessageConverterUtils.getObjectMapper()

// JSON 校验（Bean Validation）
data class MyForm(
  @JsonPattern(type = JsonType.OBJECT, message = "参数必须是 JSON 对象")
  val ext: String?
)
```

## 工作原理

1. `JsonAutoConfiguration` 自动配置 ObjectMapper
2. 集成 BigNumberSerializer 处理大数字
3. 配置 JavaTimeModule 处理时间类型
4. 设置时区和其他序列化选项
5. 初始化 JsonMessageConverterUtils 工具类
6. 提供 Bean Validation 注解 `@JsonPattern` 与校验器

## 大数字处理

- **Long/BigInteger**: 超出 JS Number.MAX_SAFE_INTEGER (9007199254740991) 范围时自动转为字符串
- **BigDecimal**: 自动转换为字符串保持精度

## 时间处理

- **LocalDateTime**: 统一格式 "yyyy-MM-dd HH:mm:ss"
- **时区**: 使用系统默认时区

## 条件装配

- `@ConditionalOnProperty`: 支持通过配置启用/禁用
- `@ConditionalOnMissingBean`: 允许自定义实现覆盖默认配置

## JSON 校验能力

- 注解：`@JsonPattern`
  - 参数：`type` 可选值 `ANY`/`OBJECT`/`ARRAY`
  - 默认消息：`不是有效的 JSON 格式`
- 工具方法：
  - `JsonUtils.isJson(String?)` 判断是否为合法 JSON
  - `JsonUtils.isJsonObject(String?)` 判断是否为 JSON 对象
  - `JsonUtils.isJsonArray(String?)` 判断是否为 JSON 数组
