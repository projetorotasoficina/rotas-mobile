package br.edu.utfpr.geocoleta.Data.Repository

import android.content.ContentValues
import android.content.Context
import br.edu.utfpr.geocoleta.Data.DatabaseContract
import br.edu.utfpr.geocoleta.Data.DatabaseHelper
import br.edu.utfpr.geocoleta.Data.Models.Coordinates

class PointRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun insert(coordinates: Coordinates): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.Coordenada.COLUMN_ROTA_ID, coordinates.trajetoId)
            put(DatabaseContract.Coordenada.COLUMN_LATITUDE, coordinates.latitude)
            put(DatabaseContract.Coordenada.COLUMN_LONGITUDE, coordinates.longitude)
            put(DatabaseContract.Coordenada.COLUMN_HORARIO, coordinates.horario)
            put(DatabaseContract.Coordenada.COLUMN_OBSERVACAO, coordinates.observacao)
            put(DatabaseContract.Coordenada.COLUMN_STATUS_ENVIO, "PENDENTE")
        }
        return db.insert(DatabaseContract.Coordenada.TABLE_NAME, null, values)
    }

    fun getPendingPoints(): List<Coordinates> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseContract.Coordenada.TABLE_NAME,
            null,
            "${DatabaseContract.Coordenada.COLUMN_STATUS_ENVIO} = ?",
            arrayOf("PENDENTE"),
            null,
            null,
            null
        )

        val points = mutableListOf<Coordinates>()
        with(cursor) {
            while (moveToNext()) {
                points.add(
                    Coordinates(
                        id = getInt(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_ID)),
                        trajetoId = getInt(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_ROTA_ID)),
                        latitude = getDouble(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_LATITUDE)),
                        longitude = getDouble(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_LONGITUDE)),
                        horario = getString(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_HORARIO)),
                        observacao = getString(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_OBSERVACAO))
                    )
                )
            }
        }
        cursor.close()
        return points
    }

    fun deleteSentPoints() {
        val db = dbHelper.writableDatabase
        db.delete(DatabaseContract.Coordenada.TABLE_NAME, null, null)
    }
}