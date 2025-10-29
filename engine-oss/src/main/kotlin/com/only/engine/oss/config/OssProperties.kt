package com.only.engine.oss.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "only.engine.oss")
class OssProperties {
    var enable: Boolean = false

    // Endpoint like https://s3.amazonaws.com or https://minio.local
    var endpoint: String = ""

    // Custom domain for public access, e.g., https://cdn.example.com
    var domain: String = ""

    // Object key prefix, e.g., uploads
    var prefix: String = "uploads"

    var accessKey: String = ""
    var secretKey: String = ""
    var bucketName: String = ""
    var region: String = ""

    // Whether to build public URL with https when domain is used
    var https: Boolean = true
}

