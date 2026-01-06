package io.github.developrofthings.kespl.utilities

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

fun getDefaultScope(
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    parent: Job? = null
): CoroutineScope = CoroutineScope(dispatcher + SupervisorJob(parent))

internal val defaultESPScope = getDefaultScope()