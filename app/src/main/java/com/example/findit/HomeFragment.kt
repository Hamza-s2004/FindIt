package com.example.findit

import Item
import ItemAdapter
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔔 Notification Bell
        val bell = view.findViewById<FrameLayout>(R.id.frameBell)
        bell.setOnClickListener {
            replaceFragment(NotificationsFragment())
        }

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerRecent)

        val list = listOf(
            Item("Lost Keys", "Cafeteria", "Today"),
            Item("Found ID Card", "Library", "Yesterday"),
            Item("Lost Phone", "Parking", "2 days ago"),
            Item("Found Bag", "Main Gate", "3 days ago")
        )

        val adapter = ItemAdapter(list) { item ->

            val bundle = Bundle()
            bundle.putString("title", item.title)
            bundle.putString("location", item.location)
            bundle.putString("time", item.time)

            val fragment = DetailFragment()
            fragment.arguments = bundle

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }

        recycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        recycler.adapter = adapter


        // 👇 Footer Navigation (from layout_footer.xml)
        val navProfile = view.findViewById<LinearLayout>(R.id.navProfile)
        val navPost = view.findViewById<LinearLayout>(R.id.navPost)
        val navSearch = view.findViewById<LinearLayout>(R.id.navSearch)

        navProfile.setOnClickListener {
            replaceFragment(ProfileFragment())
        }

        navPost.setOnClickListener {
            replaceFragment(PostFragment())
        }

        navSearch.setOnClickListener {
            replaceFragment(SearchFragment())
        }

        // (Optional) Filter Tabs
        val tvAll = view.findViewById<TextView>(R.id.tvAll)
        val tvLost = view.findViewById<TextView>(R.id.tvLost)
        val tvFound = view.findViewById<TextView>(R.id.tvFound)

        tvAll.setOnClickListener {
            // later: show all items
        }

        tvLost.setOnClickListener {
            // later: filter lost
        }

        tvFound.setOnClickListener {
            // later: filter found
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}