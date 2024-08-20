package com.moaibi.ajibi

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Platillo(
    var id: String? = null,
    var fotoUrl: String = "",
    var nombre: String = "",
    var precio: Double = 0.0,
    var ingredientes: String = "",
    var descripcion: String = "",
    var calorias: Int = 0
) : Parcelable
