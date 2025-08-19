package com.only.engine.web.filter

import com.only.engine.web.WebInitPrinter
import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.io.IOException

class RequestBodyWrapperFilter(
    private val skipPaths: Set<String>
) : Filter, WebInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(RequestBodyWrapperFilter::class.java)
    }

    init {
        printInit(RequestBodyWrapperFilter::class.java, log)
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
        servletRequest: ServletRequest,
        servletResponse: ServletResponse,
        filterChain: FilterChain
    ) {
        var request = servletRequest
        var response = servletResponse
        var needCopyBodyToResponse = false

        try {
            if (request is HttpServletRequest) {
                if (isNotSkipPath(request) && isNotFormPost(request)) {
                    if (request !is ContentCachingRequestWrapper) {
                        request = ContentCachingRequestWrapper(request)
                    }
                    if (response !is ContentCachingResponseWrapper) {
                        response = ContentCachingResponseWrapper(response as HttpServletResponse)
                        needCopyBodyToResponse = true
                    }
                }
            }

            filterChain.doFilter(request, response)
        } finally {
            if (needCopyBodyToResponse) {
                (response as ContentCachingResponseWrapper).copyBodyToResponse()
            }
        }
    }

    private fun isNotFormPost(request: HttpServletRequest): Boolean {
        val contentType = request.contentType
        return !(contentType != null &&
                 contentType.contains(MediaType.MULTIPART_FORM_DATA_VALUE) &&
                 HttpMethod.POST.matches(request.method))
    }

    private fun isNotSkipPath(request: HttpServletRequest): Boolean {
        return !skipPaths.contains(request.requestURI)
    }
}
