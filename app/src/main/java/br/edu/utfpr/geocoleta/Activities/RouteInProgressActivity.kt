package br.edu.utfpr.geocoleta.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient
import br.edu.utfpr.geocoleta.Data.Repository.CoordinatesRepository
import br.edu.utfpr.geocoleta.R
import br.edu.utfpr.geocoleta.databinding.ActivityRouteInProgressBinding
import kotlinx.coroutines.launch

class RouteInProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRouteInProgressBinding
    private lateinit var ivBack: ImageView
    private var rotaId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteInProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rotaId = intent?.getIntExtra("ROTA_ID", 0) ?: 0

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

            // 2️⃣ Lança coroutine para enviar dados
            lifecycleScope.launch {
                try {
                    // Pega todas as coordenadas do repositório local
                    val coordinates = CoordinatesRepository(this@RouteInProgressActivity).listByRotaId(rotaId)

                    if (coordinates.isNotEmpty()) {
                        // Chamada da API
                        val response = RetrovitClient.api.sendCoordinate(coordinates)

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
                    // 3️⃣ Volta para MainActivity
//                    val intent = Intent(this@RouteInProgressActivity, MainActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    startActivity(intent)
                }
            }
        }
    }
}