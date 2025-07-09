package com.atakmap.android.weatheriotplugin

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.atak.plugins.impl.PluginLayoutInflater
import com.atakmap.android.dropdown.DropDown.OnStateListener
import com.atakmap.android.dropdown.DropDownReceiver
import com.atakmap.android.ipc.AtakBroadcast
import com.atakmap.android.maps.MapView
import com.atakmap.coremap.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeatherIoTPluginMainDropDownReceiver(
    mapView: MapView?,
    pluginContext: Context,
    coroutineScope: CoroutineScope,
    private val weatherViewModel: WeatherViewModel
) : DropDownReceiver(mapView), OnStateListener {

    // Remember to use the PluginLayoutInflator if you are actually inflating a custom view
    // In this case, using it is not necessary - but I am putting it here to remind
    // developers to look at this Inflator
    private val mainView: View = PluginLayoutInflater.inflate(
        pluginContext,
        R.layout.main_layout, null
    )

    private var connectButton: Button
    private var didChangeScreen = false

    init {
        connectButton = mainView.findViewById(R.id.mqtt_connect_btn)

        connectButton.setOnClickListener {

            val brokerUri = mainView.findViewById<EditText>(R.id.brokerUriText).text.toString()
            val brokerPortStr =
                mainView.findViewById<EditText>(R.id.brokerPortEditText).text.toString()
            val brokerPort: Int?

            try {
                brokerPort = brokerPortStr.toInt()
                weatherViewModel.subMqtt(brokerUri, brokerPort)
            } catch (e: NumberFormatException) {
                Toast.makeText(mainView.context, "Invalid port number", Toast.LENGTH_SHORT).show()
                Log.w(TAG, e)
            } catch (e: IllegalArgumentException) {
                Toast.makeText(mainView.context, "Invalid IP Address", Toast.LENGTH_SHORT).show()
                Log.w(TAG, e)
            }
        }

        coroutineScope.launch {
            weatherViewModel.isConnected.drop(1).collect { isConnected ->
                if (isConnected.peekContent()) {
                    didChangeScreen = true
                    val listIntent = Intent()
                    listIntent.setAction(WeatherIoTPluginStationListDropDownReceiver.SHOW_LIST)
                    AtakBroadcast.getInstance().sendBroadcast(listIntent)
                } else if(weatherViewModel.isManuallyDisconnected.value.peekContent()) {
                    weatherViewModel.confirmManuallyDisconnected()
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            mainView.context,
                            "Failed to Connect! Please double check text fields and MQTT Broker!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }


    /**************************** PUBLIC METHODS  */
    public override fun disposeImpl() {
    }

    /**************************** INHERITED METHODS  */
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        if (action == SHOW_MAIN) {
            didChangeScreen = false
            Log.d(TAG, "showing main drop down")
            showDropDown(
                mainView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH,
                HALF_HEIGHT, false, this
            )
        }
    }

    override fun onDropDownSelectionRemoved() {
    }

    override fun onDropDownVisible(v: Boolean) {
    }

    override fun onDropDownSizeChanged(width: Double, height: Double) {
    }

    override fun onDropDownClose() {
        if (!didChangeScreen){
            weatherViewModel.disconnectMqtt()
        }
    }

    companion object {
        private const val TAG = "WeatherIoTPluginDropDownReceiver"
        const val SHOW_MAIN: String = "com.atakmap.android.weatheriotplugin.SHOW_MAIN"
    }
}
