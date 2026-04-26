package com.example.findit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.findit.ui.fragments.HomeFragment

/**
 * Single-Activity host. UI lives in Fragments (per assignment constraint).
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }
    }
}