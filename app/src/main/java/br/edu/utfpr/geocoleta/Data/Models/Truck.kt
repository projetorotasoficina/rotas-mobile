package br.edu.utfpr.geocoleta.Data.Models

data class Truck(
    val id: Int = 0,
    val placa: String,
    val modelo: String,
    val tipoColeta: Int,
    val tipoResiduo: Int,
    val ativo: Boolean
)