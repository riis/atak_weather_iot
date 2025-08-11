package com.atakmap.android.weatheriotplugin.plugin.utils

import android.os.Bundle
import androidx.annotation.IdRes

open class SingleEvent<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun getContent(): T? {
        return content
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}

sealed class NavEvent {
    data class To(@IdRes val directions: Int, val bundle: Bundle? = null) : NavEvent()
    object Up : NavEvent()
    object Back : NavEvent()
}