package com.only.engine.error

interface ErrorCode {
    val code: Int
    val name: String
    val message: String
    val category: ErrorCategory
}
