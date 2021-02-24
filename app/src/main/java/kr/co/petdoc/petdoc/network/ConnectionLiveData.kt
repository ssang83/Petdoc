package kr.co.petdoc.petdoc.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import kr.co.petdoc.petdoc.extensions.isInternetConnected

class ConnectionLiveData(private val context: Context) : LiveData<Boolean>() {
    private val connectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private var networkCallback: Any? = null

    override fun onActive() {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                postValue(context.isInternetConnected())
            }

            override fun onLost(network: Network) {
                postValue(context.isInternetConnected())
            }
        }
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder().build(),
            networkCallback as ConnectivityManager.NetworkCallback
        )
    }

    override fun onInactive() {
        super.onInactive()
        try {
            networkCallback?.let {
                connectivityManager.unregisterNetworkCallback(
                    networkCallback as ConnectivityManager.NetworkCallback
                )
            }
        } catch (e: Exception) { }
    }
}