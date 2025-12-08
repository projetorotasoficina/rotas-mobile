package br.edu.utfpr.geocoleta.Service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import br.edu.utfpr.geocoleta.Data.Models.Coordinates
import br.edu.utfpr.geocoleta.Data.Models.CoordinatesDTO
import br.edu.utfpr.geocoleta.Data.Models.TimeDistance
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient
import br.edu.utfpr.geocoleta.Data.Repository.CoordinatesRepository
import br.edu.utfpr.geocoleta.Worker.EnvioPendentesWorker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LocationService : Service() {

    companion object {
        const val ACTION_FINALIZAR = "ACTION_FINALIZAR"
        const val WORK_NAME_ENVIO_PENDENTES = "WORK_ENVIO_PENDENTES"
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var coordinatesRepository: CoordinatesRepository
    private var totalDistanceMeters: Float = 0f
    private var lastLocation: Location? = null
    private var startTime: Long = 0L
    private var rotaId: Int = 0
    private var trajetoId: Int = 0
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        coordinatesRepository = CoordinatesRepository(this)
        createNotificationChannel()

        val notification: Notification = NotificationCompat.Builder(this, "location_channel")
            .setContentTitle("Coletando localização")
            .setContentText("O app está coletando dados de GPS em segundo plano.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            ACTION_FINALIZAR -> {
                finalizeOperation()
                return START_NOT_STICKY
            }
        }

        rotaId = intent?.getIntExtra("ROTA_ID", 0) ?: 0
        trajetoId = intent?.getIntExtra("TRAJETO_ID", 0) ?: 0

        if (startTime == 0L) {
            startTime = System.currentTimeMillis()
        }
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateDistanceMeters(5f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location: Location in result.locations) {
                    lastLocation?.let { last ->
                        val distance = last.distanceTo(location)
                        if (distance > 0.5f) {
                            totalDistanceMeters += distance
                        }
                    }

                    val elapsedMillis = System.currentTimeMillis() - startTime
                    val elapsedSeconds = elapsedMillis / 1000
                    lastLocation = location

                    val date = Date(location.time)
                    val dateFormat = SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                        Locale.getDefault()
                    ).apply { timeZone = TimeZone.getTimeZone("UTC") }
                    val dateString = dateFormat.format(date)

                    val coordenada = Coordinates(
                        id = 0,
                        trajetoId = trajetoId,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        horario = dateString,
                        observacao = "",
                        statusEnvio = "PENDENTE"
                    )

                    serviceScope.launch {
                        // enviar para UI via SharedFlow
                        LocationDataBus.send(
                            TimeDistance(
                                totalDistanceMeters = totalDistanceMeters,
                                elapsedSeconds = elapsedSeconds,
                                lat = coordenada.latitude,
                                lng = coordenada.longitude
                            )
                        )

                        if (!isInternetAvailable(this@LocationService)) {
                            Log.e("API", "Sem conexão com a internet. Salvando localmente.")
                            coordenada.statusEnvio = "PENDENTE"
                            coordinatesRepository.insert(coordenada)
                            return@launch
                        }
                        try {
                            val response = RetrovitClient.api.sendOneLocation(
                                CoordinatesDTO.fromEntity(coordenada)
                            )
                            if (response.isSuccessful) {
                                coordenada.statusEnvio = "ENVIADO"
                            } else {
                                Log.e("API", "Falha ao enviar coordenada: ${response.code()}")
                            }
                        } catch (e: Exception) {
                            Log.e("API", "Erro ao enviar coordenada: ${e.message}")
                        } finally {
                            coordinatesRepository.insert(coordenada)
                        }
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(request, locationCallback, mainLooper)
    }

    /**
     * Método que tenta enviar os pontos pendentes imediatamente.
     * Caso não consiga (ex.: sem internet, ou falhas), agenda WorkManager para garantir retry.
     */
    private fun finalizeOperation() {
        serviceScope.launch {
            Log.i("FINALIZE", "Finalizando operação (tentativa imediata)...")

            try {
                val pendentes = coordinatesRepository.listarPendentes()
                var anyPendingLeft = false

                for (coord in pendentes) {
                    try {
                        val response = RetrovitClient.api.sendOneLocation(
                            CoordinatesDTO.fromEntity(coord)
                        )
                        if (response.isSuccessful) {
                            coord.statusEnvio = "ENVIADO"
                            coordinatesRepository.update(coord)
                        } else {
                            Log.e("FINALIZE", "Falha ao enviar pendente: ${response.code()}")
                            anyPendingLeft = true
                        }
                    } catch (e: Exception) {
                        Log.e("FINALIZE", "Erro ao enviar pendente: ${e.message}")
                        anyPendingLeft = true
                    }
                }

                try {
                    if (trajetoId != 0) {
                        RetrovitClient.api.finalizarTrajeto(trajetoId)
                        Log.i("FINALIZE", "Operação finalizada no servidor.")
                    }
                } catch (e: Exception) {
                    Log.e("FINALIZE", "Erro ao finalizar operação: ${e.message}")
                    anyPendingLeft = true
                }

                if (anyPendingLeft) {
                    scheduleWorkerForPending()
                }
            } catch (e: Exception) {
                Log.e("FINALIZE", "Erro durante finalizeOperation: ${e.message}")
                scheduleWorkerForPending()
            } finally {
                stopSelf()
            }
        }
    }

    /**
     * Método chamado quando a task do app é removida (swipe Recent Apps).
     * Aqui fazemos attempt de envio e, se necessário, agendamos o worker.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.i("LocationService", "onTaskRemoved chamado — app removido da lista de recentes.")
        finalizeOperation()
    }

    private fun scheduleWorkerForPending() {
        val input = Data.Builder()
            .putInt("TRAJETO_ID", trajetoId)
            .build()

        val request = OneTimeWorkRequestBuilder<EnvioPendentesWorker>()
            .setInputData(input)
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(WORK_NAME_ENVIO_PENDENTES, ExistingWorkPolicy.KEEP, request)

        Log.i("FINALIZE", "WorkManager agendado para envio pendentes.")
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            // ignore
        }
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location_channel",
                "Serviço de Localização",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
}
