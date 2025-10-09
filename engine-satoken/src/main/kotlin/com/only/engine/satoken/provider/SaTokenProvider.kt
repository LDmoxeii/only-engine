package com.only.engine.satoken.provider

import cn.dev33.satoken.stp.StpUtil
import com.only.engine.spi.idempotent.TokenProvider

/**
 * Sa-Token 实现的 Token 提供者
 *
 * @author LD_moxeii
 */
class SaTokenProvider : TokenProvider {

    override fun getToken(): String {
        return StpUtil.getTokenValue() ?: ""
    }

    override fun getTokenName(): String {
        return StpUtil.getTokenName()
    }
}
