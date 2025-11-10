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
import br.edu.utfpr.geocoleta.Data.Models.Trajeto
import br.edu.utfpr.geocoleta.Data.Models.Trucker
import br.edu.utfpr.geocoleta.Data.Repository.TrajetoRepository
import br.edu.utfpr.geocoleta.Data.Repository.TruckerRepository
import br.edu.utfpr.geocoleta.R
import br.edu.utfpr.geocoleta.Service.LocationService
import br.edu.utfpr.geocoleta.databinding.ActivityConfirmSelectionBinding

class ConfirmSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmSelectionBinding
    private lateinit var ivBack: ImageView
    private lateinit var repositoryTrucker: TruckerRepository
    private lateinit var trajetoRepository: TrajetoRepository
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
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repositoryTrucker = TruckerRepository(this)
        trajetoRepository = TrajetoRepository(this)

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
        motorista = repositoryTrucker.findByCpf(cpf)!!

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
        val trajeto = Trajeto(
            rotaId = rotaId,
            caminhaoId = caminhaoId,
            motoristaId = motorista.id
        )

        val localTrajetoId = System.currentTimeMillis().toInt()
        trajetoRepository.startTrajeto(trajeto, localTrajetoId)

        startLocationService(localTrajetoId)

        Toast.makeText(this, "Trajeto iniciado. Sincronizando em segundo plano.", Toast.LENGTH_LONG).show()

        val intent = Intent(this, RouteInProgressActivity::class.java).apply {
            putExtra("TRAJETO_ID", localTrajetoId)
        }
        startActivity(intent)
        finish()
    }

    private fun startLocationService(trajetoId: Int) {
        val intent = Intent(this, LocationService::class.java).apply {
            putExtra("ROTA_ID", trajetoId)
        }
        ContextCompat.startForegroundService(this, intent)
    }
}