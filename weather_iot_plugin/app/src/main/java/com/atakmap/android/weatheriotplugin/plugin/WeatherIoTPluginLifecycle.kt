package com.atakmap.android.weatheriotplugin.plugin

import android.content.Context
import com.atak.plugins.impl.AbstractPluginLifecycle
import com.atakmap.android.weatheriotplugin.WeatherIoTPluginMapComponent


/**
 * Please note:
 * Support for versions prior to 4.5.1 can make use of a copy of AbstractPluginLifeCycle shipped with
 * the plugin.
 */
class WeatherIoTPluginLifecycle(ctx: Context) :
    AbstractPluginLifecycle(ctx, WeatherIoTPluginMapComponent()) {
    init {
        PluginNativeLoader.init(ctx)
    }

    companion object {
        private const val TAG = "WeatherIoTPluginLifecycle"
    }
}
