package com.example.findit.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.findit.R
import com.example.findit.data.model.Item

class SearchFragment : Fragment(R.layout.fragment_search) {

    private var selectedCategory: String? = null
    private val tagIds = listOf(
        R.id.tagIdCard to "ID Card",
        R.id.tagWallet to "Wallet",
        R.id.tagElectronics to "Electronics",
        R.id.tagBooks to "Books",
        R.id.tagBags to "Bags",
        R.id.tagKeys to "Keys",
        R.id.tagOthers to "Others"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Category selection
        tagIds.forEach { (id, name) ->
            view.findViewById<TextView>(id).setOnClickListener {
                selectedCategory = if (selectedCategory == name) null else name
                refreshTagStyles(view)
            }
        }

        view.findViewById<TextView>(R.id.btnApply).setOnClickListener {

            val query = view.findViewById<EditText>(R.id.etSearch).text.toString()

            val type = when (view.findViewById<RadioGroup>(R.id.rgFilterType).checkedRadioButtonId) {
                R.id.rbFilterLost -> Item.TYPE_LOST
                R.id.rbFilterFound -> Item.TYPE_FOUND
                else -> Item.TYPE_ALL
            }

            // ✅ UPDATED (added category)
            val args = Bundle().apply {
                putString(SearchResultsFragment.ARG_QUERY, query)
                putString(SearchResultsFragment.ARG_TYPE, type)
                putString("category", selectedCategory)   // 🔥 NEW
            }

            val frag = SearchResultsFragment().apply { arguments = args }

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, frag)
                .addToBackStack(null)
                .commit()
        }

        // Footer
        view.findViewById<LinearLayout>(R.id.navHome).setOnClickListener { replaceFragment(HomeFragment()) }
        view.findViewById<LinearLayout>(R.id.navPost).setOnClickListener { replaceFragment(PostFragment()) }
        view.findViewById<LinearLayout>(R.id.navProfile).setOnClickListener { replaceFragment(ProfileFragment()) }
    }

    private fun refreshTagStyles(view: View) {
        tagIds.forEach { (id, name) ->
            val tv = view.findViewById<TextView>(id)
            if (selectedCategory == name) {
                tv.setBackgroundColor(0xFF1565C0.toInt())
                tv.setTextColor(0xFFFFFFFF.toInt())
            } else {
                tv.setBackgroundResource(R.drawable.bg_tag)
                tv.setTextColor(0xFF1565C0.toInt())
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}