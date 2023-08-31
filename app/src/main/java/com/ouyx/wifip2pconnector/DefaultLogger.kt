package com.ouyx.wifip2pconnector

import android.util.Log

internal object DefaultLogger {

    private var isDebug = true

    private const val TAG = "app"

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