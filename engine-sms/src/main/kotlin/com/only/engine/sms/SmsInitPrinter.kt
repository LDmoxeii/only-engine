package com.only.engine.sms

import com.only.engine.printer.InitPrinter

interface SmsInitPrinter : InitPrinter {
    override fun moduleName(): String = "ENGINE_SMS"
}
