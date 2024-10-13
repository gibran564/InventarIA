package com.example.invetaria.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.invetaria.R
import com.example.invetaria.model.Product
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ReabastecerFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_PRODUCT = "product"

        fun newInstance(product: Product): ReabastecerFragment {
            val fragment = ReabastecerFragment()
            val args = Bundle()
            args.putParcelable(ARG_PRODUCT, product)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var product: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            product = it.getParcelable(ARG_PRODUCT)!!
        }
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reabastecer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editStock = view.findViewById<EditText>(R.id.editStock)
        val btnReabastecer = view.findViewById<Button>(R.id.btnReabastecer)

        btnReabastecer.setOnClickListener {
            val newStockStr = editStock.text.toString()
            val newStock = newStockStr.toIntOrNull()

            if (newStock != null && newStock > 0) {
                // Implementa la lógica para actualizar el stock en el backend
                // Por ejemplo, llamar a ApiService para actualizar el producto
                dismiss()
            } else {
                editStock.error = "Ingrese una cantidad válida"
            }
        }
    }
}
