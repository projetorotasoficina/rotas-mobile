package br.edu.utfpr.geocoleta.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.geocoleta.Adapters.RouteAdapter
import br.edu.utfpr.geocoleta.Data.Models.Route
import br.edu.utfpr.geocoleta.Data.Repository.RouteRepository
import br.edu.utfpr.geocoleta.R
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectRouteActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var routeAdapter: RouteAdapter
    private lateinit var btnConfirmar: MaterialButton
    private lateinit var routeRepository: RouteRepository

    private var selectedRoute: Route? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_route)

        initializeViews()
        setupRecyclerView()
        setupConfirmButton()
        loadRoutesFromDatabase()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewRotas)
        btnConfirmar = findViewById(R.id.btnConfirmar)
        routeRepository = RouteRepository(this)
    }

    private fun setupRecyclerView() {
        routeAdapter = RouteAdapter(emptyList()) { rotaSelecionada ->
            selectedRoute = rotaSelecionada
            Toast.makeText(this, "Selecionou: ${rotaSelecionada.nome}", Toast.LENGTH_SHORT).show()
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SelectRouteActivity)
            adapter = routeAdapter
        }
    }

    private fun loadRoutesFromDatabase() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val routes = withContext(Dispatchers.IO) {
                    routeRepository.listAll()
                }

                withContext(Dispatchers.Main) {
                    if (routes.isEmpty()) {
                        Toast.makeText(
                            this@SelectRouteActivity,
                            "Nenhuma rota encontrada no banco de dados.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        updateRoutesList(routes)
                        Toast.makeText(
                            this@SelectRouteActivity,
                            "${routes.size} rota(s) carregada(s).",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    showLoading(false)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SelectRouteActivity,
                        "Erro ao carregar rotas: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    showLoading(false)
                }
                e.printStackTrace()
            }
        }
    }

    private fun updateRoutesList(routes: List<Route>) {
        routeAdapter = RouteAdapter(routes) { rotaSelecionada ->
            selectedRoute = rotaSelecionada
            Toast.makeText(this, "Selecionou: ${rotaSelecionada.nome}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = routeAdapter
    }

    private fun showLoading(isLoading: Boolean) {
        recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        btnConfirmar.isEnabled = !isLoading
    }

    private fun setupConfirmButton() {
        btnConfirmar.setOnClickListener {
            selectedRoute?.let { rota ->
                val placaCaminhao = intent.getStringExtra("placa") ?: "Não Informada"

                Toast.makeText(
                    this,
                    "Confirmação: Rota ${rota.nome} com Caminhão $placaCaminhao.",
                    Toast.LENGTH_LONG
                ).show()

                val intent = Intent(this, InitActivity::class.java).apply {
                    putExtra("ROTA_TITULO", rota.nome)
                    putExtra("ROTA_ID", rota.id)
                }
                startActivity(intent)
                finish()

            } ?: run {
                Toast.makeText(this, "Por favor, selecione uma rota.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}