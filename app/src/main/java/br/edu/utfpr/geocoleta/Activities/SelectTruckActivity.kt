package br.edu.utfpr.geocoleta.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.geocoleta.Adapters.TruckAdapter
import br.edu.utfpr.geocoleta.Data.Models.Truck
import br.edu.utfpr.geocoleta.Data.Repository.TruckRepository
import br.edu.utfpr.geocoleta.R
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectTruckActivity : AppCompatActivity() {

    private lateinit var listaTrucks: List<Truck>
    private lateinit var truckAdapter: TruckAdapter
    private lateinit var truckRepository: TruckRepository

    private var truckSelecionado: Truck? = null

    private lateinit var etBuscar: EditText
    private lateinit var btnBuscar: MaterialButton
    private lateinit var btnConfirmar: MaterialButton
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_truck)

        setupViews()
        setupListeners()
        loadTrucksFromDatabase()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerViewTrucks)
        etBuscar = findViewById(R.id.etBuscarPlaca)
        btnBuscar = findViewById(R.id.btnBuscar)
        btnConfirmar = findViewById(R.id.btnConfirmar)

        recyclerView.layoutManager = LinearLayoutManager(this)
        truckRepository = TruckRepository(this)
    }

    private fun loadTrucksFromDatabase() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                // Busca os caminhões usando o Service em background
                val trucks = withContext(Dispatchers.IO) {
                    truckRepository.listAll()
                }

                // Atualiza UI na thread principal
                withContext(Dispatchers.Main) {
                    if (trucks.isEmpty()) {
                        Toast.makeText(
                            this@SelectTruckActivity,
                            "Nenhum caminhão encontrado no banco de dados.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        setupRecyclerViewWithData(trucks)
                        Toast.makeText(
                            this@SelectTruckActivity,
                            "${trucks.size} caminhão(ões) carregado(s).",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    showLoading(false)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@SelectTruckActivity,
                        "Erro ao carregar caminhões: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    showLoading(false)
                }
                e.printStackTrace()
            }
        }
    }

    private fun setupRecyclerViewWithData(trucks: List<Truck>) {
        listaTrucks = trucks

        truckAdapter = TruckAdapter(listaTrucks) { selecionado ->
            truckSelecionado = selecionado
            Toast.makeText(this, "Selecionou: ${selecionado.placa}", Toast.LENGTH_SHORT).show()
        }

        recyclerView.adapter = truckAdapter
    }

    private fun showLoading(isLoading: Boolean) {
        recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        btnBuscar.isEnabled = !isLoading
        btnConfirmar.isEnabled = !isLoading
        etBuscar.isEnabled = !isLoading
    }

    private fun setupListeners() {
        btnBuscar.setOnClickListener {
            handleSearch()
        }

        btnConfirmar.setOnClickListener {
            handleConfirmation()
        }
    }

    private fun handleSearch() {
        val query = etBuscar.text.toString().trim().uppercase()

        val filtrados = if (query.isEmpty()) {
            listaTrucks
        } else {
            listaTrucks.filter { it.placa.contains(query, ignoreCase = true) }
        }

        truckAdapter.updateList(filtrados)
        truckSelecionado = null

        if (filtrados.isEmpty()) {
            Toast.makeText(this, "Nenhum caminhão encontrado com '$query'", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleConfirmation() {
        truckSelecionado?.let { truck ->
            Toast.makeText(this, "Confirmado: ${truck.placa}", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, SelectRouteActivity::class.java).apply {
                putExtra("placa", truck.placa)
                putExtra("descricao", truck.modelo)
                putExtra("truck_id", truck.id)
            }
            startActivity(intent)
            finish()

        } ?: run {
            Toast.makeText(this, "Por favor, selecione um caminhão!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}