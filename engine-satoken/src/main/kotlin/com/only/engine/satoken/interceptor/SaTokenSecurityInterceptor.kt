package com.only.engine.satoken.interceptor

import cn.dev33.satoken.`fun`.SaParamFunction
import cn.dev33.satoken.interceptor.SaInterceptor
import cn.dev33.satoken.stp.StpUtil
import com.only.engine.spi.security.SecurityInterceptor

class SaTokenSecurityInterceptor(
) : SecurityInterceptor, SaInterceptor(SaParamFunction { _ -> StpUtil.checkLogin() })
