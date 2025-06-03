package com.khrlanamm.ayobicarakawan.ui.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.khrlanamm.ayobicarakawan.MainActivity
import com.khrlanamm.ayobicarakawan.R
import com.khrlanamm.ayobicarakawan.ui.auth.authdata.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignInActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var progressBarLayout: RelativeLayout

    private lateinit var db: AppDatabase

    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val PREF_NAME = "user_session"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_EMAIL = "user_email"
    }

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

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        progressBarLayout = findViewById(R.id.progressBar)

        db = AppDatabase.getDatabase(applicationContext)

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        checkUserSession()

        val btnDaftar: TextView = findViewById(R.id.btnDaftar)
        btnDaftar.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        val btnMasuk: Button = findViewById(R.id.btnMasuk)
        btnMasuk.setOnClickListener {
            signInUser()
        }

        val btnUnderDevelopment: TextView = findViewById(R.id.button2)
        btnUnderDevelopment.setOnClickListener {
            Toast.makeText(this, "Sedang dalam Pengembangan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkUserSession() {
        val isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        if (isLoggedIn) {
            val userName = sharedPreferences.getString(KEY_USER_NAME, "Pengguna")
            Toast.makeText(this, "Selamat datang kembali, $userName!", Toast.LENGTH_SHORT).show()
            navigateToMain()
        }
    }

    private fun signInUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

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

        progressBarLayout.visibility = View.VISIBLE
        val startTime = System.currentTimeMillis()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userByEmail = db.userDao().getUserByEmail(email)

                val message: String
                var navigateToMainScreen = false

                if (userByEmail == null) {
                    message = "Email tidak terdaftar, Pastikan email ditulis dengan benar atau silahkan Daftar Akun"
                } else {
                    val user = db.userDao().getUserByEmailAndPassword(email, password)
                    if (user != null) {
                        saveUserSession(user.name, user.email)
                        message = "Berhasil Masuk sebagai ${user.name}"
                        navigateToMainScreen = true
                    } else {
                        message = "Password Salah"
                    }
                }

                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingDelay = 2000L - elapsedTime
                if (remainingDelay > 0) {
                    delay(remainingDelay)
                }

                withContext(Dispatchers.Main) {
                    progressBarLayout.visibility = View.INVISIBLE
                    Toast.makeText(this@SignInActivity, message, Toast.LENGTH_SHORT).show()
                    if (navigateToMainScreen) {
                        navigateToMain()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBarLayout.visibility = View.INVISIBLE
                    Toast.makeText(this@SignInActivity, "Login Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                }
                e.printStackTrace()
            }
        }
    }

    private fun saveUserSession(userName: String, userEmail: String) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_EMAIL, userEmail)
            apply()
        }
    }

    fun clearUserSession() {
        sharedPreferences.edit().apply {
            clear()
            apply()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this@SignInActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
