package com.khrlanamm.ayobicarakawan.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.khrlanamm.ayobicarakawan.MainActivity
import com.khrlanamm.ayobicarakawan.databinding.ActivitySignInBinding
import com.khrlanamm.ayobicarakawan.R

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupListeners()
        setupGoogleSignIn()
    }

    private fun setupListeners() {
        binding.btnDaftar.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.btnMasuk.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showLoading(true)

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    showLoading(false)
                    if (task.isSuccessful) {
                        goToMain()
                    } else {
                        val message = task.exception?.localizedMessage ?: "Gagal masuk"
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                        Log.e("SignInActivity", "signInWithEmailAndPassword failed", task.exception)
                    }
                }
        }

        binding.btnGoogle.setOnClickListener {
            showLoading(true)
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener { result ->
                    try {
                        googleSignInLauncher.launch(IntentSenderRequest.Builder(result.pendingIntent.intentSender).build())
                    } catch (e: Exception) {
                        showLoading(false)
                        Toast.makeText(this, "Gagal memulai Google Sign-In: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        Log.e("SignInActivity", "Google Sign-In initiation failed", e)
                    }
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    Toast.makeText(this, "Login Google gagal: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    Log.e("SignInActivity", "beginSignIn failed", e)
                }
        }
    }

    private fun setupGoogleSignIn() {
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        showLoading(false)
        if (result.resultCode == RESULT_OK) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                goToMain()
                            } else {
                                val message = task.exception?.localizedMessage ?: "Login dengan Google gagal"
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                                Log.e("SignInActivity", "signInWithCredential failed", task.exception)
                            }
                        }
                } else {
                    Toast.makeText(this, "ID Token Google tidak ditemukan", Toast.LENGTH_SHORT).show()
                    Log.e("SignInActivity", "Google ID Token is null")
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal mengambil kredensial Google: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("SignInActivity", "getSignInCredentialFromIntent failed", e)
            }
        } else {
            Toast.makeText(this, "Login Google dibatalkan", Toast.LENGTH_SHORT).show()
            Log.e("SignInActivity", "Google Sign-In canceled or failed with resultCode: ${result.resultCode}")
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }
}
