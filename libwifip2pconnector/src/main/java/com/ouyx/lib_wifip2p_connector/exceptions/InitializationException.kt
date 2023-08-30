/*
 * Copyright (c) 2022-2032 ouyx
 * 不能修改和删除上面的版权声明
 * 此代码属于ouyx编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.exceptions


/**
 * InitializationException
 *
 * @author ouyx
 * @date 2023年07月24日 16时59分
 */
class InitializationException(msg: String = "WiFiP2PConnector未初始化，请先调用WiFiP2PConnector.init") : RuntimeException(msg)