package br.edu.utfpr.geocoleta.Data.Repository

import android.content.ContentValues
import android.content.Context
import br.edu.utfpr.geocoleta.Data.DatabaseContract
import br.edu.utfpr.geocoleta.Data.DatabaseHelper
import br.edu.utfpr.geocoleta.Data.Models.Trucker
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient

class TruckerRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val apiService = RetrovitClient.api

    fun insert(trucker: Trucker): Long {
        val db = dbHelper.writableDatabase

        val cursor = db.query(
            DatabaseContract.Motorista.TABLE_NAME,
            arrayOf(DatabaseContract.Motorista.COLUMN_ID),
            "${DatabaseContract.Motorista.COLUMN_ID} = ?",
            arrayOf(trucker.id.toString()),
            null, null, null
        )

        val exists = cursor.moveToFirst()
        cursor.close()

        return if (exists) {
            update(trucker).toLong()
        } else {
            val values = ContentValues().apply {
                put(DatabaseContract.Motorista.COLUMN_ID, trucker.id)
                put(DatabaseContract.Motorista.COLUMN_NOME, trucker.nome)
                put(DatabaseContract.Motorista.COLUMN_CPF, trucker.cpf)
                put(DatabaseContract.Motorista.COLUMN_CNH_CATEGORIA, trucker.cnhCategoria)
                put(DatabaseContract.Motorista.COLUMN_CNH_VALIDADE, trucker.cnhValidade)
                put(DatabaseContract.Motorista.COLUMN_ATIVO, if (trucker.ativo) 1 else 0)
            }
            db.insert(DatabaseContract.Motorista.TABLE_NAME, null, values)
        }
    }

    fun update(trucker: Trucker): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseContract.Motorista.COLUMN_NOME, trucker.nome)
            put(DatabaseContract.Motorista.COLUMN_CPF, trucker.cpf)
            put(DatabaseContract.Motorista.COLUMN_CNH_CATEGORIA, trucker.cnhCategoria)
            put(DatabaseContract.Motorista.COLUMN_CNH_VALIDADE, trucker.cnhValidade)
            put(DatabaseContract.Motorista.COLUMN_ATIVO, if (trucker.ativo) 1 else 0)
        }
        val rows = db.update(
            DatabaseContract.Motorista.TABLE_NAME,
            values,
            "${DatabaseContract.Motorista.COLUMN_ID} = ?",
            arrayOf(trucker.id.toString())
        )
        db.close()
        return rows
    }

    fun delete(id: Int): Int {
        val db = dbHelper.writableDatabase
        val rows = db.delete(
            DatabaseContract.Motorista.TABLE_NAME,
            "${DatabaseContract.Motorista.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
        db.close()
        return rows
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
        apiService.getMotoristas().forEach { insert(it) }
    }
}