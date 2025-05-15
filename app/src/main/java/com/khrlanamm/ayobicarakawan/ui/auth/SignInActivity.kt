package com.khrlanamm.ayobicarakawan.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.khrlanamm.ayobicarakawan.MainActivity
import com.khrlanamm.ayobicarakawan.R

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Tombol "Daftar"
        val btnDaftar: TextView = findViewById(R.id.btnDaftar)
        btnDaftar.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Tombol "Masuk"
        val btnMasuk: Button = findViewById(R.id.btnMasuk)
        btnMasuk.setOnClickListener {
            Toast.makeText(this, "Berhasil Masuk", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Tombol "button2" (Sedang dalam pengembangan)
        val btnUnderDevelopment: TextView = findViewById(R.id.button2)
        btnUnderDevelopment.setOnClickListener {
            Toast.makeText(this, "Sedang dalam Pengembangan", Toast.LENGTH_SHORT).show()
        }
    }
}
