# Engine Translation 模块（Kotlin）

基于 Jackson 序列化阶段的注解式“值翻译”，用于将出参中的编码/ID 等转换为展示值（如名称/URL/标签）。

## 特性

- 注解驱动：在 Kotlin VO 上用 `@Translation(type, mapper, other)` 标注即可。
- 零侵入：仅在序列化时生效，不改动领域对象与存储结构。
- 可扩展：实现 `TranslationInterface<T>` 并用 `@TranslationType(type)` 注册即可生效。
- Kotlin 友好：基于 Kotlin 反射读取 `mapper` 属性，天然支持 Kotlin `val/var`。

## 快速开始

1) 依赖

```kotlin
implementation(project(":engine-translation"))
```

2) 在 VO 标注注解

```kotlin
import com.only.engine.translation.annotation.Translation

data class UserVo(
    val avatarId: Long?,
    @Translation(type = "oss_id_to_url", mapper = "avatarId")
    val avatarUrl: String? = null,
)
```

3) 提供翻译实现

```kotlin
import com.only.engine.translation.annotation.TranslationType
import com.only.engine.translation.core.TranslationInterface
import org.springframework.stereotype.Component

@TranslationType(type = "oss_id_to_url")
@Component
class OssIdToUrlTranslation : TranslationInterface<String> {
    override fun translation(key: Any, other: String): String? {
        val ids = key.toString() // 兼容 Long 或 逗号分隔字符串
        // TODO: 查询/拼接 URL
        return "https://cdn.example.com/$ids"
    }
}
```

## 参数说明

- `type`: 翻译类型标识，需与实现类上的 `@TranslationType.type` 一致。
- `mapper`: 可选。指定从同一对象的哪个属性读取源值；留空则使用当前字段值。
- `other`: 可选。附加参数，如字典类型、枚举类名等。

## 实现要点

- 自动装配：`TranslationAutoConfiguration` 会收集所有 `TranslationInterface` Bean，并注册到全局路由表。
- 序列化：`TranslationHandler` 在 Jackson 序列化阶段按 `type` 路由到实现，异常降级为原值输出。
- Null 处理：`TranslationBeanSerializerModifier` 保证 Null 也由翻译器统一处理行为。

## 典型用法

- 字典翻译：`type = "dict_type_to_label"`，`mapper = "statusCodeStr"`，`other = "sys_normal_disable"`。
- 用户/部门名：`type = "user_id_to_name" | "dept_id_to_name"`，`mapper` 指向 `id` 或 ID 列属性。
- 业务枚举：自定义 `enum_code_to_value`，`other` 传枚举类全名，按 `code->value` 规则翻译。

## 备注

- 本模块为基础设施，不内置具体业务翻译实现；你可以在任意模块中新增实现类并注册为 Spring Bean。

