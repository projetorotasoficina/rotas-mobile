package br.edu.utfpr.geocoleta.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.geocoleta.Adapters.RouteAdapter
import br.edu.utfpr.geocoleta.Models.Route
import br.edu.utfpr.geocoleta.R
import com.google.android.material.button.MaterialButton

class SelectRouteActivity : AppCompatActivity() {

    private var selectedRoute: Route? = null

    private val listaRotas = listOf(
        Route("ROTA A", "Fraron, Centro, Pinheiros"),
        Route("ROTA B", "Alvorada, Menino Deus"),
        Route("ROTA C", "Brasilia, Planalto"),
        Route("ROTA D", "Morumbi, São Cristovão")
    )

    private val routeAdapter = RouteAdapter(listaRotas) { rotaSelecionada ->
        // Armazena a rota selecionada
        selectedRoute = rotaSelecionada
        Toast.makeText(this, "Rota ${rotaSelecionada.titulo} selecionada.", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_route)

        setupRecyclerView()
        setupConfirmButton()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRotas)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SelectRouteActivity)
            adapter = routeAdapter
        }
    }

    private fun setupConfirmButton() {
        val btnConfirmar = findViewById<MaterialButton>(R.id.btnConfirmar)

        btnConfirmar.setOnClickListener {

            // 1. Valida se uma rota foi realmente selecionada
            selectedRoute?.let { rota ->

                // Pega dados da tela anterior (caminhão) para levar adiante
                val placaCaminhao = intent.getStringExtra("placa") ?: "Não Informada"

                Toast.makeText(this,
                    "Confirmação: Rota ${rota.titulo} com Caminhão ${placaCaminhao}.",
                    Toast.LENGTH_LONG).show()

                // 2. CORREÇÃO: Navega para a InitActivity para iniciar o serviço e pedir permissões.
                val intent = Intent(this, InitActivity::class.java).apply {
                    // Passa a rota e o caminhão para que a InitActivity possa usá-los (se necessário)
                    putExtra("ROTA_TITULO", rota.titulo)
                    putExtra("CAMINHAO_PLACA", placaCaminhao)
                }
                startActivity(intent)

                // 3. Fecha esta Activity, pois o processo de seleção terminou.
                finish()

            } ?: run {
                // Se a rota for nula
                Toast.makeText(this, "Por favor, selecione uma rota.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}