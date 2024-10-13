package com.example.invetaria.ui

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.invetaria.R
import com.example.invetaria.model.DbProduct
import com.example.invetaria.service.ApiService
import com.android.volley.toolbox.Volley
import com.example.invetaria.model.Product
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.*

class CreateProductFragment : BottomSheetDialogFragment() {

    private lateinit var apiService: ApiService
    private lateinit var token: String

    // UI Elements
    private lateinit var nombreEditText: EditText
    private lateinit var descripcionEditText: EditText
    private lateinit var categoriaEditText: EditText
    private lateinit var precioEditText: EditText
    private lateinit var stockEditText: EditText
    private lateinit var fechaReabastecimientoEditText: EditText
    private lateinit var cantidadReabastecerEditText: EditText
    private lateinit var edicionLimitadaCheckBox: CheckBox
    private lateinit var saveButton: Button

    private var product: Product? = null
    private var barcode: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiService = ApiService(Volley.newRequestQueue(requireContext()))
        token = "BE9EDB186E6E9C21153123C912368BE3" // Reemplaza con tu método de obtención de token
        product = arguments?.getParcelable("product") // Obtener el producto de los argumentos si existe
        barcode = arguments?.getString("barcode")
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("CreateProductFragment", "onCreateView llamado")
        return inflater.inflate(R.layout.fragment_create_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("CreateProductFragment", "onViewCreated llamado")

        // Inicializar elementos de la UI
        nombreEditText = view.findViewById(R.id.editTextNombre)
        descripcionEditText = view.findViewById(R.id.editTextDescripcion)
        categoriaEditText = view.findViewById(R.id.editTextCategoria)
        precioEditText = view.findViewById(R.id.editTextPrecio)
        stockEditText = view.findViewById(R.id.editTextStock)
        fechaReabastecimientoEditText = view.findViewById(R.id.editTextFechaReabastecimiento)
        cantidadReabastecerEditText = view.findViewById(R.id.editTextCantidadReabastecer)
        edicionLimitadaCheckBox = view.findViewById(R.id.checkBoxEdicionLimitada)
        saveButton = view.findViewById(R.id.buttonSave)

        // Si hay un producto en los argumentos, rellenar los campos
        product?.let {
            nombreEditText.setText(it.title)
            categoriaEditText.setText(it.category)
        }

        saveButton.setOnClickListener {
            Log.d("CreateProductFragment", "Botón de guardar presionado")
            if (validateInputs()) {
                val newProduct = getProductFromInput()
                createProduct(newProduct)
            } else {
                Log.d("CreateProductFragment", "Validación fallida")
            }
        }
    }

    private fun getProductFromInput(): DbProduct {
        val nombre = nombreEditText.text.toString()
        val barcodes = barcode.toString()
        val descripcion = descripcionEditText.text.toString()
        val categoria = categoriaEditText.text.toString()
        val precio = precioEditText.text.toString().toFloatOrNull() ?: 0f
        val stock = stockEditText.text.toString().toIntOrNull() ?: 0

        return DbProduct(
            nombre = nombre,
            barcode = barcodes,
            descripcion = descripcion,
            categoria = categoria,
            precio = precio,
            stock = stock,
        )
    }

    private fun validateInputs(): Boolean {
        if (TextUtils.isEmpty(nombreEditText.text)) {
            nombreEditText.error = "El nombre es requerido"
            return false
        }
        if (TextUtils.isEmpty(categoriaEditText.text)) {
            categoriaEditText.error = "La categoría es requerida"
            return false
        }
        return true
    }

    private fun createProduct(product: DbProduct) {
        apiService.createProduct(
            product = product,
            token = token,
            onSuccess = {
                Toast.makeText(requireContext(), "Producto creado exitosamente", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            },
            onError = { error ->
                Toast.makeText(requireContext(), "Error al crear el producto: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    companion object {
        @JvmStatic
        fun newInstance(product: Product? = null, barcode: String? = null): CreateProductFragment {
            return CreateProductFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("product", product)
                    putString("barcode", barcode)
                }
            }
        }
    }
}
