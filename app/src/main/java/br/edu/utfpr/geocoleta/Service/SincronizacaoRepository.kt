package br.edu.utfpr.geocoleta.Service

import android.content.Context
import br.edu.utfpr.geocoleta.Data.Models.CoordinatesDTO
import br.edu.utfpr.geocoleta.Data.Network.ApiService
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient
import br.edu.utfpr.geocoleta.Data.Repository.CoordinatesRepository

class SincronizacaoRepository(
    private val context: Context
) {
    private val api = RetrovitClient.api
    private val coordRepo = CoordinatesRepository(context)

    suspend fun sincronizarPontos() {
        val pendentes = coordRepo.listarPendentes()
        if (pendentes.isEmpty()) return

        try {
            val response = api.sendCoordinate(CoordinatesDTO.fromEntityList(coordRepo.listarPendentes()))
            if (response.isSuccessful) {
                pendentes.forEach { coordRepo.marcarComoEnviado(it.id) }
                println("${pendentes.size} pontos enviados com sucesso!")
            } else {
                println("⚠Erro HTTP: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            println("Erro de rede: ${e.message}")
        }
    }
}