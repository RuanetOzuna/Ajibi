package com.moaibi.ajibi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class PlatillosFragment : Fragment() {
    private lateinit var db: DatabaseReference
    private lateinit var platillosList: RecyclerView
    private lateinit var platillosAdapter: PlatillosAdapter
    private var platillos: MutableList<Platillo> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_platillos, container, false)

        db = FirebaseDatabase.getInstance().getReference("platillos")
        platillosList = view.findViewById(R.id.platillos_list)
        platillosList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        platillosAdapter = PlatillosAdapter(platillos) { platillo, platilloId ->
            editPlatillo(platillo, platilloId)
        }
        platillosList.adapter = platillosAdapter

        loadPlatillos()

        val fab: FloatingActionButton = view.findViewById(R.id.agregarPlatillo)
        fab.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.frmContenedor, AgregarEditarPlatilloDialogFragment.newInstance(null))
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }

    private fun loadPlatillos() {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                platillos.clear()
                for (platilloSnapshot in snapshot.children) {
                    val platillo = platilloSnapshot.getValue(Platillo::class.java)
                    platillo?.let {
                        it.id = platilloSnapshot.key
                        platillos.add(it)
                    }
                }
                platillosAdapter.updatePlatillos(platillos)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al cargar platillos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun editPlatillo(platillo: Platillo, platilloId: String) {
        val fragment = AgregarEditarPlatilloDialogFragment().apply {
            arguments = Bundle().apply {
                putParcelable("platillo", platillo)
                putString("platilloId", platilloId)
            }
        }
        fragment.show(childFragmentManager, "AgregarEditarPlatilloDialogFragment")
    }
}
