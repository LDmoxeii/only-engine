package com.only.engine.security.url

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.util.regex.Pattern

class UrlCollector(
    private val requestMappingHandlerMapping: RequestMappingHandlerMapping,
) : InitializingBean {

    companion object {
        private val log = LoggerFactory.getLogger(UrlCollector::class.java)
        private val PATH_VARIABLE_PATTERN = Pattern.compile("\\{(.*?)}")
    }

    private val _urls = mutableSetOf<String>()
    val urls: List<String> get() = _urls.toList()

    override fun afterPropertiesSet() {
        collectUrls()
        log.info("Collected {} URLs for security checking", _urls.size)
        if (log.isDebugEnabled) {
            _urls.forEach { url -> log.debug("Security URL: {}", url) }
        }
    }

    private fun collectUrls() {
        val handlerMethods: Map<RequestMappingInfo, HandlerMethod> =
            requestMappingHandlerMapping.handlerMethods

        handlerMethods.keys.forEach { mappingInfo ->
            mappingInfo.pathPatternsCondition?.patterns?.forEach { pathPattern ->
                val url = pathPattern.patternString
                val normalizedUrl = normalizeUrl(url)
                _urls.add(normalizedUrl)
            }
        }
    }

    private fun normalizeUrl(url: String): String {
        // 将路径变量 {id} 替换为通配符 *
        return PATH_VARIABLE_PATTERN.matcher(url).replaceAll("*")
    }

    fun containsUrl(url: String): Boolean {
        return _urls.contains(url)
    }

    fun getMatchingUrls(pattern: String): List<String> {
        val regex = pattern.replace("*", ".*").toRegex()
        return _urls.filter { it.matches(regex) }
    }
}
