@file:OptIn(ExperimentalUnsignedTypes::class)

package io.github.developrofthings.kespl.utils

import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.matches


private fun Any?.asListOrNull(): List<Any?>? = when (this) {
    is Array<*> -> asList()
    is IntArray -> asList()
    is ByteArray -> asList()
    is DoubleArray -> asList()
    is CharArray -> asList()
    is FloatArray -> asList()
    is LongArray -> asList()
    is BooleanArray -> asList()
    is ShortArray -> asList()
    is UIntArray -> asList()
    is UByteArray -> asList()
    is ULongArray -> asList()
    is UShortArray -> asList()
    else -> null
}

private  fun Any?.description(): String {
    if (this == null) return "null"
    if (this is String) return "\"$this\""
    if (this is Function<*>) return "{...}"
    val values = asListOrNull()
    if (values != null) return values.toString()
    return toString()
}

/**
 * Custom Matcher that allows up to specific the matching predicate.
 */
private class CustomMatcher<T>(
    val predicate: (left: T, right: T) -> Boolean,
    val value: T
): ArgMatcher<T> {
    override fun matches(arg: T): Boolean = predicate(arg, value)

    override fun toString(): String {
        return "custom($${value.description()})"
    }
}

public fun <T> MokkeryMatcherScope.custom(
    value: T,
    predicate: (l: T, r: T) -> Boolean,
): T = matches(matcher = CustomMatcher(predicate = predicate, value = value))