package com.example.invetaria.service

import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.RequestQueue
import com.example.invetaria.model.DbProduct
import com.example.invetaria.model.Product
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApiService(private val requestQueue: RequestQueue) {

    private val apiUrl = "https://api.upcdatabase.org/product/"
    private val apiKey = "BE9EDB186E6E9C21153123C912368BE3"
    private val baseUrl = "http://10.126.5.121:5000"

    fun checkProductExists(
        barcode: String,
        onSuccess: (Product) -> Unit,
        onNotFound: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$baseUrl/products/search_by_barcode?barcode=$barcode"

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val productJson = response.getJSONObject("product")
                    val mapper = jacksonObjectMapper().registerKotlinModule()
                    val product: Product = mapper.readValue(productJson.toString())
                    onSuccess(product) // Producto encontrado en la base de datos
                } catch (e: Exception) {
                    e.printStackTrace()
                    onError(e)
                }
            },
            { error ->
                if (error.networkResponse?.statusCode == 404) {
                    onNotFound() // Producto no encontrado en la base de datos
                } else {
                    error.printStackTrace()
                    onError(Exception(error.message))
                }
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        requestQueue.add(jsonObjectRequest)
    }


    fun fetchProductDetails(
        barcode: String,
        onSuccess: (Product) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val url = "$apiUrl$barcode"

        val stringRequest = object : StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    println("Respuesta completa: $response")
                    val jsonStartIndex = response.indexOf("{")
                    if (jsonStartIndex == -1) {
                        throw Exception("Respuesta no contiene JSON válido.")
                    }
                    val jsonString = response.substring(jsonStartIndex)

                    println("JSON extraído: $jsonString")

                    // Deserializar el JSON
                    val mapper = jacksonObjectMapper().registerKotlinModule()
                    val product: Product = mapper.readValue(jsonString)

                    onSuccess(product) // Producto encontrado en la API
                } catch (e: Exception) {
                    e.printStackTrace()
                    onError(e)
                    println("el error es {$e}")
                }
            },
            { error ->
                error.printStackTrace()
                onError(Exception(error.message))
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $apiKey"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        requestQueue.add(stringRequest)
    }


    fun createProduct(
        product: DbProduct,
        token: String,
        onSuccess: (Any?) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$baseUrl/products"
        println("Creando producto en URL: $url")

        val jsonBody = JSONObject().apply {
            put("nombre", product.nombre)
            if (product.descripcion != null) put("descripcion", product.descripcion)
            put("categoria", product.categoria)
            put("precio", product.precio)
            put("stock", product.stock)
            put("fecha_ingreso", formatDate(product.fechaIngreso))
            if (product.fechaUltimaVenta != null) put("fecha_ultima_venta", formatDate(product.fechaUltimaVenta))
            if (product.cicloVida != null) put("ciclo_vida", product.cicloVida)
            if (product.fechaReabastecimiento != null) put("fecha_reabastecimiento", formatDate(product.fechaReabastecimiento))
            if (product.cantidadReabastecer != null) put("cantidad_reabastecer", product.cantidadReabastecer)
            put("edicion_limitada", product.edicionLimitada)
            put("barcode", product.barcode)
        }


        println("Cuerpo JSON de la solicitud: $jsonBody")

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                println("Respuesta de la API: $response")
                Log.d("Response", response.toString())
                onSuccess(response)
            },
            { error ->
                error.printStackTrace()
                onError(error.message ?: "Error desconocido")
                Log.d("Falla", error.toString())
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            5000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        requestQueue.add(jsonObjectRequest)
    }

    fun formatDate(date: Date?): String? {
        return date?.let {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(it)
        }
    }
}
