package com.only.engine.json.misc

import com.fasterxml.jackson.databind.ObjectMapper

object JsonMessageConverterUtils {

    lateinit var OBJECT_MAPPER: ObjectMapper

    fun toJsonString(`object`: Any?): String = OBJECT_MAPPER.writeValueAsString(`object`)
}
