package com.atakmap.android.weatheriotplugin.plugin

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.view.View
import android.widget.*
import com.atak.plugins.impl.PluginLayoutInflater
import com.atakmap.android.dropdown.DropDown.OnStateListener
import com.atakmap.android.dropdown.DropDownReceiver
import com.atakmap.android.maps.MapView
import com.atakmap.android.weatheriotplugin.plugin.R
import com.atakmap.coremap.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter
import java.util.*

class WeatherListPaneReceiver(
    mapView: MapView,
    private val pluginCtxLazy: () -> Context,
    private val coroutineScope: CoroutineScope,
    private val weatherViewModel: WeatherViewModel,
    private val onSelect: () -> Unit,
    private val onDisconnect: () -> Unit
) : DropDownReceiver(mapView), OnStateListener {

    private val ctx get() = pluginCtxLazy()
    private val listView: View = PluginLayoutInflater.inflate(ctx, R.layout.station_list_layout, null)

    private val weatherStationSpinner: Spinner = listView.findViewById(R.id.weather_station_spinner)
    private val selectButton: Button = listView.findViewById(R.id.select_btn)
    private val disconnectButton: Button = listView.findViewById(R.id.mqtt_disconnect_btn)
    private val lastUpdatedText: TextView = listView.findViewById(R.id.detail_station_updated)
    private var didChangeScreen = false

    init {
        val spinnerAdapter = ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        weatherStationSpinner.adapter = spinnerAdapter

        coroutineScope.launch {
            weatherViewModel.weatherStations.collect { weatherStations ->
                withContext(Dispatchers.Main) {
                    if (weatherStations.isNotEmpty()) {
                        val time = weatherStations[0].dateTime
                        lastUpdatedText.text =
                            "Date Updated as of ${time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}"
                    }

                    val stationInfoList = weatherStations.map { station ->
                        val cityName = getCityName(ctx, station.latitude, station.longitude) ?: ""
                        "${station.ID}: $cityName(${station.latitude}, ${station.longitude})"
                    }
                    spinnerAdapter.clear()
                    spinnerAdapter.addAll(stationInfoList)
                    spinnerAdapter.notifyDataSetChanged()
                }
            }
        }

        selectButton.setOnClickListener {
            val stations = weatherViewModel.weatherStations.value
            val idx = weatherStationSpinner.selectedItemPosition
            Log.d("WeatherListPaneReceiver", "size=${stations.size} + index=$idx")
            weatherViewModel.setSelectedWeatherStation(idx)
            didChangeScreen = true
            onSelect()
        }

        disconnectButton.setOnClickListener {
            weatherViewModel.disconnectMqtt()
            didChangeScreen = true
            onDisconnect()
        }
    }

    fun show() {
        didChangeScreen = false
        showDropDown(
            listView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH,
            HALF_HEIGHT, false, this
        )
    }

    override fun onDropDownClose() {
        if (!didChangeScreen) weatherViewModel.disconnectMqtt()
    }

    public override fun disposeImpl() {}
    override fun onDropDownSelectionRemoved() {}
    override fun onDropDownVisible(p0: Boolean) {}
    override fun onDropDownSizeChanged(p0: Double, p1: Double) {}
    override fun onReceive(p0: Context?, p1: Intent?) {}

    private fun getCityName(context: Context, latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) "${addresses[0].locality} " else null
        } catch (e: Exception) { null }
    }
}
