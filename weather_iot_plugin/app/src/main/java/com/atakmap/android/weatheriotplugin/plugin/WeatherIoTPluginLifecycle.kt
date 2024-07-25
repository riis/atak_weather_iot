package com.atakmap.android.weatheriotplugin.plugin

import com.atak.plugins.impl.AbstractPlugin
import com.atak.plugins.impl.PluginContextProvider
import com.atakmap.android.weatheriotplugin.WeatherIoTPluginMapComponent
import com.atakmap.android.weatheriotplugin.plugin.PluginNativeLoader.init
import gov.tak.api.plugin.IServiceController


/**
 *
 * AbstractPluginLifeCycle shipped with
 * the plugin.
 */
class WeatherIoTPluginLifecycle(serviceController: IServiceController) : AbstractPlugin(
    serviceController, WeatherIoTPluginTool(
        serviceController.getService(
            PluginContextProvider::class.java
        ).pluginContext
    ), WeatherIoTPluginMapComponent()
) {
    init {
        init(
            serviceController.getService(
                PluginContextProvider::class.java
            ).pluginContext
        )
    }

    companion object {
        private const val TAG = "WeatherIoTPluginLifecycle"
    }
}
