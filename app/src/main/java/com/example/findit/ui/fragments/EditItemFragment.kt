package com.example.findit.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
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
 * F3 — Update.
 *
 * Reuses the same form layout as Post but pre-fills it from the row stored in
 * SQLite, and writes back via Repository.updateItem(). Designed so the user
 * can edit either local items or cached API rows.
 */
class EditItemFragment : Fragment(R.layout.fragment_edit_item) {

    companion object {
        const val ARG_ITEM_ID = "arg_item_id"
    }

    private lateinit var repo: ItemRepository
    private var original: Item? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = ItemRepository(requireContext())

        val itemId = arguments?.getLong(ARG_ITEM_ID, -1L) ?: -1L
        if (itemId <= 0L) {
            Toast.makeText(requireContext(), "Invalid item", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
            return
        }

        val etName = view.findViewById<EditText>(R.id.etItemName)
        val etDesc = view.findViewById<EditText>(R.id.etDescription)
        val rgType = view.findViewById<RadioGroup>(R.id.rgPostType)
        val rgCategory = view.findViewById<RadioGroup>(R.id.rgCategory)
        val etLocation = view.findViewById<EditText>(R.id.etLocation)
        val etDate = view.findViewById<EditText>(R.id.etDate)
        val etContact = view.findViewById<EditText>(R.id.etContact)

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        view.findViewById<TextView>(R.id.btnSave).setOnClickListener {
            val current = original ?: return@setOnClickListener
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
                val cat = repo.getCategoryByName(categoryName) ?: run {
                    Toast.makeText(requireContext(), "Category not found", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val updated = current.copy(
                    title = title,
                    description = etDesc.text.toString().trim(),
                    type = type,
                    categoryId = cat.id,
                    categoryName = cat.name,
                    location = etLocation.text.toString().trim(),
                    date = etDate.text.toString().trim(),
                    contact = etContact.text.toString().trim()
                )
                val rows = repo.updateItem(updated)
                if (rows > 0) {
                    Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val loaded = repo.getItemById(itemId)
            if (loaded == null) {
                Toast.makeText(requireContext(), "Item not found", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
                return@launch
            }
            original = loaded

            etName.setText(loaded.title)
            etDesc.setText(loaded.description)
            etLocation.setText(loaded.location)
            etDate.setText(loaded.date)
            etContact.setText(loaded.contact)

            val typeRb = if (loaded.type == Item.TYPE_FOUND) R.id.rbFound else R.id.rbLost
            view.findViewById<RadioButton>(typeRb).isChecked = true

            val catRb = when (loaded.categoryName) {
                "Wallet" -> R.id.rbWallet
                "Electronics" -> R.id.rbElectronics
                "Books" -> R.id.rbBooks
                "Bags" -> R.id.rbBags
                "ID Card" -> R.id.rbIdCard
                else -> R.id.rbOthers
            }
            view.findViewById<RadioButton>(catRb).isChecked = true
        }
    }
}
