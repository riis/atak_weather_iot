
package com.atakmap.android.weatheriotplugin.plugin;


import com.atak.plugins.impl.AbstractPluginLifecycle;
import com.atakmap.android.weatheriotplugin.WeatherIoTPluginMapComponent;
import android.content.Context;


/**
 * Please note:
 *     Support for versions prior to 4.5.1 can make use of a copy of AbstractPluginLifeCycle shipped with
 *     the plugin.
 */
public class WeatherIoTPluginLifecycle extends AbstractPluginLifecycle {

    private final static String TAG = "WeatherIoTPluginLifecycle";

    public WeatherIoTPluginLifecycle(Context ctx) {
        super(ctx, new WeatherIoTPluginMapComponent());
        PluginNativeLoader.init(ctx);
    }

}
