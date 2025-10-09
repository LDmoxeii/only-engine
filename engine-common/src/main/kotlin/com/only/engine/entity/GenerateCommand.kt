package com.only.engine.entity

import cn.hutool.captcha.generator.CodeGenerator
import cn.hutool.captcha.generator.RandomGenerator
import com.only.engine.enums.CaptchaCategory
import com.only.engine.enums.CaptchaChannel
import com.only.engine.enums.CaptchaType
import com.only.engine.misc.UnsignedMathGenerator


data class GenerateCommand(
    val bizType: String,
    val type: CaptchaType,
    val category: CaptchaCategory = CaptchaCategory.LINE,
    val charsetPolicy: CharsetPolicy = CharsetPolicy.MATH,
    val channel: CaptchaChannel,
    val length: Int = 4,
    val width: Int? = null,
    val height: Int? = null,
    val ttlSeconds: Long = 300,
    val targets: List<String> = emptyList(),    // 手机 / 邮箱
    val templateCode: String? = null,
    val metadata: Map<String, Any?> = emptyMap(),
) {
    enum class CharsetPolicy(
        val clazz: Class<out CodeGenerator>,
    ) {
        /**
         * 数字
         */
        MATH(UnsignedMathGenerator::class.java),

        /**
         * 字符
         */
        CHAR(RandomGenerator::class.java)
    }
}
