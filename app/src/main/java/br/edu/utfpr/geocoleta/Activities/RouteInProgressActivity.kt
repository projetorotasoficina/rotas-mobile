package br.edu.utfpr.geocoleta.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.edu.utfpr.geocoleta.Data.Models.CoordinatesDTO
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient
import br.edu.utfpr.geocoleta.Data.Repository.CoordinatesRepository
import br.edu.utfpr.geocoleta.R
import br.edu.utfpr.geocoleta.Service.LocationService
import br.edu.utfpr.geocoleta.databinding.ActivityRouteInProgressBinding
import kotlinx.coroutines.launch

class RouteInProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRouteInProgressBinding
    private lateinit var ivBack: ImageView
    private var rotaId: Int = 0
    private var trajetoId : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteInProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rotaId = intent?.getIntExtra("ROTA_ID", 0) ?: 0
        trajetoId = intent?.getIntExtra("TRAJETO_ID", 0) ?: 0

        ivBack = findViewById(R.id.ivBack)

        ivBack.setOnClickListener {
            finish()
        }

        binding.registerIncidentButton.setOnClickListener {
            val intent = Intent(this, RegisterIncidentActivity::class.java)
            startActivity(intent)
        }

        binding.finishRouteButton.setOnClickListener {
            Toast.makeText(this, "Finalizando rota...", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                val finalizadoComSucesso = finalizarTrajeto()

                if (finalizadoComSucesso) {
                    seendCoordinates()
                } else {
                    Toast.makeText(
                        this@RouteInProgressActivity,
                        "Falha ao finalizar trajeto. Coordenadas não serão enviadas.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun seendCoordinates(){
        val pendentes = CoordinatesRepository(this@RouteInProgressActivity).listarPendentes()
        if (pendentes.isEmpty()) return
        lifecycleScope.launch {
            try {
                if (pendentes.isNotEmpty()) {
                    val response = RetrovitClient.api.sendCoordinate(CoordinatesDTO.fromEntityList(pendentes))

                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@RouteInProgressActivity,
                            "Coordenadas enviadas com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@RouteInProgressActivity,
                            "Falha ao enviar coordenadas: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@RouteInProgressActivity,
                        "Nenhuma coordenada para enviar.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@RouteInProgressActivity,
                    "Erro ao enviar coordenadas: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } finally {
                val intent = Intent(this@RouteInProgressActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    suspend fun finalizarTrajeto(): Boolean {
        return try {
            val response = RetrovitClient.api.finalizarTrajeto(trajetoId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Toast.makeText(
                        this@RouteInProgressActivity,
                        "Trajeto ${body.id} finalizado! Status: ${body.status}",
                        Toast.LENGTH_LONG
                    ).show()
                    stopService(Intent(this@RouteInProgressActivity, LocationService::class.java))
                    true
                } else {
                    Toast.makeText(
                        this@RouteInProgressActivity,
                        "Resposta vazia ao finalizar trajeto.",
                        Toast.LENGTH_LONG
                    ).show()
                    false
                }
            } else {
                Toast.makeText(
                    this@RouteInProgressActivity,
                    "Erro ao finalizar trajeto: ${response.code()}",
                    Toast.LENGTH_LONG
                ).show()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this@RouteInProgressActivity,
                "Falha na conexão: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }
}