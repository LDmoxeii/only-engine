# engine-oss 后续迭代增强（Roadmap)

面向 OSS/S3 能力的增强规划，按优先级与复杂度拆分，便于逐步落地。

## 1. 兼容性与可配置增强

- Path-Style 与 Virtual-Host 切换
  - 新增配置：`only.engine.oss.path-style=true|false`，针对 MinIO/自建端点常见的路径风格兼容问题。
- 多环境域名拼装策略
  - 根据 `domain/endpoint/https` 自动选择公开 URL 构造策略，完善边界与校验提示。

## 2. 多配置/租户化支持

- 配置池与工厂
  - 参考 RuoYi `OssFactory` 思路，引入多配置池，按 `configKey` 或租户维度区分实例，带本地缓存与变更感知。
- 默认配置动态刷新
  - 支持从外部（DB/Redis/配置中心）拉取默认配置并切换，提供事件回调与日志告警。

## 3. 上传下载能力升级

- 预签名 PUT/POST（浏览器直传）
  - 生成带策略的预签名表单/URL，前端直传，后端仅校验回调签名与元信息。
- Multipart 分片上传
  - 大文件并行分片（多线程/分块大小可配），失败重试与断点续传。
- 异步/高吞吐下载
  - 基于 TransferManager/CRT 的异步下载（Backpressure/流控），适配大文件回传。

## 4. 元数据与安全

- 完整的 Content-Type/MD5/ETag 处理
  - 自动探测/强制指定 Content-Type；可选 MD5 校验，提高数据一致性。
- 私有桶访问策略
  - 更细粒度的临时授权（自定义 Header、IP 限制、有效期策略等）。
- 服务器端加密（SSE/SSE-C）
  - 支持 `SSE-S3`/`SSE-KMS`/`SSE-C` 等加密方式。

## 5. 生态与可观测性

- 指标与链路
  - 上传/下载/删除耗时、错误率、重试次数等指标；集成 Micrometer。
- 日志与审计
  - 关键操作审计日志（操作者、对象键、IP、UA 等）。
- 本地开发支持
  - MinIO docker-compose 脚手架与初始化脚本；一键启动并配置。

## 6. API 设计与扩展

- 统一异常与错误码
  - 自定义异常体系（如 OssException）与标准错误码，便于网关与前端处理。
- SPI 与供应商适配层
  - 预留非 S3 协议供应商扩展点（如 Azure Blob、GCS）与跨云迁移策略。

## 7. 与平台其他模块协同（可选）

- 与 translation 模块联动
  - 提供 `ossId -> url` 的翻译实现（BatchTranslationInterface），用于批量渲染对象 URL。
- 与 web 模块联动
  - 提供统一的上传端点/回调验签工具、限流与鉴权策略模板。

## 里程碑建议

- M1：Path-Style 开关、多配置池与默认配置切换；预签名 PUT/POST；
- M2：分片并行上传与异步下载；指标/日志与本地 MinIO 脚手架；
- M3：SSE 加密、SPI 适配层与跨云策略；

---

以上为可演进清单，实际排期可按业务优先级灵活调整。

