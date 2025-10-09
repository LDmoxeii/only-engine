package com.only.engine.satoken.interceptor

import cn.dev33.satoken.`fun`.SaFunction
import cn.dev33.satoken.`fun`.SaParamFunction
import cn.dev33.satoken.interceptor.SaInterceptor
import cn.dev33.satoken.router.SaRouter
import cn.dev33.satoken.stp.StpUtil
import com.only.engine.collector.UrlCollector
import com.only.engine.spi.security.SecurityInterceptor

/**
 * Sa-Token 拦截器包装类
 * 实现 SecurityInterceptor 接口，会被 SecurityAutoConfiguration 自动注册
 */
class SaTokenSecurityInterceptor(
    private val urlCollector: UrlCollector,
) : SecurityInterceptor, SaInterceptor(SaParamFunction { handler ->
    SaRouter.match(urlCollector.urls).check(SaFunction { StpUtil.checkLogin() })
})
