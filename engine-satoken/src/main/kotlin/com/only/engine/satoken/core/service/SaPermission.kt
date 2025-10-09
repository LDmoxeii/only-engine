package com.only.engine.satoken.core.service

import cn.dev33.satoken.stp.StpInterface
import com.only.engine.enums.UserType
import com.only.engine.exception.KnownException
import com.only.engine.satoken.utils.LoginHelper
import com.only.engine.spi.authentication.PermissionService
import org.springframework.beans.factory.ObjectProvider

/**
 * sa-token 权限管理实现类
 *
 * @author LD_moxeii
 */
class SaPermission(
    private val permissionService: ObjectProvider<PermissionService>,
) : StpInterface {

    fun getPermissionService(): PermissionService {
        requireNotNull(permissionService.ifAvailable) { KnownException("PermissionService 实例不存在") }
        return permissionService.ifAvailable!!
    }

    /**
     * 获取菜单权限列表
     */
    override fun getPermissionList(loginId: Any?, loginType: String?): List<String> {
        val userInfo = LoginHelper.getUserInfo()
        if (userInfo == null || userInfo.getLoginId() != loginId) {
            return getPermissionService().getPermission(loginId.toString().split(":")[1].toLong())
        }

        val userType = UserType.valueOf(userInfo.userType)

        return when (userType) {
            UserType.UNKNOWN -> emptyList()
            UserType.SYS_USER -> userInfo.permissions
        }
    }

    /**
     * 获取角色权限列表
     */
    override fun getRoleList(loginId: Any?, loginType: String?): List<String> {
        val userInfo = LoginHelper.getUserInfo()
        if (userInfo == null || userInfo.getLoginId() != loginId) {
            return getPermissionService().getRoles(loginId.toString().split(":")[1].toLong())
        }

        val userType = UserType.valueOf(userInfo.userType)

        return when (userType) {
            UserType.UNKNOWN -> emptyList()
            UserType.SYS_USER -> userInfo.permissions
        }
    }
}
