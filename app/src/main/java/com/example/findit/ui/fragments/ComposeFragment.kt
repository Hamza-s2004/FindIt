package com.example.findit.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.findit.ui.compose.ComposeScreen

class ComposeFragment : Fragment() {

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ComposeScreen(
                    onSuccess = {
                        replaceFragment(HomeFragment())
                    }
                )
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(com.example.findit.R.id.fragmentContainer, fragment)
            .commit()
    }
}