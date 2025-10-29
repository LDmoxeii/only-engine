package com.only.engine.oss.core

data class UploadResult(
    val url: String,
    val filename: String,
    val eTag: String?,
)

