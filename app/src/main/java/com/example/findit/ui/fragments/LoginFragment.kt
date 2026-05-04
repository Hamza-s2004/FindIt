package com.example.findit.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.findit.R
import com.example.findit.data.auth.AuthManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var auth: AuthManager
    private lateinit var googleSignInClient: GoogleSignInClient

    // Activity Result API for Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(Exception::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                handleGoogleSignInSuccess(idToken)
            } else {
                Toast.makeText(requireContext(), "ID token not received", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // provide context to AuthManager so it can persist user info
        auth = AuthManager(requireContext())

        // Initialize Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val email = view.findViewById<EditText>(R.id.etEmail)
        val pass = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val btnGoogleSignIn = view.findViewById<Button>(R.id.btnGoogleSignIn)

        // ✅ LOGIN
        btnLogin.setOnClickListener {

            val emailText = email.text.toString().trim()
            val passText = pass.text.toString().trim()

            if (emailText.isEmpty() || passText.isEmpty()) {
                Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.login(emailText, passText) { success, msg ->
                if (success) {
                    replaceFragment(HomeFragment())
                } else {
                    Toast.makeText(requireContext(), msg ?: "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 🔥 REGISTER → open Compose screen
        btnRegister.setOnClickListener {
            replaceFragment(RegisterComposeFragment())
        }

        // 🔥 GOOGLE SIGN-IN
        btnGoogleSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun handleGoogleSignInSuccess(idToken: String) {
        auth.loginWithGoogle(idToken) { success, msg ->
            if (success) {
                replaceFragment(HomeFragment())
            } else {
                Toast.makeText(requireContext(), msg ?: "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}