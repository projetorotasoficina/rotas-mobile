package br.edu.utfpr.geocoleta.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.geocoleta.Adapters.TruckAdapter
import br.edu.utfpr.geocoleta.Data.Models.Truck
import br.edu.utfpr.geocoleta.R

class SelectTruckActivity : AppCompatActivity() {

    private lateinit var adapter: TruckAdapter
    private lateinit var listaTrucks: List<Truck>
    private var truckSelecionado: Truck? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_truck)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTrucks)
        val etBuscar = findViewById<EditText>(R.id.etBuscarPlaca)
        val btnBuscar = findViewById<Button>(R.id.btnBuscar)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)

        recyclerView.layoutManager = LinearLayoutManager(this)

//        // Lista mockada (vira da API depois)
//        listaTrucks = listOf(
//            Truck("ACB000", "Caminhão Volvo, azul"),
//            Truck("XYZ123", "Caminhão Mercedes, branco"),
//            Truck("JHK456", "Caminhão Scania, vermelho"),
//            Truck("KLM789", "Caminhão Iveco, preto")
//        )
//
//        adapter = TruckAdapter(listaTrucks) { selecionado ->
//            truckSelecionado = selecionado
//        }
//
//        recyclerView.adapter = adapter
//
//        // Buscar por placa
//        btnBuscar.setOnClickListener {
//            val query = etBuscar.text.toString().trim().uppercase()
//            val filtrados = if (query.isEmpty()) {
//                listaTrucks
//            } else {
//                listaTrucks.filter { it.placa.contains(query, ignoreCase = true) }
//            }
//            adapter.updateList(filtrados)
//        }
//
//        // Confirmar
//        btnConfirmar.setOnClickListener {
//            truckSelecionado?.let {
//                Toast.makeText(this, "Confirmado: ${it.placa}", Toast.LENGTH_SHORT).show()
//                // Exemplo: enviar para próxima Activity
//                val intent = Intent(this, SelectRouteActivity::class.java)
//                intent.putExtra("placa", it.placa)
//                intent.putExtra("descricao", it.descricao)
//                startActivity(intent)
//            } ?: run {
//                Toast.makeText(this, "Selecione um caminhão!", Toast.LENGTH_SHORT).show()
//            }
//        }
    }
}