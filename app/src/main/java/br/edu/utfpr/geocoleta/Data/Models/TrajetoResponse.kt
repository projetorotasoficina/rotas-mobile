package br.edu.utfpr.geocoleta.Data.Models

data class TrajetoResponse(
    val id: Int,
    val rotaId: Int,
    val caminhaoId: Int,
    val motoristaId: Int,
    val dataInicio: String,
    val dataFim: String?,
    val distanciaTotal: Double?,
    val status: String,
    val tipoResiduo: String?
)