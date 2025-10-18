package br.edu.utfpr.geocoleta.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.edu.utfpr.geocoleta.Data.Repository.RouteRepository
import br.edu.utfpr.geocoleta.Data.Repository.TruckRepository
import br.edu.utfpr.geocoleta.Data.Repository.TruckerRepository
import br.edu.utfpr.geocoleta.R
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var  repositoryTrucker : TruckerRepository
    private lateinit var  repositoryTruck : TruckRepository
    private lateinit var  repositoryRoute : RouteRepository
    private lateinit var loadingLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadingLayout = findViewById(R.id.loadingLayout)
        repositoryTrucker = TruckerRepository(this)
        repositoryTruck = TruckRepository(this)
        repositoryRoute = RouteRepository(this)
        val btnEntrar = findViewById<Button>(R.id.btnEntrar)
        val etCpf = findViewById<EditText>(R.id.etCpf)

        lifecycleScope.launch {
            showLoading(true)
            try {
                repositoryTrucker.getTruckers()
                repositoryTruck.getTrucks()
                repositoryRoute.getRoutes()
            }catch (e : Exception){
                e.printStackTrace()
            }finally {
                showLoading(false)
            }

        }

        btnEntrar.setOnClickListener {
            val intent = Intent(this, SelectTruckActivity::class.java)
            intent.putExtra("cpf", etCpf.text.toString())
            startActivity(intent)
        }
    }
    private fun showLoading(show: Boolean) {
        loadingLayout.visibility = if (show) View.VISIBLE else View.GONE
    }


}