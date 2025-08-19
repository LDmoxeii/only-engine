package com.only.engine.web.auth

interface AuthCheck {
    fun tokenAuthCheck(token: String): String
}
