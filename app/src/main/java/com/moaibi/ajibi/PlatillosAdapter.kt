package com.moaibi.ajibi

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

class PlatillosAdapter(private var platillosList: MutableList<Platillo>, private val onItemClick: (Platillo, String) -> Unit) :
    RecyclerView.Adapter<PlatillosAdapter.PlatilloViewHolder>() {

    inner class PlatilloViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreTextView: TextView = itemView.findViewById(R.id.platillo_nombre)
        val precioTextView: TextView = itemView.findViewById(R.id.platillo_precio)
        val imageView: ShapeableImageView = itemView.findViewById(R.id.platillo_image)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val platillo = platillosList[position]
                    platillo.id?.let { id -> onItemClick(platillo, id) }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatilloViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_platillo, parent, false)
        return PlatilloViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlatilloViewHolder, position: Int) {
        val currentPlatillo = platillosList[position]

        // Formatear el nombre con dos colores
        val nombre = "Nombre: ${currentPlatillo.nombre}"
        val spannableNombre = SpannableString(nombre)
        spannableNombre.setSpan(ForegroundColorSpan(Color.BLACK), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        holder.nombreTextView.text = spannableNombre

        // Formatear el precio con dos colores
        val precio = "Precio: ${currentPlatillo.precio}"
        val spannablePrecio = SpannableString(precio)
        spannablePrecio.setSpan(ForegroundColorSpan(Color.BLACK), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        holder.precioTextView.text = spannablePrecio

        // Cargar la imagen con Glide o cualquier otra librería de carga de imágenes
        Glide.with(holder.itemView.context)
            .load(currentPlatillo.fotoUrl)
            .placeholder(R.drawable.ic_launcher_background) // Imagen de marcador de posición
            .into(holder.imageView)
    }

    override fun getItemCount() = platillosList.size

    fun updatePlatillos(platillos: List<Platillo>) {
        this.platillosList = platillos.toMutableList()
        notifyDataSetChanged()
    }
}
