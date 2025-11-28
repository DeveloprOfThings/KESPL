package io.github.developrofthings.kespl.bluetooth

/**
 * Possible connection states for __V1c/V1c-LE__.
 *
 * __NOTE:__ From the perspective, the *V1 Gen2* is considered a V1c-LE.
 */
enum class ESPConnectionStatus {
    /**
     * Idle state; no active connection
     */
    Disconnected,

    /**
     * Connection attempt in-progress
     *
     * __Note:__ This is a transitive state; the library can/will transition to [ConnectionFailed],
     * [Connected] fairly quickly.
     */
    Connecting,

    /**
     * Connection failed
     *
     * __Note:__ This is a transitive state; the library will quickly transition to [Disconnected].
     */
    ConnectionFailed,

    /**
     * Connection established
     */
    Connected,
    /**
     * Connection terminating
     *
     * __Note:__ This is a transitive state; the library will quickly transition to [Disconnected].
     */
    Disconnecting,

    /**
     * Connection unexpectedly lost
     */
    ConnectionLost,
}