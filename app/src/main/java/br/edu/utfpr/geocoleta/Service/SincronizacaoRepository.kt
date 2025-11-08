package br.edu.utfpr.geocoleta.Service

import android.content.Context
import br.edu.utfpr.geocoleta.Data.Models.Coordinates
import br.edu.utfpr.geocoleta.Data.Models.Trajeto
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient
import br.edu.utfpr.geocoleta.Data.Repository.PointRepository
import br.edu.utfpr.geocoleta.Data.Repository.TrajetoRepository

class SincronizacaoRepository(
    private val context: Context
) {
    private val api = RetrovitClient.api
    private val pointRepository = PointRepository(context)
    private val trajetoRepository = TrajetoRepository(context)

    suspend fun sincronizarTudo() {
        sincronizarTrajetos()
        sincronizarPontos()
    }

    private suspend fun sincronizarTrajetos() {
        val pendentes = trajetoRepository.getPendingTrajetos()
        if (pendentes.isEmpty()) return

        for (trajetoLocal in pendentes) {
            try {
                val response = if (trajetoLocal.status == "iniciado") {
                    val trajetoAPI = Trajeto(trajetoLocal.rotaId, trajetoLocal.caminhaoId, trajetoLocal.motoristaId)
                    api.registrarTrajeto(trajetoAPI)
                } else {
                    api.finalizarTrajeto(trajetoLocal.id)
                }

                if (response.isSuccessful) {
                    trajetoRepository.updateTrajetoStatus(trajetoLocal.id, "sincronizado")
                    println("Trajeto ${trajetoLocal.id} sincronizado com sucesso!")
                } else {
                    println("⚠Erro HTTP ao sincronizar trajeto: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                println("Erro de rede ao sincronizar trajeto: ${e.message}")
            }
        }
    }

    private suspend fun sincronizarPontos() {
        val pendentes = pointRepository.getPendingPoints()
        if (pendentes.isEmpty()) return

        try {
            val response = api.sendCoordinate(pendentes)
            if (response.isSuccessful) {
                pointRepository.deleteSentPoints()
                println("${pendentes.size} pontos enviados com sucesso!")
            } else {
                println("⚠Erro HTTP ao enviar pontos: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            println("Erro de rede ao enviar pontos: ${e.message}")
        }
    }

    fun inserirPonto(coordinates: Coordinates) {
        pointRepository.insert(coordinates)
    }
}