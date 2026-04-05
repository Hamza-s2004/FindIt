package com.example.findit

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment

class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Footer navigation
        val navHome = view.findViewById<LinearLayout>(R.id.navHome)
        val navPost = view.findViewById<LinearLayout>(R.id.navPost)
        val navSearch = view.findViewById<LinearLayout>(R.id.navSearch)
        val navProfile = view.findViewById<LinearLayout>(R.id.navProfile)

        navHome.setOnClickListener {
            replaceFragment(HomeFragment())
        }

        navPost.setOnClickListener {
            replaceFragment(PostFragment())
        }

        navSearch.setOnClickListener {
            replaceFragment(SearchFragment())
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