package com.only.engine.translation.core

/**
 * 翻译接口（Kotlin）
 */
fun interface TranslationInterface<T> {
    /**
     * @param key 源键值（来自当前字段或 mapper 指定的属性）
     * @param other 附加参数（按需使用）
     */
    fun translation(key: Any, other: String): T?
}

