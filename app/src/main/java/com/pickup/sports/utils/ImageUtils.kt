package com.pickup.sports.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.BindingAdapter
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pickup.sports.BR
import com.pickup.sports.R
import com.pickup.sports.data.model.GetAllGameApiResponse
import com.pickup.sports.data.model.GetAllGames
import com.pickup.sports.data.model.GetHostGames
import com.pickup.sports.data.model.GetHostGamesApiResponse
import com.pickup.sports.data.model.HostedGame
import com.pickup.sports.databinding.ItemLayoutGameDetailsBinding

import com.pickup.sports.ui.base.SimpleRecyclerViewAdapter
import com.pickup.sports.ui.home_modules.game_details.GameDetailsActivity
import com.pickup.sports.ui.host_game_details.HostGameDetailsActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.pickup.sports.data.model.GetAllChatApiResponse
import com.pickup.sports.data.model.GetMyGames
import com.pickup.sports.data.model.GetMyGamesApiResponse
import com.pickup.sports.data.model.GetProfileApiResponse
import com.pickup.sports.databinding.ItemLayoutHostGameDataBinding
import com.pickup.sports.databinding.ItemLayoutMyGamesDataBinding
import com.pickup.sports.utils.event.SingleRequestEvent
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object ImageUtils {

    var totalGames = 0
    var side = ""
    var rating = SingleRequestEvent<GetMyGamesApiResponse.Game>()
    var userId : String ? = null
    fun navigateWithSlideAnimations(navController: NavController, destinationId: Int) {
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right) // Define enter animation
            .setExitAnim(R.anim.slide_out_left) // Define exit animation
            .setPopEnterAnim(R.anim.slide_in_left) // Define pop enter animation
            .setPopExitAnim(R.anim.slide_out_right) // Define pop exit animation
            .build()

        navController.navigate(destinationId, null, navOptions)
    }
    fun getStatusBarColor(activity: Activity, intColor: Int) {
        activity.window.decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        activity.window.statusBarColor = ContextCompat.getColor(activity, intColor)



    }
    @BindingAdapter("setIcon")
    @JvmStatic
    fun setIcon(image: ImageView, i: Int) {
        image.setImageResource(i)
    }

    fun goActivity(context: Context, activity: Activity) {
        val intent = Intent(context, activity::class.java)
        startActivity(context, intent, null)


    }


    @BindingAdapter("shapeImage")
    @JvmStatic
    fun shapeImage(imageView: ShapeableImageView, imgUrl: String?) {
        if (!imgUrl.isNullOrEmpty()) {
            try {
                val url = URL(imgUrl)
                Glide.with(imageView.context).load(url.toString())
                    .placeholder(R.drawable.iv_place_holder).into(imageView)
            } catch (e: MalformedURLException) {
                imageView.setImageResource(R.drawable.iv_place_holder) // Set placeholder if URL is malformed
            }
        } else {
            imageView.setImageResource(R.drawable.iv_place_holder) // Set placeholder if imgUrl is null or empty
        }
    }

    @BindingAdapter("setImages")
    @JvmStatic
    fun setImages(imageView: ImageView, imgUrl: String?) {
        if (!imgUrl.isNullOrEmpty()) {
            try {
                val url = URL(imgUrl)
                Glide.with(imageView.context).load(url.toString())
                    .placeholder(R.drawable.iv_place_holder).into(imageView)
            } catch (e: MalformedURLException) {
                imageView.setImageResource(R.drawable.iv_place_holder) // Set placeholder if URL is malformed
            }
        } else {
            imageView.setImageResource(R.drawable.iv_place_holder) // Set placeholder if imgUrl is null or empty
        }
    }
    @BindingAdapter("childAdapter")
    @JvmStatic
    fun childAdapter(view : RecyclerView , myGames : List<HostedGame?>?){

        // Create and set a LayoutManager for the inner RecyclerView
        val layoutManager = LinearLayoutManager(view.context)
        view.layoutManager = layoutManager
        val context = view.context
        val gamesAdapter = SimpleRecyclerViewAdapter<HostedGame,ItemLayoutGameDetailsBinding>(R.layout.item_layout_game_details,BR.bean){v, m,pos ->
            when(v.id){
                R.id.consMain ->{
                    val intent = Intent(context , GameDetailsActivity::class.java)
                    GameDetailsActivity.side = "My games"
                    context.startActivity(intent)
                }
            }
        }
        view.adapter = gamesAdapter
        gamesAdapter.list = myGames
        gamesAdapter.notifyDataSetChanged()

    }

    @BindingAdapter("convertHostDate")
    @JvmStatic
    fun convertHostDate(view: TextView, date: String?) {
        if (date.isNullOrEmpty()) {
            view.text = ""
            return
        }

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val parsedDate = inputFormat.parse(date)

            val outputFormat = SimpleDateFormat("MMMM d, EEEE", Locale.getDefault())
            view.text = parsedDate?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            view.text = ""
        }
    }
