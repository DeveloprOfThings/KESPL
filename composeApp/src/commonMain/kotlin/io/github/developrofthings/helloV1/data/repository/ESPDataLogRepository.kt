package io.github.developrofthings.helloV1.data.repository

import kotlinx.coroutines.flow.StateFlow

interface ESPDataLogRepository {

    val filterAlertData: StateFlow<Boolean>
    val filterDisplayData: StateFlow<Boolean>

    val log: StateFlow<List<String>>

    fun setDisplayDataFiltering(enabled: Boolean)

    fun setAlertDataFiltering(enabled: Boolean)

    fun addLog(log: String)

    fun clearLog()
}