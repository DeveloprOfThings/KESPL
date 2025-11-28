@file:OptIn(ExperimentalUuidApi::class)

import io.github.developrofthings.kespl.bluetooth.EspUUID
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.CoreBluetooth.CBUUID
import platform.CoreBluetooth.CBUUID.Companion.UUIDWithString
import platform.Foundation.NSData
import platform.Foundation.NSUUID
import platform.Foundation.create
import platform.posix.memcpy
import toCBUUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun Uuid.toCBUUID(): CBUUID = UUIDWithString(theString = toString())

fun Uuid.toNSUUID(): NSUUID = NSUUID(uUIDString = toString())

fun NSUUID.toUuid(): Uuid = Uuid.parse(uuidString = this.UUIDString)

fun CBUUID.toUuid(): Uuid = Uuid.parse(uuidString = this.UUIDString)

val EspUUID.V1CONNECTION_LE_SERVICE_CBUUID: CBUUID get() =
    EspUUID.V1CONNECTION_LE_SERVICE_UUID.toCBUUID()

val EspUUID.CLIENT_OUT_V1_IN_SHORT_CHARACTERISTIC_CBUUID: CBUUID get() =
    EspUUID.CLIENT_OUT_V1_IN_SHORT_CHARACTERISTIC_UUID.toCBUUID()

val EspUUID.V1_OUT_CLIENT_IN_SHORT_CHARACTERISTIC_CBUUID: CBUUID get() =
    EspUUID.V1_OUT_CLIENT_IN_SHORT_CHARACTERISTIC_UUID.toCBUUID()


@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).apply {
    if(length > 0u) {
        usePinned {
            memcpy(
                __dst = it.addressOf(0),
                __src = bytes,
                __n = length
            )
        }
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun ByteArray.toNSData(): NSData {
    return memScoped {
        NSData.create(
            bytes = allocArrayOf(this@toNSData),
            length = this@toNSData.size.toULong()
        )
    }
}