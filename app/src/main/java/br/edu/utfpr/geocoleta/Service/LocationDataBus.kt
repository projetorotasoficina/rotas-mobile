package br.edu.utfpr.geocoleta.Service

import br.edu.utfpr.geocoleta.Data.Models.TimeDistance
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object LocationDataBus {
    private val _locationFlow = MutableSharedFlow<TimeDistance>(replay = 1)
    val locationFlow = _locationFlow.asSharedFlow()

    suspend fun send(data: TimeDistance) {
        _locationFlow.emit(data)
    }
}