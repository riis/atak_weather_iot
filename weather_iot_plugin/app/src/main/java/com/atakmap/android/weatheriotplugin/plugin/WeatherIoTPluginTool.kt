package com.atakmap.android.weatheriotplugin.plugin

import android.content.Context
import com.atak.plugins.impl.AbstractPluginTool
import com.atakmap.android.weatheriotplugin.R
import com.atakmap.android.weatheriotplugin.WeatherIoTPluginMainDropDownReceiver
import gov.tak.api.util.Disposable

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
