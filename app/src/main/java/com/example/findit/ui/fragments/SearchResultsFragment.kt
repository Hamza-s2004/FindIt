package com.example.findit.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.findit.R
import com.example.findit.data.model.Item
import com.example.findit.data.repository.ItemRepository
import com.example.findit.ui.adapters.ItemAdapter
import kotlinx.coroutines.launch

/**
 * F5 — runs Repository.searchItems() with whatever filters were collected
 * by SearchFragment, and binds the result to a vertical RecyclerView.
 *
 * Sort order can be toggled with the inline sort button.
 */
class SearchResultsFragment : Fragment(R.layout.fragment_search_results) {

    companion object {
        const val ARG_QUERY = "arg_query"
        const val ARG_TYPE = "arg_type"
        const val ARG_CATEGORY_ID = "arg_category_id"
        const val ARG_FROM = "arg_from"
        const val ARG_TO = "arg_to"
    }

    private lateinit var repo: ItemRepository
    private lateinit var adapter: ItemAdapter
    private var sort = Item.SORT_NEWEST

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = ItemRepository(requireContext())

        adapter = ItemAdapter(
            layoutResId = R.layout.item_row_full,
            onClick = { item ->
                val frag = DetailFragment().apply {
                    arguments = Bundle().apply { putLong(DetailFragment.ARG_ITEM_ID, item.id) }
                }
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, frag)
                    .addToBackStack(null)
                    .commit()
            }
        )
        view.findViewById<RecyclerView>(R.id.recyclerResults).apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@SearchResultsFragment.adapter
        }

        view.findViewById<Button>(R.id.btnSort).setOnClickListener {
            sort = when (sort) {
                Item.SORT_NEWEST -> Item.SORT_OLDEST
                Item.SORT_OLDEST -> Item.SORT_TITLE_ASC
                Item.SORT_TITLE_ASC -> Item.SORT_TITLE_DESC
                else -> Item.SORT_NEWEST
            }
            (it as Button).text = "Sort: ${sortLabel()}"
            runQuery(view)
        }

        view.findViewById<Button>(R.id.btnBackResults).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Footer
        view.findViewById<LinearLayout>(R.id.navHome).setOnClickListener { replaceFragment(HomeFragment()) }
        view.findViewById<LinearLayout>(R.id.navPost).setOnClickListener { replaceFragment(PostFragment()) }
        view.findViewById<LinearLayout>(R.id.navSearch).setOnClickListener { replaceFragment(SearchFragment()) }
        view.findViewById<LinearLayout>(R.id.navProfile).setOnClickListener { replaceFragment(ProfileFragment()) }

        runQuery(view)
    }

    private fun runQuery(view: View) {
        val args = arguments ?: Bundle()
        val q = args.getString(ARG_QUERY).orEmpty()
        val type = args.getString(ARG_TYPE) ?: Item.TYPE_ALL
        val catId = if (args.containsKey(ARG_CATEGORY_ID)) args.getLong(ARG_CATEGORY_ID) else null
        val from = args.getString(ARG_FROM)
        val to = args.getString(ARG_TO)

        viewLifecycleOwner.lifecycleScope.launch {
            val results = repo.searchItems(
                query = q,
                type = type,
                categoryId = catId,
                fromDate = from,
                toDate = to,
                sort = sort
            )
            adapter.submit(results)
            view.findViewById<TextView>(R.id.txtEmpty).visibility =
                if (results.isEmpty()) View.VISIBLE else View.GONE
            view.findViewById<TextView>(R.id.txtSummary).text =
                "${results.size} result(s) — type=$type" +
                        (q.takeIf { it.isNotBlank() }?.let { " · q=\"$it\"" } ?: "")
        }
    }

    private fun sortLabel(): String = when (sort) {
        Item.SORT_OLDEST -> "Oldest"
        Item.SORT_TITLE_ASC -> "A-Z"
        Item.SORT_TITLE_DESC -> "Z-A"
        else -> "Newest"
    }

    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}
