package com.example.newsapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * TODO
 */
interface INetworkRepository {
    val networkAvailable: StateFlow<Boolean>
}

/**
 * TODO
 */
class NetworkRepository(context: Context, coroutineScope: CoroutineScope) : INetworkRepository {
    private val _networkAvailable = MutableStateFlow(false)
    override val networkAvailable = _networkAvailable.asStateFlow()

    init {
        val connectivityManager = getSystemService(context, ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)

                    coroutineScope.launch {
                        _networkAvailable.value = true
                    }
                }
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    // Do nothing
                }
                override fun onLost(network: Network) {
                    coroutineScope.launch {
                        _networkAvailable.value = false
                    }
                }
            }
        )
    }
}