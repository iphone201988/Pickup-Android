package com.pickup.sports.ui.home_modules.home_fragment

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pickup.sports.data.model.GetAllGameApiResponse
import com.pickup.sports.data.model.GetAllGames
import com.pickup.sports.databinding.ItemLayoutEventDateBinding
import com.pickup.sports.databinding.ItemLayoutEventsBinding
import com.pickup.sports.ui.home_modules.game_details.GameDetailsActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CustomAdapter(
    private val staticDates: MutableList<GetAllGames>,
    private val onDropdownClickListener: (String) -> Unit
) : RecyclerView.Adapter<CustomAdapter.GameViewHolder>() {

    // List of static dates, should be mutable to support dynamic updates
    var list: MutableList<GetAllGames> = staticDates


    // ViewHolder for each outer item (date + dropdown + inner list)
    inner class GameViewHolder(val binding: ItemLayoutEventDateBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GetAllGames,position: Int) {
            binding.tvDate.text = item.date
            binding.eventGame.visibility = if (item.isVisible) View.VISIBLE else View.GONE
            binding.dropDown.rotation = if (item.isVisible) 180f else 0f

            // Dropdown click logic
            binding.dropDown.setOnClickListener {
                item.isVisible = !item.isVisible
                  notifyDataSetChanged()
                Log.i("Dfdsfs", "bind: $item,")

                if (item.isVisible ) {

                    onDropdownClickListener(item.dateValue)
                }
            }

            // Set up inner RecyclerView only if visible
            if (item.isVisible && item.gameList.isNotEmpty()) {
                val pageMap = mutableMapOf<String, Int>()
                val isLoading = false
                Log.i("gamelistdata", "bind: ${item.gameList}")
                val innerAdapter = InnerGameAdapter(item.gameList)
                binding.eventGame.adapter = innerAdapter
                binding.eventGame.layoutManager = LinearLayoutManager(binding.root.context)

                innerAdapter.notifyDataSetChanged()
            }
        }
    }

    // Adapter for inner RecyclerView (list of games)
    inner class InnerGameAdapter(
        private val games: List<GetAllGameApiResponse.Game>
    ) : RecyclerView.Adapter<InnerGameAdapter.InnerGameViewHolder>() {

        inner class InnerGameViewHolder(val binding: ItemLayoutEventsBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(game: GetAllGameApiResponse.Game) {
                // Log data for debugging
                Log.i("data", "bind: $game")

                // Set the sport name
                binding.sportsName.text = game.sportsData?.name

                // Set the location (assuming the location is in the 'game' object)
                binding.location.text = (game.address ?: "Unknown Location").toString()

                // Set miles (assuming 'miles' is a field in your 'game' object)
             //   binding.miles.text = "${game.} miles"

                // Set the time (assuming 'hostTimestamp' is in your 'game' object)
                binding.time.text = game.hostTimestamp?.toLong()?.let { formatTime(it) }

                // Set the sport image (assuming the URL or resource is in 'game.photos[0]')
                // This assumes you are using Glide or similar library to load the image
                Glide.with(binding.root.context)
                    .load(game.photos?.get(0))  // Assuming 'photos' is a list of image URLs
                    .into(binding.ivSportImage)

                // Handle rating
                if (game.hostData?.rating != null) {
binding.rating.text = game.hostData?.rating.toString()
                } else {
                    binding.rating.text = "N/A"
                }

                // Rating count (assuming 'ratingsCount' is a field in 'game.hostData')
                binding.ratingCount.text = "(${game.hostData?.ratingsCount} Ratings)"

                // Set person name (assuming 'firstName' and 'lastName' are in 'game.hostData')
                binding.personName.text = "${game.hostData?.firstName} ${game.hostData?.lastName}"

                // Set the price (assuming 'price' is a field in 'game')
                binding.price.text = "$${game.price}"

                // Set the number of players (assuming 'slotIds' and 'maximumSlots' are in 'game')
                binding.players.text = "${game.slotIds?.size}/${game.maximumSlots}"

                // Set player icon slot count (assuming 'slotIds' is a list and 'minimumSlots' is a field)
                binding.ivPlayerIcon.setContentDescription("${game.slotIds?.size} slots available")

                // Handle click on the main container (e.g., to navigate to the game details)
                binding.consMain.setOnClickListener {
                    val context = binding.root.context
                    val intent = Intent(context, GameDetailsActivity::class.java).apply {
                        putExtra("id", game._id) // Send the game ID to the detail page
                    }
                    context.startActivity(intent)
                }

                // You can add more logic to handle the "dot" and other UI elements similarly.
            }

            // Format the time (you can use a proper date format depending on the timestamp format)
            private fun formatTime(timestamp: Long): String {
                // Implement your own time formatting logic based on the timestamp.
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                return sdf.format(Date(timestamp))
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerGameViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemLayoutEventsBinding.inflate(inflater, parent, false)
            return InnerGameViewHolder(binding)
        }

        override fun onBindViewHolder(holder: InnerGameViewHolder, position: Int) {
            holder.bind(games[position])
        }

        override fun getItemCount(): Int = games.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLayoutEventDateBinding.inflate(inflater, parent, false)
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(list[position],position)
    }

    override fun getItemCount(): Int = list.size

    // Renamed to avoid conflict with setter
    fun updateList(newList: List<GetAllGames>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
        Log.i("print", "updateList: $list ")
    }

    // Method to add new items to the existing list (for pagination)
    fun appendToList(newList: List<GetAllGames>) {
        val startPosition = list.size
        list.addAll(newList)
        notifyItemRangeInserted(startPosition, newList.size)
    }
}





