package br.edu.utfpr.geocoleta.Data.Repository

import android.content.ContentValues
import android.content.Context
import br.edu.utfpr.geocoleta.Data.DatabaseContract
import br.edu.utfpr.geocoleta.Data.DatabaseHelper
import br.edu.utfpr.geocoleta.Data.Models.Trajeto
import br.edu.utfpr.geocoleta.Data.Models.TrajetoLocal
import java.text.SimpleDateFormat
import java.util.*

class TrajetoRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun startTrajeto(trajeto: Trajeto, id: Int): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.Trejeto.COLUMN_ID, id)
            put(DatabaseContract.Trejeto.COLUMN_ROTA_ID, trajeto.rotaId)
            put(DatabaseContract.Trejeto.COLUMN_CAMINHAO_ID, trajeto.caminhaoId)
            put(DatabaseContract.Trejeto.COLUMN_MOTORISTA_ID, trajeto.motoristaId)
            put(DatabaseContract.Trejeto.COLUMN_DATA_INICIO, SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date()))
            put(DatabaseContract.Trejeto.COLUMN_STATUS, "iniciado")
        }
        return db.insert(DatabaseContract.Trejeto.TABLE_NAME, null, values)
    }

    fun finishTrajeto(trajetoId: Int): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.Trejeto.COLUMN_STATUS, "finalizado")
            put(DatabaseContract.Trejeto.COLUMN_DATA_FIM, SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date()))
        }
        return db.update(
            DatabaseContract.Trejeto.TABLE_NAME,
            values,
            "${DatabaseContract.Trejeto.COLUMN_ID} = ?",
            arrayOf(trajetoId.toString())
        )
    }

    fun getPendingTrajetos(): List<TrajetoLocal> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseContract.Trejeto.TABLE_NAME,
            null,
            "${DatabaseContract.Trejeto.COLUMN_STATUS} = ? OR ${DatabaseContract.Trejeto.COLUMN_STATUS} = ?",
            arrayOf("iniciado", "finalizado"),
            null, null, null
        )

        val trajetos = mutableListOf<TrajetoLocal>()
        with(cursor) {
            while (moveToNext()) {
                trajetos.add(
                    TrajetoLocal(
                        id = getInt(getColumnIndexOrThrow(DatabaseContract.Trejeto.COLUMN_ID)),
                        rotaId = getInt(getColumnIndexOrThrow(DatabaseContract.Trejeto.COLUMN_ROTA_ID)),
                        caminhaoId = getInt(getColumnIndexOrThrow(DatabaseContract.Trejeto.COLUMN_CAMINHAO_ID)),
                        motoristaId = getInt(getColumnIndexOrThrow(DatabaseContract.Trejeto.COLUMN_MOTORISTA_ID)),
                        dataInicio = getString(getColumnIndexOrThrow(DatabaseContract.Trejeto.COLUMN_DATA_INICIO)),
                        dataFim = getString(getColumnIndexOrThrow(DatabaseContract.Trejeto.COLUMN_DATA_FIM)),
                        status = getString(getColumnIndexOrThrow(DatabaseContract.Trejeto.COLUMN_STATUS)),
                        distanciaTotal = getDouble(getColumnIndexOrThrow(DatabaseContract.Trejeto.COLUM_DISTANCIA_TOTAL))
                    )
                )
            }
        }
        cursor.close()
        return trajetos
    }

    fun updateTrajetoStatus(id: Int, status: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.Trejeto.COLUMN_STATUS, status)
        }
        db.update(DatabaseContract.Trejeto.TABLE_NAME, values, "${DatabaseContract.Trejeto.COLUMN_ID} = ?", arrayOf(id.toString()))
    }
}