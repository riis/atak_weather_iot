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

class WeatherIoTPluginStationListDropDownReceiver(
    mapView: MapView?,
    pluginContext: Context,
    coroutineScope: CoroutineScope
): DropDownReceiver(mapView), OnStateListener {

    private val listView: View = PluginLayoutInflater.inflate(
        pluginContext,
        R.layout.station_list_layout,
        null
    )

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        if (action == SHOW_LIST) {
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
    }

    override fun onDropDownSizeChanged(p0: Double, p1: Double) {
    }

    override fun onDropDownVisible(p0: Boolean) {
    }

    companion object {
        private const val TAG = "WeatherIoTPluginStationListDropDownReceiver"
        const val SHOW_LIST: String = "com.atakmap.android.weatheriotplugin.SHOW_LIST"
    }
}