/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.facade.data


/**
 * 设备状态
 *
 * @author ouyx
 * @date 2023年08月31日 16时00分
 */
sealed class DeviceState {
    /**
     * 可用
     */
    object AVAILABLE : DeviceState()

    /**
     * 邀请中
     */
    object INVITED : DeviceState()

    /**
     * 已连接
     */
    object CONNECTED : DeviceState()

    /**
     * 失败的
     */
    object FAILED : DeviceState()

    /**
     * 不可用的
     */
    object UNAVAILABLE : DeviceState()

    /**
     * 未知
     */
    object UNKNOWN : DeviceState()

}