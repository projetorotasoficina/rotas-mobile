package br.edu.utfpr.geocoleta.Activities

import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.edu.utfpr.geocoleta.Data.Models.Trajeto
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient
import br.edu.utfpr.geocoleta.Service.LocationDataBus
import br.edu.utfpr.geocoleta.Service.LocationService
import br.edu.utfpr.geocoleta.databinding.ActivityRouteInProgressBinding
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RouteInProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRouteInProgressBinding
    private lateinit var trajeto: Trajeto

    private var totalDistanceMeters: Float = 0f

    // controle de tempo em tempo real
    private var startTimeMillis: Long = 0L
    private var hasStartTime: Boolean = false
    private var firstUpdateReceived: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteInProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // estado inicial: mostrar overlay de carregamento (buscar GPS)
        binding.loadingLayout.visibility = View.VISIBLE
        // texto padrão definido no XML: "Preparando rota, buscando sinal de GPS..."

        val trajetoJson = intent.getStringExtra("TRAJETO_JSON")
        if (trajetoJson == null) {
            Toast.makeText(this, "Erro: Dados do trajeto não recebidos.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        trajeto = Gson().fromJson(trajetoJson, Trajeto::class.java)

        binding.registerIncidentButton.setOnClickListener {
            val intent = Intent(this, RegisterIncidentActivity::class.java)
            intent.putExtra("TRAJETO_ID", trajeto.id)
            startActivity(intent)
        }

        binding.finishRouteButton.setOnClickListener {
            lifecycleScope.launch {
                // mostra overlay de "Finalizando rota..."
                binding.loadingLayout.visibility = View.VISIBLE
                // se tiver o TextView no loader:
                binding.loadingTextView.text = "Finalizando rota..."
                finalizarTrajeto()
            }
        }

        // recebe atualizações de tempo e distância do serviço
        lifecycleScope.launch {
            LocationDataBus.locationFlow.collect { update ->
                totalDistanceMeters = update.totalDistanceMeters
                val elapsedSeconds = update.elapsedSeconds

                // primeira atualização real: some o "carregando GPS"
                if (!firstUpdateReceived) {
                    firstUpdateReceived = true
                    binding.loadingLayout.visibility = View.GONE
                }

                // calcula o horário real de início com base no elapsed recebido
                if (!hasStartTime) {
                    startTimeMillis = System.currentTimeMillis() - (elapsedSeconds * 1000L)
                    hasStartTime = true
                }

                val tempo = DateUtils.formatElapsedTime(elapsedSeconds)
                val distanciaKm = totalDistanceMeters / 1000f

                binding.durationTextView.text = "Duração: $tempo"
                binding.distanceTextView.text = String.format("Distância: %.2f km", distanciaKm)
            }
        }

        // timer para atualizar o tempo em tempo real, mesmo sem novos pontos
        lifecycleScope.launch {
            while (true) {
                if (startTimeMillis > 0L) {
                    val elapsedSecondsTimer =
                        (System.currentTimeMillis() - startTimeMillis) / 1000L
                    val tempoTimer = DateUtils.formatElapsedTime(elapsedSecondsTimer)
                    binding.durationTextView.text = "Duração: $tempoTimer"
                }
                delay(1000)
            }
        }
    }

    private suspend fun finalizarTrajeto() {
        try {
            // 1. Atualiza a distância total no objeto trajeto
            trajeto.distanciaTotal = (totalDistanceMeters / 1000.0)

            // 2. Envia o trajeto atualizado para o endpoint de update
            val updateResponse = RetrovitClient.api.updateTrajeto(trajeto.id!!, trajeto)
            if (!updateResponse.isSuccessful) {
                binding.loadingLayout.visibility = View.GONE
                Toast.makeText(
                    this,
                    "Erro ao atualizar a distância do trajeto: ${updateResponse.code()}",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            // 3. Finaliza o trajeto (agora que a distância foi salva)
            val finalizaResponse = RetrovitClient.api.finalizarTrajeto(trajeto.id!!)
            if (finalizaResponse.isSuccessful) {
                stopService(Intent(this, LocationService::class.java))
                Toast.makeText(this, "Trajeto finalizado com sucesso!", Toast.LENGTH_LONG).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            } else {
                binding.loadingLayout.visibility = View.GONE
                Toast.makeText(
                    this,
                    "Erro ao finalizar o trajeto: ${finalizaResponse.code()}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.loadingLayout.visibility = View.GONE
            Toast.makeText(this, "Falha na conexão: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}