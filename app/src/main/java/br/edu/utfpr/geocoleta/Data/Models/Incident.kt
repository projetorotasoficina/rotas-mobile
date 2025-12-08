package br.edu.utfpr.geocoleta.Data.Models

data class Incident(
    val id: Int? = 0,
    val trajetoId: Int,
    val nome: String,
    val observacoes: String?,
    val lat: Double,
    val lng: Double
)
