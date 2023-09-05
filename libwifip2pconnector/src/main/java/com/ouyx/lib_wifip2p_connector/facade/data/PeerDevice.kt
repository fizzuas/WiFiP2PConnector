/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.facade.data

import android.net.wifi.p2p.WifiP2pDevice


/**
 * 设备信息
 *
 * @author ouyx
 * @date 2023年08月31日 15时59分
 */
data class PeerDevice(val deviceName: String, val deviceAddress: String, val state: DeviceState)


fun getDeviceStatusDesc(deviceStatus: Int): String {
    return when (deviceStatus) {
        WifiP2pDevice.AVAILABLE -> "可用的"
        WifiP2pDevice.INVITED -> "邀请中"
        WifiP2pDevice.CONNECTED -> "已连接"
        WifiP2pDevice.FAILED -> "失败的"
        WifiP2pDevice.UNAVAILABLE -> "不可用的"
        else -> "未知"
    }
}
fun PeerDevice.getStatusDesc():String{
    return when (this.state) {
        DeviceState.AVAILABLE -> "可用的"
        DeviceState.INVITED -> "邀请中"
        DeviceState.CONNECTED -> "已连接"
        DeviceState.FAILED -> "失败的"
        DeviceState.UNAVAILABLE -> "不可用的"
        else -> "未知"
    }
}

fun getDeviceState(deviceStatus: Int): DeviceState {
    return when (deviceStatus) {
        WifiP2pDevice.AVAILABLE -> DeviceState.AVAILABLE
        WifiP2pDevice.INVITED -> DeviceState.INVITED
        WifiP2pDevice.CONNECTED -> DeviceState.CONNECTED
        WifiP2pDevice.FAILED -> DeviceState.FAILED
        WifiP2pDevice.UNAVAILABLE -> DeviceState.UNAVAILABLE
        else -> DeviceState.UNKNOWN
    }
}

