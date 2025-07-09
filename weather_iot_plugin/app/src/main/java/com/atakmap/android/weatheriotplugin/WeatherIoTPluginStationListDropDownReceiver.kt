package com.atakmap.android.weatheriotplugin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import com.atak.plugins.impl.PluginLayoutInflater
import com.atakmap.android.dropdown.DropDown.OnStateListener
import com.atakmap.android.dropdown.DropDownReceiver
import com.atakmap.android.ipc.AtakBroadcast
import com.atakmap.android.maps.MapView
import com.atakmap.coremap.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("SetTextI18n")
class WeatherIoTPluginStationListDropDownReceiver(
    mapView: MapView?,
    pluginContext: Context,
    coroutineScope: CoroutineScope,
    private val weatherViewModel: WeatherViewModel
): DropDownReceiver(mapView), OnStateListener {

    private val listView: View = PluginLayoutInflater.inflate(
        pluginContext,
        R.layout.station_list_layout,
        null
    )

    private val weatherStationSpinner: Spinner = listView.findViewById(R.id.weather_station_spinner)
    private val selectButton: Button = listView.findViewById(R.id.select_btn)
    private val disconnectButton: Button = listView.findViewById(R.id.mqtt_disconnect_btn)
    private val lastUpdatedText: TextView = listView.findViewById(R.id.detail_station_updated)
    private var didChangeScreen = false

    init {

        val spinnerAdapter =
            ArrayAdapter<String>(pluginContext, android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        weatherStationSpinner.adapter = spinnerAdapter

        coroutineScope.launch {
            weatherViewModel.weatherStations.collect { weatherStations ->

                if (weatherStations.isNotEmpty()) {
                    val weatherStationTime = weatherStations[0].dateTime
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val formattedDate = weatherStationTime.format(formatter)
                    lastUpdatedText.text = "Date Updated as of $formattedDate"
                }

                withContext(Dispatchers.Main) {
                    val stationInfoList = weatherStations.map { station ->
                        val cityName = getCityName(pluginContext, station.latitude, station.longitude) ?: ""
                        "${station.ID}: $cityName(${station.latitude}, ${station.longitude})"
                    }
                    spinnerAdapter.clear()
                    spinnerAdapter.addAll(stationInfoList)
                    spinnerAdapter.notifyDataSetChanged()
                }
            }
        }

        selectButton.setOnClickListener {
            val weatherStations = weatherViewModel.weatherStations.value
            val selectedWeatherStationIndex = weatherStationSpinner.selectedItemPosition
            Log.d(TAG, "size=${weatherStations.size} + index=$selectedWeatherStationIndex")
            weatherViewModel.setSelectedWeatherStation(selectedWeatherStationIndex)

            didChangeScreen = true

            val listIntent = Intent()
            listIntent.setAction(WeatherIoTPluginStationDetailDropDownReceiver.SHOW_DETAIL)
            AtakBroadcast.getInstance().sendBroadcast(listIntent)
        }

        disconnectButton.setOnClickListener {
            weatherViewModel.disconnectMqtt()

            val listIntent = Intent()
            listIntent.setAction(WeatherIoTPluginMainDropDownReceiver.SHOW_MAIN)
            AtakBroadcast.getInstance().sendBroadcast(listIntent)
        }

    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        if (action == SHOW_LIST) {
            didChangeScreen = false
            Log.d(TAG, "showing list drop down")
            showDropDown(
                listView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH,
                HALF_HEIGHT, false, this
            )
        }
    }

    override fun disposeImpl() {
    }

    override fun onDropDownSelectionRemoved() {
    }

    override fun onDropDownClose() {
        if (!didChangeScreen){
            weatherViewModel.disconnectMqtt()
        }
    }

    override fun onDropDownSizeChanged(p0: Double, p1: Double) {
    }

    override fun onDropDownVisible(p0: Boolean) {
    }

    private fun getCityName(context: Context, latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                val city = addresses[0].locality
                "$city "
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val TAG = "WeatherIoTPluginStationListDropDownReceiver"
        const val SHOW_LIST: String = "com.atakmap.android.weatheriotplugin.SHOW_LIST"
    }
}