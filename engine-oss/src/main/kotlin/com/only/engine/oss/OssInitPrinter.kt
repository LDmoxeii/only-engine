package com.only.engine.oss

import com.only.engine.printer.InitPrinter

interface OssInitPrinter : InitPrinter {
    override fun moduleName(): String = "ENGINE_OSS"
}
