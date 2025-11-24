package com.only.engine.oss.constant

import com.only.engine.constants.GlobalConstants

/**
 * 对象存储常量
 */
object OssConstant {

    /** 默认配置 Key（存于 Redis） */
    const val DEFAULT_CONFIG_KEY: String = GlobalConstants.GLOBAL_REDIS_KEY + "sys_oss:default_config"

    /** 预览列表资源开关 Key */
    const val PREVIEW_LIST_RESOURCE_KEY: String = "sys.oss.previewListResource"

    /** OSS 配置缓存名称 */
    const val OSS_CONFIG_CACHE: String = "sys_oss_config"

    /** 系统数据 id 列表 */
    val SYSTEM_DATA_IDS: List<Long> = listOf(1L, 2L, 3L, 4L)

    /** 云服务商标识 */
    val CLOUD_SERVICE: Array<String> = arrayOf("aliyun", "qcloud", "qiniu", "obs")

    /** https 状态标识 */
    const val IS_HTTPS: String = "Y"
}
