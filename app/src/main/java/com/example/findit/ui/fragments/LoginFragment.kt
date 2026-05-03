package com.example.findit.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.findit.R
import com.example.findit.data.auth.AuthManager

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val auth = AuthManager()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = view.findViewById<EditText>(R.id.etEmail)
        val pass = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)

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
    }

    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}