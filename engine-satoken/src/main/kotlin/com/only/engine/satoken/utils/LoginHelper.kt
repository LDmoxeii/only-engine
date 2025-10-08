package com.only.engine.satoken.utils

import cn.dev33.satoken.session.SaSession
import cn.dev33.satoken.stp.SaLoginModel
import cn.dev33.satoken.stp.StpUtil
import com.only.engine.entity.UserInfo
import org.slf4j.LoggerFactory

/**
 * 登录助手类
 *
 * @author LD_moxeii
 */
object LoginHelper {

    private val log = LoggerFactory.getLogger(LoginHelper::class.java)

    const val ADMIN_USER_KEY = "adminUser"
    const val ADMIN_USER_ID = "adminUserId"
    const val USER_PERMISSIONS = "userPermissions"
    const val USER_ROLES = "userRoles"

    /**
     * 用户登录
     */
    @JvmStatic
    @JvmOverloads
    fun login(loginUser: UserInfo, model: SaLoginModel? = null) {
        val loginModel = model ?: SaLoginModel()

        StpUtil.login(
            loginUser.id,
            loginModel.setExtra(ADMIN_USER_ID, loginUser.id)
                .setExtra(USER_ROLES, loginUser.roles)
                .setExtra(USER_PERMISSIONS, loginUser.permissions)
        )
        StpUtil.getSession().set(ADMIN_USER_KEY, loginUser)
    }

    /**
     * 获取用户基于session
     */
    @JvmStatic
    fun getUserInfo(): UserInfo? {
        val session: SaSession? = try {
            StpUtil.getTokenSession()
        } catch (e: Exception) {
            return null
        }

        return if (session != null) {
            StpUtil.getSession().get(ADMIN_USER_KEY) as? UserInfo
        } else {
            null
        }
    }

    /**
     * 获取用户基于token
     */
    @JvmStatic
    fun getUserInfo(token: String): UserInfo? {
        val session: SaSession? = try {
            StpUtil.getTokenSessionByToken(token)
        } catch (e: Exception) {
            return null
        }

        return session?.get(ADMIN_USER_KEY) as? UserInfo
    }

    /**
     * 获取用户权限
     */
    @JvmStatic
    fun getPermissions(): List<String> {
        return try {
            val permissions = getExtra(USER_PERMISSIONS)
            if (permissions is List<*>) {
                permissions.filterIsInstance<String>()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 获取用户角色
     */
    @JvmStatic
    fun getRoles(): List<String> {
        return try {
            val roles = getExtra(USER_ROLES)
            if (roles is List<*>) {
                roles.filterIsInstance<String>()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 获取用户id
     */
    @JvmStatic
    fun getUserId(): Long? {
        return try {
            val userId = getExtra(ADMIN_USER_ID)
            when (userId) {
                is Long -> userId
                is Number -> userId.toLong()
                is String -> userId.toLongOrNull()
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取当前 Token 的扩展信息
     *
     * @param key 键值
     * @return 对应的扩展数据
     */
    private fun getExtra(key: String): Any? {
        return try {
            StpUtil.getExtra(key)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 检查当前用户是否已登录
     *
     * @return 结果
     */
    @JvmStatic
    fun <T> isLogin(clazz: Class<T>): Boolean {
        return try {
            getUserId() != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查当前用户是否已登录（无类型参数版本）
     *
     * @return 结果
     */
    @JvmStatic
    fun isLogin(): Boolean {
        return try {
            getUserId() != null
        } catch (e: Exception) {
            false
        }
    }
}
