package com.example.findit.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.findit.R
import com.example.findit.data.model.Item
import com.example.findit.data.repository.ItemRepository
import kotlinx.coroutines.launch

/**
 * F3 — Create. Submits a new Item row into SQLite.
 */
class PostFragment : Fragment(R.layout.fragment_post) {

    private lateinit var repo: ItemRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = ItemRepository(requireContext())

        val etName = view.findViewById<EditText>(R.id.etItemName)
        val etDesc = view.findViewById<EditText>(R.id.etDescription)
        val rgType = view.findViewById<RadioGroup>(R.id.rgPostType)
        val rgCategory = view.findViewById<RadioGroup>(R.id.rgCategory)
        val etLocation = view.findViewById<EditText>(R.id.etLocation)
        val etDate = view.findViewById<EditText>(R.id.etDate)
        val etContact = view.findViewById<EditText>(R.id.etContact)
        val btnSubmit = view.findViewById<TextView>(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            val title = etName.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Item name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val type = if (rgType.checkedRadioButtonId == R.id.rbFound)
                Item.TYPE_FOUND else Item.TYPE_LOST

            val categoryName = when (rgCategory.checkedRadioButtonId) {
                R.id.rbWallet -> "Wallet"
                R.id.rbElectronics -> "Electronics"
                R.id.rbBooks -> "Books"
                R.id.rbBags -> "Bags"
                R.id.rbIdCard -> "ID Card"
                else -> "Others"
            }

            viewLifecycleOwner.lifecycleScope.launch {
                val cat = repo.getCategoryByName(categoryName)
                if (cat == null) {
                    Toast.makeText(requireContext(), "Category not found", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val newItem = Item(
                    title = title,
                    description = etDesc.text.toString().trim(),
                    type = type,
                    categoryId = cat.id,
                    categoryName = cat.name,
                    location = etLocation.text.toString().trim(),
                    date = etDate.text.toString().trim(),
                    contact = etContact.text.toString().trim(),
                    source = Item.SOURCE_LOCAL
                )
                val newId = repo.insertItem(newItem)
                if (newId > 0L) {
                    Toast.makeText(requireContext(), "Posted!", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Could not save item", Toast.LENGTH_SHORT).show()
                }
            }
        }

        view.findViewById<LinearLayout>(R.id.navHome).setOnClickListener { replaceFragment(HomeFragment()) }
        view.findViewById<LinearLayout>(R.id.navProfile).setOnClickListener { replaceFragment(ProfileFragment()) }
        view.findViewById<LinearLayout>(R.id.navSearch).setOnClickListener { replaceFragment(SearchFragment()) }
    }

    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}
