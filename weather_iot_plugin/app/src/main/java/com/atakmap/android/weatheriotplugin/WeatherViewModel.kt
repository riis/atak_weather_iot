package com.atakmap.android.weatheriotplugin

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.atakmap.android.weatheriotplugin.plugin.data.WeatherStation
import com.atakmap.android.weatheriotplugin.plugin.utils.SingleEvent
import com.atakmap.coremap.log.Log
import com.google.gson.Gson
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException
import com.hivemq.client.mqtt.exceptions.MqttClientStateException
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class WeatherViewModel(
    private val coroutineScope: CoroutineScope
) {

    companion object {
        private const val TAG = "WeatherViewModel"
    }

    private lateinit var mqttClient: Mqtt3BlockingClient
    private val gson = Gson()

    private val _weatherStations = MutableStateFlow<List<WeatherStation>>(emptyList())
    val weatherStations = _weatherStations.asStateFlow()

    private val _isConnected = MutableStateFlow(SingleEvent(false))
    val isConnected = _isConnected.asStateFlow()

    private val _selectedWeatherStation = MutableStateFlow<WeatherStation?>(null)
    val selectedWeatherStation = _selectedWeatherStation.asStateFlow()

    init {
        coroutineScope.launch {
            _weatherStations.collect { stations ->
                Log.d(TAG, "Weather stations updated: $stations")
            }
        }
    }

    fun subMqtt(serverHostIp: String, port: Int) {
        coroutineScope.launch {
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
                                station.dateTime = LocalDateTime.now()
                                updateWeatherStations(station)
                            }
                            .send()
                        _isConnected.emit(SingleEvent(true))
                    } catch (e: ConnectionFailedException) {
                        _isConnected.emit(SingleEvent(false))
                        Log.e(TAG, "MQTT Connection failed! $e")
                    } catch (e: MqttClientStateException) {
                        _isConnected.emit(SingleEvent(false))
                        Log.e(TAG, "MQTT Client State failed! $e")
                    }
                }
        }
    }

    fun disconnectMqtt() {
        coroutineScope.launch {
            if (::mqttClient.isInitialized && mqttClient.state.isConnected) {
                try {
                    mqttClient.disconnect()
                    _isConnected.emit(SingleEvent(false))
                    Log.d(TAG, "MQTT Disconnected successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "MQTT Disconnection failed! $e")
                }
            } else {
                Log.d(TAG, "MQTT Client is not connected or not initialized")
            }
        }
    }

    private fun updateWeatherStations(newStation: WeatherStation) {
        val currentList = _weatherStations.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.ID == newStation.ID }
        if (existingIndex != -1) {
            currentList[existingIndex] = newStation
        } else {
            currentList.add(newStation)
        }
        _weatherStations.value = currentList
    }

    fun setSelectedWeatherStation(weatherStation: WeatherStation?) {
        coroutineScope.launch {
            _selectedWeatherStation.emit(weatherStation)
        }
    }
}