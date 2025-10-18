package br.edu.utfpr.geocoleta.Data.Repository

import android.content.ContentValues
import android.content.Context
import br.edu.utfpr.geocoleta.Data.DatabaseContract
import br.edu.utfpr.geocoleta.Data.DatabaseHelper
import br.edu.utfpr.geocoleta.Data.Models.Truck
import br.edu.utfpr.geocoleta.Data.Models.Trucker
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient

class TruckerRepository (context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun insert(motorista: Trucker): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.Motorista.COLUMN_NOME, motorista.nome)
            put(DatabaseContract.Motorista.COLUMN_CNH, motorista.cnh)
        }
        return db.insert(DatabaseContract.Motorista.TABLE_NAME, null, values)
    }

    fun update(motorista: Trucker): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.Motorista.COLUMN_NOME, motorista.nome)
            put(DatabaseContract.Motorista.COLUMN_CNH, motorista.cnh)
        }
        return db.update(
            DatabaseContract.Motorista.TABLE_NAME,
            values,
            "${DatabaseContract.Motorista.COLUMN_ID} = ?",
            arrayOf(motorista.id.toString())
        )
    }

    fun delete(id: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            DatabaseContract.Motorista.TABLE_NAME,
            "${DatabaseContract.Motorista.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun listAll(): List<Truck> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseContract.Caminhao.TABLE_NAME,
            null, null, null, null, null, null
        )

        val lista = mutableListOf<Truck>()
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseContract.Caminhao.COLUMN_ID))
                val placa = getString(getColumnIndexOrThrow(DatabaseContract.Caminhao.COLUMN_PLACA))
                val modelo = getString(getColumnIndexOrThrow(DatabaseContract.Caminhao.COLUMN_MODELO))
                val tipoColeta = getInt(getColumnIndexOrThrow(DatabaseContract.Caminhao.COLUMN_TIPO_COLETA))
                val tipoResiduo = getInt(getColumnIndexOrThrow(DatabaseContract.Caminhao.COLUMN_TIPO_RESIDUO))
                val ativo = getInt(getColumnIndexOrThrow(DatabaseContract.Caminhao.COLUMN_STATUS)) == 1

                lista.add(
                    Truck(
                        id = id,
                        placa = placa,
                        modelo = modelo,
                        tipoColeta = tipoColeta,
                        tipoResiduo = tipoResiduo,
                        ativo = ativo
                    )
                )
            }
        }
        cursor.close()
        db.close()
        return lista
    }

    suspend fun getTruckers(){
        val truckers =RetrovitClient.api.getDrivers()
        for (truck in truckers){
            insert(truck)
        }
    }
}