package kr.co.petdoc.petdoc.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * Petdoc
 * Class: NetworkUtil
 * Created by kimjoonsung on 2020/10/28.
 *
 * Description :
 */
class NetworkUtils {
    companion object {
        const val TYPE_NOT_CONNECTED: Int = 0
        const val TYPE_WIFI: Int = 1
        const val TYPE_MOBILE: Int = 2

        fun getConnectivityStatus(context: Context):Int {
            val manager: ConnectivityManager? =
                context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (manager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val networkCapabilities = manager.getNetworkCapabilities(manager.activeNetwork)
                    if (networkCapabilities != null) {
                        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            return TYPE_WIFI
                        } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            return TYPE_MOBILE
                        }
                    }
                } else {
                    val networkInfo = manager.activeNetworkInfo
                    if (networkInfo != null) {
                        val type = networkInfo.type
                        if (type == ConnectivityManager.TYPE_WIFI) {
                            return TYPE_WIFI
                        } else if (type == ConnectivityManager.TYPE_MOBILE) {
                            return TYPE_MOBILE
                        }
                    }
                }
            }

            return TYPE_NOT_CONNECTED
        }
    }
}