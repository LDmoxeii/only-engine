package com.only.engine.captcha

import com.only.engine.printer.InitPrinter

/**
 * Captcha 模块初始化打印接口
 *
 */
interface CaptchaInitPrinter : InitPrinter {

    override fun moduleName(): String = "ENGINE_CAPTCHA"
}
