# KESPL - Kotlin Extended Serial Protocol Library

**K**otlin **E**extended **S**erial **P**rotocol **L**ibrary provides a modern, suspending API for 
two-way communication. It enables developers to build cross-platform mobile apps for the Valentine 
One (V1) radar locator and other ESP-enabled devices manufactured by Valentine Research Inc. 
KESPL makes extensive use of Kotlin's [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) 
library to handle all asynchronous operations needed to instrument two-way communication with ESP 
devices. Instead of passing around callbacks to request data, 
[IESPClient](library/src/commonMain/kotlin/com/esp/library/IESPClient.kt) provides _suspending_ 
functions for all possible ESP "requests". (see: _**ESP Packet Quick Reference**_ of the **Extended 
Serial Protocol User Guide**).

## 📱 Demo
**KESPL** was built using [Kotlin Multiplatform](https://www.jetbrains.com/kotlin-multiplatform/) (**KMP**), it supports both Android and iOS
using the native Bluetooth APIs on both platforms. Sample integrations of **KESPL** have been provided
for both platforms as well. Since **KESPL** was built using **KMP**, the sample apps were
able to be built using [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
(CMP). Compose Multiplatform is a declarative framework for sharing UI code across multiple
platforms with Kotlin. It is based on the similarly named declarative framework used in modern
Android development, [Jetpack Compose](https://developer.android.com/compose). **CMP** is in active
development by a joint collaboration of [Jetbrains](https://www.jetbrains.com/) and the open-source
community.

### Pixel 7 Pro
<img src="/artwork/pixel7pro_demo.gif" width="35%" height="35%"/>

### iPad
<img src="/artwork/ipad_demo.gif" width="75%" height="75%"/>

## 🚀 Getting Started (Installation)
```Kotlin
kotlin {
    // ... other configurations
    sourceSets {
        // ... other source sets ie Android, iOS 
        commonMain.dependencies {
            // ... Other common source dependencies
            implementation("io.github.developrofthings:kespl:0.9.2")
        }
    }
}
```

## ⚙️ Quick Usage - Android Example
Before using **KESPL**, the library must be initialized via a call to `IESPClient.init(ESPContext)`, 
passing in platform `ESPContext` instance. It's recommended to initialize the library at startup 
via a `android.app.Application` subclass:

```Kotlin
class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        IESPClient.init(espContext = ESPContext(appContext = this))
    }
}
```

Here is a very basic example using a `androidx.lifecycle.ViewModel`, that acquires an 
"auto-configured" `IESPClient` and connecting to the last **connected** or the `V1connection` with
the strongest **RSSI**. This example is purely for demonstrative purposes and shouldn't be directly 
used because it doesn't handle system permissions or checking for BT support. 

```Kotlin
class ExampleViewModel: ViewModel() {
    
    // Assumes IESPClient has been initialized via a call to `IESPClient.init(ESPContext)`
    val client = IESPClient
        // Auto select the "best" connection based on device capabilities
        .getClient(preference = V1ConnectionTypePreference.Auto)

    fun connect() {
        viewModelScope.launch {
            val client = IESPClient
                // Auto select the "best" connection based on device capabilities
                .getClient(preference = V1ConnectionTypePreference.Auto)
            val success = client
                .connect(
                    connectionStrategy = ConnectionStrategy.LastThenStrongest,
                    scanDurationMillis = 5.seconds
                )
            if(!success) {
                println("We failed to connect!!!")
                return@launch
            }
            println("We've connected")
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            client.disconnect()
        }
    }
}
```
> [!NOTE]
> for iOS/KMP Developers: While this example shows Android context initialization, the rest of
> the suspending API usage within viewModelScope.launch is identical across all Kotlin Multiplatform 
> targets._

### V1 Capabilities
To determine capabilities/features of the attached Valentine One, developers can use the 
`IESPClient.v1CapabilityInfo` state flow which contains the most recently "calculated" 
`V1CapabilityInfo`. This class is made up of `Boolean` flags that are set based on firmware version 
of a Valentine One. **KESPL** automatically requests & caches (`StateFlow`) the V1 firmware on 
connect. 

The following example can be used to determine if the attached Valentine One is a "Gen2":
```Kotlin
    espClient
        .v1CapabilityInfo
        .map { it.isGen2 }
```
"Collecting" from this flow is the recommended way for conditionally enabling/disabling app 
features/UI. 

### 📦 ESPPacket
All communication on the ESP bus is achieved through 
[ESPPacket](library/src/commonMain/kotlin/com/esp/library/packet/ESPPacket.kt). `ESPPacket` is a 
thin/light wrapper around a well structured `ByteArray` containing an optional payload. Instead of 
offering subclasses of `ESPPacket`, to retrieve response data **KESPL** goes for a more simplified 
approach and instead offers extensive extension functions that interprets `payload` data to the 
desired data. As an example, here is how you'd retrieve a version from an `respVersion` ($02):

```Kotlin
    espClient
        .packets
        .filter { it.isVersion }
        .collect {
            val versionString = it.version()
        }
```

<details>
<summary>Flows</summary>

Kotlin's `Flow` API is a foundational building block around which **KESPL**'s suspending API is built.
As "ESP" data is received via a connected `V1connection`, it is verified validity and then is 
emitted into an internal `Flow` that `IESPClient` performs 
fairly complex filtering and transformations to convert the raw `byte` data into the requested data.
For developer convenience, `IESPClient` exposes a flow of `ESPPacket` as well as several others to 
to observe `AlertData`, `DisplayData`, and stateful information such as `V1Type` and Valentine One 
capabilities.

### Stateful

| Events                         | Explanation                                               |
|--------------------------------|-----------------------------------------------------------|
| `IESPClient.v1CapabilityInfo`  | Detected capabilities/functions of attached Valentine One |
| `IESPClient.connectionStatus`  | Client's connection status w/ a `V1connection`            |
| `IESPClient.valentineOneType`  | Detected type of the attached Valentine One               |

### Event

| Events                        | Explanation                                     |
|-------------------------------|-------------------------------------------------|
| `IESPClient.noData`           | Stream of "No ESP" Data received notifications  |
| `IESPClient.notificationData` | Stream of Demo Mode "notification" messages     |

### ESP Data

| Events                            | Explanation                                                    |
|-----------------------------------|----------------------------------------------------------------|
| `IESPClient.packets`              | Stream of `ESPPacket` received from the **ESP bus**            |
| `IESPClient.displayData`          | Stream of `DisplayData` received from the **ESP bus**          |
| `IESPClient.alertTable`           | Stream of "complete" alert tables                              |
| `IESPClient.alertTableClosable`   | Same as `IESPClient.alertTable` but collection starts sending  |
| `IESPClient.priorityAlert`        | Detected "priority" `AlertData` from Valentine One alert table |
| `IESPClient.junkAlerts`           | Detected "junk" `AlertData` from Valentine One alert table     |

`IESPClient.packets` should be considered a "core" `Flow` for ESP observation. It is effectively a 
direct "wire" upon which all "ESP" data received via the **ESP bus** can be observed with no 
additional pre-processing/filter. This flow is used as a "source" for all other data `Flow`s exposed 
by `IESPClient` by applying the appropriate [`.filter {...}`](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/filter.html)
and [`.map {...}`](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/map.html) 
logic to return the expected data.

#### 📺 Display Data

The following are conveniences [Flow]s for extracting display state from the most recently received
`DisplayData` ie `IESPClient.displayData`by applying [`.filter {...}`](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/filter.html)
and [`.map {...}`](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/map.html):

| Display Data                       | Explanation                                                                               |
|------------------------------------|-------------------------------------------------------------------------------------------|
| `IESPClient.isDisplayOn`           | Indicates if the Valentine One's display is "on"                                          |
| `IESPClient.isSoft`                | Indicates if the Valentine One's audio is muted                                           |
| `IESPClient.isEuro`                | Indicates if the Valentine One is operating in "Euro" mode                                |
| `IESPClient.isLegacy`              | Indicates if the Valentine One is operating in "legacy" mode (No ESP processing)          |
| `IESPClient.isDisplayActive`       | Indicates if the Valentine One's is displaying an alert or volume                         |
| `IESPClient.isCustomSweep`         | Indicates if the Valentine One has custom sweeps defined that will be used in "Euro" mode |
| `IESPClient.isTimeSlicing`         | Indicates if the Valentine One is time slicing                                            |
| `IESPClient.isSearchingForAlerts`  | Indicates if the Valentine One is "sweeping" for alerts                                   |
| `IESPClient.infDisplayDataMode`    | Indicates if the logic mode the Valentine One is operating in (requires: **V4.1028**)     |

### Custom Flow Example
#### Simple
Below is **very** crude `Flow` to observe the Valentine One's logic mode prior to firmware 
version **V4.1028**, where latter versions contain logic mode bits were added to 
`infDisplayData.Aux1` byte:  
```Kotlin
espClient
    .packets
    .filterIsDisplayData()
    .filter { it.isSearchingForAlerts }
    .map { it.bogeyCounterMode }
    .filterNot { it == V1Mode.Invalid }
    .distinctUntilChanged()
```

This new flow is straightforward. It works by filtering for `DisplayData` that indicates the 
Valentine One is sweeping (completed sign-on sequence), then attempts to map the bogey counter 
image byte into a `V1Mode`. Since bogey counter method is not reliable, we need to explicitly 
filter out `V1Mode.Invalid` which will be returned if the Valentine One isn't idle (alerting or 
showing mode).

#### Advanced
Here is an advanced `Flow` that combines two separate `Flow`s to more reliably detects the Valentine
One's logic mode.

```Kotlin
 flow {
    combine(
        flow = espClient
            .v1CapabilityInfo
            .map { it.hasInfDisplayDataLogicMode },
        flow2 = espClient
            .packets
            .filterIsDisplayData()
            .filter { it.isSearchingForAlerts },
        ::Pair
    ).collect { (hasLogicMode, displayData) ->
        emit(value = if(hasLogicMode) displayData.mode else displayData.bogeyCounterMode)
    }
}
    .filterNot { it == V1Mode.Invalid }
    .distinctUntilChanged()
```

This new flow works by combining two separate flows into a `Flow` of `Pair<Boolean, DisplayData>`. 
The first `Flow` indicates if _infDisplayData_ contains logic mode bits in the `Aux1` byte. The 
second is a `Flow` of `DisplayData` that's filtered to make sure the Valentine One is sweeping 
(completed sign-on sequence).

Next, we collect the combined data to determine how the logic mode 
should be detected. If the _infDisplayData_ contains logic mode bits then`DisplayData.mode` is used,
otherwise the flow falls back to trying to determine the logic mode using the bogey counter image
byte. Since bogey counter method is not reliable, we need to explicitly filter out `V1Mode.Invalid` 
which will be returned if the Valentine One isn't idle (alerting or showing mode).
</details>

## 🛜 Scanning
To discover nearby `V1connection` for a given `V1cType` you must acquire a `IV1cScanner` instance by
calling `IV1cScanner.getScanner(V1cType)`. The `startScan(ESPScanMode)` will return a `Flow` of 
scanned `V1connection`s. (The `ESPScanMode` argument only used when scanning for`V1cType.LE` on the 
Android platform).  
```Kotlin
IV1cScanner
    .getScanner(connType = scanType)
    .startScan(ESPScanMode.Balanced)
    .collect { scanResult ->
        // Do something with result
    }
```
## 🔌 Connection
There are multiple options for establishing a connection with a `V1connection`. 
The `IESPClient` contains two `connect(...)` overloads and that affect how a connection is 
established. The overload that accepts a `ConnectionStrategy` enum and scan `Duration` provides the 
simplest and most robust connection mechanism for consumers. 

The `ConnectionStrategy` contains enumerations that allow you to establish a connection using 
the following rules:
* `ConnectionStrategy.First` - First scanned `V1connection`
* `ConnectionStrategy.Last` - Last scanned `V1connection` at end of scan window
* `ConnectionStrategy.Strongest` - `V1connection` with strongest _**RSSI**_ at end of scan window
* `ConnectionStrategy.LastThenStrongest` - The last connected `V1connection` or  with strongest _**RSSI**_ if it cannot be found

`ConnectionStrategy.LastThenStrongest` is the recommended strategy as it's offers functionality most
users would want; reconnect to last used `V1connection` or connect to the strongest one available. 

If developers prefer to handle `V1connection` discovery themselves `IESPClient` has a 
`connect(...)` overload that accepts a `V1connection` and side-steps pre-discovery. This overload 
also accepts a `directConnect` argument that provides developers the option to attempt a connection 
that doesn't timeout. This is ideal for background re/connections.

## 📞 Callbacks (Legacy)
To improve integration w/ existing app's that rely on the "callback" architecture, a **callback** 
library (`kespl-callbacks`) is available. Currently, the library offers 6 _listener_ interfaces:


| Events                          | Explanation                                           |
|---------------------------------|-------------------------------------------------------|
| `ESPConnectionStatusListener`   | Listen to client's connection status                  |
| `NoDataListener`                | Listen to "NO ESP" data notifications                 |
| `NotificationListener`          | Listen to Demo Mode "notification" messages           |
| `ESPPacketListener`             | Listen to `ESPPacket` received from the **ESP bus**   |
| `DisplayDataListener`           | Listen to `DisplayData` received from the **ESP bus** |
| `AlertTableListener`            | Listen to "complete" alert tables                     |

> [!NOTE]
> Since `IESPClient.connectionStatus` is a `StateFlow`, **initial** registration of a
> `ESPConnectionStatusListener` will cause it's `onConnectionStatusChange(...)` function to be 
> invoked with the client's current connection status (initially 
> `ESPConnectionStatus.Disconnected`). 

These callbacks are invoked by collecting from the respective `Flows` available in `IESPClient`. A
similar pattern can be achieved by developers to listen for additional data/events not currently
supported.

Add the following declaration to your app-module's `build.gradle`/`build.gradle.kts` dependency 
block:
```Kotlin
implementation("io.github.developrofthings:kespl-callbacks:0.9.5")
```

The callback registration functions have been declared as extension functions on `IESPClient`. Each 
callback has two deregistration functions. One for unregistering a single callback and another for 
clear all registered callbacks ie `unregisterConnectionListeners()`.

> [!NOTE]
> Removing all callbacks will cancel `Flow` collection.


Here's an example for listening to `ESPConnectionStatus` :
```Kotlin
espClient.registerConnectionListener { status ->
    // TODO do something w/ status
}

...
// Use this function with passing "inline" listener ie lambda
espClient.unregisterConnectionListeners()

// or use
espClient.unregisterConnectionListener(listener = ...)
```

## √ ESP Specification
**KESPL** is based on **v.3.015** of the ESP Specification which can be found on the official Github 
repo for [AndroidESPLibrary2](https://github.com/ValentineResearch/AndroidESPLibrary2/tree/master/Specification).

## Stability
The library is currently in beta but the API shape is considered **stable**. I hope for the library 
to reach **v.1.0.0** before the end of 2025. Developers are encouraged to play with the library and 
provide feedback on ease-of-use and correct behavior.

## ⚖️ License

KESPL is distributed under the **MIT License**. See [LICENSE](LICENSE) for more information.

> **Note on Core Functionality:**
> The fundamental approach for Bluetooth LE (GATT) communication using a suspending API was heavily 
> inspired by the implementation in the **Kotlin-BLE-Library** by Nordic Semiconductor. While 
> implementation used in KESPL written from scratch (**NO COPY & PASTE**), their original project 
> (licensed under the **BSD-3-Clause license**) was invaluable. You can find the original project 
> [here]([https://github.com/NordicSemiconductor/Kotlin-BLE-Library]).
