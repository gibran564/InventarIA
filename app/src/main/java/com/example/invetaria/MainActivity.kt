package com.example.invetaria

import BarcodeAnalyzer
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.toolbox.Volley
import com.example.invetaria.model.Product
import com.example.invetaria.service.ApiService
import com.example.invetaria.ui.CreateProductFragment
import com.example.invetaria.ui.ReabastecerFragment
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.navigation.NavigationBarView
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET
        )
        private const val TOKEN = "BE9EDB186E6E9C21153123C912368BE3"
    }

    private lateinit var previewView: PreviewView
    private lateinit var bottomAppBar: BottomAppBar
    private val apiService by lazy {
        ApiService(Volley.newRequestQueue(this))
    }

    private val cameraExecutor: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    private val detectedBarcodes = mutableSetOf<String>()
    private val debounceTime = 2000L // 2 segundos de debounce
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        bottomAppBar = findViewById(R.id.bottomAppBar)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                PERMISSION_REQUEST_CODE
            )
        }

        setupBottomAppBar()
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                this, it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun navigateToReabastecerFragment(product: Product) {
        val fragment = ReabastecerFragment.newInstance(product)
        fragment.show(supportFragmentManager, ReabastecerFragment::class.java.simpleName)
    }

    private fun navigateToCreateProductFragment(barcode: String? = null, product: Product? = null) {
        val fragment = CreateProductFragment.newInstance(product = product, barcode = barcode)
        fragment.show(supportFragmentManager, CreateProductFragment::class.java.simpleName)
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
                        processBarcodes(barcodes)
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("MainActivity", "Error iniciando la cámara: ${exc.localizedMessage}")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun processBarcodes(barcodes: List<Barcode>) {
        for (barcode in barcodes) {
            val codeValue = barcode.rawValue
            if (!codeValue.isNullOrEmpty() && detectedBarcodes.add(codeValue)) {
                Log.d("Barcode", "Código detectado: $codeValue")
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Código detectado: $codeValue",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                onBarcodeDetected(codeValue)

                // Remover el código de barras después del tiempo de debounce
                handler.postDelayed({
                    detectedBarcodes.remove(codeValue)
                }, debounceTime)
            }
        }
    }

    private fun onBarcodeDetected(barcode: String) {
        // Primero, verificar si el producto ya existe en la base de datos interna
        apiService.checkProductExists(
            barcode = barcode,
            onSuccess = { product ->
                // Producto encontrado en la base de datos interna, navegar a reabastecimiento
                navigateToReabastecerFragment(product)
            },
            onNotFound = {
                // Si no se encuentra en la base de datos, entonces consultar la API externa
                apiService.fetchProductDetails(
                    barcode = barcode,
                    onSuccess = { product ->
                        // Producto encontrado en la API, pero no está en la base de datos local
                        navigateToCreateProductFragment(barcode = barcode, product = product) // Aquí pasa el objeto 'product'
                    },
                    onError = { exception ->
                        if (exception.message?.contains("404") == true) {
                            // Producto no encontrado ni en la API, abrir el fragmento para crear uno nuevo
                            navigateToCreateProductFragment(barcode = barcode)
                        } else {
                            Toast.makeText(
                                this,
                                "Error al obtener el producto: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("maldito error", exception.toString())
                        }
                    }
                )
            },
            onError = { exception ->
                Toast.makeText(
                    this,
                    "Error al verificar el producto: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun setupBottomAppBar() {
        bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_scan -> {
                    // Accionar el escáner
                    Toast.makeText(this, "Iniciando Escáner", Toast.LENGTH_SHORT).show()
                    // Aquí podrías iniciar una nueva actividad o fragmento si deseas un flujo diferente
                    true
                }
                R.id.action_alerts -> {
                    Toast.makeText(this, "Accediendo a Mis Alertas", Toast.LENGTH_SHORT).show()
                    // Navegar a una actividad o fragmento relacionado con alertas
                    true
                }
                R.id.action_search -> {
                    Toast.makeText(this, "Buscando productos", Toast.LENGTH_SHORT).show()
                    // Navegar a una búsqueda de productos
                    true
                }
                R.id.action_all_products -> {
                    Toast.makeText(this, "Accediendo a Todos los productos", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_low_stock -> {
                    Toast.makeText(this, "Mostrando productos con existencias bajas", Toast.LENGTH_SHORT).show()
                    // Implementar lógica para mostrar productos con existencias bajas
                    true
                }
                R.id.action_temp_products -> {
                    Toast.makeText(this, "Accediendo a productos temporales", Toast.LENGTH_SHORT).show()
                    // Implementar acción para acceder a productos temporales
                    true
                }
                else -> false
            }
        }

        // Opcional: Manejar el botón FAB si lo tienes
        /*
        val fab: FloatingActionButton = findViewById(R.id.fabMenu)
        fab.setOnClickListener {
            // Acciones del FAB
        }
        */
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Se necesitan permisos de cámara e internet para funcionar.",
                    Toast.LENGTH_LONG
                ).show()

                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
        }
    }
}
