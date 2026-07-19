package com.sechat.core.p2p

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import java.net.InetSocketAddress

class TorProxyManager(private val context: Context) {

    companion object {
        const val ORBOT_PACKAGE = "org.torproject.android"
        const val ORBOT_PROXY_HOST = "127.0.0.1"
        const val ORBOT_SOCKS_PORT = 9050
    }

    fun isOrbotInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(ORBOT_PACKAGE, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun getSocksProxy(): java.net.Proxy {
        return java.net.Proxy(
            java.net.Proxy.Type.SOCKS,
            InetSocketAddress(ORBOT_PROXY_HOST, ORBOT_SOCKS_PORT)
        )
    }

    fun createTorProxySelector(): java.net.ProxySelector {
        val proxy = getSocksProxy()
        return object : java.net.ProxySelector() {
            override fun select(uri: java.net.URI?): MutableList<java.net.Proxy> {
                return mutableListOf(proxy)
            }
            override fun connectFailed(
                uri: java.net.URI?, sa: java.net.SocketAddress?, ioe: java.io.IOException?
            ) { }
        }
    }
}
