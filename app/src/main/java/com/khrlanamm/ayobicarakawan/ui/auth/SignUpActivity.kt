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
import com.khrlanamm.ayobicarakawan.R

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Tombol untuk kembali ke SignIn
        val btnMasuk: TextView = findViewById(R.id.btnMasuk)
        btnMasuk.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Tombol registrasi
        val btnSignUp: Button = findViewById(R.id.btnSignUp)
        btnSignUp.setOnClickListener {
            Toast.makeText(this, "Registrasi Berhasil, silahkan Login", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Tombol daftar dengan Google
        val btnSignUpGoogle: TextView = findViewById(R.id.btnSignUpGoogle)
        btnSignUpGoogle.setOnClickListener {
            Toast.makeText(this, "Sedang dalam Pengembangan", Toast.LENGTH_SHORT).show()
        }
    }
}
