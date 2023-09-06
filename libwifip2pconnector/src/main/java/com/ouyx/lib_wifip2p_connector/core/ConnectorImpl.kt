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
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.ouyx.lib_wifip2p_connector.core.request.ConnectCancelReason
import com.ouyx.lib_wifip2p_connector.core.request.ConnectRequest
import com.ouyx.lib_wifip2p_connector.core.request.SearchRequest
import com.ouyx.lib_wifip2p_connector.facade.callback.ConnectCallback
import com.ouyx.lib_wifip2p_connector.facade.callback.SearchDevicesCallback
import com.ouyx.lib_wifip2p_connector.facade.data.ConnectedInfo
import com.ouyx.lib_wifip2p_connector.facade.data.PeerDevice
import com.ouyx.lib_wifip2p_connector.facade.data.getDeviceState
import com.ouyx.lib_wifip2p_connector.facade.listener.PeerChangedListener
import com.ouyx.lib_wifip2p_connector.util.DefaultLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


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

    private var mPeerChangedListener: PeerChangedListener? = null


    /**
     * 当前设备列表
     */
    private val mCurPeerDeviceList = mutableListOf<PeerDevice>()

    fun getPeerDevices() = mCurPeerDeviceList.toList()

    private var mSearchRequest: SearchRequest? = null

    private var mConnectRequest: ConnectRequest? = null

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


    fun setPeerListListener(peerListListener: PeerChangedListener) {
        this.mPeerChangedListener = peerListListener
    }

    fun removePeerListListener() {
        mPeerChangedListener = null
    }

    override fun searchDevices(callback: SearchDevicesCallback) {
        if (mSearchRequest == null) {
            mSearchRequest = SearchRequest()
        }
        mSearchRequest?.searchDevices(callback)
    }

    override fun connect(address: String, callback: ConnectCallback) {
        if (mConnectRequest == null) {
            mConnectRequest = ConnectRequest()
        }
        mConnectRequest?.connect(address, callback)
    }

    /**
     * 用户主动取消
     */
    fun stopConnect() {
        mConnectRequest?.stopConnect()
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

        mSearchRequest?.close()
        mSearchRequest = null

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
                            val peerDeviceList = wifiP2pDeviceList.deviceList.map {
                                PeerDevice(
                                    deviceName = it.deviceName,
                                    deviceAddress = it.deviceAddress,
                                    state = getDeviceState(it.status)
                                )
                            }
                            mCurPeerDeviceList.clear()
                            mCurPeerDeviceList.addAll(peerDeviceList)
                            DefaultLogger.debug("当前PEER列表更新(size=${mCurPeerDeviceList.size}) : $mCurPeerDeviceList")

                            mainScope.launch {
                                mPeerChangedListener?.onPeersChanged(mCurPeerDeviceList)
                            }
                        }
                    }
                }

                //指示 Wi-Fi 点对点连接的状态已更改。从 Android 10 开始，这不是固定的。
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    getMainScope().launch {
                        val networkInfo =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                syncRequestNetworkInfo()
                            } else {
                                intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                            }
                        DefaultLogger.warning("WIFI_P2P_CONNECTION_CHANGED_ACTION ：networkInfo=$networkInfo")

                        if (networkInfo != null && networkInfo.isConnected) {
                            val p2pInfo = awaitRequestConnectionInfo()
                            DefaultLogger.debug("已连接 P2P设备  $p2pInfo")

                            val groupInfo = awaitRequestGroupInfo()
                            DefaultLogger.debug("Group信息 $groupInfo")

                            val connectedInfo = ConnectedInfo(
                                groupOwnerIP = p2pInfo.groupOwnerAddress,
                                isGroupOwner = p2pInfo.isGroupOwner,
                                groupOwnerDeviceName = groupInfo.owner.deviceName,
                                groupOwnerDeviceAddress = groupInfo.owner.deviceAddress,
                            )
                            DefaultLogger.debug("connectedInfo信息 $connectedInfo")
                            mConnectRequest?.onConnected(connectedInfo)

                        } else {
                            DefaultLogger.warning("与 P2P 设备已断开连接")
                        }
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


    /**
     * 同步获取networkInfo
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun syncRequestNetworkInfo(): NetworkInfo {
        return suspendCoroutine { continuation ->
            getWiFiManager().requestNetworkInfo(getWiFiChannel()) { networkInfo ->
                continuation.resume(networkInfo)
            }
        }
    }

    /**
     * 同步获取WifiP2pInfo
     */
    private suspend fun awaitRequestConnectionInfo(): WifiP2pInfo {
        return suspendCoroutine { continuation ->
            getWiFiManager().requestConnectionInfo(getWiFiChannel()) {
                continuation.resume(it)
            }
        }
    }

    /**
     * 同步获取 WifiP2pGroup
     */
    @SuppressLint("MissingPermission")
    private suspend fun awaitRequestGroupInfo(): WifiP2pGroup {
        return suspendCoroutine { continuation ->
            getWiFiManager().requestGroupInfo(getWiFiChannel()) {
                continuation.resume(it)
            }
        }
    }
}