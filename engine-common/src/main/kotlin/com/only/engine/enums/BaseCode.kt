package com.only.engine.enums

interface BaseCode : BaseEnum<Int>, Message {

    override val desc: String
        get() = message
}
