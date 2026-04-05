package com.example.findit

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
class SearchFragment : Fragment(R.layout.fragment_search) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Footer navigation
        val navHome = view.findViewById<LinearLayout>(R.id.navHome)
        val navPost = view.findViewById<LinearLayout>(R.id.navPost)
        val navProfile = view.findViewById<LinearLayout>(R.id.navProfile)

        navHome.setOnClickListener {
            replaceFragment(HomeFragment())
        }

        navPost.setOnClickListener {
            replaceFragment(PostFragment())
        }

        navProfile.setOnClickListener {
            replaceFragment(ProfileFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}