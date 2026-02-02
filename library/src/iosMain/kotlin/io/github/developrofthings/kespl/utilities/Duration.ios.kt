package io.github.developrofthings.kespl.utilities

import platform.darwin.NSInteger
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Returns a [Duration] equal to this [Long] number of nanoseconds.
 *
 * __Note:__ Exposes a subset of the kotlin.time API to Swift/Objective-C so developers can quickly
 * convert integer literals into [Duration] which is really just a value class around a
 * [Long].
 */
val NSInteger.toDurationNanos: Duration get() = toLong().toDuration(unit = DurationUnit.NANOSECONDS)
/**
 * Returns a [Duration] equal to this [Long] number of microseconds.
 *
 * __Note:__ Exposes a subset of the kotlin.time API to Swift/Objective-C so developers can quickly
 * convert integer literals into [Duration] which is really just a value class around a
 * [Long].
 */
val NSInteger.toDurationMicros: Duration get() = toLong().toDuration(unit = DurationUnit.MICROSECONDS)
/**
 * Returns a [Duration] equal to this [Long] number of milliseconds.
 *
 * __Note:__ Exposes a subset of the kotlin.time API to Swift/Objective-C so developers can quickly
 * convert integer literals into [Duration] which is really just a value class around a
 * [Long].
 */
val NSInteger.toDurationMillis: Duration get() = toLong().toDuration(unit = DurationUnit.MILLISECONDS)
/**
 * Returns a [Duration] equal to this [Long] number of seconds.
 *
 * __Note:__ Exposes a subset of the kotlin.time API to Swift/Objective-C so developers can quickly
 * convert integer literals into [Duration] which is really just a value class around a
 * [Long].
 */
val NSInteger.toDurationSeconds: Duration get() = toLong().toDuration(unit = DurationUnit.SECONDS)

/**
 * Returns a [Duration] equal to this [Int] number of nanoseconds.
 *
 * __Note:__ Exposes a subset of the kotlin.time API to Swift/Objective-C so developers can quickly
 * convert integer literals into [Duration] which is really just a value class around a
 * [Long].
 */
val kotlin.Int.toDurationNanos: Duration get() = toDuration(unit = DurationUnit.NANOSECONDS)
/**
 * Returns a [Duration] equal to this [Int] number of microseconds.
 *
 * __Note:__ Exposes a subset of the kotlin.time API to Swift/Objective-C so developers can quickly
 * convert integer literals into [Duration] which is really just a value class around a
 * [Long].
 */
val kotlin.Int.toDurationMicros: Duration get() = toDuration(unit = DurationUnit.MICROSECONDS)
/**
 * Returns a [Duration] equal to this [Int] number of milliseconds.
 *
 * __Note:__ Exposes a subset of the kotlin.time API to Swift/Objective-C so developers can quickly
 * convert integer literals into [Duration] which is really just a value class around a
 * [Long].
 */
val kotlin.Int.toDurationMillis: Duration get() = toDuration(unit = DurationUnit.MILLISECONDS)
/**
 * Returns a [Duration] equal to this [Int] number of seconds.
 *
 * __Note:__ Exposes a subset of the kotlin.time API to Swift/Objective-C so developers can quickly
 * convert integer literals into [Duration] which is really just a value class around a
 * [Long].
 */
val kotlin.Int.toDurationSeconds: Duration get() = toDuration(unit = DurationUnit.SECONDS)
