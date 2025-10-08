package com.only.engine.captcha.core

import com.only.engine.captcha.entity.CaptchaContent
import com.only.engine.captcha.entity.GenerateCommand
import com.only.engine.captcha.enums.CaptchaType

interface CaptchaGenerator {
    fun supports(type: CaptchaType): Boolean
    fun generate(cmd: GenerateCommand): CaptchaContent
}
