package com.only.engine.web.misc

import cn.hutool.captcha.generator.CodeGenerator
import cn.hutool.core.math.Calculator
import cn.hutool.core.util.CharUtil
import cn.hutool.core.util.RandomUtil
import kotlin.math.max
import kotlin.math.min

/**
 * 无符号计算生成器
 *
 * @author LD_moxeii
 */
class UnsignedMathGenerator(
    /**
     * 参与计算数字最大长度
     */
    private val numberLength: Int = 2,
) : CodeGenerator {

    companion object {
        private const val serialVersionUID = -5514819971774091076L
        private const val OPERATORS = "+-*"
    }

    override fun generate(): String {
        val limit = getLimit()
        val a = RandomUtil.randomInt(limit)
        val b = RandomUtil.randomInt(limit)

        val maxNum = max(a, b).toString()
        val minNum = min(a, b).toString()

        // 使用 Kotlin 标准库替代 StringUtils.rightPad
        val maxPadded = maxNum.padEnd(numberLength, CharUtil.SPACE)
        val minPadded = minNum.padEnd(numberLength, CharUtil.SPACE)

        return "$maxPadded${RandomUtil.randomChar(OPERATORS)}$minPadded="
    }

    override fun verify(code: String, userInputCode: String): Boolean {
        val result: Int = try {
            userInputCode.toInt()
        } catch (e: NumberFormatException) {
            // 用户输入非数字
            return false
        }

        val calculateResult = Calculator.conversion(code).toInt()
        return result == calculateResult
    }

    /**
     * 获取验证码长度
     *
     * @return 验证码长度
     */
    fun getLength(): Int {
        return numberLength * 2 + 2
    }

    /**
     * 根据长度获取参与计算数字最大值
     *
     * @return 最大值
     */
    private fun getLimit(): Int {
        // 使用 Kotlin 标准库替代 StringUtils.repeat
        return "1${"0".repeat(numberLength)}".toInt()
    }
}