// Group host game
fun groupGamesByDate(games: List<GetHostGamesApiResponse.Game?>?): List<GetHostGames> {

    if (games.isNullOrEmpty()) return emptyList()

    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
    val outputFormat = SimpleDateFormat("MMMM d, EEEE", Locale.getDefault())

    val groupedGames = games.filterNotNull().groupBy { game ->
        game.hostTimestamp?.let { timestamp ->
            try {
                val date = inputFormat.parse(timestamp)
                outputFormat.format(date ?: "")
            } catch (e: Exception) {
                ""
            }
        } ?: ""
    }.filterKeys { it.isNotEmpty() }

    // ✅ Convert date strings back to Date objects for proper sorting
    val sortedGroups = groupedGames.toSortedMap { date1, date2 ->
        try {
            val parsedDate1 = outputFormat.parse(date1)
            val parsedDate2 = outputFormat.parse(date2)
            parsedDate1?.compareTo(parsedDate2) ?: 0
        } catch (e: Exception) {
            0
        }
    }

    return sortedGroups.map { (date, gameList) ->
        Log.i("SDsadsa", "groupGamesByDate: $date  , $gameList")
        GetHostGames(date = date, gameList = gameList)
    }
}



//    fun groupAllGamesByDate(games: List<GetAllGameApiResponse.Game?>?): List<GetAllGames> {
//        if (games.isNullOrEmpty()) return emptyList()
//
//        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
//        val outputFormat = SimpleDateFormat("MMMM d, EEEE", Locale.getDefault())
//
//        val groupedGames = games.filterNotNull().groupBy { game ->
//            game.hostTimestamp?.let { timestamp ->
//                try {
//                    val date = inputFormat.parse(timestamp)
//                    outputFormat.format(date ?: "")
//                } catch (e: Exception) {
//                    ""
//                }
//            } ?: ""
//        }.filterKeys { it.isNotEmpty() }
//
//        // ✅ Convert date strings back to Date objects for proper sorting
//        val sortedGroups = groupedGames.toSortedMap { date1, date2 ->
//            try {
//                val parsedDate1 = outputFormat.parse(date1)
//                val parsedDate2 = outputFormat.parse(date2)
//                parsedDate1?.compareTo(parsedDate2) ?: 0
//            } catch (e: Exception) {
//                0
//            }
//        }
//
//        return sortedGroups.map { (date, gameList) ->
//            Log.i("SDsadsa", "groupGamesByDate: $date  , $gameList")
//            GetAllGames(date = date, gameList = gameList, isVisible = false)
//        }
//    }






