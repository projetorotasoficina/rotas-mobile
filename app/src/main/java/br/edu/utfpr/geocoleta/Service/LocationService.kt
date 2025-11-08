package br.edu.utfpr.geocoleta.Service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import br.edu.utfpr.geocoleta.Data.Models.Coordinates
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

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var sincronizacaoRepository: SincronizacaoRepository
    private var rotaId: Int = 0

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sincronizacaoRepository = SincronizacaoRepository(this)
        createNotificationChannel()

        val notification: Notification = NotificationCompat.Builder(this, "location_channel")
            .setContentTitle("Coletando localização")
            .setContentText("O app está coletando dados de GPS em segundo plano.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        rotaId = intent?.getIntExtra("ROTA_ID", 0) ?: 0

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
                    val date = Date(location.time)

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                    val dateString = dateFormat.format(date)

                    val coordinates = Coordinates( 0,rotaId, location.latitude, location.longitude, dateString, "")
                    sincronizacaoRepository.inserirPonto(coordinates)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(request, locationCallback, mainLooper)
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
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
}