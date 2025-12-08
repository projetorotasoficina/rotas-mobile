package br.edu.utfpr.geocoleta.Activities

import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.edu.utfpr.geocoleta.Data.Models.Trajeto
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient
import br.edu.utfpr.geocoleta.R
import br.edu.utfpr.geocoleta.Service.LocationDataBus
import br.edu.utfpr.geocoleta.Service.LocationService
import br.edu.utfpr.geocoleta.databinding.ActivityRouteInProgressBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RouteInProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRouteInProgressBinding
    private lateinit var trajeto: Trajeto

    private var totalDistanceMeters: Float = 0f

    // Controle de tempo
    private var startTimeMillis: Long = 0L
    private var hasStartTime: Boolean = false
    private var firstUpdateReceived: Boolean = false
    private var timerJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteInProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loadingLayout.visibility = View.VISIBLE

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
            showConfirmationDialog()
        }

        startCollectingLocationData()
        startTimer()
    }

    private fun startCollectingLocationData() {
        lifecycleScope.launch {
            LocationDataBus.locationFlow.collect { update ->
                totalDistanceMeters = update.totalDistanceMeters
                val elapsedSeconds = update.elapsedSeconds

                if (!firstUpdateReceived) {
                    firstUpdateReceived = true
                    binding.loadingLayout.visibility = View.GONE
                }

                if (!hasStartTime) {
                    startTimeMillis = System.currentTimeMillis() - (elapsedSeconds * 1000L)
                    hasStartTime = true
                }

                val distanciaKm = totalDistanceMeters / 1000f
                binding.distanceTextView.text = String.format("Distância: %.2f km", distanciaKm)
            }
        }
    }

    private fun startTimer() {
        timerJob = lifecycleScope.launch {
            while (true) {
                if (startTimeMillis > 0L) {
                    val elapsedSecondsTimer = (System.currentTimeMillis() - startTimeMillis) / 1000L
                    val tempoTimer = DateUtils.formatElapsedTime(elapsedSecondsTimer)
                    binding.durationTextView.text = "Duração: $tempoTimer"
                }
                delay(1000)
            }
        }
    }

    private fun showConfirmationDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_finish, null)

        val btnPositive = dialogView.findViewById<MaterialButton>(R.id.btn_positive)
        val btnNegative = dialogView.findViewById<MaterialButton>(R.id.btn_negative)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnPositive.setOnClickListener {
            timerJob?.cancel() // Para o timer imediatamente
            dialog.dismiss()
            binding.loadingLayout.visibility = View.VISIBLE
            binding.loadingTextView.text = "Finalizando rota..."
            lifecycleScope.launch {
                finalizarTrajeto()
            }
        }

        btnNegative.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private suspend fun finalizarTrajeto() {
        try {
            trajeto.distanciaTotal = (totalDistanceMeters / 1000.0)

            val updateResponse = RetrovitClient.api.updateTrajeto(trajeto.id!!, trajeto)
            if (!updateResponse.isSuccessful) {
                binding.loadingLayout.visibility = View.GONE
                Toast.makeText(this, "Erro ao atualizar a distância: ${updateResponse.code()}", Toast.LENGTH_LONG).show()
                return
            }

            val finalizaResponse = RetrovitClient.api.finalizarTrajeto(trajeto.id!!)
            if (finalizaResponse.isSuccessful) {
                stopService(Intent(this, LocationService::class.java))
                binding.loadingLayout.visibility = View.GONE

                val elapsedSeconds = if (startTimeMillis > 0L) {
                    (System.currentTimeMillis() - startTimeMillis) / 1000L
                } else {
                    0L
                }

                showRouteSummaryDialog(elapsedSeconds, totalDistanceMeters)

            } else {
                binding.loadingLayout.visibility = View.GONE
                Toast.makeText(this, "Erro ao finalizar o trajeto: ${finalizaResponse.code()}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.loadingLayout.visibility = View.GONE
            Toast.makeText(this, "Falha na conexão: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showRouteSummaryDialog(elapsedSeconds: Long, distanceMeters: Float) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_route_summary, null)

        val tvResumoDuracao = dialogView.findViewById<TextView>(R.id.tvResumoDuracao)
        val tvResumoDistancia = dialogView.findViewById<TextView>(R.id.tvResumoDistancia)
        val btnIniciarNovaRota = dialogView.findViewById<MaterialButton>(R.id.btnIniciarNovaRota)
        val btnVoltarInicio = dialogView.findViewById<MaterialButton>(R.id.btnVoltarInicio)

        tvResumoDuracao.text = "Duração: ${DateUtils.formatElapsedTime(elapsedSeconds)}"
        tvResumoDistancia.text = String.format("Distância: %.2f km", distanceMeters / 1000f)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnIniciarNovaRota.setOnClickListener {
            val intent = Intent(this, SelectTruckActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            dialog.dismiss()
            finish()
        }

        btnVoltarInicio.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }
}
