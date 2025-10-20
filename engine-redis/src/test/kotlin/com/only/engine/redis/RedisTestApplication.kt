package com.only.engine.redis

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Redis 测试应用
 */
@SpringBootApplication
class RedisTestApplication

fun main(args: Array<String>) {
    runApplication<RedisTestApplication>(*args)
}
