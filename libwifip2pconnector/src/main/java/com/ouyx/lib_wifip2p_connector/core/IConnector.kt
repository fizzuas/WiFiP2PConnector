/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.core

import com.ouyx.lib_wifip2p_connector.facade.callback.SearchDevicesCallback
import com.ouyx.lib_wifip2p_connector.launch.WiFiP2PConnector


/**
 *  IConnector
 *
 * @author ouyx
 * @date 2023年08月30日 16时48分
 */
interface IConnector {

    fun getWiFiManager() = WiFiP2PConnector.get().mWiFiP2PManager

    fun getWiFiChannel() = WiFiP2PConnector.get().mWifiP2pChannel

    fun getOptions() = WiFiP2PConnector.get().mWiFiP2POptions

    fun getApplication() = WiFiP2PConnector.get().mApplication

    /**
     *  发现对等设备
     *  仅会通知您发现进程成功和失败，但不会提供有关其发现的实际对等设备（如有）的任何信息
     */
    fun searchDevices(callback: SearchDevicesCallback)

    /**
     * 连接设备
     */
    fun connect(address: String)

    /**
     * 断开连接
     */
    fun disConnect()


    /**
     * 释放资源
     */
    fun close()
}