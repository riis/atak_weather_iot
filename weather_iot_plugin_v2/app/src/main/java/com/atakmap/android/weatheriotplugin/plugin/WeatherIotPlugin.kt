package com.atakmap.android.weatheriotplugin.plugin


import android.content.Context
import android.graphics.drawable.Drawable
import com.atak.plugins.impl.PluginContextProvider
import com.atakmap.android.maps.MapView
import com.atakmap.android.weatheriotplugin.plugin.R
import gov.tak.api.commons.graphics.Bitmap
import gov.tak.api.plugin.IPlugin
import gov.tak.api.plugin.IServiceController
import gov.tak.api.ui.IHostUIService
import gov.tak.api.ui.ToolbarItem
import gov.tak.api.ui.ToolbarItemAdapter
import gov.tak.platform.marshal.MarshalManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import com.atakmap.android.weatheriotplugin.plugin.WeatherMainPaneReceiver
import com.atakmap.android.weatheriotplugin.plugin.WeatherListPaneReceiver
import com.atakmap.android.weatheriotplugin.plugin.WeatherDetailPaneReceiver
import com.atakmap.android.weatheriotplugin.plugin.WeatherViewModel

class WeatherIotPlugin(serviceController: IServiceController) : IPlugin {

    private lateinit var pluginCtx: Context
    private lateinit var uiService: IHostUIService
    private lateinit var toolbarItem: ToolbarItem

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val viewModel: WeatherViewModel = WeatherViewModel(scope)

    private lateinit var mapView: MapView
    private lateinit var mainPane: WeatherMainPaneReceiver
    private lateinit var listPane: WeatherListPaneReceiver
    private lateinit var detailPane: WeatherDetailPaneReceiver

    init {
        val ctxProvider: PluginContextProvider = serviceController
            .getService(PluginContextProvider::class.java)
            ?: throw IllegalStateException("Missing PluginContextProvider")

        pluginCtx = ctxProvider.pluginContext.apply { setTheme(R.style.ATAKPluginTheme) }

        uiService = serviceController.getService(IHostUIService::class.java)
            ?: throw IllegalStateException("Missing IHostUIService")

        mapView = MapView.getMapView()
        mainPane = WeatherMainPaneReceiver(
            mapView = mapView,
            pluginCtxLazy = { pluginCtx },
            coroutineScope = scope,
            weatherViewModel = viewModel,
            onConnected = { showListPane() }
        )

        listPane = WeatherListPaneReceiver(
            mapView = mapView,
            pluginCtxLazy = { pluginCtx },
            coroutineScope = scope,
            weatherViewModel = viewModel,
            onSelect = { showDetailPane() },
            onDisconnect = { showMainPane() }
        )

        detailPane = WeatherDetailPaneReceiver(
            mapView = mapView,
            pluginCtxLazy = { pluginCtx },
            coroutineScope = scope,
            weatherViewModel = viewModel,
            onBack = { showListPane() }
        )

        toolbarItem = ToolbarItem.Builder(
            pluginCtx.getString(R.string.app_name),
            MarshalManager.marshal(
                pluginCtx.resources.getDrawable(R.drawable.ic_launcher),
                Drawable::class.java,
                Bitmap::class.java
            )
        ).setListener(object : ToolbarItemAdapter() {
            override fun onClick(item: ToolbarItem?) {
                showMainPane()
            }
        }).build()
    }

    override fun onStart() {
        uiService.addToolbarItem(toolbarItem)
    }

    override fun onStop() {
        uiService.removeToolbarItem(toolbarItem)
        mainPane.disposeImpl()
        listPane.disposeImpl()
        detailPane.disposeImpl()
        scope.cancel()
    }

    private fun showMainPane(): Unit = mainPane.show()
    private fun showListPane(): Unit = listPane.show()
    private fun showDetailPane(): Unit = detailPane.show()
}

