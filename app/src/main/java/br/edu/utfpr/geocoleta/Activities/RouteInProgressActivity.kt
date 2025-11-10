package br.edu.utfpr.geocoleta.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.geocoleta.Data.Repository.TrajetoRepository
import br.edu.utfpr.geocoleta.R
import br.edu.utfpr.geocoleta.Service.LocationService
import br.edu.utfpr.geocoleta.databinding.ActivityRouteInProgressBinding

class RouteInProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRouteInProgressBinding
    private lateinit var ivBack: ImageView
    private var trajetoId : Int = 0
    private lateinit var trajetoRepository: TrajetoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteInProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        trajetoId = intent?.getIntExtra("TRAJETO_ID", 0) ?: 0
        trajetoRepository = TrajetoRepository(this)

        ivBack = findViewById(R.id.ivBack)

        ivBack.setOnClickListener {
            finish()
        }

        binding.registerIncidentButton.setOnClickListener {
            val intent = Intent(this, RegisterIncidentActivity::class.java)
            startActivity(intent)
        }

        binding.finishRouteButton.setOnClickListener {
            finalizarTrajetoLocalmente()
        }
    }

    private fun finalizarTrajetoLocalmente() {
        trajetoRepository.finishTrajeto(trajetoId)
        Toast.makeText(this, "Rota finalizada. Os dados serão sincronizados em breve.", Toast.LENGTH_LONG).show()
        stopService(Intent(this, LocationService::class.java))
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}