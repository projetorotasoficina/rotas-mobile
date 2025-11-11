package br.edu.utfpr.geocoleta.Activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import br.edu.utfpr.geocoleta.Data.Models.Trajeto
import br.edu.utfpr.geocoleta.Data.Models.Trucker
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient
import br.edu.utfpr.geocoleta.Data.Repository.TruckerRepository
import br.edu.utfpr.geocoleta.R
import br.edu.utfpr.geocoleta.Service.LocationService
import br.edu.utfpr.geocoleta.databinding.ActivityConfirmSelectionBinding
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class ConfirmSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmSelectionBinding
    private lateinit var ivBack: ImageView
    private lateinit var repositoryTrucker: TruckerRepository
    private var rotaId: Int = 0
    private var caminhaoId: Int = 0
    private lateinit var motorista: Trucker

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                handlePostPermissionCheck()
            } else {
                Toast.makeText(this, "As permissões de localização são necessárias para iniciar a rota.", Toast.LENGTH_LONG).show()
                binding.startRouteButton.isEnabled = true // Re-enable button if permissions are denied
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repositoryTrucker = TruckerRepository(this)
        val sharedCpf = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val cpf = sharedCpf.getString("cpf_usuario", null)?.replace("[^\\d]".toRegex(), "")

        if (cpf.isNullOrEmpty()) {
            Toast.makeText(this, "CPF não encontrado. Faça login novamente.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val motoristaEncontrado = repositoryTrucker.findByCpf(cpf)
        if (motoristaEncontrado == null) {
            Toast.makeText(this, "Motorista não encontrado para o CPF: $cpf", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        motorista = motoristaEncontrado

        ivBack = findViewById(R.id.ivBack)

        rotaId = intent.getIntExtra("ROTA_ID", 0)
        caminhaoId = intent.getIntExtra("CAMINHAO_ID", 0)

        val rotaNome = intent.getStringExtra("ROTA_NOME")
        val rotaObservacoes = intent.getStringExtra("ROTA_OBSERVACOES")
        val caminhaoPlaca = intent.getStringExtra("CAMINHAO_PLACA")
        val caminhaoModelo = intent.getStringExtra("CAMINHAO_MODELO")

        binding.truckPlateTextView.text = caminhaoPlaca
        binding.truckModelTextView.text = caminhaoModelo
        binding.routeNameTextView.text = rotaNome
        binding.routeObservationsTextView.text = rotaObservacoes

        ivBack.setOnClickListener {
            finish()
        }

        binding.startRouteButton.setOnClickListener {
            binding.startRouteButton.isEnabled = false // Disable button on click
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
        // Inicia o fluxo principal imediatamente para não bloquear o usuário
        startLocationService()
        val intent = Intent(this, RouteInProgressActivity::class.java).apply {
            putExtra("ROTA_ID", rotaId)
        }
        startActivity(intent)
        finish()

        // Tenta registrar o trajeto na rede em segundo plano
        // O usuário já está na próxima tela, apenas um Toast informativo será exibido.
        lifecycleScope.launch {
            try {
                val trajeto = Trajeto(
                    rotaId = rotaId,
                    caminhaoId = caminhaoId,
                    motoristaId = motorista.id
                )

                val response = RetrovitClient.api.registrarTrajeto(trajeto)
                if (response.isSuccessful) {
                    // Feedback positivo quando online
                    Toast.makeText(applicationContext, "Trajeto iniciado e sincronizado.", Toast.LENGTH_SHORT).show()
                } else {
                    // Feedback para erro de servidor (4xx, 5xx)
                    Toast.makeText(applicationContext, "Falha na sincronização (Cód: ${response.code()}). O envio será feito em segundo plano.", Toast.LENGTH_LONG).show()
                }

            } catch (e: UnknownHostException) {
                // Feedback específico para offline
                Toast.makeText(applicationContext, "Sem conexão. A rota iniciou offline e será enviada depois.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                // Feedback para outros erros inesperados
                e.printStackTrace()
                Toast.makeText(applicationContext, "A rota iniciou offline. Ocorreu um erro ao sincronizar.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java).apply {
            putExtra("ROTA_ID", rotaId)
        }
        ContextCompat.startForegroundService(this, intent)
    }
}
