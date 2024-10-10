package com.example.invetaria

import BarcodeAnalyzer
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.Manifest

class MainActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_PERMISSION_CODE = 1001
    }


    private lateinit var previewView: PreviewView
    private lateinit var fabMenu: FloatingActionButton

    private val cameraExecutor: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        fabMenu = findViewById(R.id.fabMenu)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            startCamera()
        }

        //setupFloatingMenu()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcodes ->
                        // Process the detected barcodes
                        for (barcode in barcodes) {
                            Log.d("Barcode", "Detected code: ${barcode.rawValue}")
                            Toast.makeText(this, "Detected code: ${barcode.rawValue}", Toast.LENGTH_SHORT).show()

                        }
                    })
                }


            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {

                cameraProvider.unbindAll()


                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )

            } catch (exc: Exception) {
                Log.e("CameraXApp", "Error starting the camera", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

   /* private fun setupFloatingMenu() {
        fabMenu.setOnClickListener { view ->
            // ... Código anterior
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_add -> {
                        // Navegar a una actividad o mostrar un diálogo para agregar un nuevo producto
                        showAddProductDialog()
                        true
                    }
                    R.id.action_restock -> {
                        // Navegar a una actividad o mostrar un diálogo para reabastecer un producto existente
                        showRestockProductDialog()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }*/

    private fun showAddProductDialog() {
        // Implementa el diálogo o actividad para agregar producto
    }

    private fun showRestockProductDialog() {
        // Implementa el diálogo o actividad para reabastecer producto
    }



    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                startCamera()
            } else {
                // Permiso denegado
                Toast.makeText(this, "Permiso de cámara es necesario", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
