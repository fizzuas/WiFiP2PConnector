/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.core

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import com.ouyx.lib_wifip2p_connector.core.request.SearchRequest
import com.ouyx.lib_wifip2p_connector.facade.callback.SearchDevicesCallback
import com.ouyx.lib_wifip2p_connector.facade.data.PeerDevice
import com.ouyx.lib_wifip2p_connector.facade.data.getDeviceState
import com.ouyx.lib_wifip2p_connector.facade.listener.PeerChangedLsistener
import com.ouyx.lib_wifip2p_connector.util.DefaultLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


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

    private var mPeerChangedListener: PeerChangedLsistener? = null

    private val mCurPeerDeviceList = mutableListOf<PeerDevice>()

    fun getPeerDevices() = mCurPeerDeviceList.toList()

    fun clearPeerDevices() = mCurPeerDeviceList.clear()

    /**
     * P2P 是否启用
     */
    private var mP2pStateEnable = false

    fun isP2pEnable() = mP2pStateEnable

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


    fun setPeerListListener(peerListListener: PeerChangedLsistener) {
        this.mPeerChangedListener = peerListListener
    }

    fun removePeerListListener() {
        mPeerChangedListener = null
    }

    override fun searchDevices(callback: SearchDevicesCallback) {
        SearchRequest.get().searchDevices(callback)
    }


    @SuppressLint("MissingPermission")
    override fun connect(address: String) {
        val wifiP2pConfig = WifiP2pConfig()
        wifiP2pConfig.deviceAddress = address
        wifiP2pConfig.wps.setup = WpsInfo.PBC
        DefaultLogger.warning(message = "正在连接，deviceName: $address")
        getWiFiManager().connect(getWiFiChannel(), wifiP2pConfig,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    DefaultLogger.warning("connect onSuccess")
                }

                override fun onFailure(reason: Int) {
                    DefaultLogger.warning("连接失败 $reason")
                }
            })
    }

    override fun disConnect() {
        getWiFiManager().cancelConnect(getWiFiChannel(), object : WifiP2pManager.ActionListener {
            override fun onFailure(reasonCode: Int) {
                DefaultLogger.warning("cancelConnect onFailure:$reasonCode")
            }

            override fun onSuccess() {
                DefaultLogger.warning("cancelConnect onSuccess")
            }
        })
        getWiFiManager().removeGroup(getWiFiChannel(), null)
    }

    override fun close() {
        getApplication().unregisterReceiver(this)
        mPeerChangedListener = null
        mainScope.cancel()
        ioScope.cancel()
        INSTANCE = null
    }

    @SuppressLint("MissingPermission")
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
                    DefaultLogger.debug("WIFI_P2P_PEERS_CHANGED_ACTION")
                    mPeerChangedListener?.let {
                        getWiFiManager().requestPeers(getWiFiChannel()) { wifiP2pDeviceList ->

                            mainScope.launch {
                                mPeerChangedListener?.onPeersAvailable(wifiP2pDeviceList.deviceList)
                            }

                            val peerDeviceList = wifiP2pDeviceList.deviceList.map {
                                PeerDevice(
                                    deviceName = it.deviceName,
                                    deviceAddress = it.deviceAddress,
                                    state = getDeviceState(it.status)
                                )
                            }
                            mCurPeerDeviceList.clear()
                            mCurPeerDeviceList.addAll(peerDeviceList)
                            DefaultLogger.debug("当前PEER列表更新 : $mCurPeerDeviceList")
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
                                DefaultLogger.info("已连接设备= $info")
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