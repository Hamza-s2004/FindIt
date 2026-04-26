package com.example.findit.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.findit.R

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tvLogout).setOnClickListener {
            // future: clear session
        }

        view.findViewById<LinearLayout>(R.id.navHome).setOnClickListener { replaceFragment(HomeFragment()) }
        view.findViewById<LinearLayout>(R.id.navPost).setOnClickListener { replaceFragment(PostFragment()) }
        view.findViewById<LinearLayout>(R.id.navSearch).setOnClickListener { replaceFragment(SearchFragment()) }
    }

    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}
