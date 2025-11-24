package com.only.engine.oss.enums

import com.only.engine.exception.KnownException
import software.amazon.awssdk.services.s3.model.BucketCannedACL
import software.amazon.awssdk.services.s3.model.ObjectCannedACL

/**
 * 桶访问策略配置
 */
enum class AccessPolicyType(
    val type: String,
    val bucketCannedACL: BucketCannedACL,
    val objectCannedACL: ObjectCannedACL,
) {
    PRIVATE("0", BucketCannedACL.PRIVATE, ObjectCannedACL.PRIVATE),
    PUBLIC("1", BucketCannedACL.PUBLIC_READ_WRITE, ObjectCannedACL.PUBLIC_READ_WRITE),
    CUSTOM("2", BucketCannedACL.PUBLIC_READ, ObjectCannedACL.PUBLIC_READ);

    companion object {
        private val enumMap: Map<String, AccessPolicyType> by lazy {
            AccessPolicyType.entries.associateBy { it.type }
        }

        fun valueOf(value: String?): AccessPolicyType {
            return valueOfOrNull(value) ?: throw KnownException("枚举类型AccessPolicyType枚举值转换异常，不存在的值: $value")
        }

        fun valueOfOrNull(value: String?): AccessPolicyType? {
            return enumMap[value]
        }
    }
}
