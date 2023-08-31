/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import com.ouyx.lib_wifip2p_connector.facade.callback.SearchDevicesCallback
import com.ouyx.lib_wifip2p_connector.facade.data.PeerDevice
import com.ouyx.lib_wifip2p_connector.facade.data.SearchActionFailType
import com.ouyx.lib_wifip2p_connector.facade.data.getDeviceStatus
import com.ouyx.lib_wifip2p_connector.facade.listener.PeerDevicesListener
import com.ouyx.lib_wifip2p_connector.util.DefaultLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob


/**
 *  实现类
 *
 * @author ouyx
 * @date 2023年08月30日 16时47分
 */
class ConnectorImpl private constructor() : IConnector, BroadcastReceiver() {

    private val mainScope = MainScope()

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun getMainScope() = mainScope

    fun getIOScope() = ioScope

    private var mPeerListListener: PeerDevicesListener? = null

    /**
     * P2P 是否启用
     */
    private var mP2pStateEnable = false

    companion object {
        @Volatile
        private var INSTANCE: ConnectorImpl? = null
        fun get(): ConnectorImpl =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ConnectorImpl().also { INSTANCE = it }
            }
    }


    fun init() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        getApplication().registerReceiver(this, intentFilter)
    }


    fun setPeerListListener(peerListListener: PeerDevicesListener) {
        this.mPeerListListener = peerListListener
    }

    fun removePeerListListener() {
        mPeerListListener = null
    }

    override fun searchDevices(callback: SearchDevicesCallback) {
        if (!mP2pStateEnable) {
            DefaultLogger.warning("WiFi P2P 未启动,请打开WiFi")
            callback.callSearchActionFail(SearchActionFailType.P2PNotEnabled)
            return
        }

        DefaultLogger.info("使用 Wi-Fi 点对点连接开始搜索附近的设备...")
        getWiFiManager().discoverPeers(getWiFiChannel(), object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                DefaultLogger.info("扫描操作成功")
                callback.callSearchActionSuccess()
            }

            override fun onFailure(reasonCode: Int) {
                when (reasonCode) {
                    WifiP2pManager.P2P_UNSUPPORTED -> {
                        callback.callSearchActionFail(SearchActionFailType.P2pUnsupported)
                    }

                    WifiP2pManager.BUSY -> {
                        callback.callSearchActionFail(SearchActionFailType.BUSY)
                    }

                    WifiP2pManager.ERROR -> {
                        callback.callSearchActionFail(SearchActionFailType.ERROR)
                    }

                    else -> {
                        callback.callSearchActionFail(SearchActionFailType.UNKNOWN)
                    }
                }
            }
        })
    }

    override fun close() {
        getApplication().unregisterReceiver(this)
        mPeerListListener = null
        INSTANCE = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            when (intent.action) {
                // 用于指示 Wifi P2P 是否可用
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val enabled = intent.getIntExtra(
                        WifiP2pManager.EXTRA_WIFI_STATE,
                        -1
                    ) == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                    DefaultLogger.warning("WIFI_P2P_STATE_CHANGED_ACTION： $enabled")
                    mP2pStateEnable = enabled
                }

                // 对等节点列表发生了变化
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    DefaultLogger.warning("WIFI_P2P_PEERS_CHANGED_ACTION")
                    mPeerListListener?.let {
                        getWiFiManager().requestPeers(getWiFiChannel()) { wifiP2pDeviceList ->
//                            val peerDeviceList = wifiP2pDeviceList.deviceList.map {
//                                PeerDevice(
//                                    deviceName = it.deviceName,
//                                    deviceAddress = it.deviceAddress,
//                                    state = getDeviceStatus(it.status)
//                                )
//                            }
                            mPeerListListener?.onPeersAvailable(wifiP2pDeviceList.deviceList)
                        }
                    }
                }

                //指示 Wi-Fi 点对点连接的状态已更改。从 Android 10 开始，这不是固定的。
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    val networkInfo =
                        intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)

                    DefaultLogger.warning("WIFI_P2P_CONNECTION_CHANGED_ACTION ： " + networkInfo?.isConnected)

                    if (networkInfo != null && networkInfo.isConnected) {
                        getWiFiManager().requestConnectionInfo(getWiFiChannel()) { info ->
                            if (info != null) {
                            }
                        }
                        DefaultLogger.warning("已连接 P2P 设备")
                    } else {
                        DefaultLogger.warning("与 P2P 设备已断开连接")
                    }
                }
                //指示此设备的配置详细信息已更改。从 Android 10 开始，这不是固定的。
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    val wifiP2pDevice =
                        intent.getParcelableExtra<WifiP2pDevice>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
                    if (wifiP2pDevice != null) {
                    }
                    DefaultLogger.warning("WIFI_P2P_THIS_DEVICE_CHANGED_ACTION ： ${wifiP2pDevice.toString()}")
                }
            }
        }
    }


}