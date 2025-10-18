package br.edu.utfpr.geocoleta.Data.Models

data class Route(
    val id: Int = 0,
    val nome: String,
    val tipoColeta: Int,
    val tipoResiduo: Int,
    val observacoes: String?,
    val ativo: Boolean
)