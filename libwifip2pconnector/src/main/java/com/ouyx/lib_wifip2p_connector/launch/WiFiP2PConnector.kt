/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.launch

import android.app.Application
import android.content.Context.WIFI_P2P_SERVICE
import android.net.wifi.p2p.WifiP2pManager
import com.ouyx.lib_wifip2p_connector.core.ConnectorImpl
import com.ouyx.lib_wifip2p_connector.exceptions.InitializationException
import com.ouyx.lib_wifip2p_connector.facade.callback.SearchDevicesCallback
import com.ouyx.lib_wifip2p_connector.facade.listener.PeerDevicesListener
import com.ouyx.lib_wifip2p_connector.util.DefaultLogger


/**
 *  入口
 *
 * @author ouyx
 * @date 2023年08月30日 14时06分
 */
class WiFiP2PConnector {

    internal lateinit var mWiFiP2POptions: ConnectOptions

    internal lateinit var mWiFiP2PManager: WifiP2pManager

    internal lateinit var mWifiP2pChannel: WifiP2pManager.Channel

    internal lateinit var mApplication: Application


    companion object {
        @Volatile
        private var INSTANCE: WiFiP2PConnector? = null
        fun get(): WiFiP2PConnector =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: WiFiP2PConnector().also { INSTANCE = it }
            }
    }

    /**
     * 初始化
     */
    fun init(application: Application, connectOptions: ConnectOptions = ConnectOptions.getDefaultHttpOptions()) {
        mApplication = application
        mWiFiP2POptions = connectOptions

        val wifiP2pManager = application.getSystemService(WIFI_P2P_SERVICE) as? WifiP2pManager
        if (wifiP2pManager == null) {
            DefaultLogger.error(message = "初始化失败,设备不支持WiFi Direct或者该功能没有被启用")
            return
        }
        mWiFiP2PManager = wifiP2pManager


        ConnectorImpl.get().init()

        DefaultLogger.setDebug(mWiFiP2POptions.isDebug)

        mWifiP2pChannel = mWiFiP2PManager.initialize(application, application.mainLooper, null)

    }


    /**
     *  发现对等设备
     *  仅会通知您发现进程成功和失败，但不会提供有关其发现的实际对等设备（如有）的任何信息
     */
    fun searchDevices(callback: SearchDevicesCallback.() -> Unit) {
        checkInitialization()

        val searchDevicesCallback = SearchDevicesCallback()
        searchDevicesCallback.callback()
        ConnectorImpl.get().searchDevices(searchDevicesCallback)
    }

    /**
     * 设置P2P连接的对等列表监听器
     */
    fun setPeerListListener(peerListListener:PeerDevicesListener) {
       ConnectorImpl.get().setPeerListListener(peerListListener)
    }

    /**
     * 移除对等列表监听器
     */
    fun removePeerListListener() {
        ConnectorImpl.get().removePeerListListener()
    }

    /**
     * 检查 是否初始化
     */
    private fun checkInitialization() {
        if (!::mWiFiP2POptions.isInitialized) {
            throw InitializationException()
        }
    }

    /**
     * 释放 资源
     */
    fun close() {
        ConnectorImpl.get().close()
    }
}