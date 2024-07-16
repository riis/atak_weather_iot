package com.atakmap.android.weatheriotplugin

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.atak.plugins.impl.PluginLayoutInflater
import com.atakmap.android.dropdown.DropDown.OnStateListener
import com.atakmap.android.dropdown.DropDownReceiver
import com.atakmap.android.maps.MapView
import com.atakmap.android.weatheriotplugin.mqtt.MQTTClient
import com.atakmap.android.weatheriotplugin.plugin.R
import com.atakmap.coremap.log.Log

class WeatherIoTPluginDropDownReceiver(
    mapView: MapView?,
    private val pluginContext: Context
) : DropDownReceiver(mapView), OnStateListener {

    private lateinit var mqttClient: MQTTClient

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
            if (mapView != null) {
                mqttClient = MQTTClient(mapView.context, brokerUri)
                Log.d(TAG, "successful creation of MqttClient!")
                mqttClient.connect(
                    username = "hello",
                    password = "password"
                )
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
        val TAG: String = WeatherIoTPluginDropDownReceiver::class.java
            .simpleName

        const val SHOW_PLUGIN: String = "com.atakmap.android.weatheriotplugin.SHOW_PLUGIN"
    }
}
