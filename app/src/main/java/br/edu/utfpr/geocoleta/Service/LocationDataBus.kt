import br.edu.utfpr.geocoleta.Data.Models.TimeDistance
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object LocationDataBus {

    // Replay = 1 → a Activity recebe o último valor automaticamente ao ouvir
    private val _locationFlow = MutableSharedFlow<TimeDistance>(replay = 1)
    val locationFlow = _locationFlow.asSharedFlow()

    suspend fun send(update: TimeDistance) {
        _locationFlow.emit(update)
    }
}