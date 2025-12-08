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
import br.edu.utfpr.geocoleta.Adapters.TruckAdapter
import br.edu.utfpr.geocoleta.Data.Models.Truck
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient
import br.edu.utfpr.geocoleta.Data.Repository.TruckRepository
import br.edu.utfpr.geocoleta.R
import br.edu.utfpr.geocoleta.Service.LocationDataBus
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.UnknownHostException

class SelectTruckActivity : AppCompatActivity() {

    private var listaTrucks: List<Truck> = emptyList()
    private lateinit var truckAdapter: TruckAdapter
    private lateinit var truckRepository: TruckRepository

    private var motoristaId: Int = 0
    private var truckSelecionado: Truck? = null

    private lateinit var etBuscar: EditText
    private lateinit var btnConfirmar: MaterialButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var ivBack: ImageView
    private lateinit var tvEmptyState: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_truck)

        motoristaId = intent.getIntExtra("MOTORISTA_ID", 0)
        if (motoristaId == 0) {
            Toast.makeText(this, "Erro: ID do motorista não recebido.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        LocationDataBus.reset()

        setupViews()
        setupListeners()
        loadCompatibleTrucks()
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

    private fun loadCompatibleTrucks() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                // Tenta buscar online
                val compatibleTrucks = withContext(Dispatchers.IO) {
                    RetrovitClient.api.getCaminhoesCompativeis(motoristaId)
                }

                // Salva no cache para uso offline
                val truckIds = compatibleTrucks.map { it.id.toString() }.toSet()
                val prefs = getSharedPreferences("CompatibleTrucksCache", Context.MODE_PRIVATE)
                prefs.edit().putStringSet("driver_${motoristaId}_trucks", truckIds).apply()

                displayTrucks(compatibleTrucks)

            } catch (e: UnknownHostException) {
                // Modo Offline
                Toast.makeText(this@SelectTruckActivity, "Sem conexão. Exibindo caminhões disponíveis offline.", Toast.LENGTH_LONG).show()
                val allLocalTrucks = withContext(Dispatchers.IO) { truckRepository.listAll() }

                val prefs = getSharedPreferences("CompatibleTrucksCache", Context.MODE_PRIVATE)
                val cachedTruckIds = prefs.getStringSet("driver_${motoristaId}_trucks", emptySet())?.map { it.toInt() }?.toSet() ?: emptySet()

                val offlineTrucks = allLocalTrucks.filter { it.id in cachedTruckIds }
                displayTrucks(offlineTrucks)

            } catch (e: Exception) {
                // Outros erros
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    tvEmptyState.visibility = View.VISIBLE
                    Toast.makeText(this@SelectTruckActivity, "Erro ao carregar caminhões.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun displayTrucks(trucks: List<Truck>) {
        withContext(Dispatchers.Main) {
            if (trucks.isNotEmpty()) {
                setupRecyclerViewWithData(trucks.sortedByDescending { it.ativo })
                tvEmptyState.visibility = View.GONE
            } else {
                tvEmptyState.visibility = View.VISIBLE
            }
            showLoading(false)
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
}