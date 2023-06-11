package com.example.ocrapp

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.ocrapp.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    lateinit var _binding: ActivityMainBinding

    private val REQUEST_IMAGE_CAPTURE = 1
    private var imageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        _binding.apply {

            screenShotButton.setOnClickListener {

                takeImage()

                textView.text = ""

            }

            detectTextImageButton.setOnClickListener {

                processImage()

            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val extras:Bundle? = data?.extras

            imageBitmap = extras?.get("data") as Bitmap

            if (imageBitmap != null) {
                _binding.imageView.setImageBitmap(imageBitmap)
            }
        }
    }

    private fun takeImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        try {

            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)


        } catch (e:Exception) {

            println(e)

        }
    }

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

}