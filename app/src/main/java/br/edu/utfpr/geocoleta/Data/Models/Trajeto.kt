package br.edu.utfpr.geocoleta.Data.Models

data class Trajeto (
    val id: Int = 0,
    val rotaId: Int?,
    val caminhaId: Int?,
    val motoristaId: Int?,
    val dataInicio: String,
    val dataFim: String,
    val distanciaTotal: Double,
    val status : Int
)