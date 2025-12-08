package br.edu.utfpr.geocoleta.Data.Repository

import android.content.ContentValues
import android.content.Context
import br.edu.utfpr.geocoleta.Data.DatabaseContract
import br.edu.utfpr.geocoleta.Data.DatabaseHelper
import br.edu.utfpr.geocoleta.Data.Models.Incident

class IncidentRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun insert(incident: Incident): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.Incidente.COLUMN_TRAJETO_ID, incident.trajetoId)
            put(DatabaseContract.Incidente.COLUMN_NOME, incident.nome)
            put(DatabaseContract.Incidente.COLUMN_OBSERVACOES, incident.observacoes)
            put(DatabaseContract.Incidente.COLUMN_LONGITUDE, incident.lng)
            put(DatabaseContract.Incidente.COLUMN_LATITUDE, incident.lat)
        }
        val id = db.insert(DatabaseContract.Incidente.TABLE_NAME, null, values)
        db.close()
        return id
    }

    fun update(incident: Incident): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.Incidente.COLUMN_TRAJETO_ID, incident.trajetoId)
            put(DatabaseContract.Incidente.COLUMN_NOME, incident.nome)
            put(DatabaseContract.Incidente.COLUMN_OBSERVACOES, incident.observacoes)
            put(DatabaseContract.Incidente.COLUMN_LONGITUDE, incident.lng)
            put(DatabaseContract.Incidente.COLUMN_LATITUDE, incident.lat)
        }
        val rows = db.update(
            DatabaseContract.Incidente.TABLE_NAME,
            values,
            "${DatabaseContract.Incidente.COLUMN_ID} = ?",
            arrayOf(incident.id.toString())
        )
        db.close()
        return rows
    }

    fun delete(id: Int): Int {
        val db = dbHelper.writableDatabase
        val rows = db.delete(
            DatabaseContract.Incidente.TABLE_NAME,
            "${DatabaseContract.Incidente.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
        db.close()
        return rows
    }

    fun listAll(): List<Incident> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseContract.Incidente.TABLE_NAME,
            null, null, null, null, null, null
        )

        val lista = mutableListOf<Incident>()
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseContract.Incidente.COLUMN_ID))
                val trajetoId = getInt(getColumnIndexOrThrow(DatabaseContract.Incidente.COLUMN_TRAJETO_ID))
                val nome = getString(getColumnIndexOrThrow(DatabaseContract.Incidente.COLUMN_NOME))
                val observacoes = getString(getColumnIndexOrThrow(DatabaseContract.Incidente.COLUMN_OBSERVACOES))
                val ts = getString(getColumnIndexOrThrow(DatabaseContract.Incidente.COLUMN_TS))
                val longitude = getDouble(getColumnIndexOrThrow(DatabaseContract.Incidente.COLUMN_LONGITUDE))
                val latitude = getDouble(getColumnIndexOrThrow(DatabaseContract.Incidente.COLUMN_LATITUDE))
                val fotoUrl = getString(getColumnIndexOrThrow(DatabaseContract.Incidente.COLUMN_FOTO_URL))

                lista.add(
                    Incident(
                        id = id,
                        trajetoId = trajetoId,
                        nome = nome,
                        observacoes = observacoes,
                        lng = longitude,
                        lat = latitude
                    )
                )
            }
        }
        cursor.close()
        db.close()
        return lista
    }
}