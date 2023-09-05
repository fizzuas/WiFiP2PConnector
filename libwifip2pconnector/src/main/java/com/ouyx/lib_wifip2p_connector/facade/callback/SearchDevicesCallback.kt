/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.facade.callback

import com.ouyx.lib_wifip2p_connector.facade.data.PeerDevice
import com.ouyx.lib_wifip2p_connector.facade.data.SearchFailType
import kotlinx.coroutines.launch


/**
 *  发现对等设备进程回调
 *  仅会通知您发现进程成功和失败，但不会提供有关其发现的实际对等设备（如有）的任何信息
 *
 * @author ouyx
 * @date 2023年08月30日 16时52分
 */
class SearchDevicesCallback : BaseCallback() {

    private var searchStart: (() -> Unit)? = null

    private var sendFail: ((failType: SearchFailType) -> Unit)? = null

    private var searchSuccess: ((deviceList: List<PeerDevice>) -> Unit)? = null


    /**
     * 开始搜索
     */
    fun onSearchStart(onStart: (() -> Unit)) {
        this.searchStart = onStart
    }

    /**
     * 搜索操作失败
     */
    fun onSearchFail(onFail: ((failType: SearchFailType) -> Unit)) {
        this.sendFail = onFail
    }

    /**
     * 搜索成功
     */
    fun onSearchSuccess(onSuccess: ((deviceList: List<PeerDevice>) -> Unit)) {
        this.searchSuccess = onSuccess
    }


    internal fun callSearchStart() {
        mainScope.launch { searchStart?.invoke() }
    }

    internal fun callSearchFail(failType: SearchFailType) {
        mainScope.launch { sendFail?.invoke(failType) }
    }

    internal fun callSearchSuccess(deviceList: List<PeerDevice>) {
        mainScope.launch { searchSuccess?.invoke(deviceList) }
    }

}