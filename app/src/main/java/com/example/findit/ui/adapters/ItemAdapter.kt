package com.example.findit.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.example.findit.R
import com.example.findit.data.model.Item

/**
 * RecyclerView adapter shared by:
 *   - HomeFragment "Recent Posts"   (item_row.xml — compact horizontal card)
 *   - HomeFragment "All Items"      (item_row_full.xml — full-width row)
 *   - SearchResultsFragment         (item_row_full.xml)
 *
 * Pass the layout you want at construction time; the adapter only requires
 * txtTitle / txtLocation / txtTime to exist (extras are bound when present).
 */
class ItemAdapter(
    private val items: MutableList<Item> = mutableListOf(),
    @LayoutRes private val layoutResId: Int = R.layout.item_row,
    private val onClick: (Item) -> Unit,
    private val onLongClick: ((Item) -> Boolean)? = null
) : RecyclerView.Adapter<ItemAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.txtTitle)
        val location: TextView = view.findViewById(R.id.txtLocation)
        val time: TextView = view.findViewById(R.id.txtTime)
        val type: TextView? = view.findViewById(R.id.txtType)
        val description: TextView? = view.findViewById(R.id.txtDescription)
        val category: TextView? = view.findViewById(R.id.txtCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.title.text = item.title
        holder.location.text = if (item.location.isBlank()) "—" else item.location
        holder.time.text = item.date.ifBlank { item.source }

        holder.type?.text = item.type
        holder.description?.text = item.description.ifBlank { "(no description)" }
        holder.category?.text =
            if (item.categoryName.isNotBlank()) item.categoryName else "Uncategorised"

        holder.itemView.setOnClickListener { onClick(item) }
        holder.itemView.setOnLongClickListener { onLongClick?.invoke(item) ?: false }
    }

    override fun getItemCount(): Int = items.size

    /** Replace the whole list (called after each query / refresh). */
    fun submit(newItems: List<Item>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
