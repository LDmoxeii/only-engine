package com.only.engine.json.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.only.engine.entity.Result
import com.only.engine.json.JsonInitPrinter
import com.only.engine.json.misc.JsonMessageConverterUtils
import com.only.engine.json.serializer.BigNumberSerializer
import com.only.engine.json.wrapper.ResultMixIn
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

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
        // 创建 JavaTimeModule 并配置序列化器
        val javaTimeModule = JavaTimeModule().apply {
            // 大数字处理
            addSerializer(Long::class.java, BigNumberSerializer.INSTANCE)
            addSerializer(Long::class.javaObjectType, BigNumberSerializer.INSTANCE)
            addSerializer(BigInteger::class.java, BigNumberSerializer.INSTANCE)
            addSerializer(BigDecimal::class.java, ToStringSerializer.instance)

            // LocalDateTime 格式化
            val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(dateTimeFormatter))
            addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer(dateTimeFormatter))
        }

        val builder = Jackson2ObjectMapperBuilder.json()
            .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .featuresToEnable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
            .modules(
                KotlinModule.Builder().build(),
                javaTimeModule
            )
            .mixIn(Result::class.java, ResultMixIn::class.java)
            .timeZone(TimeZone.getDefault())

        // 应用所有 Builder 定制器
        builderCustomizers.forEach { it.customize(builder) }

        val objectMapper = builder.build<ObjectMapper>()
        printInit(ObjectMapper::class.java, log)
        JsonMessageConverterUtils.OBJECT_MAPPER = objectMapper
        return objectMapper
    }
}
