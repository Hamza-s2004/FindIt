package com.example.findit.data.auth

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

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
                    val user = auth.currentUser
                    if (user != null && !user.isEmailVerified) {
                        // If email not verified, send verification email and prevent login
                        user.sendEmailVerification().addOnCompleteListener { sendTask ->
                            // Sign out to ensure session is not kept for unverified user
                            auth.signOut()
                            if (sendTask.isSuccessful) {
                                callback(false, "Email not verified. Verification email sent.")
                            } else {
                                callback(false, "Email not verified. Failed to send verification email.")
                            }
                        }
                    } else {
                        // If logged in and email verified, persist username if pending or from provider
                        val userVerified = auth.currentUser
                        if (userVerified != null) {
                            val prefs = context?.getSharedPreferences("user_data", Context.MODE_PRIVATE)
                            val displayName = userVerified.displayName
                            if (!displayName.isNullOrBlank()) {
                                saveUserName(displayName)
                                prefs?.edit()?.remove("pending_name_${userVerified.email}")?.apply()
                            } else {
                                val pending = prefs?.getString("pending_name_${userVerified.email}", null)
                                if (!pending.isNullOrBlank()) {
                                    saveUserName(pending)
                                    prefs?.edit()?.remove("pending_name_${userVerified.email}")?.apply()
                                }
                            }
                        }
                        callback(true, null)
                    }
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
                    // Save pending name until user verifies email
                    val prefs = context?.getSharedPreferences("user_data", Context.MODE_PRIVATE)
                    prefs?.edit()?.putString("pending_name_$email", name)?.apply()

                    // Send verification email after registration
                    val user = auth.currentUser
                    if (user != null) {
                        user.sendEmailVerification().addOnCompleteListener { sendTask ->
                            if (sendTask.isSuccessful) {
                                callback(true, "Verification email sent")
                            } else {
                                // Still treat registration as successful but inform about email failure
                                callback(true, "Registered but failed to send verification email: ${sendTask.exception?.message}")
                            }
                        }
                    } else {
                        callback(true, null)
                    }
                } else {
                    callback(false, it.exception?.message)
                }
            }
    }

    // ✅ NEW: Google Sign-In with ID token
    fun loginWithGoogle(idToken: String, callback: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val user = auth.currentUser
                    // Save display name if available
                    user?.displayName?.let { name ->
                        saveUserName(name)
                    }
                    callback(true, null)
                } else {
                    callback(false, it.exception?.message)
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    /**
     * Resend verification email to the currently signed-in (unverified) user.
     */
    fun resendVerificationEmail(callback: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback(false, "No signed-in user")
            return
        }

        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, null)
            } else {
                callback(false, task.exception?.message)
            }
        }
    }

    /**
     * Reloads the current user and if verified, persists pending name (if any) and returns success.
     */
    fun completeRegistrationIfVerified(callback: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            callback(false, "No signed-in user")
            return
        }

        user.reload().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                callback(false, task.exception?.message)
                return@addOnCompleteListener
            }

            if (user.isEmailVerified) {
                try {
                    val prefs = context?.getSharedPreferences("user_data", Context.MODE_PRIVATE)
                    val displayName = user.displayName
                    if (!displayName.isNullOrBlank()) {
                        saveUserName(displayName)
                        prefs?.edit()?.remove("pending_name_${user.email}")?.apply()
                    } else {
                        val pending = prefs?.getString("pending_name_${user.email}", null)
                        if (!pending.isNullOrBlank()) {
                            saveUserName(pending)
                            prefs?.edit()?.remove("pending_name_${user.email}")?.apply()
                        }
                    }
                } catch (e: Exception) {
                    Log.w("AuthManager", "Failed to persist pending name: ${e.message}")
                }

                callback(true, null)
            } else {
                callback(false, "Email not verified yet")
            }
        }
    }
}