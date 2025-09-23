package br.edu.utfpr.geocoleta.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.geocoleta.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnEntrar = findViewById<Button>(R.id.btnEntrar)
        val etCpf = findViewById<EditText>(R.id.etCpf)

        btnEntrar.setOnClickListener {
            val intent = Intent(this, SelectTruckActivity::class.java)
            intent.putExtra("cpf", etCpf.text.toString())
            startActivity(intent)
        }
    }

}