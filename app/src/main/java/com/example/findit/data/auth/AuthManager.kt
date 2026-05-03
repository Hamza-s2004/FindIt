package com.example.findit.data.auth

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

class AuthManager(private val context: Context? = null) {

    private val auth = FirebaseAuth.getInstance()

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getUserEmail(): String? {
        return auth.currentUser?.email
    }

    // ✅ NEW: get username
    fun getUserName(): String? {
        val prefs = context?.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        return prefs?.getString("name", null)
    }

    // ✅ NEW: save username
    private fun saveUserName(name: String) {
        val prefs = context?.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        prefs?.edit()?.putString("name", name)?.apply()
    }

    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, it.exception?.message)
                }
            }
    }

    // 🔥 UPDATED: now takes name also
    fun register(name: String, email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    saveUserName(name)   // ✅ save name
                    callback(true, null)
                } else {
                    callback(false, it.exception?.message)
                }
            }
    }

    fun logout() {
        auth.signOut()
    }
}