package io.github.developrofthings.kespl

import android.content.Context
import io.github.developrofthings.kespl.utilities.extensions.isBTSupported
import io.github.developrofthings.kespl.utilities.extensions.isLESupported
import okio.FileSystem

/**
 * Android implementation of the "ESPContext" contract that is effectively a light-weight wrapper
 * around [Context] to support accessing system resources and global information.
 */
actual class ESPContext(val appContext: Context) {
    internal actual val platformFileSystem: FileSystem = FileSystem.SYSTEM

    actual fun isBluetoothSupported(): Boolean = appContext.isBTSupported()

    actual fun isBluetoothLESupported(): Boolean = appContext.isLESupported()

    internal actual fun resolvePath(fileName: String): String =
        appContext.filesDir.resolve(relative = fileName).absolutePath
}
