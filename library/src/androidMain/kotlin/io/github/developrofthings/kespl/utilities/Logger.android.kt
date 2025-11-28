package io.github.developrofthings.kespl.utilities

import android.util.Log

actual fun createLogger(enabled: Boolean): PlatformLogger = AndroidPlatformLogger(enabled = enabled)

class AndroidPlatformLogger(
    override var enabled: Boolean,
): PlatformLogger {
    override fun debug(tag: String, message: String) {
        if(enabled) Log.d(tag, message)
    }

    override fun info(tag: String, message: String) {
        if(enabled) Log.i(tag, message)
    }

    override fun warn(tag: String, message: String) {
        if(enabled) Log.w(tag, message)
    }

    override fun error(tag: String, message: String) {
        if(enabled) Log.e(tag, message)
    }
}