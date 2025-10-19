package br.edu.utfpr.geocoleta.Activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.edu.utfpr.geocoleta.R
import br.edu.utfpr.geocoleta.Service.LocationService
import com.google.android.material.button.MaterialButton // 💡 Melhoria: Importa o componente MaterialButton

class InitActivity : AppCompatActivity() {

    // Define as permissões de localização primárias
    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Verifica se TODAS as permissões principais foram concedidas
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                // Se concedidas, verifica se o serviço de localização deve ser iniciado ou se precisa da permissão de background
                handlePostPermissionCheck()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init)

        // 💡 Melhoria: Usa MaterialButton para corresponder ao layout XML corrigido
        val btnIniciar = findViewById<MaterialButton>(R.id.btnIniciar)

        btnIniciar.setOnClickListener {
            // Inicia o fluxo de permissões
            checkPermissions()
        }
    }

    private fun checkPermissions() {
        val allGranted = locationPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            // Se as permissões principais já estão OK, verifica e trata a permissão de background (Q+)
            handlePostPermissionCheck()
        } else {
            // Solicita as permissões principais
            requestPermissionLauncher.launch(locationPermissions)
        }
    }

    // 💡 Melhoria: Função para centralizar a lógica após as permissões FINE/COARSE
    private fun handlePostPermissionCheck() {
        // Verifica se o SDK é Q (Android 10) ou superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Solicita a permissão de localização em segundo plano (requer caixa de diálogo separada no Q+)
                requestPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                )
                // Retornar aqui é necessário, pois a navegação só ocorrerá APÓS a resposta do Background Location
                return
            }
        }

        // Se todas as permissões necessárias foram concedidas:
        startAppFlow()
    }

    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        // Usa startForegroundService para garantir que o serviço possa rodar em segundo plano
        ContextCompat.startForegroundService(this, intent)
    }

    // 💡 Melhoria: Função central para iniciar o serviço E navegar para a próxima tela
    private fun startAppFlow() {
        // 1. Inicia o serviço de localização (se for essencial rodar ANTES do login)
        startLocationService()

    }
}