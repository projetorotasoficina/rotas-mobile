package br.edu.utfpr.geocoleta.Data.Models

data class Trucker (
    val id: Int = 0,
    val nome: String,
    val cpf: String,
    val cnhCategoria: String,
    val cnhValidade: String,
    val ativo: Boolean
)