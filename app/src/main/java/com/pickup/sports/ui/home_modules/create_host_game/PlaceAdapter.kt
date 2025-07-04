package com.pickup.sports.ui.home_modules.create_host_game

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pickup.sports.R
import com.pickup.sports.data.model.PlaceDetails

class PlaceAdapter(private var places: List<PlaceDetails>,   private val onPlaceClicked: (PlaceDetails) -> Unit) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout_filter, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        val combinedNameAndAddress = "${place.name}, ${place.address}".trim(',').trim()

        holder.placeName.text = combinedNameAndAddress
        holder.itemView.setOnClickListener {
            onPlaceClicked(place)
        }
    }

    override fun getItemCount() = places.size

    fun updatePlaces(newPlaces: List<PlaceDetails>) {
        places = newPlaces
        notifyDataSetChanged()
    }

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeName: TextView = itemView.findViewById(R.id.tvTitle)
    }
}