package com.only.engine.oss.core

import com.only.engine.exception.KnownException
import com.only.engine.oss.config.OssProperties
import com.only.engine.oss.enums.AccessPolicyType
import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDate
import java.util.*

class OssClient(
    private val configKey: String,
    private val properties: OssProperties,
) {
    companion object {
        private val log = LoggerFactory.getLogger(OssClient::class.java)
    }

    private val region: Region = if (properties.region.isNotBlank()) Region.of(properties.region) else Region.AWS_GLOBAL
    private val credentials = StaticCredentialsProvider.create(
        AwsBasicCredentials.create(properties.accessKey, properties.secretKey)
    )

    private val s3: S3Client = S3Client.builder()
        .credentialsProvider(credentials)
        .region(region)
        .apply {
            if (properties.endpoint.isNotBlank()) {
                endpointOverride(URI.create(properties.endpoint))
            }
        }
        .build()

    private val presigner: S3Presigner = S3Presigner.builder()
        .credentialsProvider(credentials)
        .region(region)
        .apply {
            if (properties.endpoint.isNotBlank()) {
                endpointOverride(URI.create(properties.endpoint))
            }
        }
        .build()

    fun getPublicDomain(): String {
        if (properties.domain.isNotBlank()) return properties.domain.removeSuffix("/")
        if (properties.endpoint.isNotBlank()) return properties.endpoint.removeSuffix("/")
        return ""
    }

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
        val putReq = PutObjectRequest.builder()
            .bucket(properties.bucketName)
            .key(key)
            .contentType(contentType)
            .build()
        val resp = s3.putObject(putReq, file.toPath())
        val url = publicUrlForKey(key)
        return UploadResult(url = url, filename = key, eTag = resp.eTag())
    }

    fun upload(input: InputStream, key: String, contentLength: Long?, contentType: String? = null): UploadResult {
        val putReq = PutObjectRequest.builder()
            .bucket(properties.bucketName)
            .key(key)
            .apply { if (!contentType.isNullOrBlank()) contentType(contentType) }
            .build()
        val body = if (contentLength != null && contentLength >= 0) RequestBody.fromInputStream(
            input,
            contentLength
        ) else RequestBody.fromInputStream(input, input.available().toLong())
        val resp = s3.putObject(putReq, body)
        val url = publicUrlForKey(key)
        return UploadResult(url = url, filename = key, eTag = resp.eTag())
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
            s3.getObject(
                GetObjectRequest.builder().bucket(properties.bucketName).key(key).build(),
                tempFile
            )
            return tempFile
        } catch (e: Exception) {
            Files.deleteIfExists(tempFile)
            throw KnownException("文件下载失败，错误信息: ${e.message ?: "未知错误"}")
        }
    }

    fun download(keyOrUrl: String, out: OutputStream, contentLengthConsumer: ((Long) -> Unit)? = null) {
        val key = removeBaseUrl(keyOrUrl)
        val req = GetObjectRequest.builder()
            .bucket(properties.bucketName)
            .key(key)
            .build()
        try {
            s3.getObject(req).use { stream ->
                contentLengthConsumer?.invoke(stream.response().contentLength())
                stream.copyTo(out)
            }
        } catch (e: Exception) {
            throw KnownException("文件下载失败，错误信息: ${e.message ?: "未知错误"}")
        }
    }

    fun download(keyOrUrl: String, contentLengthConsumer: ((Long) -> Unit)? = null): (OutputStream) -> Unit {
        return { out -> download(keyOrUrl, out, contentLengthConsumer) }
    }

    fun delete(keyOrUrl: String) {
        val key = removeBaseUrl(keyOrUrl)
        s3.deleteObject(
            DeleteObjectRequest.builder().bucket(properties.bucketName).key(key).build()
        )
    }

    fun getPrivateUrl(objectKey: String, expired: Duration): String {
        val getReq = GetObjectRequest.builder()
            .bucket(properties.bucketName)
            .key(objectKey)
            .build()
        val presign = GetObjectPresignRequest.builder()
            .signatureDuration(expired)
            .getObjectRequest(getReq)
            .build()
        val url = presigner.presignGetObject(presign).url()
        return url.toString()
    }

    fun publicUrlForKey(key: String): String {
        val domain = getPublicDomain()
        return if (domain.isBlank()) key else "$domain/$key"
    }

    fun removeBaseUrl(path: String): String {
        val domain = getPublicDomain()
        return if (domain.isNotBlank() && path.startsWith(domain)) path.removePrefix("$domain/") else path.trimStart('/')
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

    private fun guessContentType(suffix: String): String? {
        val s = suffix.lowercase()
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
