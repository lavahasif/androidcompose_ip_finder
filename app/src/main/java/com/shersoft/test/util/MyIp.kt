package com.shersoft.test.util

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Context.WIFI_SERVICE
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.StrictMode
import android.text.TextUtils
import android.text.format.Formatter
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.content.getSystemService
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.shersoft.test.MainActivity
import java.io.IOException
import java.math.BigInteger
import java.net.*
import java.util.*


class MyIp(var contexts: MainActivity) {
    interface VolleyListner {

        fun Onresponse(data: String)
    }

    lateinit var utils: Utils

    init {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        utils = Utils()
    }

    fun getWifiIp(context: Context): String? {
        return context.getSystemService<WifiManager>().let {
            when {
                it == null -> "No wifi available"
                !it.isWifiEnabled -> "Wifi is disabled"
                it.connectionInfo == null -> "Wifi not connected"
                else -> {
                    val ip = it.connectionInfo.ipAddress
                    ((ip and 0xFF).toString() + "." + (ip shr 8 and 0xFF) + "." + (ip shr 16 and 0xFF) + "." + (ip shr 24 and 0xFF))
                }
            }
        }
    }

    fun getNetworkIp4LoopbackIps(): Map<String, String> = try {
        NetworkInterface.getNetworkInterfaces()
            .asSequence()
            .associate { it.displayName to it.ip4LoopbackIps() }
            .filterValues { it.isNotEmpty() }
    } catch (ex: Exception) {
        emptyMap()
    }

    private fun NetworkInterface.ip4LoopbackIps() =
        inetAddresses.asSequence()
            .filter { !it.isLoopbackAddress && it is Inet4Address }
            .map { it.hostAddress }
            .filter { it.isNotEmpty() }
            .joinToString()

    fun getdeviceIpAddress_Wifi(): String? {
        val context: Context = contexts.applicationContext
        val wifiManager = context.getSystemService(ComponentActivity.WIFI_SERVICE) as WifiManager
        val ip = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
        return ip;
    }

    fun getdeviceIpAddress_Wifi_2(): String? {
        try {
            val host = InetAddress.getByName("webAddress")
//            val host = InetAddress.getByName("nameOfDevice or webAddress")
            println(host.hostAddress)
            return host.hostAddress;
        } catch (e: UnknownHostException) {
            e.printStackTrace()
            return "0.0.0.0" + e.message
        }

    }

    fun getdeviceIpAddress(): String? {
        try {
            val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf: NetworkInterface = en.nextElement()
                val enumIpAddr: Enumeration<InetAddress> = intf.getInetAddresses()
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress: InetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress() && inetAddress is Inet4Address) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return null
    }


    fun getIpAddress(context: Context): String? {
        val wifiManager = context.applicationContext
            .getSystemService(WIFI_SERVICE) as WifiManager
        var ipAddress = intToInetAddress(wifiManager.dhcpInfo.ipAddress).toString()
        ipAddress = ipAddress.substring(1)
        return ipAddress
    }

    fun intToInetAddress(hostAddress: Int): InetAddress {
        val addressBytes = byteArrayOf(
            (0xff and hostAddress).toByte(),
            (0xff and (hostAddress shr 8)).toByte(),
            (0xff and (hostAddress shr 16)).toByte(),
            (0xff and (hostAddress shr 24)).toByte()
        )
        return try {
            InetAddress.getByAddress(addressBytes)
        } catch (e: UnknownHostException) {
            throw AssertionError()
        }
    }

