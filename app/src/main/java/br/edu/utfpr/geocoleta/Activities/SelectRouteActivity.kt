package br.edu.utfpr.geocoleta.Activities

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
    private lateinit var ivBack: ImageView
    private lateinit var tvEmptyState: TextView
    private lateinit var etBuscar: EditText
    private lateinit var listaRotas: List<Route>

    private var selectedRoute: Route? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_route)

        initializeViews()
        setupListeners()
        loadRoutesFromDatabase()
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

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                handleSearch()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnConfirmar.setOnClickListener {
            selectedRoute?.let { rota ->
                val placaCaminhao = intent.getStringExtra("placa")
                val modeloCaminhao = intent.getStringExtra("descricao")
                val caminhao_id = intent.getIntExtra("truck_id", 0)

                val intent = Intent(this, ConfirmSelectionActivity::class.java).apply {
                    putExtra("ROTA_ID", rota.id)
                    putExtra("ROTA_NOME", rota.nome)
                    putExtra("ROTA_OBSERVACOES", rota.observacoes)
                    putExtra("CAMINHAO_PLACA", placaCaminhao)
                    putExtra("CAMINHAO_MODELO", modeloCaminhao)
                    putExtra("CAMINHAO_ID", caminhao_id)
                }
                startActivity(intent)

            } ?: run {
                Toast.makeText(this, "Por favor, selecione uma rota.", Toast.LENGTH_SHORT).show()
            }
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
                    listaRotas = routes
                    if (routes.isNotEmpty()) {
                        setupRecyclerViewWithData(routes)
                        tvEmptyState.visibility = View.GONE
                    } else {
                        tvEmptyState.visibility = View.VISIBLE
                    }
                    showLoading(false)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    tvEmptyState.visibility = View.VISIBLE
                }
                e.printStackTrace()
            }
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

        if (filtradas.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
        } else {
            tvEmptyState.visibility = View.GONE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        btnConfirmar.isEnabled = !isLoading
        etBuscar.isEnabled = !isLoading
    }
}