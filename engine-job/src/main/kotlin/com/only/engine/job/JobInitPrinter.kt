package com.only.engine.job

import com.only.engine.printer.InitPrinter

/**
 * Job 模块初始化打印接口
 *
 * 用于统一 Job 模块的初始化日志输出风格
 */
interface JobInitPrinter : InitPrinter {

    override fun moduleName(): String = "ENGINE_JOB"
}
