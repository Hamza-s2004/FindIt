package com.example.findit

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Logout button
        val logout = view.findViewById<TextView>(R.id.tvLogout)

        logout.setOnClickListener {
            // later: logout logic
        }

        // Footer navigation
        val navHome = view.findViewById<LinearLayout>(R.id.navHome)
        val navPost = view.findViewById<LinearLayout>(R.id.navPost)
        val navSearch = view.findViewById<LinearLayout>(R.id.navSearch)

        navHome.setOnClickListener {
            replaceFragment(HomeFragment())
        }

        navPost.setOnClickListener {
            replaceFragment(PostFragment())
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