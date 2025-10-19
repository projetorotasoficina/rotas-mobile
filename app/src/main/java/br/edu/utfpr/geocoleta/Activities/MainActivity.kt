package br.edu.utfpr.geocoleta.Activities

import android.content.Intent
import android.os.Bundle
import android.view.View // Importe para usar View.VISIBLE e View.GONE
import android.widget.EditText
import android.widget.TextView // Importe para referenciar o TextView de erro
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.geocoleta.R
import com.google.android.material.button.MaterialButton // Use MaterialButton se estiver usando no XML

class MainActivity : AppCompatActivity() {

    // 1. Declare as Views fora do onCreate para usá-las em funções diferentes
    private lateinit var etCpf: EditText
    private lateinit var tvErroCpf: TextView
    private lateinit var btnEntrar: MaterialButton // Use MaterialButton para corresponder ao XML

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 2. Vincule as Views (Busque o ID do TextView de erro que adicionamos)
        etCpf = findViewById(R.id.etCpf)
        // Certifique-se que o ID 'tvErroCpf' está no seu activity_main.xml
        tvErroCpf = findViewById(R.id.tvErroCpf)
        btnEntrar = findViewById(R.id.btnEntrar)

        // 3. Configuração do Listener
        btnEntrar.setOnClickListener {
            // Se a validação do CPF for bem-sucedida, avance!
            if (validarEProsseguir()) {
                val intent = Intent(this, SelectTruckActivity::class.java)
                // Remove caracteres não numéricos antes de enviar o CPF
                val cpfLimpo = etCpf.text.toString().replace("[^\\d]".toRegex(), "")
                intent.putExtra("cpf", cpfLimpo)
                startActivity(intent)
            }
        }
    }

    /**
     * Função que realiza a validação e exibe o erro se necessário.
     * @return true se o CPF for válido, false caso contrário.
     */
    private fun validarEProsseguir(): Boolean {
        val cpfDigitado = etCpf.text.toString().trim()
        val cpfNumeros = cpfDigitado.replace("[^\\d]".toRegex(), "") // Limpa a string

        // A. Validação de Campo Vazio e Tamanho Básico
        if (cpfDigitado.isEmpty()) {
            tvErroCpf.text = "O campo CPF não pode estar vazio."
            tvErroCpf.visibility = View.VISIBLE
            return false
        }
        if (cpfNumeros.length != 11) {
            tvErroCpf.text = "O CPF deve ter 11 dígitos."
            tvErroCpf.visibility = View.VISIBLE
            return false
        }

        // B. Validação Matemática Real
        if (!isCpfValido(cpfNumeros)) {
            tvErroCpf.text = "Digite um CPF válido."
            tvErroCpf.visibility = View.VISIBLE
            return false
        }

        // Se chegou aqui, é válido: oculta o erro e retorna true.
        tvErroCpf.visibility = View.GONE
        return true
    }

    /**
     * Algoritmo de Validação de CPF (Complexo)
     * Verifica os dígitos verificadores (DVs) do CPF.
     */
    private fun isCpfValido(cpf: String): Boolean {
        // Bloqueia CPFs com todos os dígitos iguais (ex: 111.111.111-11)
        if (cpf.all { it == cpf[0] }) return false

        try {
            // Conversão dos 9 primeiros dígitos para cálculo do 1º DV
            val dv1Calculado = calcularDv(cpf.substring(0, 9), 10)

            // Conversão dos 10 primeiros dígitos (incluindo o 1º DV) para cálculo do 2º DV
            val dv2Calculado = calcularDv(cpf.substring(0, 10), 11)

            // Compara os DVs calculados com os DVs do CPF original
            return cpf[9].toString().toInt() == dv1Calculado && cpf[10].toString().toInt() == dv2Calculado
        } catch (e: Exception) {
            // Em caso de erro na conversão ou cálculo
            return false
        }
    }

    /**
     * Função auxiliar para calcular o dígito verificador.
     */
    private fun calcularDv(base: String, pesoInicial: Int): Int {
        var soma = 0
        var peso = pesoInicial

        for (i in base.indices) {
            soma += base[i].toString().toInt() * peso
            peso--
        }

        val resto = soma % 11
        // Regra de obtenção do dígito: se o resto for 0 ou 1, o DV é 0. Caso contrário, DV é 11 - resto.
        return if (resto < 2) 0 else 11 - resto
    }
}