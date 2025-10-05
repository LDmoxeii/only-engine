package com.only.engine.redis

import com.only.engine.printer.InitPrinter

/**
 * Redis 模块初始化打印接口
 *
 * 用于统一 Redis 模块的初始化日志输出风格
 */
interface RedisInitPrinter : InitPrinter {

    override fun moduleName(): String = "ENGINE_REDIS"
}
