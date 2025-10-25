package com.only.engine.entity

sealed class CaptchaContent {
    data class Image(val bytes: String, val mime: String = "image/png", val text: String) : CaptchaContent()

    data class Text(val value: String) : CaptchaContent()
}
