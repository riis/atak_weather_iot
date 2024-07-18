package com.atakmap.android.weatheriotplugin.plugin.data

import com.atakmap.android.maps.MapGroup
import com.atakmap.android.maps.MapView

data class MarkerData(
    val mapView: MapView,
    val name: String,
    val lat: Double,
    val lon: Double,
    val color: Int? = null,
    val icon: String? = null,
    val direction: Double? = null,
    val speed: Double? = null,
    val mapGroup: MapGroup? = null
)