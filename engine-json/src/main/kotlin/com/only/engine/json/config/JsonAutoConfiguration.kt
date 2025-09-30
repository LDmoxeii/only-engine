package com.only.engine.json.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.only.engine.entity.Result
import com.only.engine.json.JsonInitPrinter
import com.only.engine.json.misc.JsonMessageConverterUtils
import com.only.engine.json.wrapper.ResultMixIn
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

/**
 * JSON 自动配置类
 *
 * 当满足以下条件时生效：
 * 1. only.json.enabled 属性为 true（默认为 true）
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "only.json", name = ["enabled"], matchIfMissing = true)
class JsonAutoConfiguration : JsonInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(JsonAutoConfiguration::class.java)
    }

    @Bean
    @ConditionalOnMissingBean
    fun objectMapper(
        builderCustomizers: List<Jackson2ObjectMapperBuilderCustomizer> = emptyList(),
    ): ObjectMapper {
        val simpleModule = SimpleModule().apply {
            addSerializer(Long::class.java, ToStringSerializer.instance)
            addSerializer(Long::class.javaObjectType, ToStringSerializer.instance)
        }

        val builder = Jackson2ObjectMapperBuilder.json()
            .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .featuresToEnable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
            .modules(
                KotlinModule.Builder().build(),
                simpleModule
            )
            .mixIn(Result::class.java, ResultMixIn::class.java)

        // 应用所有 Builder 定制器
        builderCustomizers.forEach { it.customize(builder) }

        val objectMapper = builder.build<ObjectMapper>()
        printInit(ObjectMapper::class.java, log)
        JsonMessageConverterUtils.OBJECT_MAPPER = objectMapper
        return objectMapper
    }
}
