package br.edu.utfpr.geocoleta.Activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import br.edu.utfpr.geocoleta.R
import br.edu.utfpr.geocoleta.databinding.ActivityRegisterIncidentBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class RegisterIncidentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterIncidentBinding
    private lateinit var ivBack: ImageView
    private var latestTmpUri: Uri? = null

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            latestTmpUri?.let { uri ->
                handleImageSelection(uri)
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleImageSelection(it) }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Permissão de acesso à câmera negada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterIncidentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ivBack = findViewById(R.id.ivBack)

        savedInstanceState?.getParcelable<Uri>("latestTmpUri")?.let {
            latestTmpUri = it
        }

        setupListeners()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("latestTmpUri", latestTmpUri)
    }

    private fun setupListeners() {
        ivBack.setOnClickListener { finish() }
        binding.addPhotoContainer.setOnClickListener { showImageSourceDialog() }
        binding.removePhotoButton.setOnClickListener { resetImageSelection() }
        binding.saveIncidentButton.setOnClickListener { showSuccessAndFinish() }
        binding.cancelButton.setOnClickListener { finish() }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.saveIncidentButton.isEnabled = !isLoading
        binding.cancelButton.isEnabled = !isLoading
    }

    private fun showImageSourceDialog() {
        AlertDialog.Builder(this)
            .setTitle("Adicionar Foto")
            .setItems(arrayOf("Tirar Foto", "Escolher da Galeria")) { _, which ->
                when (which) {
                    0 -> checkAndOpenCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun checkAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        getTmpFileUri().let {
            latestTmpUri = it
            takePictureLauncher.launch(it)
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", tmpFile)
    }

    private fun handleImageSelection(uri: Uri) {
        latestTmpUri = uri
        binding.incidentImageView.setImageURI(uri)
        binding.incidentImageView.visibility = View.VISIBLE
        binding.removePhotoButton.visibility = View.VISIBLE
        binding.addPhotoContainer.visibility = View.GONE
    }

    private fun resetImageSelection() {
        latestTmpUri = null
        binding.incidentImageView.setImageURI(null)
        binding.incidentImageView.visibility = View.GONE
        binding.removePhotoButton.visibility = View.GONE
        binding.addPhotoContainer.visibility = View.VISIBLE
    }

    private fun showSuccessAndFinish() {
        showLoading(true)

        // TODO: Adicionar a lógica de envio para a API aqui
        // Simulando uma chamada de rede com um delay
        lifecycleScope.launch {
            delay(2000) // Simula 2 segundos de carregamento
            Toast.makeText(this@RegisterIncidentActivity, "Incidente registrado com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
