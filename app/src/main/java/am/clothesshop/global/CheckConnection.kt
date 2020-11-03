package am.clothesshop.global

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.os.NetworkOnMainThreadException
import java.net.InetAddress
import java.net.UnknownHostException


class CheckConnection {
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected
    }

    fun isInternetAvailable(): Boolean {
        try {
            val address: InetAddress = InetAddress.getByName("www.google.com")
            return !address.equals("")
        } catch (e: UnknownHostException) {
            println("ERROR UNKNOWN")
        } catch (e: NetworkOnMainThreadException) {
            println("ERROR ON MAIN")
        }
        return false
    }
}