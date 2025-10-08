package com.only.engine.jimmer.config

import com.only.engine.jimmer.JimmerInitPrinter
import org.babyfish.jimmer.jackson.ImmutableModule
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean

/**
 * Jimmer 自动配置类
 *
 * 当满足以下条件时生效：
 * 1. ImmutableModule 类存在于 classpath 中
 * 2. only.jimmer.enabled 属性为 true（默认为 true）
 */
@AutoConfiguration
@ConditionalOnClass(ImmutableModule::class)
@ConditionalOnProperty(prefix = "only.jimmer", name = ["enable"], havingValue = "true")
class JimmerAutoConfiguration : JimmerInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(JimmerAutoConfiguration::class.java)
    }

    /**
     * 注册 Jimmer ObjectMapper Builder 定制器
     *
     * 该定制器会被 Spring Boot 自动发现并应用到所有 ObjectMapper 实例，
     * 使 ObjectMapper 支持 Jimmer 不可变对象的序列化和反序列化
     */
    @Bean
    @ConditionalOnMissingBean
    fun jimmerJackson2ObjectMapperBuilderCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        printInit(JimmerObjectMapperBuilderCustomizer::class.java, log)
        return JimmerObjectMapperBuilderCustomizer()
    }
}
