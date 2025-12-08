package br.edu.utfpr.geocoleta.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.geocoleta.Adapters.RouteAdapter
import br.edu.utfpr.geocoleta.Data.Models.Route
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient
import br.edu.utfpr.geocoleta.Data.Repository.RouteRepository
import br.edu.utfpr.geocoleta.R
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.UnknownHostException

class SelectRouteActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var routeAdapter: RouteAdapter
    private lateinit var btnConfirmar: MaterialButton
    private lateinit var routeRepository: RouteRepository
    private lateinit var ivBack: ImageView
    private lateinit var tvEmptyState: TextView
    private lateinit var etBuscar: EditText
    private lateinit var listaRotas: List<Route>

    private var selectedRoute: Route? = null
    private var truckId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_route)

        truckId = intent.getIntExtra("truck_id", 0)
        if (truckId == 0) {
            Toast.makeText(this, "Erro: ID do caminhão não recebido.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        initializeViews()
        setupListeners()
        loadCompatibleRoutes()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewRotas)
        btnConfirmar = findViewById(R.id.btnConfirmar)
        ivBack = findViewById(R.id.ivBack)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        etBuscar = findViewById(R.id.etBuscarRota)

        recyclerView.layoutManager = LinearLayoutManager(this)
        routeRepository = RouteRepository(this)
    }

    private fun setupListeners() {
        ivBack.setOnClickListener { finish() }

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { handleSearch() }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnConfirmar.setOnClickListener {
            selectedRoute?.let {
                val placaCaminhao = intent.getStringExtra("placa")
                val modeloCaminhao = intent.getStringExtra("descricao")

                val intent = Intent(this, ConfirmSelectionActivity::class.java).apply {
                    putExtra("ROTA_ID", it.id)
                    putExtra("ROTA_NOME", it.nome)
                    putExtra("ROTA_OBSERVACOES", it.observacoes)
                    putExtra("CAMINHAO_PLACA", placaCaminhao)
                    putExtra("CAMINHAO_MODELO", modeloCaminhao)
                    putExtra("CAMINHAO_ID", truckId)
                }
                startActivity(intent)
            } ?: Toast.makeText(this, "Por favor, selecione uma rota.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCompatibleRoutes() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                // Tenta buscar online
                val compatibleRoutes = withContext(Dispatchers.IO) {
                    RetrovitClient.api.getRotasCompativeis(truckId)
                }

                // Salva no cache para uso offline
                val routeIds = compatibleRoutes.map { it.id.toString() }.toSet()
                val prefs = getSharedPreferences("CompatibleRoutesCache", Context.MODE_PRIVATE)
                prefs.edit().putStringSet("truck_${truckId}_routes", routeIds).apply()

                displayRoutes(compatibleRoutes)

            } catch (e: UnknownHostException) {
                // Modo Offline
                Toast.makeText(this@SelectRouteActivity, "Sem conexão. Exibindo rotas disponíveis offline.", Toast.LENGTH_LONG).show()
                val allLocalRoutes = withContext(Dispatchers.IO) { routeRepository.listAll() }

                val prefs = getSharedPreferences("CompatibleRoutesCache", Context.MODE_PRIVATE)
                val cachedRouteIds = prefs.getStringSet("truck_${truckId}_routes", emptySet())?.map { it.toInt() }?.toSet() ?: emptySet()

                val offlineRoutes = allLocalRoutes.filter { it.id in cachedRouteIds }
                displayRoutes(offlineRoutes)

            } catch (e: Exception) {
                // Outros erros
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    tvEmptyState.visibility = View.VISIBLE
                    Toast.makeText(this@SelectRouteActivity, "Erro ao carregar rotas.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun displayRoutes(routes: List<Route>) {
        withContext(Dispatchers.Main) {
            listaRotas = routes
            if (routes.isNotEmpty()) {
                setupRecyclerViewWithData(routes)
                tvEmptyState.visibility = View.GONE
            } else {
                tvEmptyState.text = "Nenhuma rota compatível encontrada para este caminhão."
                tvEmptyState.visibility = View.VISIBLE
            }
            showLoading(false)
        }
    }

    private fun setupRecyclerViewWithData(routes: List<Route>) {
        routeAdapter = RouteAdapter(routes) { rotaSelecionada ->
            selectedRoute = rotaSelecionada
        }
        recyclerView.adapter = routeAdapter
    }

    private fun handleSearch() {
        val query = etBuscar.text.toString().trim()
        val filtradas = if (query.isEmpty()) {
            listaRotas
        } else {
            listaRotas.filter {
                it.nome.contains(query, ignoreCase = true) || 
                (it.observacoes?.contains(query, ignoreCase = true) ?: false)
            }
        }
        routeAdapter.updateList(filtradas)
        selectedRoute = null

        tvEmptyState.visibility = if (filtradas.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        btnConfirmar.isEnabled = !isLoading
        etBuscar.isEnabled = !isLoading
    }
}