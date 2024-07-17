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
import com.atakmap.android.weatheriotplugin.plugin.R
import com.atakmap.coremap.log.Log
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException
import com.hivemq.client.mqtt.exceptions.MqttClientStateException
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client

class WeatherIoTPluginDropDownReceiver(
    mapView: MapView?,
    private val pluginContext: Context
) : DropDownReceiver(mapView), OnStateListener {

    private lateinit var mqttClient: Mqtt3BlockingClient

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
            subMqtt()
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

    private fun subMqtt() {
        mqttClient = Mqtt3Client.builder()
            .identifier("atak_plugin")
//			.serverHost("minjerd-riis-laptop.local") // Android sometimes fails to resolve .local domains :(
//          .serverHost("192.168.1.67") // Mark's laptop on home network
//			.serverHost("10.5.2.251") // Mark's laptop on RIIS network
//			.serverHost("192.168.1.195") // Michal's laptop on home network
            .serverHost("192.168.2.182") // Zain's laptop on home network
            .serverPort(1883)
            .buildBlocking().apply {
                try {
                    connect()
                    toAsync().subscribeWith()
                        .topicFilter("weather/data")
                        .qos(MqttQos.AT_LEAST_ONCE)
                        .callback { callback ->
                            Log.d(TAG, callback.payloadAsBytes.decodeToString())
                        }
                        .send()
                } catch (e: ConnectionFailedException) {
                    Log.e(TAG, "MQTT Connection failed! $e")
                } catch (e: MqttClientStateException) {
                    Log.e(TAG, "MQTT Client State failed!", e)
                }
            }
    }

    companion object {
        private const val TAG = "WeatherIoTPluginDropDownReceiver"
        const val SHOW_PLUGIN: String = "com.atakmap.android.weatheriotplugin.SHOW_PLUGIN"
    }
}
