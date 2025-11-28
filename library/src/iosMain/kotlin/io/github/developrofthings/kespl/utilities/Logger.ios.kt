package io.github.developrofthings.kespl.utilities

import platform.Foundation.NSLog

actual fun createLogger(enabled: Boolean): PlatformLogger =
    _root_ide_package_.io.github.developrofthings.kespl.utilities.IOSPlatformLogger(enabled = enabled)

internal class IOSPlatformLogger(
    override var enabled: Boolean,
): PlatformLogger {

    private fun performLog(prefix: String, message: String) = NSLog(format = "$prefix $message")

    override fun debug(tag: String, message: String) {
        if(enabled) performLog(prefix = tag, message = message)
    }

    override fun info(tag: String, message: String) {
        if(enabled) performLog(prefix = tag, message = message)
    }

    override fun warn(tag: String, message: String) {
        if(enabled) performLog(prefix = tag, message = message)
    }

    override fun error(tag: String, message: String) {
        if(enabled) performLog(prefix = tag, message = message)
    }
}