/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.facade.callback

import com.ouyx.lib_wifip2p_connector.facade.data.SearchActionFailType
import kotlinx.coroutines.launch


/**
 *  发现对等设备进程回调
 *  仅会通知您发现进程成功和失败，但不会提供有关其发现的实际对等设备（如有）的任何信息
 *
 * @author ouyx
 * @date 2023年08月30日 16时52分
 */
class SearchDevicesCallback : BaseCallback() {

    private var searchActionSuccess: (() -> Unit)? = null

    private var sendActionFail: ((failType: SearchActionFailType) -> Unit)? = null

    /**
     * 搜索操作成功
     */
    fun onSendSuccess(onSuccess: (() -> Unit)) {
        this.searchActionSuccess = onSuccess
    }

    /**
     * 搜索操作失败
     */
    fun onSendFail(onFail: ((failType: SearchActionFailType) -> Unit)) {
        this.sendActionFail = onFail
    }

    internal fun callSearchActionSuccess() {
        mainScope.launch { searchActionSuccess?.invoke() }
    }

    internal fun callSearchActionFail(failType: SearchActionFailType) {
        mainScope.launch { sendActionFail?.invoke(failType) }
    }

}