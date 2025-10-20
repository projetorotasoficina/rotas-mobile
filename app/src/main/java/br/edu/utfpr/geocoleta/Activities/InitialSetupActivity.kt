package br.edu.utfpr.geocoleta.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.edu.utfpr.geocoleta.Data.Models.ActivationRequest
import br.edu.utfpr.geocoleta.Data.Network.ApiService
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient
import br.edu.utfpr.geocoleta.databinding.ActivityInitialSetupBinding
import kotlinx.coroutines.launch

class InitialSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInitialSetupBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "GeoColetaPrefs"
    private val TOKEN_KEY = "auth_token"
    private val ACTIVATED_KEY = "is_activated"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInitialSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        RetrovitClient.initialize(this)
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        binding.btnAvancar.setOnClickListener {
            val token = binding.etToken.text.toString().trim()
            if (token.isNotEmpty()) {
                activateDevice(token)
            } else {
                showError("O token não pode estar vazio")
            }
        }
    }

    private fun activateDevice(token: String) {
        showLoading(true)
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val request = ActivationRequest(token, deviceId)

        lifecycleScope.launch {
            try {
                val response = RetrovitClient.api.activate(request)
                saveActivation(response.appToken)
                navigateToMain()
            } catch (e: Exception) {
                showError("Token de ativação inválido")
                showLoading(false)
            }
        }
    }

    private fun saveActivation(token: String) {
        sharedPreferences.edit().apply {
            putString(TOKEN_KEY, token)
            putBoolean(ACTIVATED_KEY, true)
            apply()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        binding.tvTokenError.text = message
        binding.tvTokenError.visibility = View.VISIBLE
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}