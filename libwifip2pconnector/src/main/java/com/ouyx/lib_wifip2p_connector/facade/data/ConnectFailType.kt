/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.facade.data


/**
 * 连接失败原因
 *
 * @author ouyx
 * @date 2023年09月06日 15时34分
 */
sealed class ConnectFailType {

    object UnKnown: ConnectFailType()

    object TimeOut : ConnectFailType()

    object CancelByChoice : ConnectFailType()
}