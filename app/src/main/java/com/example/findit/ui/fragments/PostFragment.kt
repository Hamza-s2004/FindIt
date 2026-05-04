package com.example.findit.ui.fragments

import android.app.AlertDialog
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
import kotlinx.coroutines.delay
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
                        requireActivity().supportFragmentManager.popBackStack()
                    } else {
                        // 🔥 ADD
                        repo.addItem(item)
                        Toast.makeText(requireContext(), "Posted!", Toast.LENGTH_SHORT).show()

                        // ✅ SMART MATCH RECOMMENDATION (only for new items, not edits)
                        // Do NOT pop back immediately - let checkForMatches handle it
                        checkForMatches(item)
                    }


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

    // ✅ SMART MATCH RECOMMENDATION: Check for similar items
    private fun checkForMatches(newItem: Item) {
        android.util.Log.d("SMART_MATCH", ">>> checkForMatches called for: ${newItem.title}")

        // Use coroutine with delay to ensure item is written to Firebase
        viewLifecycleOwner.lifecycleScope.launch {
            delay(800) // Wait 800ms for Firebase to write the item

            repo.listenItems { existingItems ->
                android.util.Log.d("SMART_MATCH", ">>> Callback fired with ${existingItems.size} items")

                // Safety check: only show dialog if fragment is still attached
                if (!isAdded) {
                    android.util.Log.d("SMART_MATCH", "Fragment not attached, skipping")
                    return@listenItems
                }

                // Filter out the newly posted item and check for matches
                val otherItems = existingItems.filter { it.title != newItem.title }
                android.util.Log.d("SMART_MATCH", "Other items (excluding exact title match): ${otherItems.size}")

                val matches = otherItems.filter { isMatch(newItem, it) }

                android.util.Log.d("SMART_MATCH", "Posted: ${newItem.title} (${newItem.type}/${newItem.categoryName})")
                android.util.Log.d("SMART_MATCH", "Matches found: ${matches.size}")
                matches.forEach {
                    android.util.Log.d("SMART_MATCH", "  ✓ Match: ${it.title} (${it.type}/${it.categoryName})")
                }

                var dialogShown = false
                if (matches.isNotEmpty()) {
                    try {
                        val matchTitles = matches.take(2).map { "• ${it.title}" }.joinToString("\n")
                        val message = if (matches.size > 1) {
                            "We found ${matches.size} similar items!\n\n$matchTitles"
                        } else {
                            "We found a similar item!\n\n$matchTitles"
                        }

                        android.util.Log.d("SMART_MATCH", "Showing dialog with: $matchTitles")
                        // Get the first matched item to show on View click
                        val firstMatch = matches.firstOrNull()
                        
                        AlertDialog.Builder(requireContext())
                            .setTitle("🎯 Smart Match Found!")
                            .setMessage(message)
                            .setPositiveButton("View") { _, _ ->
                                // Navigate to detail of first matched item
                                if (firstMatch != null) {
                                    val detailFrag = DetailFragment().apply {
                                        arguments = Bundle().apply {
                                            putString("remoteId", firstMatch.remoteId)
                                            putString("title", firstMatch.title)
                                            putString("type", firstMatch.type)
                                            putString("category", firstMatch.categoryName)
                                            putString("description", firstMatch.description)
                                            putString("location", firstMatch.location)
                                            putString("date", firstMatch.date)
                                            putString("contact", firstMatch.contact)
                                            putString("source", firstMatch.source)
                                        }
                                    }
                                    requireActivity().supportFragmentManager.beginTransaction()
                                        .replace(R.id.fragmentContainer, detailFrag)
                                        .addToBackStack(null)
                                        .commit()
                                    android.util.Log.d("SMART_MATCH", "Navigated to matched item: ${firstMatch.title}")
                                }
                            }
                            .setNegativeButton("Dismiss") { _, _ ->
                                // Pop back after dismissing
                                requireActivity().supportFragmentManager.popBackStack()
                            }
                            .setOnCancelListener {
                                // Pop back if dialog is cancelled
                                requireActivity().supportFragmentManager.popBackStack()
                            }
                            .show()
                        android.util.Log.d("SMART_MATCH", "Dialog shown!")
                        dialogShown = true
                    } catch (e: Exception) {
                        android.util.Log.e("SMART_MATCH", "Error showing dialog: ${e.message}", e)
                    }
                } else {
                    android.util.Log.d("SMART_MATCH", "No matches found - will not show dialog")
                }

                // If no dialog was shown, pop back immediately
                if (!dialogShown) {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    // ✅ Check if new item matches an existing item
    private fun isMatch(newItem: Item, existingItem: Item): Boolean {
        android.util.Log.d("MATCH_DEBUG", "=== Comparing ===")
        android.util.Log.d("MATCH_DEBUG", "New: ${newItem.title} | Type: ${newItem.type} | Cat: ${newItem.categoryName}")
        android.util.Log.d("MATCH_DEBUG", "Existing: ${existingItem.title} | Type: ${existingItem.type} | Cat: ${existingItem.categoryName}")

        // Type must be opposite (Lost vs Found)
        val oppositeType = when {
            newItem.type == Item.TYPE_LOST && existingItem.type == Item.TYPE_FOUND -> true
            newItem.type == Item.TYPE_FOUND && existingItem.type == Item.TYPE_LOST -> true
            else -> false
        }
        android.util.Log.d("MATCH_DEBUG", "Opposite type? $oppositeType (${newItem.type} vs ${existingItem.type})")
        if (!oppositeType) return false

        // Category must match (case-insensitive)
        val sameCategoryName = newItem.categoryName.equals(existingItem.categoryName, ignoreCase = true)
        android.util.Log.d("MATCH_DEBUG", "Same category? $sameCategoryName (${newItem.categoryName} vs ${existingItem.categoryName})")
        if (!sameCategoryName) return false

        // Title or description must contain similar keywords
        val titleMatch = containsSimilarKeywords(newItem.title, existingItem.title)
        android.util.Log.d("MATCH_DEBUG", "Title keywords match? $titleMatch")

        val descMatch = containsSimilarKeywords(newItem.description, existingItem.description)
        android.util.Log.d("MATCH_DEBUG", "Description keywords match? $descMatch")

        val crossMatch = containsSimilarKeywords(newItem.title, existingItem.description) ||
                         containsSimilarKeywords(newItem.description, existingItem.title)
        android.util.Log.d("MATCH_DEBUG", "Cross-match (title-desc)? $crossMatch")

        val result = titleMatch || descMatch || crossMatch
        android.util.Log.d("MATCH_DEBUG", "RESULT: IS MATCH? $result")

        return result
    }

    // ✅ Check if two text strings contain similar keywords
    private fun containsSimilarKeywords(text1: String, text2: String): Boolean {
        if (text1.isBlank() || text2.isBlank()) return false

        // Extract keywords (words longer than 2 chars for more lenient matching)
        val keywords1 = text1.lowercase()
            .split(Regex("\\s+|[^a-z0-9]"))
            .filter { it.length > 2 }
            .toSet()
        val keywords2 = text2.lowercase()
            .split(Regex("\\s+|[^a-z0-9]"))
            .filter { it.length > 2 }
            .toSet()

        android.util.Log.d("KEYWORD_MATCH", "Text1 keywords: $keywords1")
        android.util.Log.d("KEYWORD_MATCH", "Text2 keywords: $keywords2")

        // Check for intersection
        val intersection = keywords1.intersect(keywords2)
        android.util.Log.d("KEYWORD_MATCH", "Intersection: $intersection")

        return intersection.isNotEmpty()
    }
}