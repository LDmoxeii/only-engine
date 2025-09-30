package com.only.engine.web.filter

import com.only.engine.web.WebInitPrinter
import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.io.IOException

class HealthCheckFilter : Filter, WebInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(HealthCheckFilter::class.java)
        const val BEAN_NAME = "healthCheckFilter"
    }

    init {
        printInit(HealthCheckFilter::class.java, log)
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        if (httpRequest.requestURI.contains("/actuator/health")) {
            log.debug("[WEB_STARTER_INIT]: receive health check")
            httpResponse.status = HttpStatus.OK.value()
            httpResponse.writer.write("{\"status\":\"UP\"}")
        } else {
            chain.doFilter(request, response)
        }
    }
}
