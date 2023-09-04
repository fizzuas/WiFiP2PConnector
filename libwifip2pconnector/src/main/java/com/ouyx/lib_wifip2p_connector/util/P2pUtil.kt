/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.util

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build


/**
 * P2P连接工具类
 *
 * @author ouyx
 * @date 2023年09月04日 10时38分
 */
object P2pUtil {

    /**
     * 使用WiFi P2P连接是否有权限
     */
    fun isPermission(application: Application): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isPermission(application, Manifest.permission.ACCESS_NETWORK_STATE)
                    && isPermission(application, Manifest.permission.CHANGE_NETWORK_STATE)
                    && isPermission(application, Manifest.permission.ACCESS_WIFI_STATE)
                    && isPermission(application, Manifest.permission.CHANGE_WIFI_STATE)
                    && isPermission(application, Manifest.permission.CHANGE_WIFI_STATE)
                    && isPermission(application, Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            isPermission(application, Manifest.permission.ACCESS_NETWORK_STATE) &&
                    isPermission(application, Manifest.permission.CHANGE_NETWORK_STATE) &&
                    isPermission(application, Manifest.permission.ACCESS_WIFI_STATE) &&
                    isPermission(application, Manifest.permission.CHANGE_WIFI_STATE) &&
                    isPermission(application, Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    isPermission(application, Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    /**
     * 判断是否拥有[permission]权限
     * @return true = 拥有该权限
     */
    private fun isPermission(application: Application, permission: String): Boolean {
        return application.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}