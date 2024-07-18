package com.atakmap.android.weatheriotplugin

import com.atakmap.android.weatheriotplugin.plugin.data.WeatherStation
import com.atakmap.coremap.log.Log
import com.google.gson.Gson
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException
import com.hivemq.client.mqtt.exceptions.MqttClientStateException
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    coroutineScope: CoroutineScope
) {

    companion object {
        private const val TAG = "WeatherViewModel"
    }

    private lateinit var mqttClient: Mqtt3BlockingClient
    private val gson = Gson()
    private val weatherStations = MutableStateFlow<List<WeatherStation>>(emptyList())

    init {
        coroutineScope.launch {
            weatherStations.collect { stations ->
                Log.d(TAG, "Weather stations updated: $stations")
            }
        }
    }

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
                            val message = callback.payloadAsBytes.decodeToString()
                            Log.d(TAG, message)
                            val station = gson.fromJson(message, WeatherStation::class.java)
                            updateWeatherStations(station)
                        }
                        .send()
                } catch (e: ConnectionFailedException) {
                    Log.e(TAG, "MQTT Connection failed! $e")
                } catch (e: MqttClientStateException) {
                    Log.e(TAG, "MQTT Client State failed! $e")
                }
            }
    }

    private fun updateWeatherStations(newStation: WeatherStation) {
        val currentList = weatherStations.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.ID == newStation.ID }
        if (existingIndex != -1) {
            currentList[existingIndex] = newStation
        } else {
            currentList.add(newStation)
        }
        weatherStations.value = currentList
    }
}