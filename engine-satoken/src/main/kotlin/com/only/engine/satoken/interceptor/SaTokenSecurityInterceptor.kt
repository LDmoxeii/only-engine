package com.only.engine.satoken.interceptor

import cn.dev33.satoken.`fun`.SaFunction
import cn.dev33.satoken.`fun`.SaParamFunction
import cn.dev33.satoken.interceptor.SaInterceptor
import cn.dev33.satoken.router.SaRouter
import cn.dev33.satoken.stp.StpUtil
import cn.hutool.extra.spring.SpringUtil
import com.only.engine.security.handler.AllUrlHandler
import com.only.engine.spi.security.SecurityInterceptor

class SaTokenSecurityInterceptor : SecurityInterceptor, SaInterceptor(
    SaParamFunction {
        val allUrlHandler = SpringUtil.getBean(AllUrlHandler::class.java)
        SaRouter
            .match(allUrlHandler.urls)
            .check(SaFunction { StpUtil.checkLogin() })
    }
)
