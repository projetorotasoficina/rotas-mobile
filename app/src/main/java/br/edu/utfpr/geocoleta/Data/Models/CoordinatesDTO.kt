package br.edu.utfpr.geocoleta.Data.Models

data class CoordinatesDTO(
    val trajetoId: Int,
    val latitude: Double,
    val longitude: Double,
    val horario: String,
    val observacao: String
) {
    companion object {
        fun fromEntity(coordinates: Coordinates): CoordinatesDTO {
            return CoordinatesDTO(
                trajetoId = coordinates.trajetoId,
                latitude = coordinates.latitude,
                longitude = coordinates.longitude,
                horario = coordinates.horario,
                observacao = coordinates.observacao
            )
        }

        fun fromEntityList(list: List<Coordinates>): List<CoordinatesDTO> {
            return list.map { fromEntity(it) }
        }
    }

}