package com.ouyx.lib_wifip2p_connector.util

import android.util.Log

object DefaultLogger {

    private var isDebug = false

    private const val TAG = "lib-wifi-p2p-connector"

    fun setDebug(isDebug: Boolean) {
        DefaultLogger.isDebug = isDebug
    }

    fun debug(message: String) {
        if (isDebug) {
            Log.d(TAG, getExtInfo() + message)
        }
    }

    fun info(message: String) {
        if (isDebug) {
            Log.i(TAG, getExtInfo() + message)
        }
    }

    fun warning(message: String) {
        if (isDebug) {
            Log.w(TAG, getExtInfo() + message)
        }
    }

    fun error(message: String) {
        if (isDebug) {
            Log.e(TAG, getExtInfo() + message)
        }
    }


    private fun getExtInfo(): String {
        val sb = StringBuilder("[")
        val threadName = Thread.currentThread().name
        sb.append(threadName)
        sb.append(" ] ")
        return sb.toString()
    }

}