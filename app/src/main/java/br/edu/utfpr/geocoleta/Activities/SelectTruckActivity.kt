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
import br.edu.utfpr.geocoleta.Adapters.TruckAdapter
import br.edu.utfpr.geocoleta.Data.Models.Truck
import br.edu.utfpr.geocoleta.Data.Repository.TruckRepository
import br.edu.utfpr.geocoleta.R
import br.edu.utfpr.geocoleta.Service.LocationDataBus
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectTruckActivity : AppCompatActivity() {

    private var listaTrucks: List<Truck> = emptyList()
    private lateinit var truckAdapter: TruckAdapter
    private lateinit var truckRepository: TruckRepository

    private var truckSelecionado: Truck? = null

    private lateinit var etBuscar: EditText
    private lateinit var btnConfirmar: MaterialButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var ivBack: ImageView
    private lateinit var tvEmptyState: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_truck)

        LocationDataBus.reset()

        setupViews()
        setupListeners()
        loadTrucksFromDatabase()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerViewTrucks)
        etBuscar = findViewById(R.id.etBuscarPlaca)
        btnConfirmar = findViewById(R.id.btnConfirmar)
        ivBack = findViewById(R.id.ivBack)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        recyclerView.layoutManager = LinearLayoutManager(this)
        truckRepository = TruckRepository(this)
    }

    private fun loadTrucksFromDatabase() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val trucks = withContext(Dispatchers.IO) {
                    truckRepository.listAll()
                }

                withContext(Dispatchers.Main) {
                    if (trucks.isNotEmpty()) {
                        setupRecyclerViewWithData(trucks.sortedByDescending { it.ativo })
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
                    Toast.makeText(this@SelectTruckActivity, "Erro ao carregar caminhões.", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }

    private fun setupRecyclerViewWithData(trucks: List<Truck>) {
        listaTrucks = trucks

        truckAdapter = TruckAdapter(listaTrucks) { selecionado ->
            truckSelecionado = selecionado
        }

        recyclerView.adapter = truckAdapter
    }

    private fun showLoading(isLoading: Boolean) {
        recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        btnConfirmar.isEnabled = !isLoading
        etBuscar.isEnabled = !isLoading
    }

    private fun setupListeners() {
        ivBack.setOnClickListener {
            finish()
        }

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                handleSearch()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnConfirmar.setOnClickListener {
            handleConfirmation()
        }
    }

    private fun handleSearch() {
        val query = etBuscar.text.toString().trim()

        val filtrados = if (query.isEmpty()) {
            listaTrucks
        } else {
            listaTrucks.filter {
                it.placa.contains(query, ignoreCase = true) || it.modelo.contains(query, ignoreCase = true)
            }
        }

        truckAdapter.updateList(filtrados.sortedByDescending { it.ativo })
        truckSelecionado = null

        if (filtrados.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
        } else {
            tvEmptyState.visibility = View.GONE
        }
    }

    private fun handleConfirmation() {
        truckSelecionado?.let { truck ->
            val intent = Intent(this, SelectRouteActivity::class.java).apply {
                putExtra("placa", truck.placa)
                putExtra("descricao", truck.modelo)
                putExtra("truck_id", truck.id)
                putExtra("residueType", truck.tipoResiduo)
            }
            startActivity(intent)

        } ?: run {
            Toast.makeText(this, "Por favor, selecione um caminhão!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}