//    fun groupAllGamesByDate(
//        games: List<GetAllGameApiResponse.Game?>?,
//        previousList: List<GetAllGames> = emptyList()
//    ): List<GetAllGames> {
//        if (games.isNullOrEmpty()) return previousList
//
//        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
//        val outputFormat = SimpleDateFormat("MMMM d, EEEE", Locale.getDefault())
//
//        // Group new games by formatted date
//        val groupedGames = games.filterNotNull().groupBy { game ->
//            game.hostTimestamp?.let { timestamp ->
//                try {
//                    val date = inputFormat.parse(timestamp)
//                    outputFormat.format(date ?: "")
//                } catch (e: Exception) {
//                    ""
//                }
//            } ?: ""
//        }.filterKeys { it.isNotEmpty() }.toMutableMap()
//
//        // Merge with previous list
//        previousList.forEach { oldGroup ->
//            if (groupedGames.containsKey(oldGroup.date)) {
//                val combinedList = groupedGames[oldGroup.date]?.toMutableList() ?: mutableListOf()
//                combinedList.addAll(oldGroup.gameList ?: emptyList())
//                groupedGames[oldGroup.date.toString()] = combinedList
//            } else {
//                groupedGames[oldGroup.date.toString()] = oldGroup.gameList ?: emptyList()
//            }
//        }
//
//        // Sort by date
//        val sortedGroups = groupedGames.toSortedMap { date1, date2 ->
//            try {
//                val parsedDate1 = outputFormat.parse(date1)
//                val parsedDate2 = outputFormat.parse(date2)
//                parsedDate1?.compareTo(parsedDate2) ?: 0
//            } catch (e: Exception) {
//                0
//            }
//        }
//
//        return sortedGroups.map { (date, gameList) ->
//            GetAllGames(date = date, gameList = gameList)
//        }
//    }







    // Group My games
    fun groupMyGamesByDate(games: List<GetMyGamesApiResponse.Game?>?): List<GetMyGames> {

        if (games.isNullOrEmpty()) return emptyList()

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMMM d, EEEE", Locale.getDefault())

        val groupedGames = games.filterNotNull().groupBy { game ->
            game.hostTimestamp?.let { timestamp ->
                try {
                    val date = inputFormat.parse(timestamp)
                    outputFormat.format(date ?: "")
                } catch (e: Exception) {
                    ""
                }
            } ?: ""
        }.filterKeys { it.isNotEmpty() }

        // ✅ Convert date strings back to Date objects for proper sorting
        val sortedGroups = groupedGames.toSortedMap { date1, date2 ->
            try {
                val parsedDate1 = outputFormat.parse(date1)
                val parsedDate2 = outputFormat.parse(date2)
                parsedDate1?.compareTo(parsedDate2) ?: 0
            } catch (e: Exception) {
                0
            }
        }

        return sortedGroups.map { (date, gameList) ->
            Log.i("SDsadsa", "groupGamesByDate: $date  , $gameList")
            GetMyGames(date = date, gameList = gameList)
        }
    }

    @BindingAdapter("formatDateToTime")
    @JvmStatic
    fun formatDateToTime(textView: TextView, dateString: String?) {
        if (dateString.isNullOrEmpty()) {
            textView.text = ""  // Set to empty text if the date string is null or empty
            return
        }

        try {
            val inputFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()) // Input format
            val date = inputFormat.parse(dateString)

            // If date parsing is successful, format it
            val outputFormat =
                SimpleDateFormat("h:mma", Locale.getDefault()) // Output format for 12-hour time
            val formattedDate = outputFormat.format(date).toUpperCase(Locale.getDefault())

            textView.text = formattedDate
        } catch (e: Exception) {
            // Handle the exception and set default text
            e.printStackTrace()  // Log the error
            textView.text = "Invalid date"  // Display a fallback message
        }

    }

    @BindingAdapter("formatDateToMonthDay")
    @JvmStatic
    fun formatDateToMonthDay(textView: TextView, dateString: String?) {
        if (dateString.isNullOrEmpty()) {
            textView.text = ""  // Set to empty text if the date string is null or empty
            return
        }

        try {
            val inputFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()) // Input format
            val date = inputFormat.parse(dateString)

            // If date parsing is successful, format it
            val outputFormat =
                SimpleDateFormat("MMM dd", Locale.getDefault()) // Output format for "Feb 11"
            val formattedDate = outputFormat.format(date)

            textView.text = formattedDate
        } catch (e: Exception) {
            // Handle the exception and set default text
            e.printStackTrace()  // Log the error
            textView.text = "Invalid date"  // Display a fallback message
        }
    }
    @BindingAdapter("childHostAdapter")
    @JvmStatic
    fun childHostAdapter(view : RecyclerView , eventGames : List<GetHostGamesApiResponse.Game?>?){

        // Create and set a LayoutManager for the inner RecyclerView
        val layoutManager = LinearLayoutManager(view.context)
        view.layoutManager = layoutManager
        val context = view.context
        val eventAdapter = SimpleRecyclerViewAdapter<GetHostGamesApiResponse.Game, ItemLayoutHostGameDataBinding>(R.layout.item_layout_host_game_data,BR.bean){ v, m, pos ->
            when(v.id){
                R.id.consMain ->{
                    val intent = Intent(context , HostGameDetailsActivity::class.java)
                    intent.putExtra("id",m._id)
                    intent.putExtra("status", m.status)
                    context.startActivity(intent)
                }
            }
        }
        view.adapter = eventAdapter
        eventAdapter.list = eventGames
        eventAdapter.notifyDataSetChanged()

    }

    @BindingAdapter("childMyGamesAdapter")
    @JvmStatic
    fun childMyGamesAdapter(view : RecyclerView , eventGames : List<GetMyGamesApiResponse.Game?>?){

        Log.i("side", "childMyGamesAdapter: $side")
        // Create and set a LayoutManager for the inner RecyclerView
        val layoutManager = LinearLayoutManager(view.context)
        view.layoutManager = layoutManager
        val context = view.context
        val eventAdapter = SimpleRecyclerViewAdapter<GetMyGamesApiResponse.Game, ItemLayoutMyGamesDataBinding>(R.layout.item_layout_my_games_data,BR.bean){ v, m, pos ->
            when(v.id){
                R.id.consMain ->{
                    Log.i("dfdsfdsf", "childMyGamesAdapter: $side")
                    if (side == "Upcoming"){
                        GameDetailsActivity.side = "My games"
                        val intent = Intent(context , GameDetailsActivity::class.java)
                        intent.putExtra("id",m._id)
                        intent.putExtra("status", m.status)
                        context.startActivity(intent)
                    }
                    else if (side == "past"){
                        if (m.isRatingGiven == true){
                            GameDetailsActivity.side = "My games"
                            val intent = Intent(context , GameDetailsActivity::class.java)
                            intent.putExtra("id",m._id)
                            intent.putExtra("status", m.status)
                            context.startActivity(intent)
                        }
                        else{
                            rating.postValue(Resource.success("showRating",m))
                        }
                    }


                }
            }
        }
        view.adapter = eventAdapter
        eventAdapter.list = eventGames
        eventAdapter.notifyDataSetChanged()

    }


