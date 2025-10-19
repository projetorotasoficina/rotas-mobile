package br.edu.utfpr.geocoleta.Data.Repository

import android.content.ContentValues
import android.content.Context
import br.edu.utfpr.geocoleta.Data.DatabaseContract
import br.edu.utfpr.geocoleta.Data.DatabaseHelper
import br.edu.utfpr.geocoleta.Data.Models.Truck
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient

class TruckRepository (context: Context) {

    private val dbHelper = DatabaseHelper(context)

    fun insert(truck: Truck): Long {
        val db = dbHelper.writableDatabase

        val cursor = db.query(
            DatabaseContract.Caminhao.TABLE_NAME,
            arrayOf(DatabaseContract.Caminhao.COLUMN_ID),
            "${DatabaseContract.Caminhao.COLUMN_ID} = ?",
            arrayOf(truck.id.toString()),
            null, null, null
        )

        val exists = cursor.moveToFirst()
        cursor.close()

        return if (exists) {
            update(truck).toLong()
        } else {
            val values = ContentValues().apply {
                put(DatabaseContract.Caminhao.COLUMN_ID, truck.id)
                put(DatabaseContract.Caminhao.COLUMN_PLACA, truck.placa)
                put(DatabaseContract.Caminhao.COLUMN_MODELO, truck.modelo)
                put(DatabaseContract.Caminhao.COLUMN_TIPO_COLETA, truck.tipoColeta)
                put(DatabaseContract.Caminhao.COLUMN_TIPO_RESIDUO, truck.tipoResiduo)
                put(DatabaseContract.Caminhao.COLUMN_STATUS, if (truck.ativo) 1 else 0)
            }
            db.insert(DatabaseContract.Caminhao.TABLE_NAME, null, values)
        }
    }

    fun update(truck: Truck): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.Caminhao.COLUMN_PLACA, truck.placa)
            put(DatabaseContract.Caminhao.COLUMN_MODELO, truck.modelo)
            put(DatabaseContract.Caminhao.COLUMN_TIPO_COLETA, truck.tipoColeta)
            put(DatabaseContract.Caminhao.COLUMN_TIPO_RESIDUO, truck.tipoResiduo)
            put(DatabaseContract.Caminhao.COLUMN_STATUS, if (truck.ativo) 1 else 0)
        }
        val rows = db.update(
            DatabaseContract.Caminhao.TABLE_NAME,
            values,
            "${DatabaseContract.Caminhao.COLUMN_ID} = ?",
            arrayOf(truck.id.toString())
        )
        db.close()
        return rows
    }

    fun delete(id: Int): Int {
        val db = dbHelper.writableDatabase
        val rows = db.delete(
            DatabaseContract.Caminhao.TABLE_NAME,
            "${DatabaseContract.Caminhao.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
        db.close()
        return rows
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

    suspend fun getTrucks(){
        val trucks = RetrovitClient.api.getTrucks()
        for (truck in trucks){
            insert(truck)
        }
    }
}