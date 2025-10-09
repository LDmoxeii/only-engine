package com.only.engine.spi.captcha

import com.only.engine.captcha.core.entity.CaptchaContent
import com.only.engine.captcha.core.entity.GenerateCommand
import com.only.engine.captcha.core.enums.CaptchaType


interface CaptchaGenerator {
    fun supports(type: CaptchaType): Boolean
    fun generate(cmd: GenerateCommand): CaptchaContent
}
