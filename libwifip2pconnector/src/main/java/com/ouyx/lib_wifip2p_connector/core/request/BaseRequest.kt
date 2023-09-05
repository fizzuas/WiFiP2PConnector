/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.core.request

import com.ouyx.lib_wifip2p_connector.core.ConnectorImpl


/**
 * BaseRequest
 *
 * @author ouyx
 * @date 2023年09月05日 10时48分
 */
interface BaseRequest {


    fun getMainScope() = ConnectorImpl.get().getMainScope()

    fun isWiFiEnable() = ConnectorImpl.get().isP2pEnable()

    fun getApplication() = ConnectorImpl.get().getApplication()

    fun getWiFiManager() = ConnectorImpl.get().getWiFiManager()

    fun getWiFiChannel() = ConnectorImpl.get().getWiFiChannel()

}