    fun getIPAddress(useIPv4: Boolean): String {
        try {
            var interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                var addrs = Collections.list(intf.getInetAddresses());
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress()) {
                        var sAddr = addr.getHostAddress();
                        var isIPv4: Boolean
                        isIPv4 = sAddr.indexOf(':') < 0
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                var delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                if (delim < 0) {
                                    return sAddr.toUpperCase()
                                } else {
                                    return sAddr.substring(0, delim).toUpperCase()
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
        }
        return ""
    }


    fun getIpAddress_2(): String {
        var ip = ""
        try {
            val wm = contexts.getApplicationContext().getSystemService(WIFI_SERVICE) as WifiManager
            ip = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
        } catch (e: java.lang.Exception) {

        }

        if (ip.isEmpty()) {
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val networkInterface = en.nextElement()
                    val enumIpAddr = networkInterface.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                            val host = inetAddress.getHostAddress()
                            if (host.isNotEmpty()) {
                                ip = host
                                break;
                            }
                        }
                    }

                }
            } catch (e: java.lang.Exception) {

            }
        }

        if (ip.isEmpty())
            ip = "127.0.0.1"
        return ip
    }

    fun getIpAddress_Private(runnable: VolleyListner): String {
        val stringUrl = "https://ipinfo.io/ip"
//String stringUrl = "http://whatismyip.akamai.com/";
// Instantiate the RequestQueue.
//String stringUrl = "http://whatismyip.akamai.com/";
// Instantiate the RequestQueue.
        val queue: RequestQueue = Volley.newRequestQueue(contexts)
//String url ="http://www.google.com";

// Request a string response from the provided URL.
//String url ="http://www.google.com";
        var ip: String = ""
// Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, stringUrl,
            object : Response.Listener<String?> {


                override fun onResponse(response: String?) {
                    Log.e("MGLogTag", "GET IP : $response")
                    ip = response.toString();
                    runnable.Onresponse(ip)

                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                    ip = "That didn't work!";
                    runnable.Onresponse(ip)
                }
            })

// Add the request to the RequestQueue.

// Add the request to the RequestQueue.
        queue.add(stringRequest)
        return ip;
    }

    fun getMacAddress_Util(inter: String = "wlan0"): String = Utils.getMACAddress(inter);
    fun getIPAddress_Util(isTrueip4: Boolean = true): String = Utils.getIPAddress(isTrueip4);

    fun getIPAddress_Wif_3(isTrueip4: Boolean = true): String {
        val wm = contexts.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager?
        val ipAddress: String = BigInteger.valueOf(wm!!.dhcpInfo.netmask.toLong()).toString()
        return ipAddress
    }
    //


    fun getLocalIpAddress(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress) {
                        return inetAddress.hostAddress
                    }
                }
            }
        } catch (ex: java.lang.Exception) {
            Log.e("IP Address", ex.toString())
        }
        return null
    }

    fun getDeviceIpAddress(): String {
        var actualConnectedToNetwork: String? = null
        val connManager = contexts.getApplicationContext()
            .getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (connManager != null) {
            val mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (mWifi!!.isConnected) {
                actualConnectedToNetwork = getWifiIp()
            }
        }
        if (TextUtils.isEmpty(actualConnectedToNetwork)) {
            actualConnectedToNetwork = getNetworkInterfaceIpAddress()
        }
        if (TextUtils.isEmpty(actualConnectedToNetwork)) {
            actualConnectedToNetwork = "127.0.0.1"
        }
        return actualConnectedToNetwork!!
    }


    fun getWifiIp(): String? {
        val mWifiManager = contexts.getApplicationContext().getSystemService(
            WIFI_SERVICE
        ) as WifiManager
        if (mWifiManager != null && mWifiManager.isWifiEnabled) {
            val ip = mWifiManager.connectionInfo.ipAddress
            return ((ip and 0xFF).toString() + "." + (ip shr 8 and 0xFF) + "." + (ip shr 16 and 0xFF) + "."
                    + (ip shr 24 and 0xFF))
        }
        return null
    }


    fun getNetworkInterfaceIpAddress(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val networkInterface = en.nextElement()
                val enumIpAddr = networkInterface.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        val host = inetAddress.getHostAddress()
                        if (!TextUtils.isEmpty(host)) {
                            return host
                        }
                    }
                }
            }
        } catch (ex: java.lang.Exception) {
            Log.e("IP Address", "getLocalIpAddress", ex)
        }
        return null
    }

    fun getLocalIpAddress_2(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return null
    }

    @Throws(IOException::class)
    fun getLocalAddress(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress) {
                        //return inetAddress.getHostAddress().toString();
                        return inetAddress.hostAddress
                    }
                }
            }
        } catch (ex: SocketException) {
            Log.e("SALMAN", ex.toString())
        }
        return null
    }


    fun getLocalIpAddress_4(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress) {
//                        val ip = Formatter.formatIpAddress(inetAddress.hostAddress())
                        Log.i("TAG", "***** IP=")
                        return inetAddress.toString();
                    }
                }
            }
        } catch (ex: SocketException) {
            Log.e("TAG", ex.toString())
        }
        return null
    }
}

