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
import com.ouyx.lib_wifip2p_connector.facade.data.SearchActionFailType
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
class ConnectorImpl : IConnector, BroadcastReceiver() {

    private val mainScope = MainScope()

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun getMainScope() = mainScope

    init {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        getApplication().registerReceiver(this, intentFilter)

    }


    override fun searchDevices(callback: SearchDevicesCallback) {
        getWiFiManager().discoverPeers(getWiFiChannel(), object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                DefaultLogger.info("discoverPeers Success")
                callback.callSearchActionSuccess()
            }

            override fun onFailure(reasonCode: Int) {
                DefaultLogger.warning("discoverPeers Failure：$reasonCode")
                callback.callSearchActionFail(SearchActionFailType.Fail(reasonCode))
            }
        })
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val enabled = intent.getIntExtra(
                        WifiP2pManager.EXTRA_WIFI_STATE,
                        -1
                    ) == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                    if (!enabled) {

                    }
                    DefaultLogger.warning("WIFI_P2P_STATE_CHANGED_ACTION： $enabled")
                }

                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    DefaultLogger.warning("WIFI_P2P_PEERS_CHANGED_ACTION")
                    getWiFiManager().requestPeers(getWiFiChannel()) { peers ->

                    }
                }

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