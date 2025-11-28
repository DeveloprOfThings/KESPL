package io.github.developrofthings.kespl

import okio.FileSystem

/**
 * Platform abstraction for accessing system resources and global information
 */
expect class ESPContext {
    internal val platformFileSystem: FileSystem

    fun isBluetoothSupported(): Boolean
    fun isBluetoothLESupported(): Boolean
    internal fun resolvePath(fileName: String): String
}