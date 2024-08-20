package com.moaibi.ajibi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

private const val PICK_IMAGE_REQUEST = 1

class AgregarEditarPlatilloDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(platillo: Platillo?): AgregarEditarPlatilloDialogFragment {
            val fragment = AgregarEditarPlatilloDialogFragment()
            val args = Bundle()
            args.putParcelable("platillo", platillo)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var db: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var fotoImageView: ImageView
    private var selectedImageUri: Uri? = null
    private var platilloId: String? = null
    private var existingFotoUrl: String? = null

    private lateinit var txtNombre: EditText
    private lateinit var txtPrecio: EditText
    private lateinit var txtIngredientes: EditText
    private lateinit var txtDescripcion: EditText
    private lateinit var txtCalorias: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnSelectImage: Button
    private lateinit var btnPreparar: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_agregar_editar_platillo, container, false)

        db = FirebaseDatabase.getInstance().getReference("platillos")
        storage = FirebaseStorage.getInstance()

        fotoImageView = view.findViewById(R.id.foto)
        txtNombre = view.findViewById(R.id.nombre)
        txtPrecio = view.findViewById(R.id.precio)
        txtIngredientes = view.findViewById(R.id.ingredientes)
        txtDescripcion = view.findViewById(R.id.descripcion)
        txtCalorias = view.findViewById(R.id.calorias)
        btnGuardar = view.findViewById(R.id.guardar_button)
        btnSelectImage = view.findViewById(R.id.btnSelectImage)
        btnPreparar = view.findViewById(R.id.preparar_button)

        // Deshabilitar el botón preparar por defecto
        btnPreparar.isEnabled = false

        btnSelectImage.setOnClickListener {
            openImageChooser()
        }

        btnGuardar.setOnClickListener {
            hideKeyboard()
            savePlatillo()
        }

        // Verificar si se ha pasado un platillo para editar
        val args = arguments
        if (args != null) {
            val platillo = args.getParcelable<Platillo>("platillo")
            if (platillo != null) {
                platilloId = args.getString("platilloId")
                populateFields(platillo)
                btnPreparar.isEnabled = true // Habilitar si es un platillo existente
            }
        }

        return view
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                selectedImageUri = data.data
                fotoImageView.setImageURI(selectedImageUri)
            }
        }
    }

    private fun savePlatillo() {
        val nombre = txtNombre.text.toString()
        val precio = txtPrecio.text.toString().toDoubleOrNull() ?: 0.0
        val ingredientes = txtIngredientes.text.toString()
        val descripcion = txtDescripcion.text.toString()
        val calorias = txtCalorias.text.toString().toIntOrNull() ?: 0

        if (nombre.isEmpty()) {
            txtNombre.error = "El nombre es obligatorio"
            return
        }
        if (ingredientes.isEmpty()) {
            txtIngredientes.error = "Los ingredientes son obligatorios"
            return
        }
        if (descripcion.isEmpty()) {
            txtDescripcion.error = "La descripción es obligatoria"
            return
        }
        if (precio <= 0.0) {
            txtPrecio.error = "El precio debe ser mayor que 0"
            return
        }
        if (selectedImageUri == null && platilloId == null) {
            Toast.makeText(requireContext(), "Debes subir una foto", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri != null) {
            val fotoRef = storage.reference.child("fotos/${System.currentTimeMillis()}.jpg")
            fotoRef.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    fotoRef.downloadUrl.addOnSuccessListener { uri ->
                        saveOrUpdatePlatillo(uri.toString())
                        selectedImageUri = null
                    }
                }
        } else {
            saveOrUpdatePlatillo(existingFotoUrl)
        }
    }

    private fun saveOrUpdatePlatillo(fotoUrl: String?) {
        val nombre = txtNombre.text.toString()
        val precio = txtPrecio.text.toString().toDoubleOrNull() ?: 0.0
        val ingredientes = txtIngredientes.text.toString()
        val descripcion = txtDescripcion.text.toString()
        val calorias = txtCalorias.text.toString().toIntOrNull() ?: 0

        val platillo = Platillo(
            fotoUrl = fotoUrl ?: "",
            nombre = nombre,
            precio = precio,
            ingredientes = ingredientes,
            descripcion = descripcion,
            calorias = calorias
        )

        if (platilloId == null) {
            // Nuevo platillo
            platilloId = db.push().key
        }

        platilloId?.let {
            db.child(it).setValue(platillo)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Platillo guardado", Toast.LENGTH_SHORT).show()
                    btnPreparar.isEnabled = true // Habilitar el botón preparar después de guardar
                    activity?.supportFragmentManager?.setFragmentResult("platillo_guardado", Bundle())
                    dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al guardar platillo", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun populateFields(platillo: Platillo) {
        txtNombre.setText(platillo.nombre)
        txtPrecio.setText(platillo.precio.toString())
        txtIngredientes.setText(platillo.ingredientes)
        txtDescripcion.setText(platillo.descripcion)
        txtCalorias.setText(platillo.calorias.toString())
        existingFotoUrl = platillo.fotoUrl

        Glide.with(this)
            .load(platillo.fotoUrl)
            .into(fotoImageView)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}
