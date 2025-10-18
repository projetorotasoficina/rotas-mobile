package br.edu.utfpr.geocoleta.Data.Repository

import android.content.ContentValues
import android.content.Context
import br.edu.utfpr.geocoleta.Data.DatabaseContract
import br.edu.utfpr.geocoleta.Data.DatabaseHelper
import br.edu.utfpr.geocoleta.Data.Models.Route
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient

class RouteRepository (context: Context) {
    private val dbHelper = DatabaseHelper(context)


    fun insert(route: Route): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.Rota.COLUMN_NOME, route.nome)
            put(DatabaseContract.Rota.COLUMN_TIPO_COLETA, route.tipoColeta)
            put(DatabaseContract.Rota.COLUMN_TIPO_RESIDUOS, route.tipoResiduo)
            put(DatabaseContract.Rota.COLUMN_OBSERVACOES, route.observacoes)
            put(DatabaseContract.Rota.COLUMN_ATIVO, if (route.ativo) 1 else 0)
        }
        val id = db.insert(DatabaseContract.Rota.TABLE_NAME, null, values)
        db.close()
        return id
    }

    fun update(route: Route): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.Rota.COLUMN_NOME, route.nome)
            put(DatabaseContract.Rota.COLUMN_TIPO_COLETA, route.tipoColeta)
            put(DatabaseContract.Rota.COLUMN_TIPO_RESIDUOS, route.tipoResiduo)
            put(DatabaseContract.Rota.COLUMN_OBSERVACOES, route.observacoes)
            put(DatabaseContract.Rota.COLUMN_ATIVO, if (route.ativo) 1 else 0)
        }
        val rows = db.update(
            DatabaseContract.Rota.TABLE_NAME,
            values,
            "${DatabaseContract.Rota.COLUMN_ID} = ?",
            arrayOf(route.id.toString())
        )
        db.close()
        return rows
    }

    fun delete(id: Int): Int {
        val db = dbHelper.writableDatabase
        val rows = db.delete(
            DatabaseContract.Rota.TABLE_NAME,
            "${DatabaseContract.Rota.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
        db.close()
        return rows
    }

    fun listAll(): List<Route> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseContract.Rota.TABLE_NAME,
            null, null, null, null, null, null
        )

        val lista = mutableListOf<Route>()
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseContract.Rota.COLUMN_ID))
                val nome = getString(getColumnIndexOrThrow(DatabaseContract.Rota.COLUMN_NOME))
                val tipoColeta = getInt(getColumnIndexOrThrow(DatabaseContract.Rota.COLUMN_TIPO_COLETA))
                val tipoResiduo = getInt(getColumnIndexOrThrow(DatabaseContract.Rota.COLUMN_TIPO_RESIDUOS))
                val observacoes = getString(getColumnIndexOrThrow(DatabaseContract.Rota.COLUMN_OBSERVACOES))
                val ativo = getInt(getColumnIndexOrThrow(DatabaseContract.Rota.COLUMN_ATIVO)) == 1

                lista.add(
                    Route(
                        id = id,
                        nome = nome,
                        tipoColeta = tipoColeta,
                        tipoResiduo = tipoResiduo,
                        observacoes = observacoes,
                        ativo = ativo
                    )
                )
            }
        }
        cursor.close()
        db.close()
        return lista
    }

    suspend fun getRoutes(){
        val routes = RetrovitClient.api.getRoutes()
        for (route in routes){
            insert(route)
        }
    }
}