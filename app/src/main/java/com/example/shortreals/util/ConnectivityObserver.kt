package com.example.shortreals.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface ConnectivityObserver {
    val isConnected: Flow<Boolean>
}

class NetworkConnectivityObserver(context: Context) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override val isConnected: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                trySend(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(false)
            }
            
            override fun onUnavailable() {
                super.onUnavailable()
                trySend(false)
            }
        }

        val hasConnection = connectivityManager.activeNetwork != null
        trySend(hasConnection)

        connectivityManager.registerDefaultNetworkCallback(callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}
