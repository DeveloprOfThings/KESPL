package io.github.developrofthings.kespl.utilities

interface PlatformLogger {
    fun debug(tag: String = "DBUG", message: String)
    fun info(tag: String = "INFO", message: String)
    fun warn(tag: String = "WARN", message: String)
    fun error(tag: String = "ERR", message: String)
    var enabled: Boolean
}

expect fun createLogger(enabled: Boolean = false): PlatformLogger