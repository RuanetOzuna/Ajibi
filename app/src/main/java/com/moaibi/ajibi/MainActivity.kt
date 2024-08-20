package com.moaibi.ajibi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, InicioSesionActivity::class.java))
            finish()
            return
        }

        db = FirebaseDatabase.getInstance().getReference("platillos")

        val bottomNav: BottomNavigationView = findViewById(R.id.btnNavigator)
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.btnhome -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.btnmenus -> {
                    loadFragment(PlatillosFragment())
                    true
                }
                R.id.btnagregar -> {
                    loadFragment(AgregarEditarPlatilloDialogFragment())
                    true
                }
                R.id.btnadmin -> {
                    loadFragment(AdministracionFragment())
                    true
                }
                else -> false
            }
        }

        // Load the default fragment
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.btnhome
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frmContenedor, fragment)
            .commit()
    }
}
