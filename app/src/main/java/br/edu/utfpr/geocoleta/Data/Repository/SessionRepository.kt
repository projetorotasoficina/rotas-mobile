package br.edu.utfpr.geocoleta.Data.Repository

import android.content.ContentValues
import android.content.Context
import br.edu.utfpr.geocoleta.Data.DatabaseContract
import br.edu.utfpr.geocoleta.Data.DatabaseHelper
import br.edu.utfpr.geocoleta.Data.Models.Session

class SessionRepository (context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun insert(sessao: Session): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.Sessao.COLUMN_MOTORISTA_ID, sessao.motoristaId)
            put(DatabaseContract.Sessao.COLUMN_CAMINHAO_ID, sessao.caminhaoId)
            put(DatabaseContract.Sessao.COLUMN_INICIO, sessao.inicio)
            put(DatabaseContract.Sessao.COLUMN_FIM, sessao.fim)
        }
        return db.insert(DatabaseContract.Sessao.TABLE_NAME, null, values)
    }

    fun update(sessao: Session): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.Sessao.COLUMN_MOTORISTA_ID, sessao.motoristaId)
            put(DatabaseContract.Sessao.COLUMN_CAMINHAO_ID, sessao.caminhaoId)
            put(DatabaseContract.Sessao.COLUMN_INICIO, sessao.inicio)
            put(DatabaseContract.Sessao.COLUMN_FIM, sessao.fim)
        }
        return db.update(
            DatabaseContract.Sessao.TABLE_NAME,
            values,
            "${DatabaseContract.Sessao.COLUMN_ID} = ?",
            arrayOf(sessao.id.toString())
        )
    }

    fun delete(id: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            DatabaseContract.Sessao.TABLE_NAME,
            "${DatabaseContract.Sessao.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun listAll(): List<Session> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseContract.Sessao.TABLE_NAME,
            null, null, null, null, null, null
        )

        val lista = mutableListOf<Session>()
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseContract.Sessao.COLUMN_ID))
                val motoristaId = getInt(getColumnIndexOrThrow(DatabaseContract.Sessao.COLUMN_MOTORISTA_ID))
                val caminhaoId = getInt(getColumnIndexOrThrow(DatabaseContract.Sessao.COLUMN_CAMINHAO_ID))
                val inicio = getString(getColumnIndexOrThrow(DatabaseContract.Sessao.COLUMN_INICIO))
                val fim = getString(getColumnIndexOrThrow(DatabaseContract.Sessao.COLUMN_FIM))
                lista.add(Session(id, motoristaId, caminhaoId, inicio, fim))
            }
        }
        cursor.close()
        return lista
    }
}