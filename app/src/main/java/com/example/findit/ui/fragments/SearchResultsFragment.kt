package com.example.findit.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.findit.R
import com.example.findit.data.model.Item
import com.example.findit.data.repository.FirebaseRepository
import com.example.findit.ui.adapters.ItemAdapter

class SearchResultsFragment : Fragment(R.layout.fragment_search_results) {

    companion object {
        const val ARG_QUERY = "arg_query"
        const val ARG_TYPE = "arg_type"
    }

    private val repo = FirebaseRepository()
    private lateinit var adapter: ItemAdapter

    private var currentSort = Item.SORT_NEWEST   // 🔥 NEW

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ItemAdapter(
            layoutResId = R.layout.item_row_full,
            onClick = { item ->
                val frag = DetailFragment().apply {
                    arguments = Bundle().apply {
                        putString("title", item.title)
                        putString("type", item.type)
                        putString("category", item.categoryName)
                        putString("description", item.description)
                        putString("location", item.location)
                        putString("date", item.date)
                        putString("contact", item.contact)
                        putString("source", item.source)
                    }
                }

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, frag)
                    .addToBackStack(null)
                    .commit()
            }
        )

        view.findViewById<RecyclerView>(R.id.recyclerResults).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SearchResultsFragment.adapter
        }

        view.findViewById<Button>(R.id.btnBackResults).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // 🔥 SORT BUTTON
        val btnSort = view.findViewById<Button>(R.id.btnSort)

        btnSort.setOnClickListener {

            currentSort = when (currentSort) {
                Item.SORT_NEWEST -> Item.SORT_OLDEST
                Item.SORT_OLDEST -> Item.SORT_TITLE_ASC
                Item.SORT_TITLE_ASC -> Item.SORT_TITLE_DESC
                else -> Item.SORT_NEWEST
            }

            btnSort.text = when (currentSort) {
                Item.SORT_OLDEST -> "Sort: Oldest"
                Item.SORT_TITLE_ASC -> "Sort: A-Z"
                Item.SORT_TITLE_DESC -> "Sort: Z-A"
                else -> "Sort: Newest"
            }

            runQuery(view)   // 🔥 re-run
        }

        view.findViewById<LinearLayout>(R.id.navHome).setOnClickListener { replaceFragment(HomeFragment()) }
        view.findViewById<LinearLayout>(R.id.navPost).setOnClickListener { replaceFragment(PostFragment()) }
        view.findViewById<LinearLayout>(R.id.navSearch).setOnClickListener { replaceFragment(SearchFragment()) }
        view.findViewById<LinearLayout>(R.id.navProfile).setOnClickListener { replaceFragment(ProfileFragment()) }

        runQuery(view)
    }

    private fun runQuery(view: View) {
        val args = arguments ?: Bundle()
        val query = args.getString(ARG_QUERY)?.lowercase() ?: ""
        val type = args.getString(ARG_TYPE) ?: Item.TYPE_ALL
        val category = args.getString("category") ?: ""

        repo.listenItems { items ->

            val filtered = items.filter { item ->
                val matchesType = (type == Item.TYPE_ALL || item.type == type)

                val matchesQuery =
                    query.isBlank() ||
                            item.title.lowercase().contains(query) ||
                            item.description.lowercase().contains(query)

                val matchesCategory =
                    category.isBlank() ||
                            item.categoryName == category

                matchesType && matchesQuery && matchesCategory
            }

            // 🔥 SORT LOGIC
            val sorted = when (currentSort) {

                Item.SORT_OLDEST ->
                    filtered.sortedBy { it.createdAt }

                Item.SORT_TITLE_ASC ->
                    filtered.sortedBy { it.title }

                Item.SORT_TITLE_DESC ->
                    filtered.sortedByDescending { it.title }

                else ->
                    filtered.sortedByDescending { it.createdAt } // newest
            }

            requireActivity().runOnUiThread {

                adapter.submit(sorted)

                val txtEmpty = view.findViewById<TextView>(R.id.txtEmpty)
                val txtSummary = view.findViewById<TextView>(R.id.txtSummary)

                txtEmpty.visibility =
                    if (sorted.isEmpty()) View.VISIBLE else View.GONE

                txtSummary.text = "${sorted.size} result(s)"
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