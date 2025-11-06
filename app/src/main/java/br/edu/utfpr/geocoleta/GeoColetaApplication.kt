package br.edu.utfpr.geocoleta

import android.app.Application
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient
import br.edu.utfpr.geocoleta.Service.SincronizacaoWorker
import java.util.concurrent.TimeUnit

class GeoColetaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrovitClient.initialize(this)
        Log.d("GeoColetaApp", "Application iniciado com sucesso")

        val workRequest = PeriodicWorkRequestBuilder<SincronizacaoWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "envio_pontos",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}