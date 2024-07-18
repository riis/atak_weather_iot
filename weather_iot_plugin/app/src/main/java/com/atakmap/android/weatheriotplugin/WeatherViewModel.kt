package com.atakmap.android.weatheriotplugin

import com.atakmap.coremap.log.Log
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException
import com.hivemq.client.mqtt.exceptions.MqttClientStateException
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import kotlinx.coroutines.CoroutineScope

class WeatherViewModel(
    private val coroutineScope: CoroutineScope
) {

    companion object {
        private const val TAG = "WeatherViewModel"
    }

    private lateinit var mqttClient: Mqtt3BlockingClient

    fun subMqtt(serverHostIp: String, port: Int) {
        mqttClient = Mqtt3Client.builder()
            .identifier("atak_plugin")
            .serverHost(serverHostIp)
            .serverPort(port)
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
                    Log.e(TAG, "MQTT Client State failed! $e")
                }
            }
    }
}