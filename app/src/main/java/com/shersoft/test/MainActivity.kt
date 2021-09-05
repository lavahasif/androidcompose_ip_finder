package com.shersoft.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shersoft.test.ui.theme.TestTheme
import com.shersoft.test.util.MyIp
import com.shersoft.test.util.ShareFile


private val Any.connectionInfo: Any
    get() {
        TODO("Not yet implemented")
    }

class MainActivity : ComponentActivity() {
    lateinit var myIp: MyIp;
    lateinit var sharefile: ShareFile;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val showDialog = mutableStateOf(false)
        var ips = ""
        myIp = MyIp(this)
        sharefile = ShareFile(this)
        setContent {
            TestTheme {

                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Column (modifier = Modifier.padding(16.dp)){
            Button(onClick = { Share() }) {
                Greeting("Share")
            }
                        Greeting("${myIp.getdeviceIpAddress_Wifi()}")
                        Greeting("${myIp.getdeviceIpAddress_Wifi_2()}")
                        Greeting("${myIp.getIpAddress(applicationContext)}")
                        Greeting("${myIp.getIPAddress(true)}")
                        Greeting("${myIp.getIpAddress_2()}")
                        Greeting(
                            "Volley ${
                                myIp.getIpAddress_Private(object : MyIp.VolleyListner {
                                    override fun Onresponse(data: String) {
                                        ips = data; showDialog.value = true; }
                                })
                            }"
                        )
                        if (showDialog.value) {
                            Greeting(name = "Volley" + ips)
                        }

                        Greeting("${myIp.getNetworkIp4LoopbackIps()}")
                        Greeting("${myIp.getWifiIp(applicationContext)}")
                        Greeting("Util")
                        Greeting("${myIp.getIPAddress_Util(true)}")
                        Greeting("${myIp.getIPAddress_Util(false)}")
                        Greeting("${myIp.getLocalIpAddress()}")
                        Greeting("-------------------------")

                        Greeting("${myIp.getNetworkInterfaceIpAddress()}")
                        Greeting("${myIp.getDeviceIpAddress()}")
                        Greeting("${myIp.getWifiIp()}")

                        Greeting("------------------22-------")
                        Greeting("${myIp.getIPAddress_Wif_3()}")
                        Greeting("${myIp.getLocalIpAddress_2()}")
                        Greeting("${myIp?.getLocalAddress()}")
                        Greeting("${myIp.getLocalIpAddress_4()}")

                        Greeting("-------------------------")

                        Greeting(name = "notWorking")
                        Greeting("${myIp.getMacAddress_Util("eth0")}")
                        Greeting("${myIp.getMacAddress_Util("wlan0")}")
                        Greeting("${myIp.getMacAddress_Util("wlan1")}")
                        Greeting("${myIp.getMacAddress_Util("rmnet_data2")}")
                        Greeting("${myIp.getMacAddress_Util("rmnet_data1")}")
                        Greeting("${myIp.getMacAddress_Util("rmnet_data0")}")

                    }

//                    Greeting("Android${myIp.getdeviceIpAddress()}")

                }
            }
        }
    }

    private fun Share() {
        sharefile.openFile()
//        sharefile.shareAppAsAPK(this)
    }

    private fun Runnable(function: () -> Unit) {

    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "\t\t\t $name!")
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TestTheme {
        Greeting("")
    }
}