//    @BindingAdapter("eventList")
//    @JvmStatic
//    fun eventList(view : RecyclerView , eventGames : List<GetAllGameApiResponse.Game?>?){
//
//        // Create and set a LayoutManager for the inner RecyclerView
//        val layoutManager = LinearLayoutManager(view.context)
//        view.layoutManager = layoutManager
//        val context = view.context
//
//        val eventAdapter = SimpleRecyclerViewAdapter<GetAllGameApiResponse.Game,ItemLayoutGameDetailsBinding>(R.layout.item_layout_events,BR.bean){v, m,pos ->
//
//            when(v.id){
//                R.id.consMain ->{
//                    if (m.hostData!!._id.equals(userId)){
//                        val intent = Intent(context , HostGameDetailsActivity::class.java)
//                        intent.putExtra("id",m._id)
//                        context.startActivity(intent)
//                    }
//                    else{
//                        GameDetailsActivity.side = "Join Game"
//                        val intent = Intent(context , GameDetailsActivity::class.java)
//                        intent.putExtra("id",m._id)
//                        context.startActivity(intent)
//                    }
//
//                }
//            }
//        }
//        view.adapter = eventAdapter
//        eventAdapter.list = eventGames
//        eventAdapter.notifyDataSetChanged()
//
//    }



    @BindingAdapter("slotCount", "minSlots")
    @JvmStatic
    fun setPlayerIconColor(view: AppCompatImageView, slotCount: Int?, minSlots: Int?) {
        val context = view.context
        val colorResId = if ((slotCount ?: 0) >= (minSlots ?: 0)) {
            R.color.green_500 // Define your colors in `colors.xml`
        } else {
            R.color.arrow_color
        }
        view.setColorFilter(ContextCompat.getColor(context, colorResId))
    }


    @BindingAdapter("formatDateAndTime")
    @JvmStatic
    fun formatDateAndTime(textView: TextView, dateString: String?) {
        if (dateString.isNullOrEmpty()) {
            textView.text = ""  // Set to empty text if the date string is null or empty
            return
        }

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()) // Input format
            val date = inputFormat.parse(dateString)

            // If date parsing is successful, format it to "6:00 PM, Oct 3"
            val outputFormat = SimpleDateFormat("h:mm a, MMM dd", Locale.getDefault()) // Output format for time and month/day
            var formattedDate = outputFormat.format(date)

            // Convert AM/PM to uppercase
            formattedDate = formattedDate.replace("am", "AM").replace("pm", "PM")

            textView.text = formattedDate
        } catch (e: Exception) {
            // Handle the exception and set default text
            e.printStackTrace()  // Log the error
            textView.text = "Invalid date"  // Display a fallback message
        }
    }


    @BindingAdapter("convertSecondsToMinutes")
    @JvmStatic
    fun convertSecondsToMinutes(textView: TextView, seconds: Int?) {
        if (seconds == null || seconds < 0) {
            textView.text = "0 minutes"  // Default text if duration is null or negative
            return
        }

        val minutes = seconds / 60  // Convert seconds to minutes
        textView.text = "$minutes minutes"
    }
    inline fun <reified T> parseJson(json: String): T? {
        return try {
            val gson = Gson()
            gson.fromJson(json, T::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    @BindingAdapter("formattedDate")
    @JvmStatic
    fun TextView.setFormattedDate(dateString: String?) {
        if (dateString.isNullOrBlank()) {
            this.text = "-" // Fallback for null or empty values
            return
        }

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM ‘yy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            this.text = date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            this.text = dateString // Fallback to the original if parsing fails
        }
    }

    @BindingAdapter("formatDate")
    @JvmStatic
    fun formatDate(textView: TextView, date: String?) {
        if (date.isNullOrEmpty()) {
            textView.text = "" // Set empty text if date is null or empty
            return
        }

        try {
            // Define input format
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Ensure correct parsing

            // Parse the date string
            val parsedDate = inputFormat.parse(date)

            // Define output format
            val outputFormat = SimpleDateFormat("d MMMM", Locale.getDefault())

            // Set formatted date if parsed successfully, otherwise keep it empty
            textView.text = parsedDate?.let { outputFormat.format(it) } ?: ""

        } catch (e: Exception) {
            textView.text = "" // Set empty text if parsing fails
            e.printStackTrace()
        }
    }


    @BindingAdapter("setEditFormatDate")
    @JvmStatic
    fun setEditFormatDate(view: TextView, dateStr: String?) {
        if (!dateStr.isNullOrEmpty()) {
            try {
                // Parse the input date format
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val date = inputFormat.parse(dateStr)

                // Convert to desired output format
                val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
                val formattedDate = outputFormat.format(date ?: return)

                view.text = formattedDate
            } catch (e: Exception) {
                view.text = "" // Fallback to original if parsing fails
            }
        }
    }


    @BindingAdapter("setEditFormattedTime")
    @JvmStatic
    fun setEditFormattedTime(view: TextView, dateStr: String?) {
        if (!dateStr.isNullOrEmpty()) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                val date = inputFormat.parse(dateStr)

                val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault()) // Format for time
                view.text = outputFormat.format(date ?: return)
            } catch (e: Exception) {
                view.text = "" // Fallback if parsing fails
            }
        }
    }



    fun setFormattedTime(dateString: String?): String {
        if (dateString.isNullOrBlank()) return ""

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            e.printStackTrace() // Debugging: Print the exception stack trace
            ""
        }
    }

    @BindingAdapter("setFormattedDuration")
    @JvmStatic
    fun setFormattedDuration(view: TextView, durationInSeconds: Int?) {
        if (durationInSeconds != null && durationInSeconds > 0) {
            val hours = TimeUnit.SECONDS.toHours(durationInSeconds.toLong())
            val minutes = TimeUnit.SECONDS.toMinutes(durationInSeconds.toLong()) % 60

            // Format the output based on hours and minutes
            val formattedDuration = when {
                hours > 0 && minutes > 0 -> "$hours hours $minutes minutes"
                hours > 0 -> "$hours hours 0 minutes"
                else -> "$minutes minutes"
            }

            view.text = formattedDuration
        } else {
            view.text = "" // Default fallback
        }
    }

    @BindingAdapter("convertDate")
    @JvmStatic
    fun convertDate(view: TextView, date: String?) {
        if (date.isNullOrBlank()) {
            view.text = "" // Set empty text if the date is null or blank
            return
        }

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
            val outputFormat = SimpleDateFormat("d MMMM", Locale.getDefault())

            // Optional: Adjust time zone handling if necessary
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val parsedDate = inputFormat.parse(date)
            view.text = parsedDate?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            view.text = "Invalid date" // Set fallback text in case of an error
        }
    }


