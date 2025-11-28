package io.github.developrofthings.kespl.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.github.developrofthings.kespl.ESPContext
import io.github.developrofthings.kespl.utilities.extensions.acquireBtAdapter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.annotation.Factory

@Factory
class AndroidBluetoothStateManager(private val espContext: ESPContext) {

    fun enabled(): Flow<Boolean> {
       return callbackFlow {
           val receiver = object : BroadcastReceiver() {
               override fun onReceive(
                   context: Context,
                   intent: Intent
               ) {
                   val action = intent.action
                   if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                       val current = intent.getIntExtra(
                           /* name = */ BluetoothAdapter.EXTRA_STATE,
                           /* defaultValue = */ BluetoothAdapter.STATE_OFF,
                       )
                       trySend(
                           element = current.bluetoothAdapterStateToBoolean()
                       )
                   }
               }
           }

           espContext.appContext.registerReceiver(
               /* receiver = */ receiver,
               /* filter = */ IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
           )
           // Not sure if this is necessary but just in-case the BroadcastReceiver doesn't fire
           // after registration we want to emit the current Bluetooth Radio state
           trySend(
               element = espContext.acquireBtAdapter().isEnabled
           )


           // Suspend until the flow collection has been cancelled, at that point clear the broadcast
           // receiver
           awaitClose {
               espContext.appContext.unregisterReceiver(receiver)
           }
       }
    }
}

private fun Int.bluetoothAdapterStateToBoolean(): Boolean = when(this) {
    BluetoothAdapter.STATE_ON -> true
    BluetoothAdapter.STATE_OFF -> false
    BluetoothAdapter.STATE_TURNING_OFF -> false
    BluetoothAdapter.STATE_TURNING_ON -> false
    else -> false
}