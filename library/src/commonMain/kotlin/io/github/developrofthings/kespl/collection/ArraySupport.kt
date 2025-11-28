package io.github.developrofthings.kespl.collection

import io.github.developrofthings.kespl.ESPOutOfMemoryError

fun newLength(oldLength: Int, minGrowth: Int, prefGrowth: Int): Int {
    val prefLength = oldLength + minGrowth.coerceAtLeast(prefGrowth) // might overflow
    return if (0 < prefLength && prefLength <= SOFT_MAX_ARRAY_LENGTH) prefLength
    else {
        // put code cold in a separate method
        hugeLength(oldLength, minGrowth)
    }
}

private fun hugeLength(oldLength: Int, minGrowth: Int): Int {
    val minLength = oldLength + minGrowth
    return if (minLength < 0) {
        throw ESPOutOfMemoryError("Required array length $oldLength + $minGrowth is too large")
    }
    else if (minLength <= SOFT_MAX_ARRAY_LENGTH) SOFT_MAX_ARRAY_LENGTH
    else minLength
}

private const val SOFT_MAX_ARRAY_LENGTH: Int = Int.MAX_VALUE - 8