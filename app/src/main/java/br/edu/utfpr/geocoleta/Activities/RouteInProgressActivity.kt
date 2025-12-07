package br.edu.utfpr.geocoleta.Activities

import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.edu.utfpr.geocoleta.Data.Models.Trajeto
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient
import br.edu.utfpr.geocoleta.R
import br.edu.utfpr.geocoleta.Service.LocationDataBus
import br.edu.utfpr.geocoleta.Service.LocationService
import br.edu.utfpr.geocoleta.databinding.ActivityRouteInProgressBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch

class RouteInProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRouteInProgressBinding
    private lateinit var ivBack: ImageView
    private lateinit var trajeto: Trajeto

    private var totalDistanceMeters: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteInProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val trajetoJson = intent.getStringExtra("TRAJETO_JSON")
        if (trajetoJson == null) {
            Toast.makeText(this, "Erro: Dados do trajeto não recebidos.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        trajeto = Gson().fromJson(trajetoJson, Trajeto::class.java)

        ivBack = findViewById(R.id.ivBack)

        ivBack.setOnClickListener {
            finish()
        }

        binding.registerIncidentButton.setOnClickListener {
            val intent = Intent(this, RegisterIncidentActivity::class.java)
            intent.putExtra("TRAJETO_ID", trajeto.id)
            startActivity(intent)
        }

        binding.finishRouteButton.setOnClickListener {
            Toast.makeText(this, "Finalizando rota...", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                finalizarTrajeto()
            }
        }

        lifecycleScope.launch {
            LocationDataBus.locationFlow.collect { update ->
                totalDistanceMeters = update.totalDistanceMeters
                val elapsedSeconds = update.elapsedSeconds

                val tempo = DateUtils.formatElapsedTime(elapsedSeconds)
                val distanciaKm = totalDistanceMeters / 1000f

                binding.durationTextView.text = "Duração: $tempo"
                binding.distanceTextView.text = String.format("Distância: %.2f km", distanciaKm)
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
                Toast.makeText(this, "Erro ao atualizar a distância do trajeto: ${updateResponse.code()}", Toast.LENGTH_LONG).show()
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
                Toast.makeText(this, "Erro ao finalizar o trajeto: ${finalizaResponse.code()}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Falha na conexão: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}