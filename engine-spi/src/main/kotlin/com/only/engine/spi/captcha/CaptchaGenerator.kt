package com.only.engine.spi.captcha

import com.only.engine.entity.CaptchaContent
import com.only.engine.entity.GenerateCommand
import com.only.engine.enums.CaptchaType


interface CaptchaGenerator {
    fun supports(type: CaptchaType): Boolean
    fun generate(cmd: GenerateCommand): CaptchaContent
}
