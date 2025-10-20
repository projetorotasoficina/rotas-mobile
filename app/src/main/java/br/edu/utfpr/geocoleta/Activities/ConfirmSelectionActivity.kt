package br.edu.utfpr.geocoleta.Activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.edu.utfpr.geocoleta.R
import br.edu.utfpr.geocoleta.Service.LocationService
import br.edu.utfpr.geocoleta.databinding.ActivityConfirmSelectionBinding

class ConfirmSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmSelectionBinding
    private lateinit var ivBack: ImageView

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                handlePostPermissionCheck()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ivBack = findViewById(R.id.ivBack)

        val rotaNome = intent.getStringExtra("ROTA_NOME")
        val rotaObservacoes = intent.getStringExtra("ROTA_OBSERVACOES")
        val caminhaoPlaca = intent.getStringExtra("CAMINHAO_PLACA")
        val caminhaoModelo = intent.getStringExtra("CAMINHAO_MODELO") // Recebe o modelo

        binding.truckPlateTextView.text = caminhaoPlaca
        binding.truckModelTextView.text = caminhaoModelo // Exibe o modelo
        binding.routeNameTextView.text = rotaNome
        binding.routeObservationsTextView.text = rotaObservacoes

        ivBack.setOnClickListener {
            finish()
        }

        binding.startRouteButton.setOnClickListener {
            checkPermissions()
        }
    }

    private fun checkPermissions() {
        val allGranted = locationPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            handlePostPermissionCheck()
        } else {
            requestPermissionLauncher.launch(locationPermissions)
        }
    }

    private fun handlePostPermissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                )
                return
            }
        }

        startAppFlow()
    }

    private fun startAppFlow() {
        startLocationService()
        val intent = Intent(this, RouteInProgressActivity::class.java)
        startActivity(intent)
    }

    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }
}