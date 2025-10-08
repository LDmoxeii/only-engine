package com.only.engine.json.deserializer

import cn.hutool.core.date.DateUtil
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.util.*

/**
 * 自定义 Date 类型反序列化处理器（支持多种格式）
 *
 * @author AprilWind
 */
class CustomDateDeserializer : JsonDeserializer<Date>() {

    /**
     * 反序列化逻辑：将字符串转换为 Date 对象
     */
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Date? {
        val parse = DateUtil.parse(p.text) ?: return null
        return parse.toJdkDate()
    }
}
