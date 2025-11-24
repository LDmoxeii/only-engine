package com.only.engine.redis.config

import cn.dev33.satoken.dao.SaTokenDao
import cn.dev33.satoken.dao.auto.SaTokenDaoBySessionFollowObject
import com.only.engine.redis.RedisInitPrinter
import com.only.engine.redis.dao.OnlySaTokenDao
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean

/**
 * Sa-Token DAO 自动配置：使用 Redis + Caffeine 实现多级缓存
 */
@AutoConfiguration
@ConditionalOnClass(SaTokenDaoBySessionFollowObject::class)
@ConditionalOnProperty(
    name = ["only.engine.redis.enable", "only.engine.sa-token.enable"],
    havingValue = "true"
)
class SaTokenDaoAutoConfiguration : RedisInitPrinter {

    companion object {
        private val log = LoggerFactory.getLogger(SaTokenDaoAutoConfiguration::class.java)
    }

    @Bean
    @ConditionalOnMissingBean(SaTokenDao::class)
    fun onlySaTokenDao(): SaTokenDaoBySessionFollowObject {
        printInit(OnlySaTokenDao::class.java, log)
        return OnlySaTokenDao()
    }
}
