package br.edu.utfpr.geocoleta.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
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
import br.edu.utfpr.geocoleta.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repositoryTrucker: TruckerRepository
    private lateinit var repositoryTruck: TruckRepository
    private lateinit var repositoryRoute: RouteRepository

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "GeoColetaPrefs"
    private val CPF_KEY = "cpf_key"
    private val REMEMBER_CPF_KEY = "remember_cpf_key"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupListeners()
        loadSavedCpf()
        setupCpfMask()
        syncData()
    }

    private fun setupViews() {
        repositoryTrucker = TruckerRepository(this)
        repositoryTruck = TruckRepository(this)
        repositoryRoute = RouteRepository(this)
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun syncData() {
        lifecycleScope.launch {
            showLoading(true)
            binding.etCpf.isEnabled = false
            binding.btnEntrar.isEnabled = false
            try {
                repositoryTrucker.getTruckers()
                repositoryTruck.getTrucks()
                repositoryRoute.getRoutes()
            } catch (e: UnknownHostException) {
                Toast.makeText(this@MainActivity, "Falha na conexão. Verifique sua internet.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Ocorreu um erro inesperado.", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            } finally {
                showLoading(false)
                binding.etCpf.isEnabled = true
                binding.btnEntrar.isEnabled = true
            }
        }
    }

    private fun setupListeners() {
        binding.btnEntrar.setOnClickListener {
            validarEProsseguir()
        }

        binding.etCpf.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                resetCpfErrorState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validarEProsseguir() {
        val cpfDigitado = binding.etCpf.text.toString().trim()
        val cpfNumeros = cpfDigitado.replace("[^\\d]".toRegex(), "")

        if (cpfDigitado.isEmpty()) {
            showCpfError("O campo CPF não pode estar vazio.")
            return
        }
        if (!isCpfValido(cpfNumeros)) {
            showCpfError("Digite um CPF válido.")
            return
        }

        lifecycleScope.launch {
            showLoading(true)
            val motorista = repositoryTrucker.findByCpf(cpfNumeros)
            showLoading(false)

            if (motorista != null) {
                if (motorista.ativo) {
                    showConfirmationDialog(motorista)
                } else {
                    showCpfError("Este usuário está inativo e não pode acessar o sistema.")
                }
            } else {
                showCpfError("Motorista não encontrado.")
            }
        }
    }

    private fun showConfirmationDialog(motorista: Trucker) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_identity, null)

        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialog_message)
        val btnPositive = dialogView.findViewById<MaterialButton>(R.id.btn_positive)
        val btnNegative = dialogView.findViewById<MaterialButton>(R.id.btn_negative)

        dialogMessage.text = "Você é ${motorista.nome}?"

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnPositive.setOnClickListener {
            saveCpfPreference(binding.etCpf.text.toString().trim())
            val intent = Intent(this, SelectTruckActivity::class.java).apply {
                putExtra("MOTORISTA_ID", motorista.id)
            }
            startActivity(intent)
            dialog.dismiss()
        }

        btnNegative.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showCpfError(message: String) {
        binding.tvErroCpf.text = message
        binding.tvErroCpf.visibility = View.VISIBLE
        binding.etCpf.setBackgroundResource(R.drawable.input_error_background)
        binding.tvCpfLabel.setTextColor(ContextCompat.getColor(this, R.color.destructive))
    }

    private fun resetCpfErrorState() {
        binding.tvErroCpf.visibility = View.GONE
        binding.etCpf.setBackgroundResource(R.drawable.input_background)
        binding.tvCpfLabel.setTextColor(ContextCompat.getColor(this, R.color.foreground))
    }

    private fun isCpfValido(cpf: String): Boolean {
        if (cpf.length != 11 || cpf.all { it == cpf[0] }) return false

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
        binding.etCpf.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "###.###.###-##"

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) {
                    isUpdating = false
                    return
                }

                val str = s.toString().replace("[^\\d]".toRegex(), "")
                var cpf = ""
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
                binding.etCpf.setText(cpf)
                binding.etCpf.setSelection(cpf.length)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun saveCpfPreference(cpf: String) {
        val editor = sharedPreferences.edit()
        if (binding.cbLembrarCpf.isChecked) {
            editor.putString(CPF_KEY, cpf)
            editor.putBoolean(REMEMBER_CPF_KEY, true)
        } else {
            editor.remove(CPF_KEY)
            editor.remove(REMEMBER_CPF_KEY)
        }
        editor.apply()

        val userSessionPrefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        userSessionPrefs.edit().putString("cpf_usuario", cpf).apply()
    }

    private fun loadSavedCpf() {
        val remember = sharedPreferences.getBoolean(REMEMBER_CPF_KEY, false)
        binding.cbLembrarCpf.isChecked = remember
        if (remember) {
            binding.etCpf.setText(sharedPreferences.getString(CPF_KEY, ""))
        }
    }

    private fun showLoading(show: Boolean) {
        binding.loadingLayout.visibility = if (show) View.VISIBLE else View.GONE
    }
}