//    fun mergeGroupedLists(
//        oldList: List<GetAllGames>,
//        newList: List<GetAllGames>
//    ): List<GetAllGames> {
//        val mergedMap = oldList.associateBy { it.date }.toMutableMap()
//
//        for (newGroup in newList) {
//            val existingGroup = mergedMap[newGroup.date]
//            if (existingGroup != null) {
//                val combinedGames = existingGroup.gameList + newGroup.gameList
//                mergedMap[newGroup.date] = existingGroup.copy(gameList = combinedGames)
//            } else {
//                mergedMap[newGroup.date] = newGroup
//            }
//        }
//
//        return mergedMap.values.sortedBy {
//            try {
//                SimpleDateFormat("MMMM d, EEEE", Locale.getDefault()).parse(it.toString())
//            } catch (e: Exception) {
//                null
//            }
//        }
//    }


//    fun generateStaticGameDates(gameList: ArrayList<GetAllGameApiResponse.Game>, oldList: List<GetAllGames>): List<GetAllGames> {
//        val result = mutableListOf<GetAllGames>()
//        val calendar = Calendar.getInstance()
//        val formatter = SimpleDateFormat("MMM d, EEEE", Locale.getDefault()) // e.g., "May 4, Saturday"
//
//        for (i in 0..13) {
//            if (i != 0) {
//                calendar.add(Calendar.DAY_OF_MONTH, 1)
//            }
//
//            val label = when (i) {
//                0 -> "Today"
//                1 -> "Tomorrow"
//                else -> formatter.format(calendar.time)
//            }
//
//            if(oldList.isNotEmpty()){
//                result.add(
//                    GetAllGames(
//                        date = label,
//                        oldList[i].isVisible,
//                        gameList = gameList
//                    )
//                )
//            }else{
//                result.add(
//                    GetAllGames(
//                        date = label,
//                        false,
//                        gameList = gameList
//                    )
//                )
//            }
//
//        }
//
//        return result
//    }

