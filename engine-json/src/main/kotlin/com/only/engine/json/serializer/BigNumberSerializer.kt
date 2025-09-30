package com.only.engine.json.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl
import com.fasterxml.jackson.databind.ser.std.NumberSerializer
import java.io.IOException

/**
 * 超出 JS 最大最小值处理
 *
 * 当数字超出 JavaScript Number.MAX_SAFE_INTEGER 和 Number.MIN_SAFE_INTEGER 范围时，
 * 将其序列化为字符串以避免精度丢失
 *
 * @author LD_moxeii
 */
@JacksonStdImpl
class BigNumberSerializer(rawType: Class<out Number>) : NumberSerializer(rawType) {

    companion object {
        /**
         * 根据 JS Number.MAX_SAFE_INTEGER 与 Number.MIN_SAFE_INTEGER 得来
         */
        private const val MAX_SAFE_INTEGER = 9007199254740991L
        private const val MIN_SAFE_INTEGER = -9007199254740991L

        /**
         * 提供实例
         */
        @JvmField
        val INSTANCE = BigNumberSerializer(Number::class.java)
    }

    @Throws(IOException::class)
    override fun serialize(value: Number, gen: JsonGenerator, provider: SerializerProvider) {
        // 超出范围序列化为字符串
        if (value.toLong() > MIN_SAFE_INTEGER && value.toLong() < MAX_SAFE_INTEGER) {
            super.serialize(value, gen, provider)
        } else {
            gen.writeString(value.toString())
        }
    }
}