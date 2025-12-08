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
import br.edu.utfpr.geocoleta.Data.Models.Incident
import br.edu.utfpr.geocoleta.Data.Network.RetrovitClient
import br.edu.utfpr.geocoleta.R
import br.edu.utfpr.geocoleta.databinding.ActivityRegisterIncidentBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class RegisterIncidentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterIncidentBinding
    private lateinit var ivBack: ImageView
    private var latestTmpUri: Uri? = null
    private var trajetoId : Int = 0
    private var lat_atual : Double = 0.0
    private var lng_atual : Double = 0.0

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissão de acesso à câmera negada",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun checkAndRequestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                AlertDialog.Builder(this)
                    .setTitle("Permissão necessária")
                    .setMessage("Para tirar fotos do incidente, o app precisa acessar a câmera.")
                    .setPositiveButton("Permitir") { _, _ ->
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // ---------------------------------------------------------

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterIncidentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ivBack = findViewById(R.id.ivBack)

        savedInstanceState?.getParcelable<Uri>("latestTmpUri")?.let {
            latestTmpUri = it
        }

        trajetoId = intent?.getIntExtra("TRAJETO_ID", 0) ?: 0
        lat_atual = intent?.getDoubleExtra("LAT_ATUAL", 0.0) ?: 0.0
        lng_atual = intent?.getDoubleExtra("LNG_ATUAL", 0.0) ?: 0.0

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
                    0 -> checkAndRequestCameraPermission() // <--- AGORA AQUI!
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
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

        return FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            tmpFile
        )
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
        if (latestTmpUri == null) {
            Toast.makeText(this, "Selecione uma foto antes de enviar!", Toast.LENGTH_SHORT).show()
            return
        }

        val file = uriToFile(latestTmpUri!!)
        if (file == null) {
            Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            try {
                val api = RetrovitClient.api

                val incidenteJson = createIncidentJson()
                val fotoPart = createPhotoPart(file)

                api.criarIncidenteComFoto(incidenteJson, fotoPart)

                Toast.makeText(
                    this@RegisterIncidentActivity,
                    "Incidente registrado!",
                    Toast.LENGTH_SHORT
                ).show()

                finish()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@RegisterIncidentActivity,
                    "Erro ao enviar: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                showLoading(false)
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("incident_photo", ".jpg", cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        return tempFile
    }

    private fun createIncidentJson(): RequestBody {
        val incidente = Incident(
            trajetoId = trajetoId,
            nome = binding.incidentNameEditText.text.toString(),
            observacoes = binding.incidentDetailsEditText.text.toString(),
            lat = lat_atual,
            lng = lng_atual
        )

        val json = Gson().toJson(incidente)
        return json.toRequestBody("application/json".toMediaType())
    }

    private fun createPhotoPart(file: File): MultipartBody.Part {
        val requestFile = file.asRequestBody("image/jpeg".toMediaType())
        return MultipartBody.Part.createFormData(
            "foto",
            file.name,
            requestFile
        )
    }
}
