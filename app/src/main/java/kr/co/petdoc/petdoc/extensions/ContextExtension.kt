package kr.co.petdoc.petdoc.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo

inline fun <reified T : Activity> Context.startActivity() {
    val intent = Intent(this, T::class.java)
    startActivity(intent)
}

inline fun <reified T : Activity> Context.startActivity(block: Intent.() -> Unit) {
    val intent = Intent(this, T::class.java)
    intent.block()
    startActivity(intent)
}

inline fun <reified T : Activity> Activity.startActivityForResult(
    requestCode: Int,
    block: Intent.() -> Unit
) {
    val intent = Intent(this, T::class.java)
    intent.block()
    startActivityForResult(intent, requestCode)
}

fun Context.isInternetConnected(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networks = cm.allNetworks
    var hasInternet = false
    if (networks.isNotEmpty()) {
        networks.forEach {
            val networkCapabilities = cm.getNetworkCapabilities(it)
            if (networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                hasInternet = true
            }
        }
    }
    return hasInternet
}