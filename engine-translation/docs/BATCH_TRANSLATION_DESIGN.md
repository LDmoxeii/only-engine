# engine-translation 批量翻译技术方案（保持现有 JSON 序列化链路）

本文档描述在不新增 ResponseBodyAdvice 的前提下，为 engine-translation 增强“批量翻译聚合”能力的技术方案；并将“统一封装识别策略（优先处理
Result.data）”标记为后续迭代，不在本次实现范围内。

## 背景与目标

- 现状：当前通过 Jackson 注解 `@Translation` + `TranslationHandler` 在字段序列化时逐个调用
  `TranslationInterface.translation(key, other)`，在列表/分页等高基数场景，会产生大量重复调用。
- 目标：
    - 引入批量聚合能力，按 (type, other) 收集全部 key，优先走批量接口一次查全，降为 O(U) 级别（U 为去重后的 key
      数），剩余命中走上下文缓存。
    - 保持现有 JSON 序列化链路，不新增 ResponseBodyAdvice，不改变对外序列化语义（仍为“值替换”，非“追加冗余字段”）。
    - 与既有实现向下兼容：未实现批量接口的翻译实现类，回退为逐条调用，不影响行为。

## 设计约束

- 不新增 Web 层 ResponseBodyAdvice。
- 不改变 `@Translation` 的语义：仍然是“在序列化阶段替换字段值”。
- 尽量减少反射带来的开销，采用类级别元数据缓存 + 阈值控制。

## 核心方案

### 1. 批量接口（向下兼容）

- 新增接口（示意）：

  ```kotlin
  interface BatchTranslationInterface<T> {
      fun translationBatch(keys: Collection<Any>, other: String): Map<Any, T?>
  }
  ```

- 行为：若某个 `@TranslationType(type=...)` 的实现类同时实现了 `BatchTranslationInterface`，则批量阶段优先调用
  `translationBatch`，否则回退到单个 `translation` 调用。

### 2. 线程上下文缓存（单批次作用域）

- 新增 `TranslationContext`（基于 ThreadLocal）：
    - 缓存命中：`(type, other, key) -> value`。
    - 批量映射：`(type, other) -> Map<key, value>`。
    - 生命周期：一次“容器序列化”（列表/数组/Map）作为一个批次作用域，进入时 `beginScope()`，finally 中 `endScope()`
      清理，避免跨请求/跨容器污染。

### 3. 预收集容器序列化器

- 修改 `TranslationBeanSerializerModifier`，覆盖以下方法，返回“预收集包装”的序列化器：
    - `modifyCollectionSerializer`
    - `modifyMapSerializer`
    - `modifyArraySerializer`

- 包装器的通用流程：
    1) `beginScope()`；
    2) 预扫描容器元素，反射读取被 `@Translation` 标注的字段：
        - 如果注解 `mapper` 非空，则从当前 bean 的 `mapper` 指定属性读取源值；否则使用该字段值；
        - 按 `(type, other)` 分组 keys，去重；
    3) 对每一组 `(type, other)`：
        - 若实现为 `BatchTranslationInterface`，调用 `translationBatch(keys, other)` 一次拿到映射并写入上下文；
        - 否则回退逐个 `translation` 并写入上下文（可串行，降低复杂度）；
    4) 将实际序列化委托给原始序列化器；
    5) finally `endScope()`。

- 阈值控制：
    - 小集合（如 `size < threshold`，默认 8）不触发预收集，直接走现有序列化流程，避免预扫描成本浪费。

### 4. 字段序列化命中缓存

- 修改 `TranslationHandler.serialize`：
    - 优先从 `TranslationContext` 命中 `(type, other, key)`；
    - 未命中则按原逻辑调用单次 `translation`，并将结果回填到上下文，以提升同一容器后续命中率；
    - 保留异常降级：异常时输出原值，保证健壮性。

### 5. 类元数据缓存

- 新增 `BeanIntrospectCache`：缓存 `Class -> 待翻译字段元数据`（字段名、type、other、mapper 提取规则、读取/写出指针），降低重复反射开销。

## 配置项（建议）

- `only.engine.translation.batch.enable`：是否启用预收集批量翻译，默认 `true`。
- `only.engine.translation.batch.threshold`：触发预收集的集合大小阈值，默认 `8`。
- `only.engine.translation.batch.max-keys-per-group`：单组 `(type, other)` 最大 key 数，默认 `2000`（防止一次批量过大）。
- `only.engine.translation.batch.cache-enabled`：是否启用 ThreadLocal 缓存命中，默认 `true`。

> 以上配置通过 `TranslationAutoConfiguration` 读取并传入 `TranslationBeanSerializerModifier`/包装器。

## 兼容性与迁移

- 完全兼容现有 `TranslationInterface`；未实现批量接口的实现类按照既有方式逐条调用。
- 业务方可按需逐步为高频/高基数的翻译实现增加 `BatchTranslationInterface`，逐步获得收益。
- 不改变对外 JSON 结构，也不改变 `@Translation` 的注解语义。

## 性能预期

- 列表/分页的重复 key 场景：请求维度从 O(N) 次调用降为 O(U)（U 为去重 key 数），随后字段序列化直接命中上下文缓存。
- 小集合自动回避预收集（小于阈值），抑制额外反射扫描成本。

## 失败安全与可观测性

- 所有批量调用与单次调用异常均降级为“输出原值”。
- 在 debug 级别打印：
    - 本次批量分组数量、key 去重后数量、批量调用耗时；
    - 命中率统计（可选，按采样打印）。

## 代码改动清单（概要）

- 新增：
    - `core/BatchTranslationInterface.kt`：批量接口定义。
    - `core/TranslationContext.kt`：ThreadLocal 上下文与作用域管理。
    - `core/collector/PrecollectingCollectionSerializer.kt`
    - `core/collector/PrecollectingMapSerializer.kt`
    - `core/collector/PrecollectingArraySerializer.kt`
    - `core/collector/BeanIntrospectCache.kt`

- 修改：
    - `core/handler/TranslationBeanSerializerModifier.kt`：覆盖 `modifyCollectionSerializer`/`modifyMapSerializer`/
      `modifyArraySerializer`，返回预收集包装器；读取配置开关与阈值。
    - `core/handler/TranslationHandler.kt`：增加上下文缓存命中与回填。

## 里程碑与发布计划

- M1（当前迭代）
    - 完成上述批量翻译能力与配置项；默认开启，阈值 8。
    - 为一个示例实现（如字典翻译）增加 `BatchTranslationInterface` 支持，进行压测评估收益。

- M2（后续迭代，暂不实现）：统一封装识别策略
    - 目标：进一步在序列化阶段“优先识别 Result.data”作为容器批次，扩大批量聚合收益；
    - 方案：可在 `TranslationBeanSerializerModifier` 的 `modifySerializer` 针对 `Result` 做定制包装器（不引入
      ResponseBodyAdvice），在进入 `data` 序列化前完成预收集；
    - 风险控制：严格限定类型与开关，避免影响非统一封装的响应结构。

## 验收标准

- 功能保持一致：不开启批量功能时，输出与现有逻辑完全一致。
- 性能提升：在 `N=1000`、重复度高的列表翻译场景，外部调用次数显著下降（接近去重后 key 数）。
- 稳定性：异常不影响响应，发生降级仍能输出原值；无内存泄漏（作用域退出清理到位）。

---

备注：本次实现仅覆盖“批量翻译聚合”；“统一封装识别策略（Result.data 优先）”作为后续迭代项，不在当前提交范围内。

