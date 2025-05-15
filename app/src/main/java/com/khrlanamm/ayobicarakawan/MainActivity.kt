package com.khrlanamm.ayobicarakawan

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.khrlanamm.ayobicarakawan.databinding.ActivityMainBinding
import com.khrlanamm.ayobicarakawan.ui.auth.SignInActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        auth = FirebaseAuth.getInstance()

        // ✅ Cek apakah user sudah login
        if (auth.currentUser == null) {
            // ❌ Belum login, lempar ke SignInActivity
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        // ✅ Sudah login, lanjutkan ke tampilan utama
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_report,
                R.id.navigation_chat,
                R.id.navigation_history
            )
        )

        navView.setupWithNavController(navController)
    }
}
