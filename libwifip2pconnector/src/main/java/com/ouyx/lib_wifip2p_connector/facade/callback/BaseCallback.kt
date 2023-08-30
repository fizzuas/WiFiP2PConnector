/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.facade.callback

import com.ouyx.lib_wifip2p_connector.core.ConnectorImpl


/**
 * Callback 基类
 *
 * @author ouyx
 * @date 2023年08月18日 10时15分
 */
open class BaseCallback {

    val mainScope = ConnectorImpl.get().getMainScope()

}