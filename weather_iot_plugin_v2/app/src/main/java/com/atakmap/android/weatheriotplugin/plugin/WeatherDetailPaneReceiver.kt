package com.atakmap.android.weatheriotplugin.plugin

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.atak.plugins.impl.PluginLayoutInflater
import com.atakmap.android.dropdown.DropDown.OnStateListener
import com.atakmap.android.dropdown.DropDownReceiver
import com.atakmap.android.maps.MapView
import com.atakmap.android.weatheriotplugin.plugin.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs

class WeatherDetailPaneReceiver(
    mapView: MapView,
    private val pluginCtxLazy: () -> android.content.Context,
    private val coroutineScope: CoroutineScope,
    private val weatherViewModel: WeatherViewModel,
    private val onBack: () -> Unit
) : DropDownReceiver(mapView), OnStateListener {

    private val ctx get() = pluginCtxLazy()
    private val detailView: View = PluginLayoutInflater.inflate(ctx, R.layout.station_detail_layout, null)
    private var didChangeScreen = false

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
    private val backBtn: Button = detailView.findViewById(R.id.detail_all_stations_btn)

    init {
        coroutineScope.launch {
            combine(weatherViewModel.selectedWeatherStationIndex, weatherViewModel.weatherStations) { idx, stations ->
                idx?.takeIf { it in stations.indices }?.let { stations[it] }
            }.collect { station ->
                withContext(Dispatchers.Main) {
                    if (station != null) {
                        stationIdText.text = "🟢 ${station.ID}"
                        latLonText.text = formatLatLong(station.latitude, station.longitude)
                        lastUpdatedText.text = "Date Updated as of ${
                            station.dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        }"

                        tempValueText.text = String.format(Locale.US, "%.1f°F", station.temp)
                        feelsLikeValueText.text = String.format(Locale.US, "%.1f°F", station.feelslike)
                        frostPointValueText.text = String.format(Locale.US, "%.1f°F", station.frostpoint)
                        humidityValueText.text = String.format(Locale.US, "%.1f%%", station.humidity)
                        indoorTempValueText.text = String.format(Locale.US, "%.1f°F", station.indoortemp)
                        indoorHumidityValueText.text = String.format(Locale.US, "%.1f%%", station.indoorhumidity)
                        windSpeedValueText.text = String.format(Locale.US, "%.1f mph", station.windspeed)
                        windGustValueText.text = String.format(Locale.US, "%.1f mph", station.windgust)
                        windDirValueText.text = String.format(Locale.US, "%.1f°", station.winddir)
                        rainValueText.text = String.format(Locale.US, "%.1f in", station.rain)
                        solarRadiationValueText.text = String.format(Locale.US, "%.1f W/m²", station.solarradiation)
                        uvValueText.text = String.format(Locale.US, "%.1f", station.UV)
                    }
                }
            }
        }

        backBtn.setOnClickListener {
            weatherViewModel.setSelectedWeatherStation(null)
            didChangeScreen = true
            onBack()
        }
    }

    fun show() {
        didChangeScreen = false
        showDropDown(
            detailView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH,
            HALF_HEIGHT, false, this
        )
    }

    override fun onDropDownClose() {
        if (!didChangeScreen) weatherViewModel.disconnectMqtt()
    }

    public override fun disposeImpl() {}
    override fun onDropDownSelectionRemoved() {}
    override fun onDropDownSizeChanged(width: Double, height: Double) {}
    override fun onDropDownVisible(visible: Boolean) {}
    override fun onReceive(p0: Context?, p1: Intent?) {}

    private fun formatLatLong(latitude: Double, longitude: Double): String {
        val latDirection = if (latitude >= 0) "N" else "S"
        val lonDirection = if (longitude >= 0) "E" else "W"
        val latAbs = abs(latitude)
        val lonAbs = abs(longitude)
        return String.format(Locale.US, "%.4f° %s, %.4f° %s", latAbs, latDirection, lonAbs, lonDirection)
    }
}
