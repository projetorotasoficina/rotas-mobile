package br.edu.utfpr.geocoleta.Service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SincronizacaoWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        SincronizacaoRepository(applicationContext).sincronizarPontos()
        return Result.success()
    }
}