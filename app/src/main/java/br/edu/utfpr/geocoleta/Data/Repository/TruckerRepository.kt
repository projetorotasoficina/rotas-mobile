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
            put(DatabaseContract.Motorista.COLUMN_CPF, motorista.cpf)
            put(DatabaseContract.Motorista.COLUMN_CNH_CATEGORIA, motorista.cnhCategoria)
            put(DatabaseContract.Motorista.COLUMN_CNH_VALIDADE, motorista.cnhValidade)
            put(DatabaseContract.Motorista.COLUMN_ATIVO, if (motorista.ativo) 1 else 0)
        }
        return db.insert(DatabaseContract.Motorista.TABLE_NAME, null, values)
    }

    fun update(motorista: Trucker): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.Motorista.COLUMN_NOME, motorista.nome)
            put(DatabaseContract.Motorista.COLUMN_CPF, motorista.cpf)
            put(DatabaseContract.Motorista.COLUMN_CNH_CATEGORIA, motorista.cnhCategoria)
            put(DatabaseContract.Motorista.COLUMN_CNH_VALIDADE, motorista.cnhValidade)
            put(DatabaseContract.Motorista.COLUMN_ATIVO, if (motorista.ativo) 1 else 0)
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

    fun listAll(): List<Trucker> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseContract.Motorista.TABLE_NAME,
            null, null, null, null, null, null
        )

        val lista = mutableListOf<Trucker>()
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseContract.Motorista.COLUMN_ID))
                val nome = getString(getColumnIndexOrThrow(DatabaseContract.Motorista.COLUMN_NOME))
                val cpf = getString(getColumnIndexOrThrow(DatabaseContract.Motorista.COLUMN_CPF))
                val cnhCategoria = getString(getColumnIndexOrThrow(DatabaseContract.Motorista.COLUMN_CNH_CATEGORIA))
                val cnhValidade = getString(getColumnIndexOrThrow(DatabaseContract.Motorista.COLUMN_CNH_VALIDADE))
                val ativo = getInt(getColumnIndexOrThrow(DatabaseContract.Motorista.COLUMN_ATIVO)) == 1

                lista.add(
                    Trucker(
                        id = id,
                        nome = nome,
                        cpf = cpf,
                        cnhCategoria = cnhCategoria,
                        cnhValidade = cnhValidade,
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