package br.edu.utfpr.geocoleta.Data.Models

data class TimeDistance (
    val totalDistanceMeters: Float,
    val elapsedSeconds: Long,
    val lat: Double,
    val lng: Double
)