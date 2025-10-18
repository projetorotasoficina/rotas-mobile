package br.edu.utfpr.geocoleta.Data.Models

data class Session (
    val id: Int = 0,
    val motoristaId: Int,
    val caminhaoId: Int,
    val inicio: String?,
    val fim: String?
)