/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.core.request

import com.ouyx.lib_wifip2p_connector.facade.data.ConnectedInfo
import java.util.concurrent.CancellationException


/**
 * 连接请求的 job 取消原因
 *
 * @author ouyx
 * @date 2023年09月06日 17时22分
 */
sealed class ConnectCancelReason : CancellationException() {
    /**
     * 主动取消Job
     */
    object CancelByChoice : ConnectCancelReason()

    /**
     * 因为异常而取消Job
     */
    object CancelByError : ConnectCancelReason()

    /**
     * 操作成功 而取消Job
     */
    class CancelBySuccess(val wifiConnectInfo: ConnectedInfo) : ConnectCancelReason()
}