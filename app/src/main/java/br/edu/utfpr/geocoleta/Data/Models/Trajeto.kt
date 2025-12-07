package br.edu.utfpr.geocoleta.Data.Models

import com.google.gson.annotations.SerializedName

data class Trajeto (
    @SerializedName("id") val id: Int? = null,
    @SerializedName("rotaId") val rotaId: Int?,
    @SerializedName("caminhaoId") val caminhaoId: Int?,
    @SerializedName("motoristaId") val motoristaId: Int?,
    @SerializedName("dataInicio") val dataInicio: String? = null,
    @SerializedName("dataFim") val dataFim: String? = null,
    @SerializedName("distanciaTotal") var distanciaTotal: Double? = null,
    @SerializedName("status") val status: String? = null
)
