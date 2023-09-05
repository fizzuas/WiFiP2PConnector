/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.facade.data


/**
 *  发现对等设备 失败原因
 *
 * @author ouyx
 * @date 2023年08月30日 16时59分
 */
sealed class SearchFailType {

    /**
     * WiFi P2P 未启用
     */
    object P2PNotEnabled : SearchFailType()

    /**
     * 权限不够
     */
    object PermissionNotEnough : SearchFailType()

    /**
     * 运行该应用的设备不支持 WLAN 点对点。
     */
    object P2pUnsupported : SearchFailType()

    /**
     * 由于出现内部错误，操作失败
     */
    object ERROR : SearchFailType()

    /**
     * 系统太忙，无法处理请求
     */
    object BUSY : SearchFailType()

    /**
     * 未知错误
     */
    object UNKNOWN : SearchFailType()


}