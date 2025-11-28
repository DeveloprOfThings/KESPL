package io.github.developrofthings.kespl.bluetooth

import io.github.developrofthings.kespl.ESPContext
import io.github.developrofthings.kespl.di.ESPIsolatedKoinContext
import kotlinx.serialization.Serializable

@Serializable
enum class V1cType {
    Legacy,
    LE,
    Demo,
}

fun supportedTypes(): List<V1cType> = supportedV1cTypes(
    espContext = ESPIsolatedKoinContext.koin.get<ESPContext>(),
)

internal expect fun supportedV1cTypes(
    espContext: ESPContext,
): List<V1cType>