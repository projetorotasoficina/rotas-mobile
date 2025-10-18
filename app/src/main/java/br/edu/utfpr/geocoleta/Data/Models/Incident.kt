package br.edu.utfpr.geocoleta.Data.Models

data class Incident(
    val id : Int?,
    val trajetoId : Int?,
    val nome : String,
    val observacoes : String,
    val ts : String,
    val longitude : Double,
    val latitude :Double,
    val fotoUrl : String
)
