package com.only.engine.spi.idempotent

/**
 * Token 提供者接口
 *
 * 用于获取当前请求的唯一标识 Token
 * 不同的认证框架可以提供不同的实现
 *
 * @author LD_moxeii
 */
interface TokenProvider {

    /**
     * 获取当前请求的 Token
     *
     * @return Token 值，如果不存在返回空字符串
     */
    fun getToken(): String

    /**
     * 获取 Token 的 Header 名称
     *
     * @return Token Header 名称，默认为 "Authorization"
     */
    fun getTokenName(): String = "Authorization"
}
