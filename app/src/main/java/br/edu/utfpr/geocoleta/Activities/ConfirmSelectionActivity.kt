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

class ConfirmSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmSelectionBinding
    private lateinit var ivBack: ImageView
    private lateinit var repositoryTrucker: TruckerRepository
    private var rotaId: Int = 0
    private var caminhaoId: Int = 0
    private lateinit var motorista: Trucker
    private var rota_id_back : Int = 0

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



        ivBack = findViewById(R.id.ivBack)

        rotaId = intent.getIntExtra("ROTA_ID", 0)
        caminhaoId = intent.getIntExtra("CAMINHAO_ID", 0)
        motorista = repositoryTrucker.findByCpf(cpf!!)!!

        val rotaNome = intent.getStringExtra("ROTA_NOME")
        val rotaObservacoes = intent.getStringExtra("ROTA_OBSERVACOES")
        val caminhaoPlaca = intent.getStringExtra("CAMINHAO_PLACA")
        val caminhaoModelo = intent.getStringExtra("CAMINHAO_MODELO") // Recebe o modelo

        binding.truckPlateTextView.text = caminhaoPlaca
        binding.truckModelTextView.text = caminhaoModelo // Exibe o modelo
        binding.routeNameTextView.text = rotaNome
        binding.routeObservationsTextView.text = rotaObservacoes
        binding.driverNameTextView.text = motorista.nome

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
        lifecycleScope.launch {
            try {
                val trajeto = Trajeto(
                    rotaId = rotaId,
                    caminhaoId = caminhaoId,
                    motoristaId = motorista.id
                )

                val response = RetrovitClient.api.registrarTrajeto(trajeto)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val id = body.id
                        val rotaIdRetornado = body.rotaId

                        rota_id_back = id

                        Toast.makeText(
                            this@ConfirmSelectionActivity,
                            "Trajeto enviado com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(
                            this@ConfirmSelectionActivity,
                            RouteInProgressActivity::class.java
                        ).apply {
                            putExtra("TRAJETO_ID", id)
                            putExtra("ROTA_ID", rotaId)
                        }
                        startLocationService()
                        startActivity(intent)

                        return@launch
                    }
                } else {
                    Toast.makeText(
                        this@ConfirmSelectionActivity,
                        "Falha ao enviar: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@ConfirmSelectionActivity,
                    "Erro: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }
        }
    }

    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java).apply {
            putExtra("TRAJETO_ID", rota_id_back)
            putExtra("ROTA_ID", rotaId)
        }

        ContextCompat.startForegroundService(this, intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        val finalizeIntent = Intent(this, LocationService::class.java).apply {
            action = LocationService.ACTION_FINALIZAR
        }

        ContextCompat.startForegroundService(this, finalizeIntent)
    }
}