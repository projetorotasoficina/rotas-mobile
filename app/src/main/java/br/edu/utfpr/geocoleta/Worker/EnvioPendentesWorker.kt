package br.edu.utfpr.geocoleta.Worker
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient
import br.edu.utfpr.geocoleta.Data.Repository.CoordinatesRepository
import br.edu.utfpr.geocoleta.Data.Models.CoordinatesDTO

class EnvioPendentesWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repo = CoordinatesRepository(applicationContext)
        try {
            val pendentes = repo.listarPendentes()
            for (coord in pendentes) {
                try {
                    val response = RetrovitClient.api.sendOneLocation(CoordinatesDTO.fromEntity(coord))
                    if (response.isSuccessful) {
                        coord.statusEnvio = "ENVIADO"
                        repo.update(coord)
                    } else {
                        Log.e("WorkerEnvio", "Falha ao enviar ponto pendente: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("WorkerEnvio", "Erro ao enviar ponto: ${e.message}")
                }
            }
            // Tentar finalizar operação se id do trajeto foi passado
            val trajetoId = inputData.getInt("TRAJETO_ID", 0)
            if (trajetoId != 0) {
                try {
                    RetrovitClient.api.finalizarTrajeto(trajetoId)
                } catch (e: Exception) {
                    Log.e("WorkerEnvio", "Erro ao finalizar operação: ${e.message}")
                }
            }
            return Result.success()
        } catch (e: Exception) {
            Log.e("WorkerEnvio", "Erro genérico do worker: ${e.message}")
            return Result.retry()
        }
    }
}
