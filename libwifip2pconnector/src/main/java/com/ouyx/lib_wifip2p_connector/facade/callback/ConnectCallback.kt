/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.facade.callback

import com.ouyx.lib_wifip2p_connector.facade.data.ConnectFailType
import com.ouyx.lib_wifip2p_connector.facade.data.ConnectedInfo
import kotlinx.coroutines.launch


/**
 * 连接回调
 *
 * @author ouyx
 * @date 2023年09月06日 15时33分
 */
class ConnectCallback : BaseCallback() {

    private var connectStart: (() -> Unit)? = null

    private var connectFail: ((failType: ConnectFailType) -> Unit)? = null

    private var searchSuccess: ((connectedInfo: ConnectedInfo) -> Unit)? = null



    /**
     * 开始连接
     */
    fun onConnectStart(onStart: (() -> Unit)) {
        this.connectStart = onStart
    }

    /**
     * 连接操作失败
     */
    fun onConnectFail(onFail: ((failType: ConnectFailType) -> Unit)) {
        this.connectFail = onFail
    }


    /**
     * 连接成功
     */
    fun onConnectSuccess(onSuccess: ((connectedInfo: ConnectedInfo) -> Unit)) {
        this.searchSuccess = onSuccess
    }




    internal fun callConnectStart() {
        mainScope.launch { connectStart?.invoke() }
    }

    internal fun callConnectFail(failType: ConnectFailType) {
        mainScope.launch { connectFail?.invoke(failType) }
    }

    internal fun callConnectSuccess(connectedInfo: ConnectedInfo) {
        mainScope.launch { searchSuccess?.invoke(connectedInfo) }
    }




}