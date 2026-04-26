package com.example.findit.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.findit.R
import com.example.findit.data.model.Item
import com.example.findit.data.repository.ItemRepository
import kotlinx.coroutines.launch

/**
 * Read + entry point for Update / Delete (F3).
 */
class DetailFragment : Fragment(R.layout.fragment_detail) {

    companion object {
        const val ARG_ITEM_ID = "arg_item_id"
    }

    private lateinit var repo: ItemRepository
    private var item: Item? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = ItemRepository(requireContext())

        val itemId = arguments?.getLong(ARG_ITEM_ID, -1L) ?: -1L
        if (itemId <= 0L) {
            Toast.makeText(requireContext(), "Invalid item", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
            return
        }

        view.findViewById<Button>(R.id.btnBack).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        val btnEdit = view.findViewById<Button>(R.id.btnEdit)
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)

        btnEdit.setOnClickListener {
            val current = item ?: return@setOnClickListener
            val frag = EditItemFragment().apply {
                arguments = Bundle().apply { putLong(EditItemFragment.ARG_ITEM_ID, current.id) }
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, frag)
                .addToBackStack(null)
                .commit()
        }

        btnDelete.setOnClickListener {
            val current = item ?: return@setOnClickListener
            AlertDialog.Builder(requireContext())
                .setTitle("Delete item")
                .setMessage("Remove \"${current.title}\" permanently?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        val rows = repo.deleteItem(current.id)
                        if (rows > 0) {
                            Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
                            requireActivity().supportFragmentManager.popBackStack()
                        } else {
                            Toast.makeText(requireContext(), "Could not delete", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val loaded = repo.getItemById(itemId)
            if (loaded == null) {
                Toast.makeText(requireContext(), "Item not found", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
                return@launch
            }
            item = loaded
            bind(view, loaded)
        }
    }

    private fun bind(view: View, item: Item) {
        view.findViewById<TextView>(R.id.txtTitle).text = item.title
        view.findViewById<TextView>(R.id.txtType).text = item.type
        view.findViewById<TextView>(R.id.txtCategory).text = item.categoryName
        view.findViewById<TextView>(R.id.txtDescription).text =
            item.description.ifBlank { "(no description)" }
        view.findViewById<TextView>(R.id.txtLocation).text =
            item.location.ifBlank { "—" }
        view.findViewById<TextView>(R.id.txtTime).text =
            item.date.ifBlank { item.source }
        view.findViewById<TextView>(R.id.txtContact).text =
            item.contact.ifBlank { "—" }
        view.findViewById<TextView>(R.id.txtSource).text =
            "Source: ${item.source}"
    }
}
