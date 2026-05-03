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
import com.example.findit.data.repository.FirebaseRepository
import kotlinx.coroutines.launch

class DetailFragment : Fragment(R.layout.fragment_detail) {

    private var item: Item? = null
    private val repo = FirebaseRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ BACK
        view.findViewById<Button>(R.id.btnBack).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        val remoteId = arguments?.getString("remoteId")

        // 🔥 GET DATA
        item = Item(
            id = System.currentTimeMillis(),
            title = arguments?.getString("title") ?: "",
            type = arguments?.getString("type") ?: "",
            categoryName = arguments?.getString("category") ?: "",
            description = arguments?.getString("description") ?: "",
            location = arguments?.getString("location") ?: "",
            date = arguments?.getString("date") ?: "",
            contact = arguments?.getString("contact") ?: "",
            source = arguments?.getString("source") ?: ""
        )

        bind(view, item!!)

        // ✅ EDIT (NOW WORKING)
        view.findViewById<Button>(R.id.btnEdit).setOnClickListener {

            val frag = PostFragment().apply {
                arguments = Bundle().apply {
                    putString("remoteId", remoteId)

                    putString("title", item?.title)
                    putString("description", item?.description)
                    putString("type", item?.type)
                    putString("category", item?.categoryName)
                    putString("location", item?.location)
                    putString("date", item?.date)
                    putString("contact", item?.contact)
                }
            }

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, frag)
                .addToBackStack(null)
                .commit()
        }

        // ✅ DELETE (REAL FIREBASE)
        view.findViewById<Button>(R.id.btnDelete).setOnClickListener {

            if (remoteId == null) {
                Toast.makeText(requireContext(), "Cannot delete item", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Delete Item")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete") { _, _ ->

                    lifecycleScope.launch {
                        repo.deleteItem(remoteId)

                        Toast.makeText(requireContext(), "Deleted successfully", Toast.LENGTH_SHORT).show()

                        requireActivity().supportFragmentManager.popBackStack()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
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