package com.only.engine.jimmer.config

import com.only.engine.jimmer.JimmerInitPrinter
import com.only.engine.jimmer.config.properties.JimmerProperties
import org.babyfish.jimmer.jackson.ImmutableModule
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

/**
 * Jimmer 自动配置类
 *
 * 当满足以下条件时生效：
 * 1. ImmutableModule 类存在于 classpath 中
 * 2. only.engine.web.enabled 属性为 true（默认为 true）
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "only.engine.jimmer", name = ["enable"], havingValue = "true")
@EnableConfigurationProperties(JimmerProperties::class)
class JimmerAutoConfiguration : JimmerInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(JimmerAutoConfiguration::class.java)
    }

    @Bean
    fun jimmerJackson2ObjectMapperBuilderCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        printInit("jimmerJackson2ObjectMapperBuilderCustomizer", log)

        return Jackson2ObjectMapperBuilderCustomizer { jacksonObjectMapperBuilder ->
            jacksonObjectMapperBuilder.modules(ImmutableModule())
        }

    }
}
