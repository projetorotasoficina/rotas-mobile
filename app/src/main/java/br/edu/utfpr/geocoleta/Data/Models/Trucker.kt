package br.edu.utfpr.geocoleta.Data.Models

data class Trucker (
    val id: Int = 0,
    val nome: String,
    val cpf: String,
    val cnh: String,
    val cnhValidade: String,
    val ativo: Boolean
)