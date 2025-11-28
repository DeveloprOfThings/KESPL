package io.github.developrofthings.kespl

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import io.github.developrofthings.kespl.bluetooth.AndroidBluetoothManager
import io.github.developrofthings.kespl.bluetooth.AndroidBluetoothStateManager
import io.github.developrofthings.kespl.bluetooth.IBluetoothManager
import io.github.developrofthings.kespl.bluetooth.V1cType
import io.github.developrofthings.kespl.bluetooth.connection.demo.DemoConnection
import io.github.developrofthings.kespl.bluetooth.connection.le.LeConnection
import io.github.developrofthings.kespl.bluetooth.connection.legacy.LegacyConnection
import io.github.developrofthings.kespl.di.ESPIsolatedKoinContext
import io.github.developrofthings.kespl.utilities.PlatformLogger
import io.github.developrofthings.kespl.utilities.createLogger
import io.github.developrofthings.kespl.utilities.extensions.acquireBtAdapter
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IESPClientTest {

    private val platformContext = mockk<Context> {
        val pkgManager = mockk<PackageManager>()
        every { packageManager } returns pkgManager
    }

    private val espContext = ESPContext(appContext = platformContext)

    private val testModules = listOf(
        module {
            single { createLogger(false) } bind PlatformLogger::class
            /*
            IMPORTANT

            The ESP library uses an Isolated Koin Context so that DI doesn't interface with any Koin
            DI a consuming app may use. As a direct result of context isolation is we cannot take
            use of Koin's start and stop functions to reinitialize the DI graph. Effectively the
            ESP isolated context is a singleton and once initialized its always initialized since
            there are no hooks to easily unload and load modules.. This introduces a problem for us
            since unit test case can/will mock conflicting functionality/behavior that will cause
            subsequent test to fail when run as a suite. To work around this, we take advantage of
            Koin ability to allow you to overwrite existing DI definition. So we want to always
            "force" a new singleton DI definition of android.content.Context, ESPContext, and
            AndroidBluetoothManager This will make sure every test case has a "clean" mock object
            which they can guarantee no side-effects from other test cases.
             */
            single { platformContext }

            single { espContext }

            single {
                AndroidBluetoothManager(
                    espContext = get<ESPContext>(),
                    bluetoothStateManager = get<AndroidBluetoothStateManager>(),
                )
            } bind (IBluetoothManager::class)
        }
    )

    @BeforeTest
    fun setup() {
        IESPClient.init(
            espContext = espContext,
            loggingEnabled = false,
        )
        ESPIsolatedKoinContext.koin.loadModules(testModules)
        // Mock the static Log class before each test
        mockkStatic(Log::class)

    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
        // Unload test modules
        ESPIsolatedKoinContext.koin.unloadModules(testModules)
        // Unmock the static Log class after each test
        unmockkStatic(Log::class)
    }

    @Test
    fun `When IESPClient_init(context, loggingEnabled = false), then logging is disabled`() {
        val logger = ESPIsolatedKoinContext.koin.get<PlatformLogger>()
        assertNotNull(actual = logger)
        assertFalse(actual = logger.enabled)
    }

    @Test
    fun `When IESPClient_enableLogging(enabled = true), then logging is disabled`() {
        every { Log.d(any(), any()) } returns 1
        IESPClient.enableLogging(enabled = false)
        val logger = ESPIsolatedKoinContext.koin.get<PlatformLogger>()
        assertNotNull(actual = logger)
        logger.debug(message = "Test DBUG Log!")
        logger.info(message = "Test INFO Log!")
        logger.warn(message = "Test WARN Log!")
        logger.error(message = "Test ERR Log!")
        // Make sure the functions weren't called
        verify(exactly = 0) { Log.d(any<String>(), any<String>()) }
        verify(exactly = 0) { Log.i(any<String>(), any<String>()) }
        verify(exactly = 0) { Log.w(any<String>(), any<String>()) }
        verify(exactly = 0) { Log.e(any<String>(), any<String>()) }
    }

    @Test
    fun `When IESPClient_enableLogging(enabled = false), then logging is enabled`() {
        every { Log.d(any<String>(), any<String>()) } returns 1
        every { Log.i(any<String>(), any<String>()) } returns 1
        every { Log.w(any<String>(), any<String>()) } returns 1
        every { Log.e(any<String>(), any<String>()) } returns 1
        // First we want to make sure we have a logger... this verify that the IESPClient.init(...)
        // works
        IESPClient.enableLogging(enabled = true)
        val logger = ESPIsolatedKoinContext.koin.get<PlatformLogger>()
        assertNotNull(actual = logger)
        logger.debug(message = "Test DBUG Log!")
        logger.info(message = "Test INFO Log!")
        logger.warn(message = "Test WARN Log!")
        logger.error(message = "Test ERR Log!")
        // Make sure the functions weren't called
        verify(exactly = 1) { Log.d("DBUG", "Test DBUG Log!") }
        verify(exactly = 1) { Log.i("INFO", "Test INFO Log!") }
        verify(exactly = 1) { Log.w("WARN", "Test WARN Log!") }
        verify(exactly = 1) { Log.e("ERR", "Test ERR Log!") }
    }

    @Test
    fun `When BT is not supported, then IESPClient_checkForBluetoothSupport() returns false`() =
        runTest {
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns false
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
            }
            val actual = IESPClient.querySystemBluetoothSupport()
            assertFalse(actual = actual)
        }

    @Test
    fun `When BLE is not supported, then IESPClient_checkForBluetoothSupport() returns false`() =
        runTest {
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
            }
            val actual = IESPClient.querySystemBluetoothLESupport()
            assertFalse(actual = actual)
        }

    @Test
    fun `When BT is supported, then IESPClient_checkForBluetoothSupport() returns true`() =
        runTest {
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
            }
            val actual = IESPClient.querySystemBluetoothSupport()
            assertTrue(actual = actual)
        }

    @Test
    fun `When BLE is supported, then IESPClient_checkForBluetoothSupport() returns true`() =
        runTest {
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns true
            }

            val actual = IESPClient.querySystemBluetoothLESupport()
            assertTrue(actual = actual)
        }

    @Test
    fun `IESPClient_getDemoClient() return ESPClient instance configured for demo`() = runTest {
        val client = assertIs<ESPClient>(IESPClient.getDemoClient(scope = backgroundScope))
        assertIs<DemoConnection>(client.connection)
        assertEquals(expected = V1cType.Demo, actual = client.connectionType)
    }

    @Test
    fun `When BT is not supported, then resolveConnectionType(V1ConnectionTypePreference_Legacy) returns V1cType_Legacy`() =
        runTest {
            // When
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns false
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
            }

            val expect = V1cType.Legacy

            // Then
            val actual = V1ConnectionTypePreference.Legacy.resolveConnectionType(espContext)
            assertEquals(
                expected = expect,
                actual = actual,
            )
        }

    @Test
    fun `When BT is supported, then resolveConnectionType(V1ConnectionTypePreference_Legacy) returns V1cType_Legacy`() =
        runTest {
            // When
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
            }

            val expect = V1cType.Legacy

            // Then
            val actual = V1ConnectionTypePreference.Legacy.resolveConnectionType(espContext)
            assertEquals(
                expected = expect,
                actual = actual,
            )
        }

    @Test
    fun `When BLE is not supported, then resolveConnectionType(V1ConnectionTypePreference_LE) returns V1cType_LE`() =
        runTest {
            // When
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
            }

            val espContext = ESPContext(appContext = platformContext)
            val expect = V1cType.LE

            // Then
            val actual = V1ConnectionTypePreference.LE.resolveConnectionType(espContext)
            assertEquals(
                expected = expect,
                actual = actual,
            )
        }

    @Test
    fun `When BLE is supported, then resolveConnectionType(V1ConnectionTypePreference_LE) returns V1cType_LE`() =
        runTest {
            // When
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns true
            }

            val espContext = ESPContext(appContext = platformContext)
            val expect = V1cType.LE

            // Then
            val actual = V1ConnectionTypePreference.LE.resolveConnectionType(espContext)
            assertEquals(
                expected = expect,
                actual = actual,
            )
        }

    @Test
    fun `When BT is not supported, then resolveConnectionType(V1ConnectionTypePreference_Auto) returns V1cType_Demo`() =
        runTest {
            // When
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns false
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
            }
            val expect = V1cType.Demo

            // Then
            val actual = V1ConnectionTypePreference.Auto.resolveConnectionType(espContext)
            assertEquals(
                expected = expect,
                actual = actual,
            )
        }

    @Test
    fun `When BT is supported, then resolveConnectionType(V1ConnectionTypePreference_Auto) returns V1cType_Legacy`() =
        runTest {
            // When
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
            }
            val expect = V1cType.Legacy

            // Then
            val actual = V1ConnectionTypePreference.Auto.resolveConnectionType(espContext)
            assertEquals(
                expected = expect,
                actual = actual,
            )
        }

    @Test
    fun `When BLE is supported, then resolveConnectionType(V1ConnectionTypePreference_Auto) returns V1cType_Legacy`() =
        runTest {
            // When
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns true
            }
            val expect = V1cType.LE

            // Then
            val actual = V1ConnectionTypePreference.Auto.resolveConnectionType(espContext)
            assertEquals(
                expected = expect,
                actual = actual,
            )
        }

    @Test
    fun `When BT is not supported, then getConnection(V1cType_Demo) return DemoConnection`() =
        runTest {
            // When
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns true
            }

            // Then
            val connection = getConnection(
                espContext = espContext,
                connectionScope = this,
                connType = V1cType.Demo,
            )
            assertIs<DemoConnection>(connection)
        }

    @Test
    fun `When BT is supported, then getConnection(V1cType_Demo) return DemoConnection`() = runTest {
        // When
        platformContext.packageManager.apply {
            every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
            every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
        }

        // Then
        val connection = getConnection(
            espContext = espContext,
            connectionScope = this,
            connType = V1cType.Demo,
        )
        assertIs<DemoConnection>(connection)
    }

    @Test
    fun `When BLE is supported, then getConnection(V1cType_Demo) return DemoConnection`() =
        runTest {
            // When
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns true
            }

            // Then
            val connection = getConnection(
                espContext = espContext,
                connectionScope = this,
                connType = V1cType.Demo,
            )
            assertIs<DemoConnection>(connection)
        }

    @Test
    fun `When BT is not supported, then getConnection(V1cType_Legacy) throws BTUnsupported exception`() =
        runTest {
            // When
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns false
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
            }

            // Then
            assertFailsWith<BTUnsupported> {
                getConnection(
                    espContext = espContext,
                    connectionScope = this,
                    connType = V1cType.Legacy,
                )
            }
        }

    @Test
    fun `When BT is supported, then getConnection(V1cType_Legacy) returns LegacyConnection`() =
        runTest {
            // When
            // Mock BT support and Bluetooth Adapter
            platformContext.apply {
                packageManager.apply {
                    every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                    every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
                }

                val btManager = mockk<BluetoothManager> {
                    val btAdapter = mockk<BluetoothAdapter>()
                    every { adapter } returns btAdapter
                }

                every { getSystemService(Context.BLUETOOTH_SERVICE) } returns btManager
            }

            assertNotNull(espContext.acquireBtAdapter())

            // Then
            val connection = getConnection(
                espContext = espContext,
                connectionScope = this,
                connType = V1cType.Legacy,
            )
            assertIs<LegacyConnection>(connection)
        }

    @Test
    fun `When BLE is not supported, then getConnection(V1cType_LE) throws LeUnsupported exception`() =
        runTest {
            // When
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
            }

            // Then
            assertFailsWith<LeUnsupported> {
                getConnection(
                    espContext = espContext,
                    connectionScope = this,
                    connType = V1cType.LE,
                )
            }
        }

    @Test
    fun `When BLE is supported, then getConnection(V1cType_LE) returns LegacyConnection`() =
        runTest {
            // When
            platformContext.apply {
                packageManager.apply {
                    every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                    every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns true
                }

                val btManager = mockk<BluetoothManager> {
                    val btAdapter = mockk<BluetoothAdapter>()
                    every { adapter } returns btAdapter
                }

                every { getSystemService(Context.BLUETOOTH_SERVICE) } returns btManager
            }

            // Then
            val connection = getConnection(
                espContext = espContext,
                connectionScope = this,
                connType = V1cType.LE,
            )
            assertIs<LeConnection>(connection)
        }

    @Test
    fun `When BT is not supported, then IESPClient_getClient(V1ConnectionTypePreference_Legacy) throws BTUnsupported exception`() =
        runTest {
            // When
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns false
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
            }
            // Then
            assertFailsWith<BTUnsupported> {
                IESPClient.getClient(
                    preference = V1ConnectionTypePreference.Legacy,
                    connectionScope = this,
                )
            }
        }

    @Test
    fun `When BT is supported, then IESPClient_getClient(V1ConnectionTypePreference_Legacy) returns ESPClient configured for V1c_Legacy`() =
        runTest {
            // When
            platformContext.apply {
                packageManager.apply {
                    every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                    every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
                }

                val btManager = mockk<BluetoothManager> {
                    val btAdapter = mockk<BluetoothAdapter>()
                    every { adapter } returns btAdapter
                }

                every { getSystemService(Context.BLUETOOTH_SERVICE) } returns btManager
            }

            // Then
            val client = assertIs<ESPClient>(
                IESPClient.getClient(
                    preference = V1ConnectionTypePreference.Legacy,
                    connectionScope = this.backgroundScope,
                )
            )

            assertIs<LegacyConnection>(client.connection)
            assertEquals(expected = V1cType.Legacy, actual = client.connectionType)
        }

    @Test
    fun `When BLE is not supported, then IESPClient_getClient(V1ConnectionTypePreference_LE) throws LeUnsupported exception`() =
        runTest {
            // When
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
            }
            // Then
            assertFailsWith<LeUnsupported> {
                IESPClient.getClient(
                    preference = V1ConnectionTypePreference.LE,
                    connectionScope = this,
                )
            }
        }

    @Test
    fun `When BLE is supported, then IESPClient_getClient(V1ConnectionTypePreference_LE) returns ESPClient configured for V1c_Legacy`() =
        runTest {
            // When
            platformContext.apply {
                packageManager.apply {
                    every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                    every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns true
                }

                val btManager = mockk<BluetoothManager> {
                    val btAdapter = mockk<BluetoothAdapter>()
                    every { adapter } returns btAdapter
                }

                every { getSystemService(Context.BLUETOOTH_SERVICE) } returns btManager
            }
            // Then
            val client = assertIs<ESPClient>(
                IESPClient.getClient(
                    preference = V1ConnectionTypePreference.LE,
                    connectionScope = this.backgroundScope,
                )
            )

            assertIs<LeConnection>(client.connection)
            assertEquals(expected = V1cType.LE, actual = client.connectionType)
        }

    @Test
    fun `When BT is supported, then IESPClient_getClient(V1ConnectionTypePreference_Auto) returns ESPClient configured for V1c_Legacy`() =
        runTest {
            // When
            platformContext.apply {
                packageManager.apply {
                    every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                    every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
                }

                val btManager = mockk<BluetoothManager> {
                    val btAdapter = mockk<BluetoothAdapter>()
                    every { adapter } returns btAdapter
                }

                every { getSystemService(Context.BLUETOOTH_SERVICE) } returns btManager
            }
            // Then
            val client = assertIs<ESPClient>(
                IESPClient.getClient(
                    preference = V1ConnectionTypePreference.Auto,
                    connectionScope = this.backgroundScope,
                )
            )

            assertIs<LegacyConnection>(client.connection)
            assertEquals(expected = V1cType.Legacy, actual = client.connectionType)
        }

    @Test
    fun `When BLE is supported, then IESPClient_getClient(V1ConnectionTypePreference_Auto) returns ESPClient configured for V1c_LE`() =
        runTest {
            // When
            platformContext.apply {
                packageManager.apply {
                    every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                    every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns true
                }

                val btManager = mockk<BluetoothManager> {
                    val btAdapter = mockk<BluetoothAdapter>()
                    every { adapter } returns btAdapter
                }

                every { getSystemService(Context.BLUETOOTH_SERVICE) } returns btManager
            }
            // Then
            val client = assertIs<ESPClient>(
                IESPClient.getClient(
                    preference = V1ConnectionTypePreference.Auto,
                    connectionScope = this.backgroundScope,
                )
            )

            assertIs<LeConnection>(client.connection)
            assertEquals(expected = V1cType.LE, actual = client.connectionType)
        }

    @Test
    fun `When BT is not supported, then IESPClient_getClient(V1cType_Demo) returns ESPClient configured for V1c_Demo`() =
        runTest {
            // When
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns false
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
            }
            // Then
            val client = assertIs<ESPClient>(
                IESPClient.getClient(
                    connectionType = V1cType.Demo,
                    connectionScope = this.backgroundScope,
                )
            )
            assertIs<DemoConnection>(client.connection)
            assertEquals(expected = V1cType.Demo, actual = client.connectionType)
        }

    @Test
    fun `When BT is not supported, then IESPClient_getClient(V1cType_Legacy) throws BTUnsupported exception`() =
        runTest {
            // When
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns false
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
            }
            // Then
            assertFailsWith<BTUnsupported> {
                IESPClient.getClient(
                    connectionType = V1cType.Legacy,
                    connectionScope = this.backgroundScope,
                )
            }
        }

    @Test
    fun `When BT is supported, then IESPClient_getClient(V1cType_Legacy) returns ESPClient configured for V1c_Legacy`() =
        runTest {
            // When
            platformContext.apply {
                packageManager.apply {
                    every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                    every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
                }

                val btManager = mockk<BluetoothManager> {
                    val btAdapter = mockk<BluetoothAdapter>()
                    every { adapter } returns btAdapter
                }

                every { getSystemService(Context.BLUETOOTH_SERVICE) } returns btManager
            }

            // Then
            val client = assertIs<ESPClient>(
                IESPClient.getClient(
                    connectionType = V1cType.Legacy,
                    connectionScope = this.backgroundScope,
                )
            )

            assertIs<LegacyConnection>(client.connection)
            assertEquals(expected = V1cType.Legacy, actual = client.connectionType)
        }

    @Test
    fun `When BLE is not supported, then IESPClient_getClient(V1cType_LE) throws LeUnsupported exception`() =
        runTest {
            // When
            platformContext.packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns false
            }

            // Then
            assertFailsWith<LeUnsupported> {
                IESPClient.getClient(
                    connectionType = V1cType.LE,
                    connectionScope = this.backgroundScope,
                )
            }
        }

    @Test
    fun `When BLE is supported, then IESPClient_getClient(V1cType_LE) returns ESPClient configured for V1c_LE`() =
        runTest {
            // When
            platformContext.apply {
                packageManager.apply {
                    every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                    every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns true
                }

                val btManager = mockk<BluetoothManager> {
                    val btAdapter = mockk<BluetoothAdapter>()
                    every { adapter } returns btAdapter
                }

                every { getSystemService(Context.BLUETOOTH_SERVICE) } returns btManager
            }

            // Then
            val client = assertIs<ESPClient>(
                IESPClient.getClient(
                    connectionType = V1cType.LE,
                    connectionScope = this.backgroundScope,
                )
            )

            assertIs<LeConnection>(client.connection)
            assertEquals(expected = V1cType.LE, actual = client.connectionType)
        }

    @Test
    fun `When provided a preconstructed DemoConnection instance, then ESPClient_getClient(connection = ) returns ESPClient configured for V1c_Legacy `() = runTest {
        val connection = assertIs<DemoConnection>(
            getConnection(
                espContext = espContext,
                connectionScope = this,
                connType = V1cType.Demo,
            )
        )

        val client = assertIs<ESPClient>(
            IESPClient.getClient(
                connection = connection,
                scope = this.backgroundScope,
            )
        )
        assertEquals(expected = V1cType.Demo, actual = client.connectionType)
    }

    @Test
    fun `When provided a preconstructed LegacyConnection instance, then ESPClient_getClient(connection = ) returns ESPClient configured for V1c_Legacy `() = runTest {
        platformContext.apply {
            packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns true
            }

            val btManager = mockk<BluetoothManager> {
                val btAdapter = mockk<BluetoothAdapter>()
                every { adapter } returns btAdapter
            }

            every { getSystemService(Context.BLUETOOTH_SERVICE) } returns btManager
        }

        val connection = assertIs<LegacyConnection>(
            getConnection(
                espContext = espContext,
                connectionScope = this,
                connType = V1cType.Legacy,
            )
        )

        val client = assertIs<ESPClient>(
            IESPClient.getClient(
                connection = connection,
                scope = this.backgroundScope,
            )
        )
        assertEquals(expected = V1cType.Legacy, actual = client.connectionType)
    }

    @Test
    fun `When provided a preconstructed LeConnection instance, then ESPClient_getClient(connection = ) returns ESPClient configured for V1c_Legacy `() = runTest {
        platformContext.apply {
            packageManager.apply {
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) } returns true
                every { hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) } returns true
            }

            val btManager = mockk<BluetoothManager> {
                val btAdapter = mockk<BluetoothAdapter>()
                every { adapter } returns btAdapter
            }

            every { getSystemService(Context.BLUETOOTH_SERVICE) } returns btManager
        }

        val connection = assertIs<LeConnection>(
            getConnection(
                espContext = espContext,
                connectionScope = this,
                connType = V1cType.LE,
            )
        )

        val client = assertIs<ESPClient>(
            IESPClient.getClient(
                connection = connection,
                scope = this.backgroundScope,
            )
        )
        assertEquals(expected = V1cType.LE, actual = client.connectionType)
    }
}