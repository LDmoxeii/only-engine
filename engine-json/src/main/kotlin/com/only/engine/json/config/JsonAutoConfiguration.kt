package com.only.engine.json.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.only.engine.entity.Result
import com.only.engine.json.JsonInitPrinter
import com.only.engine.json.config.properties.JsonProperties
import com.only.engine.json.deserializer.CustomDateDeserializer
import com.only.engine.json.serializer.BigNumberSerializer
import com.only.engine.json.wrapper.ResultMixIn
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@AutoConfiguration
@ConditionalOnProperty(prefix = "only.engine.json", name = ["enable"], havingValue = "true")
@EnableConfigurationProperties(JsonProperties::class)
class JsonAutoConfiguration : JsonInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(JsonAutoConfiguration::class.java)
    }

    @Bean
    fun javaTimeModule(): Module {
        printInit(JavaTimeModule::class.java, log)

        // 全局配置序列化返回 JSON 处理
        val javaTimeModule = JavaTimeModule()
        javaTimeModule.addSerializer(Long::class.java, BigNumberSerializer.INSTANCE)
        javaTimeModule.addSerializer(Long::class.javaObjectType, BigNumberSerializer.INSTANCE)
        javaTimeModule.addSerializer(BigInteger::class.java, BigNumberSerializer.INSTANCE)
        javaTimeModule.addSerializer(BigDecimal::class.java, ToStringSerializer.instance)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        javaTimeModule.addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(formatter))
        javaTimeModule.addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer(formatter))
        javaTimeModule.addDeserializer(Date::class.java, CustomDateDeserializer())

        return javaTimeModule
    }

    @Bean
    fun customizer(): Jackson2ObjectMapperBuilderCustomizer {
        printInit("customizer", log)

        return Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .featuresToEnable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
                .mixIn(Result::class.java, ResultMixIn::class.java)
                .timeZone(TimeZone.getDefault())
        }
    }
}
