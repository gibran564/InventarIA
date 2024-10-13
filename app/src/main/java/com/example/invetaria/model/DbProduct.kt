package com.example.invetaria.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class DbProduct(
    val id: Int? = null, // ID auto-incremental
    val barcode: String,
    val nombre: String,
    val descripcion: String? = null,
    val categoria: String,
    val precio: Float,
    val stock: Int,
    val fechaIngreso: Date = Date(),
    val fechaUltimaVenta: Date? = null,
    val cicloVida: String? = null,
    val fechaReabastecimiento: Date? = null,
    val cantidadReabastecer: Int? = null,
    val edicionLimitada: Boolean = false
) : Parcelable