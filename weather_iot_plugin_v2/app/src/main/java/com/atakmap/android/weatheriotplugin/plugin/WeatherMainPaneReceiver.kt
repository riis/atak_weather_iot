package com.atakmap.android.weatheriotplugin.plugin

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.atak.plugins.impl.PluginLayoutInflater
import com.atakmap.android.dropdown.DropDown.OnStateListener
import com.atakmap.android.dropdown.DropDownReceiver
import com.atakmap.android.maps.MapView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeatherMainPaneReceiver(
    mapView: MapView,
    private val pluginCtxLazy: () -> Context,
    private val coroutineScope: CoroutineScope,
    private val weatherViewModel: WeatherViewModel,
    private val onConnected: () -> Unit
) : DropDownReceiver(mapView), OnStateListener {

    private val ctx get() = pluginCtxLazy()
    private val mainView: View = PluginLayoutInflater.inflate(ctx, R.layout.main_layout, null)

    private var didChangeScreen = false

    init {
        val connectButton: Button = mainView.findViewById(R.id.mqtt_connect_btn)
        connectButton.setOnClickListener {
            val brokerUri = mainView.findViewById<EditText>(R.id.brokerUriText).text.toString()
            val portStr   = mainView.findViewById<EditText>(R.id.brokerPortEditText).text.toString()
            try {
                weatherViewModel.subMqtt(brokerUri, portStr.toInt())
            } catch (_: NumberFormatException) {
                Toast.makeText(mainView.context, "Invalid port number", Toast.LENGTH_SHORT).show()
            } catch (_: IllegalArgumentException) {
                Toast.makeText(mainView.context, "Invalid IP Address", Toast.LENGTH_SHORT).show()
            }
        }

        coroutineScope.launch {
            weatherViewModel.isConnected.drop(1).collect { isConnected ->
                if (isConnected.peekContent()) {
                    didChangeScreen = true
                    withContext(Dispatchers.Main) { onConnected() }
                } else if (weatherViewModel.isManuallyDisconnected.value.peekContent()) {
                    weatherViewModel.confirmManuallyDisconnected()
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            mainView.context,
                            "Failed to Connect! Please double check MQTT settings",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    fun show() {
        didChangeScreen = false
        showDropDown(
            mainView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH,
            HALF_HEIGHT, false, this
        )
    }

    override fun onDropDownClose() {
        if (!didChangeScreen) weatherViewModel.disconnectMqtt()
    }

    public override fun disposeImpl() { /* optional cleanup */ }

    override fun onDropDownSelectionRemoved() {}
    override fun onDropDownVisible(v: Boolean) {}
    override fun onDropDownSizeChanged(width: Double, height: Double) {}
    override fun onReceive(p0: Context?, p1: Intent?) {}

    companion object { private const val TAG = "WeatherMainPaneReceiver" }
}