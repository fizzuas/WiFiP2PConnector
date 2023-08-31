/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.facade.listener

import android.net.wifi.p2p.WifiP2pDevice
import com.ouyx.lib_wifip2p_connector.facade.data.PeerDevice


/**
 *  搜索列表变化 接口
 *
 * @author ouyx
 * @date 2023年08月31日 16时05分
 */
interface PeerDevicesListener {

    fun onPeersAvailable(wifiP2pDeviceList: Collection<WifiP2pDevice>)

}