package com.example.findit

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class PostFragment : Fragment(R.layout.fragment_post) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Submit button
        val btnSubmit = view.findViewById<TextView>(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            // later: save data
        }

        // Footer navigation
        val navHome = view.findViewById<LinearLayout>(R.id.navHome)
        val navProfile = view.findViewById<LinearLayout>(R.id.navProfile)
        val navSearch = view.findViewById<LinearLayout>(R.id.navSearch)

        navHome.setOnClickListener {
            replaceFragment(HomeFragment())
        }

        navProfile.setOnClickListener {
            replaceFragment(ProfileFragment())
        }

        navSearch.setOnClickListener {
            replaceFragment(SearchFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}