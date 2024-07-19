package com.atakmap.android.weatheriotplugin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import com.atak.plugins.impl.PluginLayoutInflater
import com.atakmap.android.dropdown.DropDown.OnStateListener
import com.atakmap.android.dropdown.DropDownReceiver
import com.atakmap.android.maps.MapView
import com.atakmap.android.weatheriotplugin.plugin.R
import com.atakmap.coremap.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@SuppressLint("SetTextI18n")
class WeatherIoTPluginStationDetailDropDownReceiver(
    mapView: MapView?,
    pluginContext: Context,
    coroutineScope: CoroutineScope,
    weatherViewModel: WeatherViewModel
): DropDownReceiver(mapView), OnStateListener {

    private val detailView: View = PluginLayoutInflater.inflate(
        pluginContext,
        R.layout.station_detail_layout,
        null
    )

    private val stationIdText: TextView = detailView.findViewById(R.id.detail_station_id)
    private val latLonText: TextView = detailView.findViewById(R.id.detail_station_lat_lon)
    private val lastUpdatedText: TextView = detailView.findViewById(R.id.detail_station_updated)
    private val tempValueText: TextView = detailView.findViewById(R.id.temp_value)
    private val feelsLikeValueText: TextView = detailView.findViewById(R.id.feelslike_value)
    private val frostPointValueText: TextView = detailView.findViewById(R.id.frostpoint_value)
    private val humidityValueText: TextView = detailView.findViewById(R.id.humidity_value)
    private val indoorTempValueText: TextView = detailView.findViewById(R.id.indoortemp_value)
    private val indoorHumidityValueText: TextView = detailView.findViewById(R.id.indoorhumidity_value)
    private val windSpeedValueText: TextView = detailView.findViewById(R.id.windspeed_value)
    private val windGustValueText: TextView = detailView.findViewById(R.id.windgust_value)
    private val windDirValueText: TextView = detailView.findViewById(R.id.winddir_value)
    private val rainValueText: TextView = detailView.findViewById(R.id.rain_value)
    private val solarRadiationValueText: TextView = detailView.findViewById(R.id.solarradiation_value)
    private val uvValueText: TextView = detailView.findViewById(R.id.uv_value)


    init {
        coroutineScope.launch {
            weatherViewModel.selectedWeatherStation.collect { weatherStation ->
                if (weatherStation != null) {

                    // Station ID
                    stationIdText.text = "\uD83D\uDFE2 ${weatherStation.ID}"

                    // Location
                    latLonText.text = formatLatLong(weatherStation.latitude, weatherStation.longitude)

                    // Last Updated
                    val weatherStationTime = weatherStation.dateTime
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val formattedDate = weatherStationTime.format(formatter)
                    lastUpdatedText.text = "Date Updated as of $formattedDate"

                    // Table values
                    tempValueText.text = String.format(Locale.US,"%.1f°F", weatherStation.temp)
                    feelsLikeValueText.text = String.format(Locale.US,"%.1f°F", weatherStation.feelslike)

                }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        if (action == SHOW_DETAIL) {
            Log.d(TAG, "showing detail drop down")
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
    }

    override fun onDropDownSizeChanged(width: Double, height: Double) {
        // Handle the drop down size changed event here
    }

    override fun onDropDownVisible(visible: Boolean) {
        // Handle the drop down visibility change here
    }

    private fun formatLatLong(latitude: Double, longitude: Double): String {
        val latDirection = if (latitude >= 0) "N" else "S"
        val lonDirection = if (longitude >= 0) "E" else "W"
        val latAbs = abs(latitude)
        val lonAbs = abs(longitude)
        return String.format(Locale.US,"%.4f° %s, %.4f° %s", latAbs, latDirection, lonAbs, lonDirection)
    }

    companion object {
        private const val TAG = "WeatherIoTPluginStationDetailDropDownReceiver"
        const val SHOW_DETAIL: String = "com.atakmap.android.weatheriotplugin.SHOW_DETAIL"
    }
}
