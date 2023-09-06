/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.core.request

import android.annotation.SuppressLint
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import com.ouyx.lib_wifip2p_connector.facade.callback.ConnectCallback
import com.ouyx.lib_wifip2p_connector.facade.data.ConnectFailType
import com.ouyx.lib_wifip2p_connector.facade.data.ConnectedInfo
import com.ouyx.lib_wifip2p_connector.util.DefaultLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/**
 * 连接
 *
 * @author ouyx
 * @date 2023年09月06日 15时27分
 */
class ConnectRequest : BaseRequest {

    private var mTargetMacAddress: String? = null

    private var mConnectCallback: ConnectCallback? = null

    private val isConnecting = AtomicBoolean(false)

    private var mConnectJob: Job? = null

    @SuppressLint("MissingPermission")
    fun connect(address: String, connectCallback: ConnectCallback) {
        mTargetMacAddress = address
        mConnectCallback = connectCallback

        val wifiP2pConfig = WifiP2pConfig()
        wifiP2pConfig.deviceAddress = address
        wifiP2pConfig.wps.setup = WpsInfo.PBC


        getIoScope().launch {
            DefaultLogger.warning(message = "正在连接，deviceName: $address")

            when (awaitConnectAction(wifiP2pConfig)) {
                is Reason.Fail -> {
                    mConnectCallback?.callConnectFail(ConnectFailType.UnKnown)
                }

                Reason.SUCCESS -> {
                    mConnectCallback?.callConnectStart()
                    mConnectJob = getIoScope().launch {
                        DefaultLogger.debug("等待 连接结果...")
                        withTimeout(5000) {
                            isConnecting.set(true)

                            delay(5000)
                        }
                    }

                    mConnectJob?.invokeOnCompletion { throwable ->
                        isConnecting.set(false)
                        when (throwable) {
                            is TimeoutCancellationException->{
                                mConnectCallback?.callConnectFail(ConnectFailType.TimeOut)
                            }
                            is ConnectCancelReason ->{
                                when(throwable){
                                    ConnectCancelReason.CancelByChoice -> {
                                        mConnectCallback?.onConnectFail {  }
                                    }
                                    ConnectCancelReason.CancelByError -> {
                                        mConnectCallback?.callConnectFail(ConnectFailType.UnKnown)
                                    }
                                    is ConnectCancelReason.CancelBySuccess -> {
                                        mConnectCallback?.callConnectSuccess(throwable.wifiConnectInfo)

                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    /**
     *
     */
    sealed class Reason {

        object SUCCESS : Reason()

        class Fail(val reason: Int) : Reason()
    }

    @SuppressLint("MissingPermission")
    private suspend fun awaitConnectAction(wifiP2pConfig: WifiP2pConfig): Reason {
        return suspendCoroutine {
            getWiFiManager().connect(getWiFiChannel(), wifiP2pConfig,
                object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        DefaultLogger.error("连接进程启动成功，开始连接...")
                        it.resume(Reason.SUCCESS)
                    }

                    override fun onFailure(reason: Int) {
                        DefaultLogger.error("连接失败 $reason")
                        it.resume(Reason.Fail(reason))
                    }
                })
        }
    }


    /**
     * 用户主动取消
     */
    fun stopConnect() {

        mConnectJob?.cancel(ConnectCancelReason.CancelByChoice)
    }

    /**
     *  有设备连接上
     */
    fun onConnected(connectedInfo: ConnectedInfo) {

        if (connectedInfo.groupOwnerDeviceAddress.trim() == mTargetMacAddress?.trim()) {
            mConnectJob?.cancel(ConnectCancelReason.CancelBySuccess(connectedInfo))
        }
    }


    /**
     * 销毁资源
     */
    override fun close() {
        mConnectCallback = null
    }
}