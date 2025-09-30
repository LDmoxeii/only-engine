package com.only.engine.web.misc

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.only.engine.entity.Result
import com.only.engine.web.wrapper.ResultMixIn
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

object WebMessageConverterUtils {

    fun createObjectMapper(
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

        return builder.build()
    }

    lateinit var OBJECT_MAPPER: ObjectMapper

    fun toJsonString(`object`: Any?): String = OBJECT_MAPPER.writeValueAsString(`object`)
}
