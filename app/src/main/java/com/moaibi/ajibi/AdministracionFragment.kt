package com.moaibi.ajibi


import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import java.io.IOException
import java.io.OutputStream
import java.util.*

class AdministracionFragment : Fragment() {

    private lateinit var spinnerNombre: Spinner
    private lateinit var txtPrecio: EditText
    private lateinit var txtIngredientes: EditText
    private lateinit var txtDescripcion: EditText
    private lateinit var txtCalorias: EditText
    private lateinit var fotoImageView: ImageView
    private lateinit var btnIniciar: Button
    private lateinit var btnEnCola: Button
    private lateinit var btnEnPreparacion: Button
    private lateinit var btnListo: Button

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    private lateinit var db: DatabaseReference
    private var platillos: MutableList<Platillo> = mutableListOf()

    private val HC05_ADDRESS = "98:DA:E0:01:1F:6D" // Reemplaza con la dirección MAC de tu módulo HC-05
    private val HC05_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    companion object {
        fun newInstance(): AdministracionFragment {
            return AdministracionFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_administracion, container, false)

        spinnerNombre = view.findViewById(R.id.spinner_nombre)
        txtPrecio = view.findViewById(R.id.precio)
        txtIngredientes = view.findViewById(R.id.ingredientes)
        txtDescripcion = view.findViewById(R.id.descripcion)
        txtCalorias = view.findViewById(R.id.calorias)
        fotoImageView = view.findViewById(R.id.foto)
        btnIniciar = view.findViewById(R.id.btnIniciar)
        btnEnCola = view.findViewById(R.id.btnEnCola)
        btnEnPreparacion = view.findViewById(R.id.btnEnPreparacion)
        btnListo = view.findViewById(R.id.btnListo)

        db = FirebaseDatabase.getInstance().getReference("platillos")

        loadPlatillos()

        // Conectar Bluetooth cuando se presiona el botón "Iniciar"
        btnIniciar.setOnClickListener { connectBluetooth() }

        // Enviar comandos cuando se presionan los botones
        btnEnCola.setOnClickListener { sendCommand("1") }
        btnEnPreparacion.setOnClickListener { sendCommand("2") }
        btnListo.setOnClickListener { sendCommand("3") }

        spinnerNombre.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedPlatillo = platillos[position]
                fillPlatilloDetails(selectedPlatillo)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Manejo de caso donde no se selecciona nada
            }
        }

        return view
    }

    private fun connectBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(requireContext(), "Bluetooth no está habilitado", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar si la versión del SDK es Android 12 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 1)
                return
            }
        }

        val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(HC05_ADDRESS)
        bluetoothSocket = device.createRfcommSocketToServiceRecord(HC05_UUID)

        try {
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            Toast.makeText(requireContext(), "Conectado a HC-05", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
            bluetoothSocket?.close()
        }
    }

    private fun sendCommand(command: String) {
        if (outputStream == null) {
            Toast.makeText(requireContext(), "No conectado a HC-05", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            outputStream?.write(command.toByteArray())
            Toast.makeText(requireContext(), "Comando enviado: $command", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al enviar comando", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPlatillos() {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                platillos.clear()
                val platilloNames = mutableListOf<String>()
                for (platilloSnapshot in snapshot.children) {
                    val platillo = platilloSnapshot.getValue(Platillo::class.java)
                    platillo?.let {
                        it.id = platilloSnapshot.key
                        platillos.add(it)
                        platilloNames.add(it.nombre)
                    }
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, platilloNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerNombre.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al cargar platillos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fillPlatilloDetails(platillo: Platillo) {
        txtPrecio.setText(platillo.precio.toString())
        txtIngredientes.setText(platillo.ingredientes)
        txtDescripcion.setText(platillo.descripcion)
        txtCalorias.setText(platillo.calorias.toString())

        Glide.with(this)
            .load(platillo.fotoUrl)
            .into(fotoImageView)
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothSocket?.close()
    }
}
