package br.edu.utfpr.geocoleta.Data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

object DatabaseContract {
    const val DATABASE_NAME = "geocoleta.db"
    const val DATABASE_VERSION = 4

    object Caminhao {
        const val TABLE_NAME = "caminhao"
        const val COLUMN_ID = "id"
        const val COLUMN_PLACA = "placa"
        const val COLUMN_MODELO = "modelo"
        const val COLUMN_TIPO_COLETA = "tipo_coleta"
        const val COLUMN_TIPO_RESIDUO = "tipo_residuo"
        const val COLUMN_STATUS = "status"
    }

    object Motorista {
        const val TABLE_NAME = "motorista"
        const val COLUMN_ID = "id"
        const val COLUMN_NOME = "nome"
        const val COLUMN_CNH_CATEGORIA = "cnh_categoria"
        const val COLUMN_CNH_VALIDADE = "cnh_validade"
        const val COLUMN_CPF = "cpf"
        const val COLUMN_ATIVO = "ativo"
    }

    object Sessao {
        const val TABLE_NAME = "sessao"
        const val COLUMN_ID = "id"
        const val COLUMN_MOTORISTA_ID = "motorista_id"
        const val COLUMN_CAMINHAO_ID = "caminhao_id"
        const val COLUMN_INICIO = "inicio"
        const val COLUMN_FIM = "fim"
    }

    object Rota {
        const val TABLE_NAME = "rota"
        const val COLUMN_ID = "id"
        const val COLUMN_NOME = "nome"
        const val COLUMN_TIPO_COLETA = "tipo_coleta"
        const val COLUMN_TIPO_RESIDUOS = "tipo_residuo"
        const val COLUMN_OBSERVACOES = "observacoes"
        const val COLUMN_ATIVO = "ativo"
    }

    object Coordenada {
        const val TABLE_NAME = "coordenada"
        const val COLUMN_ID = "id"
        const val COLUMN_ROTA_ID = "rota_id"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_STATUS_ENVIO = "status_envio"
        const val COLUMN_HORARIO = "horario"
        const val COLUMN_OBSERVACAO = "observacao"
    }

    object Incidente {
        const val TABLE_NAME = "incidente"
        const val COLUMN_ID = "id"
        const val COLUMN_TRAJETO_ID = "trajeto_id"
        const val COLUMN_NOME = "nome"
        const val COLUMN_OBSERVACOES = "observacoes"
        const val COLUMN_TS = "ts"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_FOTO_URL = "foto_url"
    }

