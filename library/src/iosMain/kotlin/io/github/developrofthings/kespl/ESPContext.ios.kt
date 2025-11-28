package io.github.developrofthings.kespl

import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * iOS implementation of the "ESPContext" contract for accessing system resources and global
 * information.
 * @constructor configures an instance with a [Boolean] flag indicating context is for an iPhone
 * simulator environment.
 */
actual class ESPContext(val isSimulator: Boolean = false) {
    internal actual val platformFileSystem: FileSystem = FileSystem.SYSTEM
    actual fun isBluetoothSupported(): Boolean {
        // iOS offers no synchronous API to determine if Bluetooth is supported but technically
        // all devices support BT
        return !isSimulator
    }

    actual fun isBluetoothLESupported(): Boolean {
        // iOS offers no synchronous API to determine if Bluetooth is supported but technically
        // all devices support BT LE
        return !isSimulator
    }

    internal actual fun resolvePath(fileName: String): String {
        @OptIn(ExperimentalForeignApi::class)
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        return requireNotNull(documentDirectory).path + "/$fileName"
    }
}