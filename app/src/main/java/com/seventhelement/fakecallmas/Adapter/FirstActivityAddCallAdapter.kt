// FirstActivityAddCallAdapter.kt
package com.seventhelement.fakecallmas.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.seventhelement.fakecallmas.Database.CallEntity
import com.seventhelement.fakecallmas.R

class FirstActivityAddCallAdapter(
    private val contacts: List<CallEntity>,
    private val listener: OnItemClickListener,
    private var selectedPosition: Int
) : RecyclerView.Adapter<FirstActivityAddCallAdapter.ContactViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int, name: String, phoneNumber: String)
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        val ll: LinearLayout = itemView.findViewById(R.id.ll)
        val name: TextView = itemView.findViewById(R.id.name)
        val phoneNumber: TextView = itemView.findViewById(R.id.phone_number)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.name.text = contact.name
        holder.phoneNumber.text = contact.number.toString()

        // Change background color when item is selected
        if (position == selectedPosition) {
            holder.ll.setBackgroundResource(R.drawable.rounded_corner_background_green)
        } else {
            holder.ll.setBackgroundResource(R.drawable.rounded_corner_background_white)
        }

        // Handle item click
        holder.itemView.setOnClickListener {
            // Update the selected position
            val previousSelectedPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            // Notify item changes to update UI
            notifyItemChanged(previousSelectedPosition)
            notifyItemChanged(selectedPosition)
            // Call the listener to pass the item position to the activity
            listener.onItemClick(selectedPosition, contact.name, contact.number.toString())
        }
    }

    override fun getItemCount() = contacts.size
}