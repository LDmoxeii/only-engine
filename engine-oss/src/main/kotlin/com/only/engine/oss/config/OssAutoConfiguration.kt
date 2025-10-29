package com.only.engine.oss.config

import com.only.engine.oss.core.OssClient
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(OssProperties::class)
@ConditionalOnProperty(prefix = "only.engine.oss", name = ["enable"], havingValue = "true")
class OssAutoConfiguration {
    companion object {
        private val log = LoggerFactory.getLogger(OssAutoConfiguration::class.java)
    }

    @Bean
    fun ossClient(props: OssProperties): OssClient {
        log.info("Initializing OssClient for bucket: {} endpoint: {}", props.bucketName, props.endpoint)
        return OssClient("default", props)
    }
}