    object Trejeto {
        const val TABLE_NAME = "trajeto"
        const val COLUMN_ID = "id"
        const val COLUMN_ROTA_ID = "rota_id"
        const val COLUMN_CAMINHAO_ID = "caminhao_id"
        const val COLUMN_MOTORISTA_ID = "motorista_id"
        const val COLUMN_DATA_INICIO = "data_inicio"
        const val COLUMN_DATA_FIM = "data_fim"
        const val COLUMN_STATUS = "status"
        const val COLUM_DISTANCIA_TOTAL = "distancia_total"
    }
}

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createTableCaminhao())
        db.execSQL(createTableMotorista())
        db.execSQL(createTableSessao())
        db.execSQL(createTableRota())
        db.execSQL(createTableCoordenada())
        db.execSQL(createTableIncidente())
        db.execSQL(createTableTrajeto())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseContract.Incidente.TABLE_NAME}")
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseContract.Coordenada.TABLE_NAME}")
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseContract.Rota.TABLE_NAME}")
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseContract.Sessao.TABLE_NAME}")
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseContract.Motorista.TABLE_NAME}")
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseContract.Caminhao.TABLE_NAME}")
        db.execSQL("DROP TABLE IF EXISTS ${DatabaseContract.Trejeto.TABLE_NAME}")
        onCreate(db)
    }

    private fun createTableCaminhao() = """
        CREATE TABLE ${DatabaseContract.Caminhao.TABLE_NAME} (
            ${DatabaseContract.Caminhao.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${DatabaseContract.Caminhao.COLUMN_PLACA} TEXT NOT NULL,
            ${DatabaseContract.Caminhao.COLUMN_MODELO} TEXT,
            ${DatabaseContract.Caminhao.COLUMN_TIPO_COLETA} INTEGER,
            ${DatabaseContract.Caminhao.COLUMN_TIPO_RESIDUO} INTEGER,
            ${DatabaseContract.Caminhao.COLUMN_STATUS} INTEGER
        )
    """.trimIndent()

    private fun createTableMotorista() = """
        CREATE TABLE ${DatabaseContract.Motorista.TABLE_NAME} (
            ${DatabaseContract.Motorista.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${DatabaseContract.Motorista.COLUMN_NOME} TEXT NOT NULL,
            ${DatabaseContract.Motorista.COLUMN_CNH_CATEGORIA} TEXT NOT NULL,
            ${DatabaseContract.Motorista.COLUMN_CNH_VALIDADE} TEXT,
            ${DatabaseContract.Motorista.COLUMN_CPF} TEXT,
            ${DatabaseContract.Motorista.COLUMN_ATIVO} INTEGER
        )
    """.trimIndent()

    private fun createTableSessao() = """
        CREATE TABLE ${DatabaseContract.Sessao.TABLE_NAME} (
            ${DatabaseContract.Sessao.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${DatabaseContract.Sessao.COLUMN_MOTORISTA_ID} INTEGER NOT NULL,
            ${DatabaseContract.Sessao.COLUMN_CAMINHAO_ID} INTEGER NOT NULL,
            ${DatabaseContract.Sessao.COLUMN_INICIO} TEXT,
            ${DatabaseContract.Sessao.COLUMN_FIM} TEXT,
            FOREIGN KEY(${DatabaseContract.Sessao.COLUMN_MOTORISTA_ID}) REFERENCES ${DatabaseContract.Motorista.TABLE_NAME}(${DatabaseContract.Motorista.COLUMN_ID}),
            FOREIGN KEY(${DatabaseContract.Sessao.COLUMN_CAMINHAO_ID}) REFERENCES ${DatabaseContract.Caminhao.TABLE_NAME}(${DatabaseContract.Caminhao.COLUMN_ID})
        )
    """.trimIndent()

    private fun createTableRota() = """
        CREATE TABLE ${DatabaseContract.Rota.TABLE_NAME} (
            ${DatabaseContract.Rota.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${DatabaseContract.Rota.COLUMN_NOME} TEXT NOT NULL,
            ${DatabaseContract.Rota.COLUMN_TIPO_COLETA} TEXT,
            ${DatabaseContract.Rota.COLUMN_TIPO_RESIDUOS} TEXT,
            ${DatabaseContract.Rota.COLUMN_OBSERVACOES} TEXT,
            ${DatabaseContract.Rota.COLUMN_ATIVO} INTEGER NOT NULL DEFAULT 1
        )
    """.trimIndent()

    private fun createTableCoordenada() = """
        CREATE TABLE ${DatabaseContract.Coordenada.TABLE_NAME} (
            ${DatabaseContract.Coordenada.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${DatabaseContract.Coordenada.COLUMN_ROTA_ID} INTEGER NOT NULL,
            ${DatabaseContract.Coordenada.COLUMN_LATITUDE} REAL NOT NULL,
            ${DatabaseContract.Coordenada.COLUMN_LONGITUDE} REAL NOT NULL,
            ${DatabaseContract.Coordenada.COLUMN_STATUS_ENVIO} INTEGER NOT NULL DEFAULT 'PENDENTE',
            ${DatabaseContract.Coordenada.COLUMN_HORARIO} TEXT,
            ${DatabaseContract.Coordenada.COLUMN_OBSERVACAO} TEXT DEFAULT 'Sem observacao',
            FOREIGN KEY(${DatabaseContract.Coordenada.COLUMN_ROTA_ID}) REFERENCES ${DatabaseContract.Rota.TABLE_NAME}(${DatabaseContract.Rota.COLUMN_ID})
        )
    """.trimIndent()

    private fun createTableIncidente() = """
    CREATE TABLE ${DatabaseContract.Incidente.TABLE_NAME} (
        ${DatabaseContract.Incidente.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
        ${DatabaseContract.Incidente.COLUMN_TRAJETO_ID} INTEGER,
        ${DatabaseContract.Incidente.COLUMN_NOME} TEXT NOT NULL,
        ${DatabaseContract.Incidente.COLUMN_OBSERVACOES} TEXT,
        ${DatabaseContract.Incidente.COLUMN_TS} TEXT,
        ${DatabaseContract.Incidente.COLUMN_LONGITUDE} REAL,
        ${DatabaseContract.Incidente.COLUMN_LATITUDE} REAL,
        ${DatabaseContract.Incidente.COLUMN_FOTO_URL} TEXT,
        FOREIGN KEY(${DatabaseContract.Incidente.COLUMN_TRAJETO_ID}) REFERENCES ${DatabaseContract.Rota.TABLE_NAME}(${DatabaseContract.Rota.COLUMN_ID})
    )
""".trimIndent()

    private fun createTableTrajeto() = """
        CREATE TABLE ${DatabaseContract.Trejeto.TABLE_NAME} (
            ${DatabaseContract.Trejeto.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${DatabaseContract.Trejeto.COLUMN_ROTA_ID} INTEGER NOT NULL,
            ${DatabaseContract.Trejeto.COLUMN_CAMINHAO_ID} INTEGER NOT NULL,
            ${DatabaseContract.Trejeto.COLUMN_MOTORISTA_ID} INTEGER NOT NULL,
            ${DatabaseContract.Trejeto.COLUMN_DATA_INICIO} TEXT,
            ${DatabaseContract.Trejeto.COLUMN_DATA_FIM} TEXT,
            ${DatabaseContract.Trejeto.COLUMN_STATUS} INTEGER,
            ${DatabaseContract.Trejeto.COLUM_DISTANCIA_TOTAL} REAL,
            FOREIGN KEY(${DatabaseContract.Trejeto.COLUMN_ROTA_ID}) REFERENCES ${DatabaseContract.Rota.TABLE_NAME}(${DatabaseContract.Rota.COLUMN_ID}),
            FOREIGN KEY(${DatabaseContract.Trejeto.COLUMN_MOTORISTA_ID}) REFERENCES ${DatabaseContract.Motorista.TABLE_NAME}(${DatabaseContract.Motorista.COLUMN_ID}),
            FOREIGN KEY(${DatabaseContract.Trejeto.COLUMN_CAMINHAO_ID}) REFERENCES ${DatabaseContract.Caminhao.TABLE_NAME}(${DatabaseContract.Caminhao.COLUMN_ID})
            
        )
    """.trimIndent()
}