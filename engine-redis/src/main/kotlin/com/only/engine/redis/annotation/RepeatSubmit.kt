package com.only.engine.redis.annotation

import java.util.concurrent.TimeUnit

/**
 * 防止表单重复提交注解
 *
 * @author LD_moxeii
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class RepeatSubmit(
    /**
     * 间隔时间，小于此时间视为重复提交
     */
    val interval: Int = 5000,

    /**
     * 时间单位
     */
    val timeUnit: TimeUnit = TimeUnit.MILLISECONDS,

    /**
     * 提示消息，支持国际化，格式为 {code}
     */
    val message: String = "{repeat.submit.message}",
)
