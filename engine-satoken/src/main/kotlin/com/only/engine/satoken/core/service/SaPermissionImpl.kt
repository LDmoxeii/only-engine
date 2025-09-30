package com.only.engine.satoken.core.service

import cn.dev33.satoken.stp.StpInterface
import com.only.engine.satoken.utils.LoginHelper

/**
 * sa-token 权限管理实现类
 *
 * @author LD_moxeii
 */
class SaPermissionImpl : StpInterface {

    /**
     * 获取菜单权限列表
     */
    override fun getPermissionList(loginId: Any?, loginType: String?): List<String> {
        return LoginHelper.getPermissions().mapNotNull { it.code }
    }

    /**
     * 获取角色权限列表
     */
    override fun getRoleList(loginId: Any?, loginType: String?): List<String> {
        return LoginHelper.getRoles().mapNotNull { it.name }
    }
}