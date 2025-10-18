package br.edu.utfpr.geocoleta.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.geocoleta.Adapters.RouteAdapter
import br.edu.utfpr.geocoleta.Data.Models.Route
import br.edu.utfpr.geocoleta.R

class SelectRouteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_route)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRotas)
        recyclerView.layoutManager = LinearLayoutManager(this)

//        val listaRotas = listOf(
//            Route("ROTA A", "Fraron, Centro, Pinheiros"),
//            Route("ROTA B", "Alvorada, Menino Deus"),
//            Route("ROTA C", "Brasilia, Planalto"),
//            Route("ROTA D", "Morumbi, São Cristovão")
//        )

//        val adapter = RouteAdapter(listaRotas) { rotaSelecionada ->
//            Toast.makeText(this, "Selecionou: ${rotaSelecionada.titulo}", Toast.LENGTH_SHORT).show()
//        }

//        recyclerView.adapter = adapter

        val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)
        btnConfirmar.setOnClickListener {
            val intent = Intent(this, InitActivity::class.java)
            startActivity(intent)
        }
    }
}