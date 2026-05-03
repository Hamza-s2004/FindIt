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
import com.example.findit.data.repository.FirebaseRepository
import kotlinx.coroutines.launch

class PostFragment : Fragment(R.layout.fragment_post) {

    private val repo = FirebaseRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName = view.findViewById<EditText>(R.id.etItemName)
        val etDesc = view.findViewById<EditText>(R.id.etDescription)
        val rgType = view.findViewById<RadioGroup>(R.id.rgPostType)
        val rgCategory = view.findViewById<RadioGroup>(R.id.rgCategory)
        val etLocation = view.findViewById<EditText>(R.id.etLocation)
        val etDate = view.findViewById<EditText>(R.id.etDate)
        val etContact = view.findViewById<EditText>(R.id.etContact)
        val btnSubmit = view.findViewById<TextView>(R.id.btnSubmit)

        // 🔥 CHECK EDIT MODE
        val remoteId = arguments?.getString("remoteId")
        val isEdit = remoteId != null

        // 🔥 PREFILL (EDIT MODE)
        if (isEdit) {
            etName.setText(arguments?.getString("title"))
            etDesc.setText(arguments?.getString("description"))
            etLocation.setText(arguments?.getString("location"))
            etDate.setText(arguments?.getString("date"))
            etContact.setText(arguments?.getString("contact"))

            if (arguments?.getString("type") == Item.TYPE_FOUND) {
                rgType.check(R.id.rbFound)
            } else {
                rgType.check(R.id.rbLost)
            }

            btnSubmit.text = "Update"
        }

        btnSubmit.setOnClickListener {

            val title = etName.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Item name required", Toast.LENGTH_SHORT).show()
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

            val item = Item(
                title = title,
                description = etDesc.text.toString().trim(),
                type = type,
                categoryId = 0L,
                categoryName = categoryName,
                location = etLocation.text.toString().trim(),
                date = etDate.text.toString().trim(),
                contact = etContact.text.toString().trim(),
                source = Item.SOURCE_LOCAL
            )

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    if (isEdit) {
                        // 🔥 UPDATE
                        repo.deleteItem(remoteId!!)
                        repo.addItem(item)
                        Toast.makeText(requireContext(), "Updated!", Toast.LENGTH_SHORT).show()
                    } else {
                        // 🔥 ADD
                        repo.addItem(item)
                        Toast.makeText(requireContext(), "Posted!", Toast.LENGTH_SHORT).show()
                    }

                    requireActivity().supportFragmentManager.popBackStack()

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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