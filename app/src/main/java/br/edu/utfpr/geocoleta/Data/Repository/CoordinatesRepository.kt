package br.edu.utfpr.geocoleta.Data.Repository

import android.content.ContentValues
import android.content.Context
import br.edu.utfpr.geocoleta.Data.DatabaseContract
import br.edu.utfpr.geocoleta.Data.DatabaseHelper
import br.edu.utfpr.geocoleta.Data.Models.Coordinates

class CoordinatesRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun insert(coord: Coordinates): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.Coordenada.COLUMN_ROTA_ID, coord.trajetoId)
            put(DatabaseContract.Coordenada.COLUMN_LATITUDE, coord.latitude)
            put(DatabaseContract.Coordenada.COLUMN_LONGITUDE, coord.longitude)
            put(DatabaseContract.Coordenada.COLUMN_STATUS_ENVIO, coord.statusEnvio)
            put(DatabaseContract.Coordenada.COLUMN_HORARIO, coord.horario)
            put(DatabaseContract.Coordenada.COLUMN_OBSERVACAO, coord.observacao)
        }
        return db.insert(DatabaseContract.Coordenada.TABLE_NAME, null, values)
    }

    fun update(coord: Coordinates): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.Coordenada.COLUMN_ROTA_ID, coord.trajetoId)
            put(DatabaseContract.Coordenada.COLUMN_LATITUDE, coord.latitude)
            put(DatabaseContract.Coordenada.COLUMN_LONGITUDE, coord.longitude)
            put(DatabaseContract.Coordenada.COLUMN_HORARIO, coord.horario)
            put(DatabaseContract.Coordenada.COLUMN_OBSERVACAO, coord.observacao)
        }
        return db.update(
            DatabaseContract.Coordenada.TABLE_NAME,
            values,
            "${DatabaseContract.Coordenada.COLUMN_ID} = ?",
            arrayOf(coord.id.toString())
        )
    }

    fun delete(id: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            DatabaseContract.Coordenada.TABLE_NAME,
            "${DatabaseContract.Coordenada.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun listAll(): List<Coordinates> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(DatabaseContract.Coordenada.TABLE_NAME, null, null, null, null, null, null)

        val lista = mutableListOf<Coordinates>()
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_ID))
                val trajetoId = getInt(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_ROTA_ID))
                val latitude = getDouble(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_LATITUDE))
                val longitude = getDouble(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_LONGITUDE))
                val horario = getString(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_HORARIO))
                val observacao = getString(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_OBSERVACAO))
                val statusEnvio = getString(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_STATUS_ENVIO))
                lista.add(Coordinates(id, trajetoId, latitude, longitude, horario, observacao, statusEnvio))
            }
        }
        cursor.close()
        return lista
    }

    fun listarPendentes(): List<Coordinates> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseContract.Coordenada.TABLE_NAME,
            null,
            "${DatabaseContract.Coordenada.COLUMN_STATUS_ENVIO} = ?",
            arrayOf("PENDENTE"),
            null, null, null
        )

        val lista = mutableListOf<Coordinates>()
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_ID))
                val trajetoId = getInt(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_ROTA_ID))
                val latitude = getDouble(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_LATITUDE))
                val longitude = getDouble(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_LONGITUDE))
                val horario = getString(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_HORARIO))
                val observacao = getString(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_OBSERVACAO))
                val statusEnvio = getString(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_STATUS_ENVIO))
                lista.add(Coordinates(id, trajetoId, latitude, longitude, horario, observacao, statusEnvio))
            }
        }
        cursor.close()
        return lista
    }

    fun listByRotaId(rotaId: Int): List<Coordinates> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseContract.Coordenada.TABLE_NAME,
            null,
            "${DatabaseContract.Coordenada.COLUMN_ROTA_ID} = ?",
            arrayOf(rotaId.toString()),
            null,
            null,
            null
        )

        val lista = mutableListOf<Coordinates>()
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_ID))
                val trajetoId = getInt(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_ROTA_ID))
                val latitude = getDouble(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_LATITUDE))
                val longitude = getDouble(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_LONGITUDE))
                val horario = getString(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_HORARIO))
                val observacao = getString(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_OBSERVACAO))
                val statusEnvio = getString(getColumnIndexOrThrow(DatabaseContract.Coordenada.COLUMN_STATUS_ENVIO))
                lista.add(Coordinates(id, trajetoId, latitude, longitude, horario, observacao, statusEnvio))
            }
        }
        cursor.close()
        return lista
    }

    fun marcarComoEnviado(id: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.Coordenada.COLUMN_STATUS_ENVIO, "ENVIADO")
        }
        db.update(
            DatabaseContract.Coordenada.TABLE_NAME,
            values,
            "${DatabaseContract.Coordenada.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }
}
