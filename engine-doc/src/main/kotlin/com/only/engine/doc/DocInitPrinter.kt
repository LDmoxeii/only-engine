package com.only.engine.doc

import com.only.engine.printer.InitPrinter

/**
 * Doc 模块初始化打印接口
 *
 * 用于统一 Doc 模块的初始化日志输出风格
 */
interface DocInitPrinter : InitPrinter {

    override fun moduleName(): String = "ENGINE_DOC"
}
