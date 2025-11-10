package br.edu.utfpr.geocoleta.Data.Models

import com.google.gson.annotations.SerializedName

// Esta classe representa a estrutura da tabela 'trajeto' no banco de dados local.
data class TrajetoLocal(
    @SerializedName("id")
    val id: Int,
    @SerializedName("rotaId")
    val rotaId: Int?,
    @SerializedName("caminhaoId")
    val caminhaoId: Int?,
    @SerializedName("motoristaId")
    val motoristaId: Int?,
    @SerializedName("dataInicio")
    val dataInicio: String?,
    @SerializedName("dataFim")
    val dataFim: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("distanciaTotal")
    val distanciaTotal: Double?
)