package com.only.engine.web.misc

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.only.engine.entity.Result
import com.only.engine.web.wrapper.ResultMixIn

object WebMessageConverterUtils {
    val OBJECT_MAPPER = ObjectMapper().apply {
        val simpleModule = SimpleModule().apply {
            addSerializer(Long::class.java, ToStringSerializer.instance)
            addSerializer(java.lang.Long.TYPE, ToStringSerializer.instance)
        }
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
        registerKotlinModule()
        registerModule(simpleModule)
        addMixIn(Result::class.java, ResultMixIn::class.java)
    }

    fun toJsonString(`object`: Any?): String = OBJECT_MAPPER.writeValueAsString(`object`)
}
