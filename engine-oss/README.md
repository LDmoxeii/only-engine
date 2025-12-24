# engine-oss

Kotlin 版对象存储（OSS/S3）封装，基于 AWS SDK v2（S3 Async + TransferManager + 预签名 URL）。默认聚焦通用 S3 兼容实现（阿里云、腾讯云、七牛、MinIO
等端点均可兼容）。

## 安装

- settings.gradle.kts 已包含 `:engine-oss` 子模块；业务模块添加依赖：

```kotlin
dependencies {
    implementation(project(":engine-oss"))
}
```

## 开启与配置

模块会自动绑定 `OssProperties`，`OssFactory.instance()` 优先读取缓存配置，
若缓存缺失则回退使用本地 `OssProperties`。

application.yml 示例：

```yaml
only:
  engine:
    oss:
      endpoint: https://minio.example.com           # S3 兼容端点（或留空走公有云默认）
      domain: https://cdn.example.com               # 对外访问域名（用于拼接公开 URL，可留空）
      bucketName: my-bucket
      accessKey: xxxx
      secretKey: yyyy
      region: us-east-1                             # 区域（部分 S3 兼容实现可留空）
      prefix: uploads                               # 对象前缀
      https: true                                   # domain/endpoint 未带协议时默认使用 https
      accessPolicy: "0"
```

## 快速开始

通过 `OssFactory` 获取 `OssClient` 后直接调用：

```kotlin
@RestController
class DemoController {
    private val oss = com.only.engine.oss.factory.OssFactory.instance()

    @PostMapping("/upload-bytes")
    fun uploadBytes(@RequestBody data: ByteArray): String {
        val r = oss.uploadSuffix(data, ".png", contentType = "image/png")
        return r.url
    }

    @PostMapping("/upload-stream")
    fun uploadStream(input: InputStream, size: Long): String {
        val r = oss.uploadSuffix(input, ".mp4", contentLength = size, contentType = "video/mp4")
        return r.url
    }

    @GetMapping("/download")
    fun download(@RequestParam("key") keyOrUrl: String): ResponseEntity<InputStreamResource> {
        val ins = oss.getObjectContent(keyOrUrl)
        return ResponseEntity.ok().body(InputStreamResource(ins))
    }

    @DeleteMapping("/delete")
    fun delete(@RequestParam("key") keyOrUrl: String) {
        oss.delete(keyOrUrl)
    }

    @GetMapping("/private-url")
    fun privateUrl(@RequestParam("key") objectKey: String): String {
        return oss.getPrivateUrl(objectKey, Duration.ofMinutes(10))
    }
}
```

对象键与 URL：

- `publicUrlForKey(key)`：根据 `domain/endpoint` 拼装公开 URL（非云厂商端点会自动拼桶名）。
- `removeBaseUrl(urlOrKey)`：去除前缀，转为对象键，可复用 download/delete。
- `buildObjectKey(prefix, suffix)`：默认生成 `prefix/yyyy/MM/dd/uuid.suffix`。

## API 概览

- `UploadResult uploadSuffix(data: ByteArray, suffix: String, contentType: String?)`
- `UploadResult uploadSuffix(input: InputStream, suffix: String, contentLength: Long?, contentType: String?)`
- `UploadResult upload(file: File, suffix: String)`
- `InputStream getObjectContent(keyOrUrl: String)`
- `void delete(keyOrUrl: String)`
- `String getPrivateUrl(objectKey: String, expired: Duration)`
- `String createPresignedGetUrl(objectKey: String, expired: Duration)`
- `String createPresignedPutUrl(objectKey: String, expired: Duration, metadata: Map<String, String>)`
- `String publicUrlForKey(key: String)` / `String removeBaseUrl(path: String)` /
  `String buildObjectKey(prefix: String?, suffix: String)`

## 注意事项

- MinIO/自建端点：默认根据 `endpoint` 自动启用 path-style 访问（非云厂商标识即启用）；若遇到域名/路径风格兼容问题，可优先检查 `endpoint` 是否为实际服务地址。
- contentType：文件上传建议显式传入正确的 `contentType` 以便浏览器正确预览。
- 大文件/高并发：已使用 TransferManager；分片并行上传等增强仍在 Roadmap 规划中。

---

更多未来增强请见 `docs/ROADMAP.md`。
