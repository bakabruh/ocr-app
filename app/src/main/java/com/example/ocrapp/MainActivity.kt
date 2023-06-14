package com.example.ocrapp

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.example.ocrapp.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.Exception

class MainActivity : AppCompatActivity() {

    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    lateinit var _binding: ActivityMainBinding

    private val REQUEST_IMAGE_CAPTURE = 1 // Constante pour identifier la demande de capture d'image
    private val REQUEST_IMAGE_PICK = 2 // Constante pour identifier la demande de sélection d'image dans la galerie
    private var imageBitmap: Bitmap? = null

    // Array des permissions à demander à l'utilisateur
    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_MEDIA_IMAGES
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        _binding.apply {
            screenShotButton.setOnClickListener {
                // On donne à la fonction askMultiplePermissions la liste de permissions à demander au click du bouton
                askMultiplePermission.launch(permissions)

                textView.text = ""
            }

            detectTextImageButton.setOnClickListener {
                processImage()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    imageBitmap = data?.extras?.get("data") as Bitmap
                    if (imageBitmap != null) {
                        _binding.imageView.setImageBitmap(imageBitmap)
                    }
                }
                REQUEST_IMAGE_PICK -> {
                    val imageUri = data?.data
                    imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    if (imageBitmap != null) {
                        _binding.imageView.setImageBitmap(imageBitmap)
                    }
                }
            }
        }
    }

    // Fonction pour récupérer une photo prise
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
        catch (e:Exception) {
            println(e)
        }
    }

    // Fonction pour récupérer une image depuis la galerie
    private fun dispatchPickPictureIntent() {
        val pickPictureIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPictureIntent, REQUEST_IMAGE_PICK)
    }

    // Fonction qui permet d'afficher un choix de source à l'utilisateur
    // Puis lance la fonction selon la source choisie
    private fun openImagePicker() {
        val options = arrayOf<CharSequence>("Prendre une photo", "Choisir depuis la galerie", "Annuler")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Sélectionner une photo")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Prendre une photo" -> {
                    dispatchTakePictureIntent()
                }
                options[item] == "Choisir depuis la galerie" -> {
                    dispatchPickPictureIntent()
                }
                options[item] == "Annuler" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    // Fonction lisant l'image et récupérant le texte présent sur celle-ci
    private fun processImage() {
        if (imageBitmap != null) {
            val image = imageBitmap?.let {
                InputImage.fromBitmap(it, 0)
            }

            image?.let {
                recognizer.process(it)
                    .addOnSuccessListener {
                        _binding.textView.text = it.text
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Bruh nothing to show", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this, "Please select image first", Toast.LENGTH_SHORT).show()
        }

        try {

        } catch (e: Exception) {

        }
    }

    // Fonction demandant à l'utilisateur une liste de permissions à demander
    private val askMultiplePermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){ result ->
        var allGranted = true
        for (isGranted in result.values) {
            allGranted = allGranted && isGranted
        }

        if (allGranted) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}