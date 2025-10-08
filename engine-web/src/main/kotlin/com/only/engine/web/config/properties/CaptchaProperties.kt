package com.only.engine.web.config.properties

import com.only.engine.web.enums.CaptchaCategory
import com.only.engine.web.enums.CaptchaType
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 验证码 配置属性
 *
 * @author LD_moxeii
 */
@ConfigurationProperties(prefix = "captcha")
data class CaptchaProperties(
    /**
     * 是否启用验证码
     */
    var enable: Boolean = false,

    /**
     * 验证码类型
     */
    var type: CaptchaType = CaptchaType.MATH,

    /**
     * 验证码类别
     */
    var category: CaptchaCategory = CaptchaCategory.LINE,

    /**
     * 数字验证码位数
     */
    var numberLength: Int = 4,

    /**
     * 字符验证码长度
     */
    var charLength: Int = 4,
)
