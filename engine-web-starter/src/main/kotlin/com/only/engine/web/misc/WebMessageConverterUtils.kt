package com.only.engine.web.misc

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer

object WebMessageConverterUtils {
    val OBJECT_MAPPER = ObjectMapper().apply {
        val simpleModule = SimpleModule().apply {
            addSerializer(Long::class.java, ToStringSerializer.instance)
            addSerializer(java.lang.Long.TYPE, ToStringSerializer.instance)
        }
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
        registerModule(simpleModule)
        // TODO: Uncomment if you have a custom mixin for R
        // addMixIn(R::class.java, ResultMixIn::class.java);
    }

    fun toJsonString(`object`: Any?): String = OBJECT_MAPPER.writeValueAsString(`object`)
}
