package br.edu.utfpr.geocoleta.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.geocoleta.Adapters.TruckAdapter
import br.edu.utfpr.geocoleta.Models.Truck
import br.edu.utfpr.geocoleta.R
import com.google.android.material.button.MaterialButton // 💡 Melhoria: Importa o componente correto

class SelectTruckActivity : AppCompatActivity() {

    // 💡 Melhoria: Mova a lista mockada para ser inicializada de forma tardia
    private lateinit var listaTrucks: List<Truck>

    // 💡 Melhoria: Torne o adapter lateinit, inicializando-o no setup.
    private lateinit var truckAdapter: TruckAdapter

    private var truckSelecionado: Truck? = null

    // Declarações das Views para evitar passagens repetidas ou acesso lento
    private lateinit var etBuscar: EditText
    private lateinit var btnBuscar: MaterialButton // 💡 Sincronia: Usa MaterialButton
    private lateinit var btnConfirmar: MaterialButton // 💡 Sincronia: Usa MaterialButton
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_truck)

        // 1. Inicializa Views
        setupViews()

        // 2. Prepara e configura a lista de caminhões (Mock/API)
        loadTrucksAndSetupRecyclerView()

        // 3. Configura a lógica dos botões
        setupListeners()
    }

    // --- Funções de Setup e Inicialização ---

    private fun setupViews() {
        // 💡 Melhoria: Mapeamento de Views de forma centralizada e com o tipo correto
        recyclerView = findViewById(R.id.recyclerViewTrucks)
        etBuscar = findViewById(R.id.etBuscarPlaca)
        btnBuscar = findViewById(R.id.btnBuscar)
        btnConfirmar = findViewById(R.id.btnConfirmar)

        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadTrucksAndSetupRecyclerView() {
        // Lista mockada (Simula a busca inicial da API)
        listaTrucks = listOf(
            Truck("ACB000", "Caminhão Volvo, azul"),
            Truck("XYZ123", "Caminhão Mercedes, branco"),
            Truck("JHK456", "Caminhão Scania, vermelho"),
            Truck("KLM789", "Caminhão Iveco, preto")
        )

        // 💡 Melhoria: Renomeado para 'truckAdapter' para maior clareza
        truckAdapter = TruckAdapter(listaTrucks) { selecionado ->
            truckSelecionado = selecionado
            // Opcional: Adicionar feedback visual ao caminhão selecionado aqui
        }

        recyclerView.adapter = truckAdapter
    }

    // --- Funções de Lógica ---

    private fun setupListeners() {
        // Lógica do Botão Buscar
        btnBuscar.setOnClickListener {
            handleSearch()
        }

        // Lógica do Botão Confirmar
        btnConfirmar.setOnClickListener {
            handleConfirmation()
        }
    }

    private fun handleSearch() {
        val query = etBuscar.text.toString().trim().uppercase()

        // Filtra a lista, ignorando case e mantendo a lista original se a query estiver vazia
        val filtrados = if (query.isEmpty()) {
            listaTrucks
        } else {
            listaTrucks.filter { it.placa.contains(query, ignoreCase = true) }
        }

        truckAdapter.updateList(filtrados)
        // 💡 Melhoria UX: Zera o caminhão selecionado após uma nova busca/filtro
        truckSelecionado = null
    }

    private fun handleConfirmation() {
        // Utiliza o safe call 'let' para garantir que há um caminhão selecionado
        truckSelecionado?.let { truck ->
            Toast.makeText(this, "Confirmado: ${truck.placa}", Toast.LENGTH_SHORT).show()

            // Navegação: Envia o dado e avança para a próxima Activity (SelectRouteActivity)
            val intent = Intent(this, SelectRouteActivity::class.java).apply {
                putExtra("placa", truck.placa)
                putExtra("descricao", truck.descricao)
            }
            startActivity(intent)

            // 💡 CORREÇÃO DE FLUXO: Fecha esta Activity para que o botão 'Voltar'
            // no Android não a reabra a partir da próxima tela.
            finish()

        } ?: run {
            // Caso o caminhão seja nulo, mostra um aviso
            Toast.makeText(this, "Por favor, selecione um caminhão!", Toast.LENGTH_SHORT).show()
        }
    }
}