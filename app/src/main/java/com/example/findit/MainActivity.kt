package com.example.findit

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.findit.data.auth.AuthManager
import com.example.findit.ui.fragments.HomeFragment
import com.example.findit.ui.fragments.LoginFragment
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val auth = AuthManager()

        // ✅ Auto-login logic
        if (savedInstanceState == null) {
            val startFragment = if (auth.isLoggedIn()) {
                HomeFragment()
            } else {
                LoginFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, startFragment)
                .commit()
        }

        // ✅ FCM token logging
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_TOKEN", token)
            } else {
                Log.e("FCM_TOKEN", "Failed to get token", task.exception)
            }
        }
    }
}