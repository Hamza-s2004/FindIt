package com.example.findit.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.findit.R
import com.example.findit.data.model.Item
import com.example.findit.data.repository.FirebaseRepository
import com.example.findit.ui.adapters.ItemAdapter

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val repo = FirebaseRepository()

    private lateinit var recentAdapter: ItemAdapter
    private lateinit var allAdapter: ItemAdapter

    private var currentType: String = Item.TYPE_ALL
    private var allItems: List<Item> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<FrameLayout>(R.id.frameBell).setOnClickListener {
            replaceFragment(NotificationsFragment())
        }

        // Recent
        val recyclerRecent = view.findViewById<RecyclerView>(R.id.recyclerRecent)
        recentAdapter = ItemAdapter(
            layoutResId = R.layout.item_row,
            onClick = { openDetail(it) }
        )
        recyclerRecent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerRecent.adapter = recentAdapter

        // All
        val recyclerAll = view.findViewById<RecyclerView>(R.id.recyclerAll)
        allAdapter = ItemAdapter(
            layoutResId = R.layout.item_row_full,
            onClick = { openDetail(it) }
        )
        recyclerAll.layoutManager = LinearLayoutManager(requireContext())
        recyclerAll.adapter = allAdapter

        val tvAll = view.findViewById<TextView>(R.id.tvAll)
        val tvLost = view.findViewById<TextView>(R.id.tvLost)
        val tvFound = view.findViewById<TextView>(R.id.tvFound)

        fun updateUI() {
            tvAll.setBackgroundResource(if (currentType == Item.TYPE_ALL) R.drawable.bg_selected else 0)
            tvLost.setBackgroundResource(if (currentType == Item.TYPE_LOST) R.drawable.bg_selected else 0)
            tvFound.setBackgroundResource(if (currentType == Item.TYPE_FOUND) R.drawable.bg_selected else 0)

            val filtered = when (currentType) {
                Item.TYPE_LOST -> allItems.filter { it.type == Item.TYPE_LOST }
                Item.TYPE_FOUND -> allItems.filter { it.type == Item.TYPE_FOUND }
                else -> allItems
            }

            recentAdapter.submit(allItems.take(10))
            allAdapter.submit(filtered)
        }

        tvAll.setOnClickListener {
            currentType = Item.TYPE_ALL
            updateUI()
        }

        tvLost.setOnClickListener {
            currentType = Item.TYPE_LOST
            updateUI()
        }

        tvFound.setOnClickListener {
            currentType = Item.TYPE_FOUND
            updateUI()
        }

        repo.listenItems { items ->
            allItems = items
            updateUI()
        }

        view.findViewById<LinearLayout>(R.id.navProfile).setOnClickListener { replaceFragment(ProfileFragment()) }
        view.findViewById<LinearLayout>(R.id.navPost).setOnClickListener { replaceFragment(PostFragment()) }
        view.findViewById<LinearLayout>(R.id.navSearch).setOnClickListener { replaceFragment(SearchFragment()) }
    }

    // ✅ FIXED: now passing remoteId
    private fun openDetail(item: Item) {
        val frag = DetailFragment().apply {
            arguments = Bundle().apply {

                putString("remoteId", item.remoteId) // 🔥 FIX

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
        replaceFragment(frag)
    }

    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}