//    fun generateStaticGameDates(
//        gameList: ArrayList<GetAllGameApiResponse.Game>,
//        oldList: List<GetAllGames>
//    ): List<GetAllGames> {
//        val result = mutableListOf<GetAllGames>()
//        val calendar = Calendar.getInstance()
//        val labelFormatter = SimpleDateFormat("MMM d, EEEE", Locale.getDefault()) // e.g., "May 4, Saturday"
//        val apiFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())    // e.g., "2025-05-02"
//
//        for (i in 0..13) {
//            if (i != 0) {
//                calendar.add(Calendar.DAY_OF_MONTH, 1)
//            }
//
//            val label = when (i) {
//                0 -> "Today"
//                1 -> "Tomorrow"
//                else -> labelFormatter.format(calendar.time)
//            }
//
//            val apiDate = apiFormatter.format(calendar.time)
//
//                        if(oldList.isNotEmpty()){
//                result.add(
//                    GetAllGames(
//                        date = label,
//                        oldList[i].isVisible,
//                        gameList = gameList,
//                        dateValue = apiDate
//                    )
//                )
//            }else{
//                result.add(
//                    GetAllGames(
//                        date = label,
//                        false,
//                        gameList = gameList,
//                        dateValue = apiDate
//                    )
//                )
//            }
//        }
//
//        return result
//    }



    fun generateStaticGameDates(
        gameList: ArrayList<GetAllGameApiResponse.Game>,
        oldList: List<GetAllGames>,
        checkList  :List<GetAllGames>,
    ): List<GetAllGames> {
        val result = mutableListOf<GetAllGames>()
        val calendar = Calendar.getInstance()
        val labelFormatter = SimpleDateFormat("MMM d, EEEE", Locale.getDefault())
        val apiFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Updated to handle timezone offset in the timestamp
        val hostTimeFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC") // Parse as UTC then convert
        }


        for (i in 0..13) {
            if (i != 0) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            val currentDate = calendar.time
            val label = when (i) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> labelFormatter.format(currentDate)
            }

            val apiDate = apiFormatter.format(currentDate)


            val gamesForDate = gameList.filter { game ->
                try {
                    game.hostTimestamp?.let { timestamp ->
                        // Parse the timestamp (with timezone) and convert to local date
                        val gameDate = hostTimeFormatter.parse(timestamp)
                        val gameCalendar = Calendar.getInstance().apply {
                            time = gameDate
                            // Normalize to device timezone
                            timeZone = TimeZone.getDefault()
                        }

                        // Compare just the dates (year/month/day)
                        gameCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                                gameCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                                gameCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
                    } ?: false
                } catch (e: Exception) {
                    Log.e("DATE_DEBUG", "Error parsing date for game ${game._id}: ${e.message}")
                    false
                }
            }


            Log.d("fgdf", "ChgameList: ${gameList.size}")
            if (oldList.isNotEmpty()) {
                result.add(
                    GetAllGames(
                        date = label,
                        isVisible = checkList[i].isVisible,
                        gameList = ArrayList(gamesForDate),
                        dateValue = apiDate
                    )
                )
            } else {
                result.add(
                    GetAllGames(
                        date = label,
                        isVisible = i == 0,
                        gameList = ArrayList(gamesForDate),
                        dateValue = apiDate
                    )
                )
            }
        }

        return result
    }

    @BindingAdapter("setVisibleWhenListEmpty")
    @JvmStatic
    fun setVisibleWhenListEmpty(view: View, eventGames: List<GetAllGameApiResponse.Game?>?) {
        view.visibility = if (eventGames.isNullOrEmpty()) View.VISIBLE else View.GONE
    }



    @BindingAdapter("setSportNames")
    @JvmStatic
    fun setSportNames(editText: AppCompatEditText, sportList: List<GetProfileApiResponse.User.SportId?>?) {
        try {
            val names = sportList?.mapNotNull { it?.name?.trim() }?.filter { it.isNotEmpty() }?.joinToString(", ")
            editText.setText(names ?: "")
        } catch (e: Exception) {
            // Prevent crash: log the error or handle it silently
            editText.setText("")
        }
    }

    @BindingAdapter("setNames")
    @JvmStatic
    fun setNames(textData: TextView, sportList: List<GetAllChatApiResponse.Chat.Participant?>?) {
        val participant = sportList
            ?.filterNotNull()
            ?.firstOrNull()

        val name = buildString {
            participant?.firstName?.trim()?.let { append(it) }
            participant?.lastName?.trim()?.takeIf { it.isNotEmpty() }?.let { append(" $it") }
        }

        textData.text = name
    }


    @BindingAdapter("setCity")
    @JvmStatic
    fun setCity(textView: TextView, participantList: List<GetAllChatApiResponse.Chat.Participant?>?) {
        val city = participantList
            ?.filterNotNull()
            ?.firstOrNull()
            ?.city
            ?.trim()
            ?: ""

        textView.text = city
    }


    @BindingAdapter("formatTime")
    @JvmStatic
    fun formatTime(textView: TextView, dateString: String?) {
        if (dateString.isNullOrEmpty()) {
            textView.text = ""
            return
        }

        try {
            // Input format with milliseconds and 'Z' as literal (UTC)
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val date = inputFormat.parse(dateString)

            // Output format in local time zone
            val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getDefault()

            val formattedTime = date?.let { outputFormat.format(it) } ?: ""
            textView.text = formattedTime
        } catch (e: Exception) {
            e.printStackTrace()
            textView.text = "Invalid time"
        }
    }



    @BindingAdapter("joinedCount","totalGames")
    @JvmStatic
    fun setJoinedText(textView: TextView, joined: Int?, total : Int?) {
        val joinedVal = joined ?: 0
        val totalVal = total ?:0
        textView.text = "Joined $joinedVal/$totalVal"
    }


    @BindingAdapter("loadProfileImage")
    @JvmStatic
    fun loadProfileImage(view: ShapeableImageView, participants: List<GetAllChatApiResponse.Chat.Participant>?) {
        if (!participants.isNullOrEmpty()) {
            val imageUrl = participants[0].profileImage
            Glide.with(view.context)
                .load(imageUrl)
                .placeholder(R.drawable.iv_place_holder)
                .into(view)
        } else {
            view.setImageResource(R.drawable.iv_place_holder)
        }
    }


    @BindingAdapter("monthYear")
    @JvmStatic
    fun monthYear(text: TextView, dateString: String?) {
        if (dateString.isNullOrEmpty()) {
            text.text = ""
            return
        }

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val outputFormat = SimpleDateFormat("M/yyyy", Locale.getDefault()) // Numeric month
            val date = inputFormat.parse(dateString)

            text.text = date?.let { "Joined ${outputFormat.format(it)}" } ?: ""
        } catch (e: Exception) {
            text.text = ""
        }
    }


    @BindingAdapter("cityNameOnly")
    @JvmStatic
    fun TextView.setCityNameOnly(fullCity: String?) {
        val city = fullCity?.substringBefore(",")?.trim().orEmpty()
        text = city
    }


}