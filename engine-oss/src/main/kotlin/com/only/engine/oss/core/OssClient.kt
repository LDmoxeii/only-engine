package com.only.engine.oss.core

import com.only.engine.exception.KnownException
import com.only.engine.oss.config.properties.OssProperties
import com.only.engine.oss.constant.OssConstant
import com.only.engine.oss.enums.AccessPolicyType
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.core.async.BlockingInputStreamAsyncRequestBody
import software.amazon.awssdk.core.async.ResponsePublisher
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.Delete
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import software.amazon.awssdk.transfer.s3.S3TransferManager
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest
import software.amazon.awssdk.transfer.s3.model.DownloadRequest
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest
import software.amazon.awssdk.transfer.s3.model.UploadRequest
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDate
import java.util.Locale
import java.util.UUID

class OssClient(
    private val configKey: String,
    private val properties: OssProperties,
) {
    private val region: Region = if (properties.region.isNotBlank()) Region.of(properties.region) else Region.AWS_GLOBAL
    private val credentials = StaticCredentialsProvider.create(
        AwsBasicCredentials.create(properties.accessKey, properties.secretKey)
    )
    private val pathStyleEnabled: Boolean = shouldUsePathStyle()

    private val s3: S3Client = S3Client.builder()
        .credentialsProvider(credentials)
        .region(region)
        .serviceConfiguration(
            S3Configuration.builder()
                .pathStyleAccessEnabled(pathStyleEnabled)
                .build()
        )
        .apply { endpointUri()?.let { endpointOverride(it) } }
        .build()

    private val asyncClient: S3AsyncClient = S3AsyncClient.builder()
        .credentialsProvider(credentials)
        .region(region)
        .serviceConfiguration(
            S3Configuration.builder()
                .pathStyleAccessEnabled(pathStyleEnabled)
                .build()
        )
        .apply { endpointUri()?.let { endpointOverride(it) } }
        .httpClient(
            NettyNioAsyncHttpClient.builder()
                .connectionTimeout(Duration.ofSeconds(60))
                .connectionAcquisitionTimeout(Duration.ofSeconds(30))
                .maxConcurrency(100)
                .maxPendingConnectionAcquires(1000)
                .build()
        )
        .build()

    private val transferManager: S3TransferManager = S3TransferManager.builder()
        .s3Client(asyncClient)
        .build()

    private val presigner: S3Presigner = S3Presigner.builder()
        .credentialsProvider(credentials)
        .region(region)
        .serviceConfiguration(
            S3Configuration.builder()
                .chunkedEncodingEnabled(false)
                .pathStyleAccessEnabled(pathStyleEnabled)
                .build()
        )
        .apply { presignEndpointUri()?.let { endpointOverride(it) } }
        .build()

    fun getPublicDomain(): String = resolveDomain()

    fun buildObjectKey(prefix: String?, suffix: String): String {
        val p = (prefix ?: properties.prefix).trim('/').ifEmpty { "uploads" }
        val today = LocalDate.now()
        val uuid = UUID.randomUUID().toString().replace("-", "")
        val cleanSuffix = if (suffix.startsWith(".")) suffix else ".$suffix"
        return "$p/${today.year}/${String.format("%02d", today.monthValue)}/${
            String.format(
                "%02d",
                today.dayOfMonth
            )
        }/$uuid$cleanSuffix"
    }

    fun uploadSuffix(data: ByteArray, suffix: String, contentType: String? = null): UploadResult {
        return upload(data.inputStream(), buildObjectKey(null, suffix), data.size.toLong(), contentType)
    }

    fun uploadSuffix(
        input: InputStream,
        suffix: String,
        contentLength: Long?,
        contentType: String? = null
    ): UploadResult {
        return upload(input, buildObjectKey(null, suffix), contentLength, contentType)
    }

    fun upload(file: File, suffix: String): UploadResult {
        val key = buildObjectKey(null, suffix)
        val contentType = guessContentType(suffix)
        return try {
            val request = UploadFileRequest.builder()
                .putObjectRequest(
                    PutObjectRequest.builder()
                        .bucket(properties.bucketName)
                        .key(key)
                        .apply { if (!contentType.isNullOrBlank()) contentType(contentType) }
                        .build()
                )
                .addTransferListener(LoggingTransferListener.create())
                .source(file.toPath())
                .build()
            val resp = transferManager.uploadFile(request).completionFuture().join()
            val url = publicUrlForKey(key)
            UploadResult(url = url, filename = key, eTag = resp.response().eTag())
        } catch (e: Exception) {
            throw KnownException("上传文件失败，错误信息: ${e.message ?: "未知错误"}")
        }
    }

    fun upload(input: InputStream, key: String, contentLength: Long?, contentType: String? = null): UploadResult {
        var inputStream = input
        var length = contentLength
        if (length == null || length < 0) {
            if (inputStream !is ByteArrayInputStream) {
                val bytes = inputStream.readBytes()
                inputStream = ByteArrayInputStream(bytes)
                length = bytes.size.toLong()
            } else {
                length = inputStream.available().toLong()
            }
        }
        return try {
            val body = BlockingInputStreamAsyncRequestBody.builder()
                .contentLength(length)
                .subscribeTimeout(Duration.ofSeconds(120))
                .build()
            val request = UploadRequest.builder()
                .requestBody(body)
                .addTransferListener(LoggingTransferListener.create())
                .putObjectRequest(
                    PutObjectRequest.builder()
                        .bucket(properties.bucketName)
                        .key(key)
                        .apply { if (!contentType.isNullOrBlank()) contentType(contentType) }
                        .build()
                )
                .build()
            val upload = transferManager.upload(request)
            body.writeInputStream(inputStream)
            val resp = upload.completionFuture().join()
            val url = publicUrlForKey(key)
            UploadResult(url = url, filename = key, eTag = resp.response().eTag())
        } catch (e: Exception) {
            throw KnownException("上传文件失败，错误信息: ${e.message ?: "未知错误"}")
        }
    }

    fun getObjectContent(keyOrUrl: String): InputStream {
        val key = removeBaseUrl(keyOrUrl)
        val req = GetObjectRequest.builder()
            .bucket(properties.bucketName)
            .key(key)
            .build()
        return s3.getObject(req)
    }

    fun fileDownload(path: String): Path {
        val key = removeBaseUrl(path)
        val tempFile = Files.createTempFile("oss-", ".tmp")
        try {
            val request = DownloadFileRequest.builder()
                .getObjectRequest(
                    GetObjectRequest.builder()
                        .bucket(properties.bucketName)
                        .key(key)
                        .build()
                )
                .addTransferListener(LoggingTransferListener.create())
                .destination(tempFile)
                .build()
            transferManager.downloadFile(request).completionFuture().join()
            return tempFile
        } catch (e: Exception) {
            Files.deleteIfExists(tempFile)
            throw KnownException("文件下载失败，错误信息: ${e.message ?: "未知错误"}")
        }
    }

    fun download(keyOrUrl: String, out: OutputStream, contentLengthConsumer: ((Long) -> Unit)? = null) {
        try {
            download(keyOrUrl, contentLengthConsumer).invoke(out)
        } catch (e: Exception) {
            throw KnownException("文件下载失败，错误信息: ${e.message ?: "未知错误"}")
        }
    }

    fun download(keyOrUrl: String, contentLengthConsumer: ((Long) -> Unit)? = null): (OutputStream) -> Unit {
        val key = removeBaseUrl(keyOrUrl)
        return try {
            val request: DownloadRequest<ResponsePublisher<GetObjectResponse>> = DownloadRequest.builder()
                .getObjectRequest(
                    GetObjectRequest.builder()
                        .bucket(properties.bucketName)
                        .key(key)
                        .build()
                )
                .addTransferListener(LoggingTransferListener.create())
                .responseTransformer(AsyncResponseTransformer.toPublisher())
                .build()
            val download = transferManager.download(request)
            val publisher = download.completionFuture().join().result()
            contentLengthConsumer?.invoke(publisher.response().contentLength())
            val writer: (OutputStream) -> Unit = { out: OutputStream ->
                publisher.subscribe { buffer: ByteBuffer ->
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    out.write(bytes)
                }.join()
            }
            writer
        } catch (e: Exception) {
            throw KnownException("文件下载失败，错误信息: ${e.message ?: "未知错误"}")
        }
    }

    fun delete(keyOrUrl: String) {
        val key = removeBaseUrl(keyOrUrl)
        s3.deleteObject(
            DeleteObjectRequest.builder().bucket(properties.bucketName).key(key).build()
        )
    }

    fun listKeysByPrefix(prefix: String): List<String> {
        val cleanPrefix = prefix.trim().trimStart('/')
        if (cleanPrefix.isBlank()) {
            throw KnownException.illegalArgument("prefix")
        }
        val results = mutableListOf<String>()
        var token: String? = null
        do {
            val resp = s3.listObjectsV2(
                ListObjectsV2Request.builder()
                    .bucket(properties.bucketName)
                    .prefix(cleanPrefix)
                    .continuationToken(token)
                    .maxKeys(1000)
                    .build()
            )
            results.addAll(resp.contents().map { it.key() })
            token = resp.nextContinuationToken()
        } while (token.isNullOrBlank().not())
        return results
    }

    fun downloadToFile(keyOrUrl: String, target: Path) {
        val key = removeBaseUrl(keyOrUrl)
        val req = GetObjectRequest.builder()
            .bucket(properties.bucketName)
            .key(key)
            .build()
        s3.getObject(req, ResponseTransformer.toFile(target))
    }

    fun deleteByPrefix(prefix: String): Int {
        val cleanPrefix = prefix.trim().trimStart('/').trimEnd('/') + "/"
        if (cleanPrefix == "/") {
            throw KnownException.illegalArgument("prefix")
        }
        var deletedCount = 0
        var token: String? = null
        do {
            val resp = s3.listObjectsV2(
                ListObjectsV2Request.builder()
                    .bucket(properties.bucketName)
                    .prefix(cleanPrefix)
                    .continuationToken(token)
                    .maxKeys(1000)
                    .build()
            )
            val keys = resp.contents().map { it.key() }.filter { it.isNotBlank() }
            if (keys.isNotEmpty()) {
                val deleteReq = DeleteObjectsRequest.builder()
                    .bucket(properties.bucketName)
                    .delete(
                        Delete.builder()
                            .objects(keys.map { ObjectIdentifier.builder().key(it).build() })
                            .build()
                    )
                    .build()
                val deleteResp = s3.deleteObjects(deleteReq)
                deletedCount += deleteResp.deleted().size
            }
            token = resp.nextContinuationToken()
        } while (token.isNullOrBlank().not())
        return deletedCount
    }

    fun getPrivateUrl(objectKey: String, expired: Duration): String {
        return createPresignedGetUrl(objectKey, expired)
    }

    fun createPresignedGetUrl(objectKey: String, expired: Duration): String {
        val getReq = GetObjectRequest.builder()
            .bucket(properties.bucketName)
            .key(objectKey)
            .build()
        val presign = GetObjectPresignRequest.builder()
            .signatureDuration(expired)
            .getObjectRequest(getReq)
            .build()
        return presigner.presignGetObject(presign).url().toString()
    }

    fun createPresignedPutUrl(objectKey: String, expired: Duration, metadata: Map<String, String> = emptyMap()): String {
        val putReq = PutObjectRequest.builder()
            .bucket(properties.bucketName)
            .key(objectKey)
            .apply { if (metadata.isNotEmpty()) metadata(metadata) }
            .build()
        val presign = PutObjectPresignRequest.builder()
            .signatureDuration(expired)
            .putObjectRequest(putReq)
            .build()
        return presigner.presignPutObject(presign).url().toString()
    }

    fun publicUrlForKey(key: String): String {
        val baseUrl = resolvePublicBaseUrl()
        return if (baseUrl.isBlank()) key else "$baseUrl/$key"
    }

    fun removeBaseUrl(path: String): String {
        val baseUrl = resolvePublicBaseUrl()
        val normalized = path.trim()
        if (baseUrl.isBlank()) {
            return normalized.trimStart('/')
        }
        if (normalized.startsWith(baseUrl)) {
            return normalized.removePrefix("$baseUrl/")
        }
        val baseNoScheme = stripScheme(baseUrl)
        val normalizedNoScheme = stripScheme(normalized)
        return if (normalizedNoScheme.startsWith(baseNoScheme)) {
            normalizedNoScheme.removePrefix("$baseNoScheme/").trimStart('/')
        } else {
            normalized.trimStart('/')
        }
    }

    fun checkPropertiesSame(other: OssProperties): Boolean {
        return properties.endpoint == other.endpoint &&
                properties.domain == other.domain &&
                properties.prefix == other.prefix &&
                properties.accessKey == other.accessKey &&
                properties.secretKey == other.secretKey &&
                properties.bucketName == other.bucketName &&
                properties.region == other.region &&
                properties.https == other.https &&
                properties.accessPolicy == other.accessPolicy &&
                properties.tenantId == other.tenantId
    }

    fun getAccessPolicy(): AccessPolicyType =
            AccessPolicyType.valueOf(properties.accessPolicy)

    private fun schemePrefix(): String = if (properties.https) "https://" else "http://"

    private fun hasScheme(value: String): Boolean {
        val v = value.lowercase(Locale.ROOT)
        return v.startsWith("http://") || v.startsWith("https://")
    }

    private fun normalizeWithScheme(value: String): String {
        val v = value.trim()
        if (v.isBlank()) return ""
        return if (hasScheme(v)) v else schemePrefix() + v
    }

    private fun stripScheme(value: String): String {
        val v = value.trim()
        return when {
            v.startsWith("http://", ignoreCase = true) -> v.substring(7)
            v.startsWith("https://", ignoreCase = true) -> v.substring(8)
            else -> v
        }
    }

    private fun resolveDomain(): String {
        val domain = properties.domain.trim()
        val endpoint = properties.endpoint.trim()
        val base = when {
            domain.isNotBlank() -> domain
            endpoint.isNotBlank() -> endpoint
            else -> ""
        }
        return if (base.isBlank()) "" else normalizeWithScheme(base).removeSuffix("/")
    }

    private fun resolvePublicBaseUrl(): String {
        val domain = properties.domain.trim()
        val endpoint = properties.endpoint.trim()
        if (domain.isBlank() && endpoint.isBlank()) return ""
        if (containsCloudService(endpoint)) {
            val base = if (domain.isNotBlank()) domain else "${properties.bucketName}.$endpoint"
            return normalizeWithScheme(base).removeSuffix("/")
        }
        val base = if (domain.isNotBlank()) domain else endpoint
        return normalizeWithScheme(base).removeSuffix("/") + "/" + properties.bucketName
    }

    private fun containsCloudService(endpoint: String): Boolean {
        val lower = endpoint.lowercase(Locale.ROOT)
        return OssConstant.CLOUD_SERVICE.any { lower.contains(it) }
    }

    private fun shouldUsePathStyle(): Boolean {
        val endpoint = properties.endpoint.trim()
        return endpoint.isNotBlank() && !containsCloudService(endpoint)
    }

    private fun endpointUri(): URI? {
        val endpoint = properties.endpoint.trim()
        if (endpoint.isBlank()) return null
        return URI.create(normalizeWithScheme(endpoint))
    }

    private fun presignEndpointUri(): URI? {
        val domain = resolveDomain()
        return if (domain.isBlank()) null else URI.create(domain)
    }

    private fun guessContentType(suffix: String): String? {
        val s = suffix.lowercase(Locale.ROOT)
        return when {
            s.endsWith(".png") -> "image/png"
            s.endsWith(".jpg") || s.endsWith(".jpeg") -> "image/jpeg"
            s.endsWith(".gif") -> "image/gif"
            s.endsWith(".webp") -> "image/webp"
            s.endsWith(".mp4") -> "video/mp4"
            s.endsWith(".mp3") -> "audio/mpeg"
            else -> null
        }
    }
}
