package com.khrlanamm.ayobicarakawan.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.khrlanamm.ayobicarakawan.R
import com.khrlanamm.ayobicarakawan.ui.auth.authdata.AppDatabase
import com.khrlanamm.ayobicarakawan.ui.auth.authdata.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var progressBarLayout: RelativeLayout

    private lateinit var db: AppDatabase

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

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        progressBarLayout = findViewById(R.id.progressBar) // Inisialisasi progress bar layout

        db = AppDatabase.getDatabase(applicationContext)

        val btnMasuk: TextView = findViewById(R.id.btnMasuk)
        btnMasuk.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnSignUp: Button = findViewById(R.id.btnSignUp)
        btnSignUp.setOnClickListener {
            registerUser()
        }

        val btnSignUpGoogle: TextView = findViewById(R.id.btnSignUpGoogle)
        btnSignUpGoogle.setOnClickListener {
            Toast.makeText(this, "Sedang dalam Pengembangan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerUser() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (name.isEmpty()) {
            etName.error = "Nama tidak boleh kosong"
            etName.requestFocus()
            return
        }
        if (email.isEmpty()) {
            etEmail.error = "Email tidak boleh kosong"
            etEmail.requestFocus()
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Format email tidak valid"
            etEmail.requestFocus()
            return
        }
        if (password.isEmpty()) {
            etPassword.error = "Password tidak boleh kosong"
            etPassword.requestFocus()
            return
        }
        if (password.length < 6) {
            etPassword.error = "Password minimal 6 karakter"
            etPassword.requestFocus()
            return
        }
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Konfirmasi password tidak boleh kosong"
            etConfirmPassword.requestFocus()
            return
        }
        if (password != confirmPassword) {
            etConfirmPassword.error = "Password tidak cocok"
            etConfirmPassword.requestFocus()
            return
        }

        progressBarLayout.visibility = View.VISIBLE
        val startTime = System.currentTimeMillis()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existingUser = db.userDao().getUserByEmail(email)

                val message: String
                val navigateToSignIn: Boolean

                if (existingUser != null) {
                    message = "Email sudah terdaftar"
                    navigateToSignIn = false
                } else {
                    val newUser = User(name = name, email = email, password = password)
                    db.userDao().insertUser(newUser)
                    message = "Registrasi Berhasil, silahkan Login"
                    navigateToSignIn = true
                }

                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingDelay = 2000L - elapsedTime
                if (remainingDelay > 0) {
                    delay(remainingDelay)
                }

                withContext(Dispatchers.Main) {
                    progressBarLayout.visibility = View.INVISIBLE
                    Toast.makeText(this@SignUpActivity, message, Toast.LENGTH_SHORT).show()
                    if (navigateToSignIn) {
                        val intent = Intent(this@SignUpActivity, SignInActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBarLayout.visibility = View.INVISIBLE
                    Toast.makeText(this@SignUpActivity, "Registrasi Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                }
                e.printStackTrace()
            }
        }
    }
}
