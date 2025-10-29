package com.only.engine.json.misc

import cn.hutool.extra.spring.SpringUtil
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import java.io.IOException

/**
 * JSON 工具类
 *
 * 提供完整的 JSON 序列化和反序列化功能
 */
object JsonUtils {

    val OBJECT_MAPPER: ObjectMapper by lazy { SpringUtil.getBean(ObjectMapper::class.java) }

    /**
     * 将对象转换为JSON格式的字符串
     *
     * @param `object` 要转换的对象
     * @return JSON格式的字符串，如果对象为null，则返回null
     * @throws RuntimeException 如果转换过程中发生JSON处理异常，则抛出运行时异常
     */
    fun toJsonString(`object`: Any?): String? {
        if (`object` == null) {
            return null
        }
        return try {
            OBJECT_MAPPER.writeValueAsString(`object`)
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 将JSON格式的字符串转换为指定类型的对象
     *
     * @param text JSON格式的字符串
     * @param clazz 要转换的目标对象类型
     * @return 转换后的对象，如果字符串为空则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    fun <T> parseObject(text: String?, clazz: Class<T>): T? {
        if (text.isNullOrEmpty()) {
            return null
        }
        return try {
            OBJECT_MAPPER.readValue(text, clazz)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 将字节数组转换为指定类型的对象
     *
     * @param bytes 字节数组
     * @param clazz 要转换的目标对象类型
     * @return 转换后的对象，如果字节数组为空则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    fun <T> parseObject(bytes: ByteArray?, clazz: Class<T>): T? {
        if (bytes == null || bytes.isEmpty()) {
            return null
        }
        return try {
            OBJECT_MAPPER.readValue(bytes, clazz)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 将JSON格式的字符串转换为指定类型的对象，支持复杂类型
     *
     * @param text JSON格式的字符串
     * @param typeReference 指定类型的TypeReference对象
     * @return 转换后的对象，如果字符串为空则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    fun <T> parseObject(text: String?, typeReference: TypeReference<T>): T? {
        if (text.isNullOrBlank()) {
            return null
        }
        return try {
            OBJECT_MAPPER.readValue(text, typeReference)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 将JSON格式的字符串转换为Map对象
     *
     * @param text JSON格式的字符串
     * @return 转换后的Map对象，如果字符串为空或者不是JSON格式则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    fun parseMap(text: String?): Map<String, Any>? {
        if (text.isNullOrBlank()) {
            return null
        }
        return try {
            val mapType: JavaType = OBJECT_MAPPER.typeFactory.constructMapType(
                Map::class.java,
                String::class.java,
                Any::class.java
            )
            OBJECT_MAPPER.readValue(text, mapType)
        } catch (e: MismatchedInputException) {
            // 类型不匹配说明不是json
            null
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 将JSON格式的字符串转换为Map对象的列表
     *
     * @param text JSON格式的字符串
     * @return 转换后的Map对象的列表，如果字符串为空则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    fun parseArrayMap(text: String?): List<Map<String, Any>>? {
        if (text.isNullOrBlank()) {
            return null
        }
        return try {
            val listType: JavaType = OBJECT_MAPPER.typeFactory.constructCollectionType(
                List::class.java,
                OBJECT_MAPPER.typeFactory.constructMapType(
                    Map::class.java,
                    String::class.java,
                    Any::class.java
                )
            )
            OBJECT_MAPPER.readValue(text, listType)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 将JSON格式的字符串转换为指定类型对象的列表
     *
     * @param text JSON格式的字符串
     * @param clazz 要转换的目标对象类型
     * @return 转换后的对象的列表，如果字符串为空则返回空列表
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    fun <T> parseArray(text: String?, clazz: Class<T>): List<T> {
        if (text.isNullOrEmpty()) {
            return emptyList()
        }
        return try {
            val listType: JavaType = OBJECT_MAPPER.typeFactory.constructCollectionType(List::class.java, clazz)
            OBJECT_MAPPER.readValue(text, listType)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun compressJson(jsonStr: String): String {
        val tree = OBJECT_MAPPER.readTree(jsonStr)
        return OBJECT_MAPPER.writeValueAsString(tree)
    }

    /**
     * 判断字符串是否为合法 JSON（对象或数组）
     *
     * @param str 待校验字符串
     * @return true = 合法 JSON，false = 非法或空
     */
    fun isJson(str: String?): Boolean {
        if (str.isNullOrBlank()) return false
        return try {
            OBJECT_MAPPER.readTree(str)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 判断字符串是否为 JSON 对象（{}）
     */
    fun isJsonObject(str: String?): Boolean {
        if (str.isNullOrBlank()) return false
        return try {
            val node = OBJECT_MAPPER.readTree(str)
            node.isObject
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 判断字符串是否为 JSON 数组（[]）
     */
    fun isJsonArray(str: String?): Boolean {
        if (str.isNullOrBlank()) return false
        return try {
            val node = OBJECT_MAPPER.readTree(str)
            node.isArray
        } catch (_: Exception) {
            false
        }
    }
}
