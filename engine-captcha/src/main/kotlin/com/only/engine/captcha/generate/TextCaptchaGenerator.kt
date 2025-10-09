package com.only.engine.captcha.generate

import com.only.engine.captcha.core.entity.CaptchaContent
import com.only.engine.captcha.core.entity.GenerateCommand
import com.only.engine.captcha.core.enums.CaptchaType
import com.only.engine.spi.captcha.CaptchaGenerator
import kotlin.random.Random

class TextCaptchaGenerator : CaptchaGenerator {
    override fun supports(type: CaptchaType) = type == CaptchaType.TEXT

    override fun generate(cmd: GenerateCommand): CaptchaContent {
        val chars = when (cmd.charsetPolicy) {
            GenerateCommand.CharsetPolicy.NUMERIC -> "0123456789"
            GenerateCommand.CharsetPolicy.ALPHA -> "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz"
            GenerateCommand.CharsetPolicy.MIXED -> "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz0123456789"
        }
        val value = (1..cmd.length).joinToString("") { chars[Random.nextInt(chars.length)].toString() }
        return CaptchaContent.Text(value)
    }
}
