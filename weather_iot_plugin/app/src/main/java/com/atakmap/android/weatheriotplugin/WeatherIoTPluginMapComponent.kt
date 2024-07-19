package com.atakmap.android.weatheriotplugin

import android.content.Context
import android.content.Intent
import com.atakmap.android.dropdown.DropDownMapComponent
import com.atakmap.android.ipc.AtakBroadcast.DocumentedIntentFilter
import com.atakmap.android.maps.MapGroup
import com.atakmap.android.maps.MapView
import com.atakmap.android.maps.Marker
import com.atakmap.android.user.PlacePointTool
import com.atakmap.android.weatheriotplugin.plugin.R
import com.atakmap.android.weatheriotplugin.plugin.data.MarkerData
import com.atakmap.android.weatheriotplugin.plugin.data.WeatherStation
import com.atakmap.coremap.log.Log
import com.atakmap.coremap.maps.coords.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherIoTPluginMapComponent : DropDownMapComponent() {
    private var pluginContext: Context? = null

    private var mainDdr: WeatherIoTPluginMainDropDownReceiver? = null
    private var listDdr: WeatherIoTPluginStationListDropDownReceiver? = null
    private var detailDdr: WeatherIoTPluginStationDetailDropDownReceiver? = null

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val weatherViewModel = WeatherViewModel(coroutineScope)

    private var previousWeatherStations: List<WeatherStation> = emptyList()

    override fun onCreate(
        context: Context,
        intent: Intent,
        view: MapView
    ) {
        context.setTheme(R.style.ATAKPluginTheme)
        super.onCreate(context, intent, view)
        pluginContext = context

        mainDdr = WeatherIoTPluginMainDropDownReceiver(
            mapView = view,
            pluginContext = context,
            coroutineScope = coroutineScope,
            weatherViewModel = weatherViewModel
        )

        Log.d(TAG, "registering the main filter")
        val mainDdFilter = DocumentedIntentFilter()
        mainDdFilter.addAction(WeatherIoTPluginMainDropDownReceiver.SHOW_MAIN)
        registerDropDownReceiver(mainDdr, mainDdFilter)

        listDdr = WeatherIoTPluginStationListDropDownReceiver(
            mapView = view,
            pluginContext = context,
            coroutineScope = coroutineScope
        )

        Log.d(TAG, "registering the list filter")
        val listDdFilter = DocumentedIntentFilter()
        listDdFilter.addAction(WeatherIoTPluginStationListDropDownReceiver.SHOW_LIST)
        registerDropDownReceiver(listDdr, listDdFilter)

        detailDdr = WeatherIoTPluginStationDetailDropDownReceiver(
            mapView = view,
            pluginContext = context,
            coroutineScope = coroutineScope
        )

        Log.d(TAG, "registering the detail filter")
        val detailDdFilter = DocumentedIntentFilter()
        detailDdFilter.addAction(WeatherIoTPluginStationDetailDropDownReceiver.SHOW_DETAIL)
        registerDropDownReceiver(mainDdr, detailDdFilter)

        coroutineScope.launch {
            weatherViewModel.weatherStations.collect { newWeatherStations ->
                val newIds = newWeatherStations.map { it.ID }.toSet()
                val oldIds = previousWeatherStations.map { it.ID }.toSet()

                val addedStations = newWeatherStations.filter { it.ID !in oldIds }
                val removedStations = previousWeatherStations.filter { it.ID !in newIds }

                addedStations.forEach { station ->
                    placeMarker(
                        MarkerData(
                            name = station.ID,
                            lat = station.latitude,
                            lon = station.longitude,
                            icon = WAYPOINT_GREEN,
                            mapView = view,
                            mapGroup = view.rootGroup
                        )
                    )
                }

                removedStations.forEach { station ->
                    removeMarker(
                        mapView = view,
                        name = station.ID,
                        mapGroup = view.rootGroup
                    )
                }

                previousWeatherStations = newWeatherStations
            }
        }
    }

    override fun onDestroyImpl(context: Context, view: MapView) {
        super.onDestroyImpl(context, view)
    }

    private fun placeMarker(
        markerData: MarkerData
    ) {
        val mapItem = markerData.mapView.getMapItem(markerData.name)
        val marker = if (mapItem != null) {
            if (mapItem !is Marker) return
            mapItem.apply {
                point = GeoPoint(markerData.lat, markerData.lon)
                color.let { this.color = it }
            }
        } else {
            val markerCreator =
                PlacePointTool.MarkerCreator(GeoPoint(markerData.lat, markerData.lon)).apply {
                    setUid(markerData.name)
                    setCallsign(markerData.name)
                    setType("a-u-G")
                    markerData.icon?.let { setIconPath(it) }
                    markerData.color?.let { setColor(it) }
                    showCotDetails(false)
                    setNeverPersist(false)
                }

            markerCreator.placePoint().apply {
                setMetaBoolean("movable", true)
                setMetaString("how", "h-g-i-g-o")
                if (markerData.direction != null) {
                    style = Marker.STYLE_ROTATE_HEADING_MASK
                }
            }
        }
        marker?.apply {
            markerData.direction?.let { setTrack(it, markerData.speed ?: 0.0) }
            movable = false
        }
        markerData.mapGroup?.addItem(marker)
    }

    private fun removeMarker(
        mapView: MapView?,
        name: String,
        mapGroup: MapGroup? = null
    ) {
        val mapItem = mapView?.getMapItem(name)
        if (mapItem is Marker) {
            mapGroup?.removeItem(mapItem) ?: mapView.rootGroup?.removeItem(mapItem)
        }
    }

    companion object {
        private const val TAG = "WeatherIoTPluginMapComponent"
        private const val WAYPOINT_GREEN = "6d781afb-89a6-4c07-b2b9-a89748b6a38f/Waypoints/wptgreen.png"
    }
}
