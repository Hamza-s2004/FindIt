package com.example.findit.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.findit.R
import com.example.findit.data.auth.AuthManager

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var auth: AuthManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = AuthManager(requireContext())

        // ✅ Get views
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)

        // ✅ Set data properly
        val name = auth.getUserName()
        tvName.text = name ?: "User"
        tvEmail.text = auth.getUserEmail() ?: "Guest"

        // ✅ Logout
        val tvLogout = view.findViewById<TextView>(R.id.tvLogout)
        tvLogout.setOnClickListener {
            auth.logout()
            replaceFragment(LoginFragment())
        }

        // ✅ Navigation
        view.findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            replaceFragment(HomeFragment())
        }

        view.findViewById<LinearLayout>(R.id.navPost).setOnClickListener {
            replaceFragment(PostFragment())
        }

        view.findViewById<LinearLayout>(R.id.navSearch).setOnClickListener {
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