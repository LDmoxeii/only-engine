package com.only.engine.web.filter

import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * HttpServletRequest wrapper that caches the body so it can be read multiple times.
 */
class CachedBodyHttpServletRequest(request: HttpServletRequest) : HttpServletRequestWrapper(request) {

    private val cachedBody: ByteArray
    private val requestCharset: Charset

    init {
        requestCharset = resolveCharset(request.characterEncoding)
        cachedBody = try {
            request.inputStream.use { it.readBytes() }
        } catch (ex: IOException) {
            throw IllegalStateException("Failed to read request body for caching", ex)
        }
    }

    override fun getInputStream(): ServletInputStream {
        val byteArrayInputStream = ByteArrayInputStream(cachedBody)
        return object : ServletInputStream() {
            override fun read(): Int = byteArrayInputStream.read()

            override fun isFinished(): Boolean = byteArrayInputStream.available() == 0

            override fun isReady(): Boolean = true

            override fun setReadListener(readListener: ReadListener?) {
                throw UnsupportedOperationException("Async read not supported")
            }
        }
    }

    override fun getReader(): BufferedReader {
        return BufferedReader(InputStreamReader(getInputStream(), requestCharset))
    }

    fun cachedBody(): ByteArray = cachedBody.copyOf()

    fun cachedBodyAsString(): String = String(cachedBody, requestCharset)

    private fun resolveCharset(encoding: String?): Charset {
        if (encoding.isNullOrBlank()) {
            return StandardCharsets.UTF_8
        }
        return try {
            Charset.forName(encoding)
        } catch (ex: Exception) {
            StandardCharsets.UTF_8
        }
    }
}

