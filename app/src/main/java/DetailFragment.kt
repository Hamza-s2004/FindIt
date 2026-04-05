package com.example.findit

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class DetailFragment : Fragment(R.layout.fragment_detail) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = view.findViewById<TextView>(R.id.txtTitle)
        val location = view.findViewById<TextView>(R.id.txtLocation)
        val time = view.findViewById<TextView>(R.id.txtTime)
        val btnBack = view.findViewById<Button>(R.id.btnBack)

        // Get data
        val bundle = arguments
        title.text = bundle?.getString("title")
        location.text = bundle?.getString("location")
        time.text = bundle?.getString("time")

        // Back button
        btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
}