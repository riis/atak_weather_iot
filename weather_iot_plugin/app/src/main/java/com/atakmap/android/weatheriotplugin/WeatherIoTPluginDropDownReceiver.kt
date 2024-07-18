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
import com.atakmap.android.maps.MapView
import com.atakmap.android.weatheriotplugin.plugin.R
import com.atakmap.coremap.log.Log
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException
import com.hivemq.client.mqtt.exceptions.MqttClientStateException
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class WeatherIoTPluginDropDownReceiver(
    mapView: MapView?,
    private val pluginContext: Context
) : DropDownReceiver(mapView), OnStateListener {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val weatherViewModel = WeatherViewModel(coroutineScope)

    // Remember to use the PluginLayoutInflator if you are actually inflating a custom view
    // In this case, using it is not necessary - but I am putting it here to remind
    // developers to look at this Inflator
    private val mainView: View = PluginLayoutInflater.inflate(
        pluginContext,
        R.layout.main_layout, null
    )

    private var connectButton: Button

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
            } catch (e: IllegalArgumentException) {
                Toast.makeText(mainView.context, "Invalid IP Address", Toast.LENGTH_SHORT).show()
            }
        }
    }


    /**************************** PUBLIC METHODS  */
    public override fun disposeImpl() {
    }

    /**************************** INHERITED METHODS  */
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        if (action == SHOW_PLUGIN) {
            Log.d(TAG, "showing plugin drop down")
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
    }

    companion object {
        private const val TAG = "WeatherIoTPluginDropDownReceiver"
        const val SHOW_PLUGIN: String = "com.atakmap.android.weatheriotplugin.SHOW_PLUGIN"
    }
}
