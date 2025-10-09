package com.only.engine.entity

sealed class CaptchaContent {
    data class Image(val bytes: ByteArray, val mime: String = "image/png", val text: String) : CaptchaContent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Image) return false

            if (!bytes.contentEquals(other.bytes)) return false
            if (mime != other.mime) return false
            if (text != other.text) return false

            return true
        }

        override fun hashCode(): Int {
            var result = bytes.contentHashCode()
            result = 31 * result + mime.hashCode()
            result = 31 * result + text.hashCode()
            return result
        }
    }

    data class Text(val value: String) : CaptchaContent()
}
