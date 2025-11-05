package br.edu.utfpr.geocoleta.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.geocoleta.R

class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "GeoColetaPrefs"
    private val ACTIVATED_KEY = "is_activated"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        Handler(Looper.getMainLooper()).postDelayed({
            if (isActivated()) {
                navigateToMain()
            } else {
                navigateToSetup()
            }
        }, 2000) // 2 segundos
    }

    private fun isActivated(): Boolean {
        return sharedPreferences.getBoolean(ACTIVATED_KEY, false)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToSetup() {
        val intent = Intent(this, InitialSetupActivity::class.java)
        startActivity(intent)
        finish()
    }
}
