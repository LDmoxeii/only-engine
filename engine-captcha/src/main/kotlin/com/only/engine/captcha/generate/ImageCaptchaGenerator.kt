package com.only.engine.captcha.generate

import cn.hutool.core.util.ReflectUtil
import com.only.engine.entity.CaptchaContent
import com.only.engine.entity.GenerateCommand
import com.only.engine.enums.CaptchaType
import com.only.engine.spi.captcha.CaptchaGenerator
import org.springframework.expression.spel.standard.SpelExpressionParser
import java.awt.Color
import java.awt.Font

class ImageCaptchaGenerator : CaptchaGenerator {

    companion object {
        private val BACKGROUND = Color.LIGHT_GRAY
        private val FONT = Font("Arial", Font.BOLD, 48)
    }

    override fun supports(type: CaptchaType) = type == CaptchaType.IMAGE

    override fun generate(cmd: GenerateCommand): CaptchaContent {
        val width = cmd.width ?: 120
        val height = cmd.height ?: 40
        val length = cmd.length
        val charsetPolicy = cmd.charsetPolicy

        val isMath = charsetPolicy == GenerateCommand.CharsetPolicy.MATH

        val codeGenerator = ReflectUtil.newInstance(charsetPolicy.clazz, length)
        val captcha = ReflectUtil.newInstance(cmd.category.clazz, width, height)
        captcha.setBackground(BACKGROUND)
        captcha.setFont(FONT)
        captcha.generator = codeGenerator

        var code = captcha.code
        if (isMath) {
            val parser = SpelExpressionParser()
            val exp = parser.parseExpression(code.replace("=", ""))
            code = exp.getValue(String::class.java) ?: code
        }
        val imageBytes = captcha.imageBytes
        return CaptchaContent.Image(imageBytes, text = code)
    }
}
