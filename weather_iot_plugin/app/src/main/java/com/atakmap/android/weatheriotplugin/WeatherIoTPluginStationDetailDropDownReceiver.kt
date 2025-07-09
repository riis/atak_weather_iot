package com.atakmap.android.weatheriotplugin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.atak.plugins.impl.PluginLayoutInflater
import com.atakmap.android.dropdown.DropDown.OnStateListener
import com.atakmap.android.dropdown.DropDownReceiver
import com.atakmap.android.ipc.AtakBroadcast
import com.atakmap.android.maps.MapView
import com.atakmap.coremap.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@SuppressLint("SetTextI18n")
class WeatherIoTPluginStationDetailDropDownReceiver(
    mapView: MapView?,
    pluginContext: Context,
    coroutineScope: CoroutineScope,
    private val weatherViewModel: WeatherViewModel
) : DropDownReceiver(mapView), OnStateListener {

    private val detailView: View = PluginLayoutInflater.inflate(
        pluginContext,
        R.layout.station_detail_layout,
        null
    )

    private var didChangeScreen  = false

    private val weatherStations = weatherViewModel.weatherStations

    private val stationIdText: TextView = detailView.findViewById(R.id.detail_station_id)
    private val latLonText: TextView = detailView.findViewById(R.id.detail_station_lat_lon)
    private val lastUpdatedText: TextView = detailView.findViewById(R.id.detail_station_updated)
    private val tempValueText: TextView = detailView.findViewById(R.id.temp_value)
    private val feelsLikeValueText: TextView = detailView.findViewById(R.id.feelslike_value)
    private val frostPointValueText: TextView = detailView.findViewById(R.id.frostpoint_value)
    private val humidityValueText: TextView = detailView.findViewById(R.id.humidity_value)
    private val indoorTempValueText: TextView = detailView.findViewById(R.id.indoortemp_value)
    private val indoorHumidityValueText: TextView =
        detailView.findViewById(R.id.indoorhumidity_value)
    private val windSpeedValueText: TextView = detailView.findViewById(R.id.windspeed_value)
    private val windGustValueText: TextView = detailView.findViewById(R.id.windgust_value)
    private val windDirValueText: TextView = detailView.findViewById(R.id.winddir_value)
    private val rainValueText: TextView = detailView.findViewById(R.id.rain_value)
    private val solarRadiationValueText: TextView =
        detailView.findViewById(R.id.solarradiation_value)
    private val uvValueText: TextView = detailView.findViewById(R.id.uv_value)
    private val allWeatherStationsButton: Button =
        detailView.findViewById(R.id.detail_all_stations_btn)


    init {
        coroutineScope.launch {
            combine(
                weatherViewModel.selectedWeatherStationIndex,
                weatherViewModel.weatherStations
            ) { weatherStationIndex, weatherStations ->
                Pair(weatherStationIndex, weatherStations)
            }.collect { (weatherStationIndex, weatherStations) ->
                withContext(Dispatchers.Main) {

                    if (weatherStationIndex != null && weatherStations.isNotEmpty() && weatherStationIndex < weatherStations.size) {

                        val weatherStation = weatherStations[weatherStationIndex]

                        // Station ID
                        stationIdText.text = "\uD83D\uDFE2 ${weatherStation.ID}"

                        // Location
                        latLonText.text =
                            formatLatLong(weatherStation.latitude, weatherStation.longitude)

                        // Last Updated
                        val weatherStationTime = weatherStation.dateTime
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        val formattedDate = weatherStationTime.format(formatter)
                        lastUpdatedText.text = "Date Updated as of $formattedDate"

                        // Table values
                        tempValueText.text = String.format(Locale.US, "%.1f°F", weatherStation.temp)
                        feelsLikeValueText.text =
                            String.format(Locale.US, "%.1f°F", weatherStation.feelslike)
                        frostPointValueText.text =
                            String.format(Locale.US, "%.1f°F", weatherStation.frostpoint)
                        humidityValueText.text =
                            String.format(Locale.US, "%.1f%%", weatherStation.humidity)
                        indoorTempValueText.text =
                            String.format(Locale.US, "%.1f°F", weatherStation.indoortemp)
                        indoorHumidityValueText.text =
                            String.format(Locale.US, "%.1f%%", weatherStation.indoorhumidity)
                        windSpeedValueText.text =
                            String.format(Locale.US, "%.1f mph", weatherStation.windspeed)
                        windGustValueText.text =
                            String.format(Locale.US, "%.1f mph", weatherStation.windgust)
                        windDirValueText.text =
                            String.format(Locale.US, "%.1f°", weatherStation.winddir)
                        rainValueText.text = String.format(Locale.US, "%.1f in", weatherStation.rain)
                        solarRadiationValueText.text =
                            String.format(Locale.US, "%.1f W/m²", weatherStation.solarradiation)
                        uvValueText.text = String.format(Locale.US, "%.1f", weatherStation.UV)
                    }
                }
            }
        }

        allWeatherStationsButton.setOnClickListener {
            backToAllWeatherStationsScreen()
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        if (action == SHOW_DETAIL) {
            Log.d(TAG, "showing detail drop down")
            didChangeScreen = true
            showDropDown(
                detailView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH,
                HALF_HEIGHT, false, this
            )
        }
    }

    override fun disposeImpl() {
        // Add any required cleanup code here
    }

    override fun onDropDownSelectionRemoved() {
        // Handle the drop down selection removed event here
    }

    override fun onDropDownClose() {
        // Handle the drop down close event here
        if (!didChangeScreen){
            weatherViewModel.disconnectMqtt()
        }
    }

    override fun onDropDownSizeChanged(width: Double, height: Double) {
        // Handle the drop down size changed event here
    }

    override fun onDropDownVisible(visible: Boolean) {
        // Handle the drop down visibility change here
    }

    private fun backToAllWeatherStationsScreen() {
        weatherViewModel.setSelectedWeatherStation(null)
        didChangeScreen = true
        val listIntent = Intent()
        listIntent.setAction(WeatherIoTPluginStationListDropDownReceiver.SHOW_LIST)
        AtakBroadcast.getInstance().sendBroadcast(listIntent)
    }

    private fun formatLatLong(latitude: Double, longitude: Double): String {
        val latDirection = if (latitude >= 0) "N" else "S"
        val lonDirection = if (longitude >= 0) "E" else "W"
        val latAbs = abs(latitude)
        val lonAbs = abs(longitude)
        return String.format(
            Locale.US,
            "%.4f° %s, %.4f° %s",
            latAbs,
            latDirection,
            lonAbs,
            lonDirection
        )
    }

    companion object {
        private const val TAG = "WeatherIoTPluginStationDetailDropDownReceiver"
        const val SHOW_DETAIL: String = "com.atakmap.android.weatheriotplugin.SHOW_DETAIL"
    }
}
