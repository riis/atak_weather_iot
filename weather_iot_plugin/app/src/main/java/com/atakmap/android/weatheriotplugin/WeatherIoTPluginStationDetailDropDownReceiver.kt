package com.atakmap.android.weatheriotplugin

import android.content.Context
import android.content.Intent
import android.view.View
import com.atak.plugins.impl.PluginLayoutInflater
import com.atakmap.android.dropdown.DropDown.OnStateListener
import com.atakmap.android.dropdown.DropDownReceiver
import com.atakmap.android.maps.MapView
import com.atakmap.android.weatheriotplugin.plugin.R
import com.atakmap.coremap.log.Log
import kotlinx.coroutines.CoroutineScope

class WeatherIoTPluginStationDetailDropDownReceiver(
    mapView: MapView?,
    pluginContext: Context,
    coroutineScope: CoroutineScope
): DropDownReceiver(mapView), OnStateListener {

    private val detailView: View = PluginLayoutInflater.inflate(
        pluginContext,
        R.layout.station_detail_layout,
        null
    )

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

    companion object {
        private const val TAG = "WeatherIoTPluginStationDetailDropDownReceiver"
        const val SHOW_DETAIL: String = "com.atakmap.android.weatheriotplugin.SHOW_DETAIL"
    }
}
