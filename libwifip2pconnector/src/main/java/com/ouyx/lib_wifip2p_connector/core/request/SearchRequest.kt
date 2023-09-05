/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.core.request

import android.annotation.SuppressLint
import android.net.wifi.p2p.WifiP2pManager
import com.ouyx.lib_wifip2p_connector.Constant.SEARCH_TIME_MS
import com.ouyx.lib_wifip2p_connector.core.ConnectorImpl
import com.ouyx.lib_wifip2p_connector.facade.callback.SearchDevicesCallback
import com.ouyx.lib_wifip2p_connector.facade.data.SearchFailType
import com.ouyx.lib_wifip2p_connector.util.DefaultLogger
import com.ouyx.lib_wifip2p_connector.util.P2pUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean


/**
 * 扫描请求
 *
 * @author ouyx
 * @date 2023年09月05日 10时48分
 */
class SearchRequest : BaseRequest {

    /**
     * 扫描状态
     */
    private val isSearching = AtomicBoolean(false)

    private var mCallback: SearchDevicesCallback? = null

    companion object {
        @Volatile
        private var INSTANCE: SearchRequest? = null
        fun get(): SearchRequest =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SearchRequest().also { INSTANCE = it }
            }
    }


    /**
     * 扫描周围设备
     */
    fun searchDevices(callback: SearchDevicesCallback) {
        mCallback = callback
        if (isSearching.get()) {
            DefaultLogger.warning("设备忙碌，请待会再搜索")
            mCallback?.callSearchFail(SearchFailType.BUSY)
            return
        }
        if (!isWiFiEnable()) {
            DefaultLogger.warning("WiFi P2P 未启动,请打开WiFi")
            mCallback?.callSearchFail(SearchFailType.P2PNotEnabled)
            return
        }
        if (!P2pUtil.isPermission(getApplication())) {
            DefaultLogger.warning("WiFi P2P 连接权限不够")
            mCallback?.callSearchFail(SearchFailType.PermissionNotEnough)
            return
        }

        startSearch()
    }


    @SuppressLint("MissingPermission")
    private fun startSearch() {
        DefaultLogger.info("使用 Wi-Fi 点对点连接开始搜索附近的设备...")
        getWiFiManager().discoverPeers(getWiFiChannel(), object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                //开始搜索周围设备，将上次扫描到的设备集合清空
                isSearching.set(true)
                ConnectorImpl.get().clearPeerDevices()
                mCallback?.callSearchStart()

                getMainScope().launch {
                    delay(SEARCH_TIME_MS)

                    mCallback?.callSearchSuccess(ConnectorImpl.get().getPeerDevices())
                    isSearching.set(false)
                }
            }

            override fun onFailure(reasonCode: Int) {
                when (reasonCode) {
                    WifiP2pManager.P2P_UNSUPPORTED -> {
                        mCallback?.callSearchFail(SearchFailType.P2pUnsupported)
                    }

                    WifiP2pManager.BUSY -> {
                        mCallback?.callSearchFail(SearchFailType.BUSY)
                    }

                    WifiP2pManager.ERROR -> {
                        mCallback?.callSearchFail(SearchFailType.ERROR)
                    }

                    else -> {
                        mCallback?.callSearchFail(SearchFailType.UNKNOWN)
                    }
                }
            }
        })
    }
}