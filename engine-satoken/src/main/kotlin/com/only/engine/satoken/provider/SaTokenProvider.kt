package com.only.engine.satoken.provider

import cn.dev33.satoken.stp.StpUtil
import com.only.engine.spi.idempotent.TokenProvider

class SaTokenProvider : TokenProvider {

    override fun getToken(): String {
        return StpUtil.getTokenValue() ?: ""
    }

    override fun getTokenName(): String {
        return StpUtil.getTokenName()
    }
}
