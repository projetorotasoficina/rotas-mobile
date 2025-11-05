package br.edu.utfpr.geocoleta.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.geocoleta.R
import br.edu.utfpr.geocoleta.databinding.ActivityRouteInProgressBinding

class RouteInProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRouteInProgressBinding
    private lateinit var ivBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteInProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ivBack = findViewById(R.id.ivBack)

        ivBack.setOnClickListener {
            finish()
        }

        binding.registerIncidentButton.setOnClickListener {
            val intent = Intent(this, RegisterIncidentActivity::class.java)
            startActivity(intent)
        }

        binding.finishRouteButton.setOnClickListener {
            Toast.makeText(this, "Rota Finalizada com sucesso!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}