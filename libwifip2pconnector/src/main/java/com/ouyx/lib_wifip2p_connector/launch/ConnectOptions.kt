/*
 * Copyright (c) 2022-2032 上海微创卜算子医疗科技有限公司
 * 不能修改和删除上面的版权声明
 * 此代码属于上海微创卜算子医疗科技有限公司编写，在未经允许的情况下不得传播复制
 */
package com.ouyx.lib_wifip2p_connector.launch

import com.ouyx.lib_wifip2p_connector.Constant.DEFAULT_IS_DEBUG


/**
 * 配置参数
 *
 * @author ouyx
 * @date 2023年08月30日 14时11分
 */
class ConnectOptions private constructor(builder: Builder) {

    /**
     * 是否开启日志
     */
    val isDebug = builder.isDebug



    companion object {

        @JvmStatic
        fun getDefaultHttpOptions() = ConnectOptions(Builder())
    }

    class Builder {

        internal var isDebug = DEFAULT_IS_DEBUG



        /**
         * 设置是否开启调试
         */
        fun setDebug(isDebug: Boolean): Builder {
            this.isDebug = isDebug
            return this
        }



        fun build(): ConnectOptions = ConnectOptions(this)

    }

}