package com.only.engine.web.enums

import cn.hutool.captcha.generator.CodeGenerator
import cn.hutool.captcha.generator.RandomGenerator
import com.only.engine.web.misc.UnsignedMathGenerator

/**
 * 验证码类型
 *
 * @author LD_moxeii
 */
enum class CaptchaType(
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
