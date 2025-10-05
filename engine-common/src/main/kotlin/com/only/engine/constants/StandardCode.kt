package com.only.engine.constants

import com.only.engine.enums.HttpStatus

/**
 * 标准状态码定义
 * 使用sealed class提供类型安全，同时保持JVM兼容性
 */
sealed class StandardCode(
    @JvmField val code: Int,
    @JvmField val message: String
) {

    /**
     * 正常执行返回
     */
    data object Success : StandardCode(HttpStatus.OK.value * 100, "成功")

    /**
     * 用户端异常
     */
    sealed class UserSide(code: Int, message: String) : StandardCode(40000 + code, message) {

        companion object {
            const val EXCEPTION = 40000
            const val REGISTER_EXCEPTION = 40100
            const val NOT_AGREE_PRIVACY_AGREEMENT = REGISTER_EXCEPTION + 1

            const val USERNAME_INVALID = 40110
            const val USERNAME_ALREADY_EXISTS = USERNAME_INVALID + 1
            const val USERNAME_CONTAIN_SENSITIVE_WORDS = USERNAME_ALREADY_EXISTS + 1
            const val USERNAME_CONTAIN_SPECIAL_CHAR = USERNAME_CONTAIN_SENSITIVE_WORDS + 1

            const val PWD_INVALID = 40120
            const val PWD_LENGTH_INSUFFICIENT = PWD_INVALID + 1
            const val PWD_STRENGTH_INSUFFICIENT = PWD_LENGTH_INSUFFICIENT + 1

            const val VERIFICATION_CODE_INVALID = 40130
            const val VERIFICATION_CODE_SMS_INVALID = VERIFICATION_CODE_INVALID + 1
            const val VERIFICATION_CODE_EMAIL_INVALID = VERIFICATION_CODE_SMS_INVALID + 1

            const val VERIFY_BASIC_INFO_INVALID = 40150
            const val VERIFY_MOBILE_FORMAT_INVALID = VERIFY_BASIC_INFO_INVALID + 1
            const val VERIFY_ADDRESS_FORMAT_INVALID = VERIFY_MOBILE_FORMAT_INVALID + 1
            const val VERIFY_EMAIL_FORMAT_INVALID = VERIFY_ADDRESS_FORMAT_INVALID + 1

            const val LOGIN_EXCEPTION = 40200
            const val LOGIN_ACCOUNT_NOT_EXIST = LOGIN_EXCEPTION + 1
            const val LOGIN_ACCOUNT_FREEZE = LOGIN_ACCOUNT_NOT_EXIST + 1
            const val LOGIN_ACCOUNT_DISABLED = LOGIN_ACCOUNT_FREEZE + 1
            const val LOGIN_ACCOUNT_LOCKED = LOGIN_ACCOUNT_DISABLED + 1
            const val LOGIN_ACCOUNT_ALREADY_LOGIN = LOGIN_ACCOUNT_LOCKED + 1
            const val LOGIN_ACCOUNT_FIRST_LOGIN_NEED_CHANGE_PWD = LOGIN_ACCOUNT_ALREADY_LOGIN + 1

            const val LOGIN_PWD_NOT_MATCH = 40210
            const val LOGIN_PWD_NOT_MATCH_EXCEEDS_LIMIT = LOGIN_PWD_NOT_MATCH + 1

            const val LOGIN_TOKEN_EXCEPTION = 40230
            const val LOGIN_TOKEN_INVALID = LOGIN_TOKEN_EXCEPTION + 1
            const val LOGIN_TOKEN_EXPIRED = LOGIN_TOKEN_INVALID + 1

            const val LOGIN_VERIFICATION_CODE_NOT_MATCH = 40240
            const val LOGIN_VERIFICATION_CODE_NOT_MATCH_EXCEEDS_LIMIT = 40241

            const val UNAUTHORIZED = 40300
            const val UNAUTHORIZED_ACCESS_DENIED = UNAUTHORIZED + 1
            const val UNAUTHORIZED_AUTHORIZATION_EXPIRED = UNAUTHORIZED_ACCESS_DENIED + 1
            const val UNAUTHORIZED_BLACKLIST_USER = UNAUTHORIZED_AUTHORIZATION_EXPIRED + 1
            const val UNAUTHORIZED_TOKEN_INVALID = UNAUTHORIZED_BLACKLIST_USER + 1

            const val REQUEST_PARAMETER_EXCEPTION = 40400
            const val REQUEST_PARAMETER_REQUIRED_IS_NOT_NULL = REQUEST_PARAMETER_EXCEPTION + 1
            const val REQUEST_PARAMETER_OUT_OF_ALLOWED_RANGE = REQUEST_PARAMETER_REQUIRED_IS_NOT_NULL + 1
            const val REQUEST_PARAMETER_FORMAT_NOT_MATCH = REQUEST_PARAMETER_OUT_OF_ALLOWED_RANGE + 1
            const val REQUEST_PARAMETER_AMOUNT_EXCEEDS_LIMIT = REQUEST_PARAMETER_FORMAT_NOT_MATCH + 1
            const val REQUEST_PARAMETER_QUANTITY_EXCEEDS_LIMIT = REQUEST_PARAMETER_AMOUNT_EXCEEDS_LIMIT + 1
            const val REQUEST_PARAMETER_BATCH_QUANTITY_EXCEEDS_LIMIT = REQUEST_PARAMETER_QUANTITY_EXCEEDS_LIMIT + 1
            const val REQUEST_PARAMETER_JSON_PARSE_FAILED = REQUEST_PARAMETER_BATCH_QUANTITY_EXCEEDS_LIMIT + 1
            const val REQUEST_PARAMETER_USER_INPUT_INVALID = REQUEST_PARAMETER_JSON_PARSE_FAILED + 1
            const val REQUEST_PARAMETER_USER_OPERATE_EXCEPTION = REQUEST_PARAMETER_USER_INPUT_INVALID + 1

            const val REQUEST_EXCEPTION = 40500
            const val REQUEST_TIMES_EXCEEDS_LIMIT = REQUEST_EXCEPTION + 1
            const val REQUEST_CONCURRENT_EXCEEDS_LIMIT = REQUEST_TIMES_EXCEEDS_LIMIT + 1
            const val REQUEST_WAIT_FOR_OPERATION = REQUEST_CONCURRENT_EXCEEDS_LIMIT + 1
            const val REQUEST_WEB_SOCKET_CONNECT_EXCEPTION = REQUEST_WAIT_FOR_OPERATION + 1
            const val REQUEST_WEB_SOCKET_DISCONNECT = REQUEST_WEB_SOCKET_CONNECT_EXCEPTION + 1
            const val REQUEST_REPEAT = REQUEST_WEB_SOCKET_DISCONNECT + 1

            const val UPLOAD_EXCEPTION = 40600
            const val UPLOAD_FILE_TYPE_NOT_MATCH = UPLOAD_EXCEPTION + 1
            const val UPLOAD_FILE_TOO_LARGE = UPLOAD_FILE_TYPE_NOT_MATCH + 1
            const val UPLOAD_IMG_TOO_LARGE = UPLOAD_FILE_TOO_LARGE + 1
            const val UPLOAD_VIDEO_TOO_LARGE = UPLOAD_IMG_TOO_LARGE + 1
            const val UPLOAD_COMPRESSED_FILE_TOO_LARGE = UPLOAD_VIDEO_TOO_LARGE + 1
        }

        // 常用的用户端异常实例
        data object Exception : UserSide(0, "用户端异常")
        data object RegisterException : UserSide(100, "注册异常")
        data object NotAgreePrivacyAgreement : UserSide(101, "未同意隐私协议")
        data object UsernameInvalid : UserSide(110, "用户名无效")
        data object UsernameAlreadyExists : UserSide(111, "用户名已存在")
        data object PasswordInvalid : UserSide(120, "密码无效")
        data object PasswordLengthInsufficient : UserSide(121, "密码长度不足")
        data object RequestParameterException : UserSide(400, "请求参数异常")
        data object RequestParameterFormatNotMatch : UserSide(403, "请求参数格式不匹配")
        data object Unauthorized : UserSide(300, "未授权")
        data object UploadException : UserSide(600, "上传异常")
    }

    /**
     * 系统错误
     */
    sealed class SystemSide(code: Int, message: String) : StandardCode(50000 + code, message) {

        companion object {
            const val EXCEPTION = 50000
            const val EXECUTION_TIMEOUT = 50100
            const val RESOURCE_EXCEPTION = 50200
            const val OUT_OF_HARD_DISK = RESOURCE_EXCEPTION + 1
            const val OUT_OF_MEMORY = OUT_OF_HARD_DISK + 1
        }

        data object Exception : SystemSide(0, "系统异常")
        data object ExecutionTimeout : SystemSide(100, "执行超时")
        data object ResourceException : SystemSide(200, "资源异常")
        data object OutOfHardDisk : SystemSide(201, "硬盘空间不足")
        data object OutOfMemory : SystemSide(202, "内存不足")
    }

    /**
     * 第三方服务错误
     */
    sealed class ThirdParty(code: Int, message: String) : StandardCode(60000 + code, message) {

        companion object {
            const val EXCEPTION = 60000
            const val RPC_EXCEPTION = 60100
            const val RPC_SERVICE_NOT_FOUND = RPC_EXCEPTION + 1
            const val RPC_SERVICE_INTERFACE_NOT_FOUND = RPC_SERVICE_NOT_FOUND + 1
            const val RPC_EXECUTION_TIMEOUT = RPC_SERVICE_INTERFACE_NOT_FOUND + 1

            const val CACHE_SERVICE_EXCEPTION = 60200
            const val CACHE_KEY_LENGTH_EXCEEDS_LIMIT = CACHE_SERVICE_EXCEPTION + 1
            const val CACHE_VALUE_LENGTH_EXCEEDS_LIMIT = CACHE_KEY_LENGTH_EXCEEDS_LIMIT + 1
            const val CACHE_STORAGE_CAPACITY_IS_FULL = CACHE_VALUE_LENGTH_EXCEEDS_LIMIT + 1
            const val CACHE_UNSUPPORTED_DATA_FORMAT = CACHE_STORAGE_CAPACITY_IS_FULL + 1
            const val CACHE_SERVICE_TIMEOUT = CACHE_UNSUPPORTED_DATA_FORMAT + 1

            const val DATABASE_EXCEPTION = 60300
            const val DATABASE_SERVICE_TIMEOUT = DATABASE_EXCEPTION + 1
            const val DATABASE_TABLE_NOT_EXIST = DATABASE_SERVICE_TIMEOUT + 1
            const val DATABASE_COLUMN_NOT_EXIST = DATABASE_TABLE_NOT_EXIST + 1
            const val DATABASE_DEADLOCK = DATABASE_COLUMN_NOT_EXIST + 1
            const val DATABASE_PRIMARY_KEY_CONFLICT = DATABASE_DEADLOCK + 1
            const val DATABASE_MULTI_TABLE_ASSOCIATION_EXIST_SAME_COLUMNS_NAME = DATABASE_PRIMARY_KEY_CONFLICT + 1
            const val DATABASE_BAD_SQL_GRAMMAR = DATABASE_MULTI_TABLE_ASSOCIATION_EXIST_SAME_COLUMNS_NAME + 1

            const val MQ_EXCEPTION = 60400
            const val MQ_DELIVERY_EXCEPTION = MQ_EXCEPTION + 1
            const val MQ_CONSUME_EXCEPTION = MQ_DELIVERY_EXCEPTION + 1
            const val MQ_SUBSCRIPTION_EXCEPTION = MQ_CONSUME_EXCEPTION + 1
            const val MQ_GROUP_NOT_FOUND = MQ_SUBSCRIPTION_EXCEPTION + 1
            const val MQ_DELIVERY_TIMEOUT = MQ_GROUP_NOT_FOUND + 1

            const val CONFIG_SERVICE_EXCEPTION = 60500
            const val CONFIG_SERVICE_TIMEOUT = CONFIG_SERVICE_EXCEPTION + 1

            const val NOTICE_SERVICE_EXCEPTION = 60600
            const val NOTICE_SERVICE_SMS_NOTICE_EXCEPTION = NOTICE_SERVICE_EXCEPTION + 1
            const val NOTICE_SERVICE_VOICE_NOTICE_EXCEPTION = NOTICE_SERVICE_SMS_NOTICE_EXCEPTION + 1
            const val NOTICE_SERVICE_EMAIL_EXCEPTION = NOTICE_SERVICE_VOICE_NOTICE_EXCEPTION + 1

            const val DISASTER_RECOVERY_EXCEPTION = 60700
            const val DISASTER_RECOVERY_RATE_LIMIT = DISASTER_RECOVERY_EXCEPTION + 1
            const val DISASTER_RECOVERY_DEGRADED = DISASTER_RECOVERY_RATE_LIMIT + 1
        }

        data object Exception : ThirdParty(0, "第三方服务异常")
        data object RpcException : ThirdParty(100, "RPC调用异常")
        data object RpcServiceNotFound : ThirdParty(101, "RPC服务未找到")
        data object RpcExecutionTimeout : ThirdParty(103, "RPC执行超时")
        data object CacheServiceException : ThirdParty(200, "缓存服务异常")
        data object CacheServiceTimeout : ThirdParty(205, "缓存服务超时")
        data object DatabaseException : ThirdParty(300, "数据库异常")
        data object DatabaseServiceTimeout : ThirdParty(301, "数据库服务超时")
        data object DatabaseDeadlock : ThirdParty(304, "数据库死锁")
        data object MqException : ThirdParty(400, "消息队列异常")
        data object ConfigServiceException : ThirdParty(500, "配置服务异常")
        data object NoticeServiceException : ThirdParty(600, "通知服务异常")
        data object DisasterRecoveryException : ThirdParty(700, "容灾异常")
    }
}
