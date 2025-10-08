package com.only.engine.spi.service

/**
 * 用户权限处理
 *
 * @author Lion Li
 */
interface PermissionService {

    /**
     * 获取角色数据权限
     *
     * @param userId 用户id
     * @return 角色权限信息
     */
    fun getRoles(userId: Long): List<String>

    /**
     * 获取菜单数据权限
     *
     * @param userId 用户id
     * @return 菜单权限信息
     */
    fun getPermission(userId: Long): List<String>
}
