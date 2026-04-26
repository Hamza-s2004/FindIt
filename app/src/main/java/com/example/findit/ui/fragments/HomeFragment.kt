package com.example.findit.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
 * F4 strategy = Option A (offline-first):
 *   1. Read from SQLite immediately so the UI is never blank.
 *   2. Fire syncApiToDb() in the background; reload when it returns.
 *
 * F3 (Read) and F5 (filter by type) are wired through the All/Lost/Found tabs.
 */
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var repo: ItemRepository
    private lateinit var recentAdapter: ItemAdapter
    private lateinit var allAdapter: ItemAdapter

    private var currentType: String = Item.TYPE_ALL

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = ItemRepository(requireContext())

        // ---- Header bell -> notifications -------------------------------
        view.findViewById<FrameLayout>(R.id.frameBell).setOnClickListener {
            replaceFragment(NotificationsFragment())
        }

        // ---- Recent (horizontal) ---------------------------------------
        val recyclerRecent = view.findViewById<RecyclerView>(R.id.recyclerRecent)
        recentAdapter = ItemAdapter(
            layoutResId = R.layout.item_row,
            onClick = { openDetail(it.id) }
        )
        recyclerRecent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerRecent.adapter = recentAdapter

        // ---- All items (vertical) --------------------------------------
        val recyclerAll = view.findViewById<RecyclerView>(R.id.recyclerAll)
        allAdapter = ItemAdapter(
            layoutResId = R.layout.item_row_full,
            onClick = { openDetail(it.id) }
        )
        recyclerAll.layoutManager = LinearLayoutManager(requireContext())
        recyclerAll.isNestedScrollingEnabled = false
        recyclerAll.adapter = allAdapter

        // ---- Filter tabs (F5: filter by type) --------------------------
        val tvAll = view.findViewById<TextView>(R.id.tvAll)
        val tvLost = view.findViewById<TextView>(R.id.tvLost)
        val tvFound = view.findViewById<TextView>(R.id.tvFound)
        val tabs = listOf(
            tvAll to Item.TYPE_ALL,
            tvLost to Item.TYPE_LOST,
            tvFound to Item.TYPE_FOUND
        )
        tabs.forEach { (tab, type) ->
            tab.setOnClickListener {
                currentType = type
                tabs.forEach { (t, _) ->
                    t.setBackgroundColor(if (t === tab) 0xFF1565C0.toInt() else 0x00000000)
                    t.setTextColor(if (t === tab) 0xFFFFFFFF.toInt() else 0xFF212121.toInt())
                }
                reloadAllItems()
            }
        }

        // ---- Footer navigation -----------------------------------------
        view.findViewById<LinearLayout>(R.id.navProfile).setOnClickListener { replaceFragment(ProfileFragment()) }
        view.findViewById<LinearLayout>(R.id.navPost).setOnClickListener { replaceFragment(PostFragment()) }
        view.findViewById<LinearLayout>(R.id.navSearch).setOnClickListener { replaceFragment(SearchFragment()) }

        // ---- F4 Option A: load DB immediately, then refresh from API ----
        loadFromDb()
        refreshFromApi(view.findViewById(R.id.progressSync))
    }

    override fun onResume() {
        super.onResume()
        // Coming back from Post/Edit/Delete should reflect the new DB state.
        loadFromDb()
    }

    private fun loadFromDb() {
        viewLifecycleOwner.lifecycleScope.launch {
            recentAdapter.submit(repo.getAllItems().take(10))
            reloadAllItems()
        }
    }

    private fun reloadAllItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            allAdapter.submit(repo.getItemsByType(currentType))
        }
    }

    private fun refreshFromApi(progress: ProgressBar?) {
        progress?.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                repo.syncApiToDb(limit = 15)
                loadFromDb()
            } catch (t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Could not refresh from server: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                progress?.visibility = View.GONE
            }
        }
    }

    private fun openDetail(itemId: Long) {
        val frag = DetailFragment().apply {
            arguments = Bundle().apply { putLong(DetailFragment.ARG_ITEM_ID, itemId) }
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
