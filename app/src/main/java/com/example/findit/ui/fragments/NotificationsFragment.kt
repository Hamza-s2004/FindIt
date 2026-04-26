package com.example.findit.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.findit.R

class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<LinearLayout>(R.id.navHome).setOnClickListener { replaceFragment(HomeFragment()) }
        view.findViewById<LinearLayout>(R.id.navPost).setOnClickListener { replaceFragment(PostFragment()) }
        view.findViewById<LinearLayout>(R.id.navSearch).setOnClickListener { replaceFragment(SearchFragment()) }
        view.findViewById<LinearLayout>(R.id.navProfile).setOnClickListener { replaceFragment(ProfileFragment()) }
    }

    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}
