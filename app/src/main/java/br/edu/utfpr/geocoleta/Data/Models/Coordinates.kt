package br.edu.utfpr.geocoleta.Data.Models

data class Coordinates (
    val id: Int,
    val trajetoId: Int,
    val latitude: Double,
    val longitude: Double,
    val horario : String,
    val observacao : String
)
