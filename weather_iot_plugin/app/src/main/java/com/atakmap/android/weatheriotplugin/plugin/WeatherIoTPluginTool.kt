package com.atakmap.android.weatheriotplugin.plugin

import android.content.Context
import com.atak.plugins.impl.AbstractPluginTool
import com.atakmap.android.weatheriotplugin.WeatherIoTPluginMainDropDownReceiver
import com.atakmap.util.Disposable

/**
 * Please note:
 * Support for versions prior to 4.5.1 can make use of a copy of AbstractPluginTool shipped with
 * the plugin.
 */
class WeatherIoTPluginTool(context: Context) : AbstractPluginTool(
    context,
    context.getString(R.string.app_name),
    context.getString(R.string.app_name),
    context.resources.getDrawable(R.drawable.ic_launcher),
    WeatherIoTPluginMainDropDownReceiver.SHOW_MAIN
), Disposable {
    override fun dispose() {
    }
}
