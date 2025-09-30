package com.only.engine.web

import com.only.engine.printer.InitPrinter

interface WebInitPrinter: InitPrinter {

    override fun moduleName(): String = "ENGINE_WEB_STARTER"
}
