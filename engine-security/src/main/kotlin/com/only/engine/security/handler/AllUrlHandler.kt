package com.only.engine.security.handler

import cn.hutool.extra.spring.SpringUtil
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

class AllUrlHandler : SmartInitializingSingleton {

    private val variablePattern = Regex("\\{(.*?)}")

    val urls: MutableList<String> = mutableListOf()

    override fun afterSingletonsInstantiated() {
        val set = LinkedHashSet<String>()
        val mapping = SpringUtil.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping::class.java)
        val map: Map<RequestMappingInfo, *> = mapping.handlerMethods
        map.keys.forEach { info ->
            info.pathPatternsCondition?.patterns?.forEach { pattern ->
                val normalized = variablePattern.replace(pattern.patternString, "*")
                set.add(normalized)
            }
        }
        urls.addAll(set)
    }
}
