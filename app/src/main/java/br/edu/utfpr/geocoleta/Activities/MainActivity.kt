package br.edu.utfpr.geocoleta.Activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import br.edu.utfpr.geocoleta.Data.Models.Trucker
import br.edu.utfpr.geocoleta.Data.Repository.RouteRepository
import br.edu.utfpr.geocoleta.Data.Repository.TruckRepository
import br.edu.utfpr.geocoleta.Data.Repository.TruckerRepository
import br.edu.utfpr.geocoleta.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {
    private lateinit var repositoryTrucker: TruckerRepository
    private lateinit var repositoryTruck: TruckRepository
    private lateinit var repositoryRoute: RouteRepository
    private lateinit var loadingLayout: FrameLayout

    private lateinit var etCpf: EditText
    private lateinit var tvErroCpf: TextView
    private lateinit var tvCpfLabel: TextView
    private lateinit var btnEntrar: MaterialButton
    private lateinit var cbLembrarCpf: MaterialCheckBox

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "GeoColetaPrefs"
    private val CPF_KEY = "cpf_key"
    private val REMEMBER_CPF_KEY = "remember_cpf_key"
    private val FIRST_RUN_KEY = "first_run"

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
        setupListeners()
        loadSavedCpf()
        setupCpfMask()
        setupNetworkCallback()

        val isFirstRun = sharedPreferences.getBoolean(FIRST_RUN_KEY, true)
        if (isFirstRun) {
            syncData()
            sharedPreferences.edit().putBoolean(FIRST_RUN_KEY, false).apply()
        }
    }

    override fun onResume() {
        super.onResume()
        val networkRequest = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onPause() {
        super.onPause()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun setupViews() {
        loadingLayout = findViewById(R.id.loadingLayout)
        etCpf = findViewById(R.id.etCpf)
        tvErroCpf = findViewById(R.id.tvErroCpf)
        tvCpfLabel = findViewById(R.id.tvCpfLabel)
        btnEntrar = findViewById(R.id.btnEntrar)
        cbLembrarCpf = findViewById(R.id.cbLembrarCpf)

        repositoryTrucker = TruckerRepository(this)
        repositoryTruck = TruckRepository(this)
        repositoryRoute = RouteRepository(this)
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private fun syncData() {
        lifecycleScope.launch {
            showLoading(true)
            try {
                repositoryTrucker.getTruckers()
                repositoryTruck.getTrucks()
                repositoryRoute.getRoutes()
            } catch (e: UnknownHostException) {
                Toast.makeText(this@MainActivity, "Falha na conexão. Usando dados locais.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("MainActivity", "An unexpected error occurred", e)
                Toast.makeText(this@MainActivity, "Ocorreu um erro inesperado. Tente novamente mais tarde.", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun setupListeners() {
        btnEntrar.setOnClickListener {
            validateAndConfirm()
        }

        etCpf.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                resetCpfErrorState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validateAndConfirm() {
        val cpfDigitado = etCpf.text.toString().trim()
        val cpfNumeros = cpfDigitado.replace("[^\\d]".toRegex(), "")

        if (cpfDigitado.isEmpty()) {
            showCpfError("O campo CPF não pode estar vazio.")
            return
        }
        if (cpfNumeros.length != 11) {
            showCpfError("O CPF deve conter 11 dígitos.")
            return
        }
        if (!isCpfValido(cpfNumeros)) {
            showCpfError("Digite um CPF válido.")
            return
        }

        val trucker = repositoryTrucker.findByCpf(cpfNumeros)
        if (trucker != null) {
            showConfirmationDialog(trucker)
        } else {
            showCpfError("Motorista não encontrado.")
        }
    }

    private fun showConfirmationDialog(trucker: Trucker) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_identity, null)
        val tvTruckerName = dialogView.findViewById<TextView>(R.id.tvTruckerName)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

        tvTruckerName.text = trucker.nome

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            saveCpfPreference()

            val sharedCpf = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            sharedCpf.edit()
                .putString("cpf_usuario", etCpf.text.toString())
                .apply()

            val intent = Intent(this, SelectTruckActivity::class.java)
            intent.putExtra("cpf", etCpf.text.toString())
            startActivity(intent)
        }

        dialog.show()
    }

    private fun showCpfError(message: String) {
        tvErroCpf.text = message
        tvErroCpf.visibility = View.VISIBLE
        etCpf.setBackgroundResource(R.drawable.input_error_background)
        tvCpfLabel.setTextColor(ContextCompat.getColor(this, R.color.destructive))
    }

    private fun resetCpfErrorState() {
        tvErroCpf.visibility = View.GONE
        etCpf.setBackgroundResource(R.drawable.input_background)
        tvCpfLabel.setTextColor(ContextCompat.getColor(this, R.color.foreground))
    }

    private fun isCpfValido(cpf: String): Boolean {
        if (cpf.all { it == cpf[0] }) return false

        try {
            val dv1 = calcularDv(cpf.substring(0, 9), 10)
            val dv2 = calcularDv(cpf.substring(0, 10), 11)
            return cpf[9].toString().toInt() == dv1 && cpf[10].toString().toInt() == dv2
        } catch (e: Exception) {
            return false
        }
    }

    private fun calcularDv(base: String, pesoInicial: Int): Int {
        var soma = 0
        var peso = pesoInicial
        for (i in base.indices) {
            soma += base[i].toString().toInt() * peso--
        }
        val resto = soma % 11
        return if (resto < 2) 0 else 11 - resto
    }

    private fun setupCpfMask() {
        etCpf.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "###.###.###-##"

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val str = s.toString().replace("[^\\d]".toRegex(), "")
                var cpf = ""

                if (isUpdating) {
                    isUpdating = false
                    return
                }

                var i = 0
                for (m in mask.toCharArray()) {
                    if (m != '#' && str.length > i) {
                        cpf += m
                        continue
                    }
                    try {
                        cpf += str[i]
                    } catch (e: Exception) {
                        break
                    }
                    i++
                }

                isUpdating = true
                etCpf.setText(cpf)
                etCpf.setSelection(cpf.length)
                resetCpfErrorState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun saveCpfPreference() {
        val editor = sharedPreferences.edit()
        if (cbLembrarCpf.isChecked) {
            editor.putString(CPF_KEY, etCpf.text.toString())
            editor.putBoolean(REMEMBER_CPF_KEY, true)
        } else {
            editor.remove(CPF_KEY)
            editor.remove(REMEMBER_CPF_KEY)
        }
        editor.apply()
    }

    private fun loadSavedCpf() {
        val remember = sharedPreferences.getBoolean(REMEMBER_CPF_KEY, false)
        cbLembrarCpf.isChecked = remember
        if (remember) {
            etCpf.setText(sharedPreferences.getString(CPF_KEY, ""))
        }
    }

    private fun showLoading(show: Boolean) {
        loadingLayout.visibility = if (show) View.VISIBLE else View.GONE
        btnEntrar.isEnabled = !show
    }

    private fun setupNetworkCallback() {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Conexão reestabelecida. Sincronizando dados.", Toast.LENGTH_SHORT).show()
                    lifecycleScope.launch {
                        repositoryRoute.syncRoutes()
                    }
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Sem conexão. Seus dados serão enviados quando a internet voltar.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
