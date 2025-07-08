package com.pickup.sports.ui.home_modules.home_fragment

import android.content.Intent
import android.location.Geocoder
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pickup.sports.BR
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.api.Constants.position
import com.pickup.sports.data.model.GetAllGameApiResponse
import com.pickup.sports.data.model.GetAllGames
import com.pickup.sports.data.model.GetAllPlayersApiResponse
import com.pickup.sports.data.model.Players
import com.pickup.sports.databinding.FragmentHomeBinding
import com.pickup.sports.databinding.ItemLayoutEventDateBinding
import com.pickup.sports.databinding.ItemLayoutGameDetailsBinding
import com.pickup.sports.databinding.ItemLayoutListBinding
import com.pickup.sports.databinding.ItemLayoutPlayerListBinding
import com.pickup.sports.ui.auth.login_signup_module.MainViewModel
import com.pickup.sports.ui.base.BaseFragment
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.base.SimpleRecyclerViewAdapter
import com.pickup.sports.ui.home_modules.HomeDashBoardActivity
import com.pickup.sports.ui.home_modules.HomeDashBoardActivity.Companion.loginObr
import com.pickup.sports.ui.home_modules.chat_module.ChatActivity
import com.pickup.sports.ui.home_modules.game_details.GameDetailsActivity
import com.pickup.sports.ui.home_modules.location_module.LocationActivity
import com.pickup.sports.ui.home_modules.select_sports.SelectSportsActivity
import com.pickup.sports.ui.host_game_details.HostGameDetailsActivity
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.Resource
import com.pickup.sports.utils.SocketHandler
import com.pickup.sports.utils.Status
import com.pickup.sports.utils.VerticalPagination
import com.pickup.sports.utils.event.SingleRequestEvent
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.logging.Handler

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {


    private lateinit var gameAdapter: SimpleRecyclerViewAdapter<GetAllGames, ItemLayoutEventDateBinding>
    private lateinit var categoryAdapter : SimpleRecyclerViewAdapter<Players, ItemLayoutListBinding>
    private lateinit var playersAdapter  : SimpleRecyclerViewAdapter<GetAllPlayersApiResponse.User,ItemLayoutPlayerListBinding>

    private lateinit var customAdapter: CustomAdapter
    private var staticDates = mutableListOf<GetAllGames>()

    private val viewModel: MainViewModel by viewModels()
    private var address: String = ""
    private lateinit var mSocket: Socket
    private var gameList = arrayListOf<GetAllGameApiResponse.Game>()
    private var dummy: List<GetAllGames> = listOf()
    private var savedOldList = ArrayList<GetAllGames>()
    private var  categoryList = ArrayList<Players>()
    val inputFormatter = SimpleDateFormat("MMM d, EEEE, yyyy", Locale.ENGLISH)
    val outputFormatter =
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH) // or whatever format your API expects

    var selectedDate: String? = null
    var positionData = -1

    companion object {
        var sportName: String? = null
        var gameId: String? = null
        var lat = 0.0
        var long = 0.0
        var radius: String? = null
        var locationAddress: String? = null
        var searchLat = 0.0
        var searchLong = 0.0
        var side: String? = null


        var paginationData = SingleRequestEvent<Int>()
        var pagination: VerticalPagination? = null
        var currentPage = 1
        var playerCurrentPage = 1

        var scroll = 0

        @BindingAdapter("eventList")
        @JvmStatic
        fun eventList(view: RecyclerView, eventGames: List<GetAllGameApiResponse.Game?>?) {

            // Create and set a LayoutManager for the inner RecyclerView
//            val layoutManager = LinearLayoutManager(view.context)
//            view.layoutManager = layoutManager
            val context = view.context
            val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            view.layoutManager = layoutManager
            // Create the adapter
            val eventAdapter =
                SimpleRecyclerViewAdapter<GetAllGameApiResponse.Game, ItemLayoutGameDetailsBinding>(
                    R.layout.item_layout_events, BR.bean
                ) { v, m, pos ->
                    when (v.id) {
                        R.id.consMain -> {
                            if (m.hostData?._id == ImageUtils.userId) {
                                val intent = Intent(context, HostGameDetailsActivity::class.java)
                                intent.putExtra("id", m._id)
                                context.startActivity(intent)
                            } else {
                                GameDetailsActivity.side = "Join Game"
                                val intent = Intent(context, GameDetailsActivity::class.java)
                                intent.putExtra("id", m._id)
                                context.startActivity(intent)
                            }
                        }
                    }
                }

            // Set the adapter to the RecyclerView
            view.adapter = eventAdapter


            // If eventGames is not null, set it to the adapter and notify it
            eventGames?.let {
                // If you're using a list property inside SimpleRecyclerViewAdapter
                eventAdapter.list = it
                Log.i("dsdsdasa", "eventList: ${it.size} ")

                /*    if (eventGames.isNotEmpty() && eventGames.size > 0){
                        if (eventGames?.isVisible == true){
                            view.visibility = View.VISIBLE
                        }
                        else{
                            view.visibility = View.GONE
                        }
                    }*/

                eventAdapter.notifyDataSetChanged()


                // Alternatively, if you're using a ListAdapter, use submitList(it)
                // eventAdapter.submitList(it)
            } ?: run {
                // Handle the case when eventGames is null (maybe clear the list or show a placeholder)
                eventAdapter.list = emptyList()
                eventAdapter.notifyDataSetChanged()
            }
            //  val lm = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
//            pagination = VerticalPagination(layoutManager, 2)
//           pagination?.setListener(object : VerticalPagination.VerticalScrollListener {
//            override fun onLoadMore() {
//
//
//
////                val data = HashMap<String, Any>()
////                if (!gameId.isNullOrEmpty()){
////                    data["sportId"] = gameId.toString()
////                }
////                if (radius != null){
////                    data["radius"] = radius!!
////                }
////                if (sharedPrefManager.getCurrentUser() != null){
////                    data["userId"] = sharedPrefManager.getCurrentUser()?.userId.toString()
////                    data["latitude"] = if (searchLat != 0.0) searchLat else lat
////                    data["longitude"] = if (searchLong != 0.0) searchLong else long
////                }else{
////                    if (searchLat != 0.0 && searchLong != 0.0){
////                        data["latitude"] =  searchLat
////                        data["longitude"] = searchLong
////                    }
////
////                }
////                data["page"] = page
////                viewModel.getGames(data, Constants.GET_ALL_GAMES)
//
//
//            }
//        })
//        pagination?.let {
//            view.addOnScrollListener(it)
//        }

        }
    }

    var itemCount = 0
    override fun onCreateView(view: View) {
        initOnClick()
        getAddressFromLatLong(lat.toString(), long.toString())
        if (sharedPrefManager.getCurrentUser() != null) {
            ImageUtils.userId = sharedPrefManager.getCurrentUser()!!.userId
        }
        socketHandler()
        getCategoryList()
        initAdapter()
        //  initCustomAdapter()
        initObserver()

        Log.i("dfdf", "onCreateView:")
//        binding.rvEventDate.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                // Safely cast layout manager
//                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
//
//                // Get last visible item position
//                val lastVisible = layoutManager.findLastVisibleItemPosition()
//                if (lastVisible == RecyclerView.NO_POSITION) return
//
//                // Get total items in the RecyclerView
//                val totalItemCount = layoutManager.itemCount
//
//                // Dynamically determine how close to the end we want to trigger loading
//                val itemCount = (13 - positionData).coerceAtLeast(3)  // Minimum threshold: 3
//
//                // Calculate the safe threshold for triggering the load
//                val triggerThreshold = (totalItemCount - itemCount).coerceAtLeast(0)
//
//                // If not loading, and user has scrolled near the end, trigger load
//                if (!isLoading && lastVisible >= triggerThreshold) {
//                    Log.i("scroll", "scroll: ${scroll}")
//                    if (scroll == 1){
//                   currentPage++
//                        paginations()
//                        scroll = 0
//                        Log.i("scroll", "onScrolled: ")
//                    }
////                    page ++
////                    loadMoreGamesForSelectedDate(page)
//
//                }
//            }
//        })

        binding.rvEventDate.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if (lastVisible == RecyclerView.NO_POSITION) return
                val totalItemCount = layoutManager.itemCount
                val itemCount = (13 - positionData).coerceAtLeast(3)
                val triggerThreshold = (totalItemCount - itemCount).coerceAtLeast(0)
                if (dy > 0 && !isLoading && lastVisible >= triggerThreshold) {
                    if (scroll == 1) {
                        currentPage++
                        paginations()
                        scroll = 0
                    }
                }

            }
        })
    }

    private fun getPlayerData() {
        playerCurrentPage = 1
        if (sharedPrefManager.getCurrentUser() != null){
            val data = HashMap<String,Any>()
            data["page"] = 1
            data["userId"] = sharedPrefManager.getCurrentUser()?.userId.toString()
            if (searchLat != 0.0 && searchLong != 0.0) {
                data["latitude"] = searchLat
                data["longitude"] = searchLong
            } else {
                if (lat != 0.0 && long != 0.0) {
                    data["latitude"] = lat
                    data["longitude"] = long
                }
            }
            if (radius != null) {
                data["radius"] = radius!!
            }


            viewModel.getAllPlayer(data, Constants.GET_ALL_PLAYER)
        }
        else{
            val data = HashMap<String,Any>()
            data["page"] = 1
            if (searchLat != 0.0 && searchLong != 0.0) {
                data["latitude"] = searchLat
                data["longitude"] = searchLong
            } else {
                if (lat != 0.0 && long != 0.0) {
                    data["latitude"] = lat
                    data["longitude"] = long
                }
            }
            if (radius != null) {
                data["radius"] = radius!!
            }
            viewModel.getAllPlayer(data, Constants.GET_ALL_PLAYER)
        }


    }

    private fun getCategoryList() {
        categoryList.add(Players("Game List"))
        categoryList.add(Players("Player List"))
    }


    private fun paginations() {
        val data = HashMap<String, Any>()
        if (!gameId.isNullOrEmpty()) {
            data["sportId"] = gameId.toString()
        }
        if (radius != null) {
            data["radius"] = radius!!
        }
        if (sharedPrefManager.getCurrentUser() != null) {
            data["userId"] = sharedPrefManager.getCurrentUser()?.userId.toString()
            data["latitude"] = if (searchLat != 0.0) searchLat else lat
            data["longitude"] = if (searchLong != 0.0) searchLong else long
        } else {
            if (searchLat != 0.0 && searchLong != 0.0) {
                data["latitude"] = searchLat
                data["longitude"] = searchLong
            }

        }
        data["date"] = selectedDate.toString()
        data["page"] = currentPage
        data["limit"] = "10"
        viewModel.getGames(data, Constants.GET_ALL_GAMES)

    }


    private fun getData() {
        if (sharedPrefManager.getCurrentUser() != null) {
            currentPage = 1
            val data = HashMap<String, Any>()
            if (!gameId.isNullOrEmpty()) {
                data["sportId"] = gameId.toString()
            }
            if (radius != null) {
                data["radius"] = radius!!
            }
            data["userId"] = sharedPrefManager.getCurrentUser()?.userId.toString()
            data["latitude"] = if (searchLat != 0.0) searchLat else lat
            data["longitude"] = if (searchLong != 0.0) searchLong else long
            data["page"] = 1
            data["date"] = selectedDate.toString()
            data["limit"] = "10"
            viewModel.getGames(data, Constants.GET_ALL_GAMES)
        } else {
            currentPage = 1
            val data = HashMap<String, Any>()
            if (!gameId.isNullOrEmpty()) {
                data["sportId"] = gameId.toString()
            }
            if (radius != null) {
                data["radius"] = radius!!
            }
//            data["sportId"] = ""
//            data["longitude"] = long
//            data["latitude"] = lat
            data["page"] = 1
            if (searchLat != 0.0 && searchLong != 0.0) {
                data["latitude"] = searchLat
                data["longitude"] = searchLong
            }
            data["limit"] = "10"
            data["date"] = selectedDate.toString()
            viewModel.getGames(data, Constants.GET_ALL_GAMES)
        }

    }

    private fun socketHandler() {
        val token = sharedPrefManager.getCurrentUser()?.token
        try {
            if (!token.isNullOrEmpty()) {
                SocketHandler.setSocket(token)  // Establish socket connection with token
                SocketHandler.establishConnection()
                mSocket = SocketHandler.getSocket()!!
                Log.i("SocketHandler", "socketHandler: $mSocket")
                Log.e("SocketHandler", "Connection is established.")


            } else {
                Log.e("SocketHandler", "Token is missing! Cannot establish connection.")
            }
        }catch ( e: Exception){
            e.printStackTrace()
        }

    }

    private fun initObserver() {
        viewModel.obrCommon.observe(viewLifecycleOwner, Observer {
            when (it?.status) {
                Status.LOADING -> {
                    if (currentPage == 1){
                        progressDialogAvl.isLoading(true)
                    }
                }

                Status.SUCCESS -> {
                    hideLoading()
                    when (it.message) {

                        "getGame" -> {

                            val myDataModel: GetAllGameApiResponse? =
                                ImageUtils.parseJson(it.data.toString())
                            try {

                                if (myDataModel?.games.isNullOrEmpty()){
                                    showToast("Games not found")
                                }
                                if (myDataModel != null && !myDataModel.games.isNullOrEmpty()) {

                                    // ✅ Clear previous data only if on first page
                                    if (currentPage == 1) {
                                        gameList.clear()
                                       // savedOldList.clear()
                                    }

                                    Log.d("fgdf", "initObserver: ${myDataModel.games!!.size}")
                                    // Append new data to the existing game list
                                    gameList.addAll(myDataModel.games as ArrayList<GetAllGameApiResponse.Game>)

                                    val generatedList = ImageUtils.generateStaticGameDates(
                                        gameList,
                                        savedOldList,
                                        gameAdapter.list
                                    )

                                    // Ensure we're adding new generated data to savedOldList
                                    savedOldList.addAll(generatedList)

                                    lifecycleScope.launch {
                                        // Pass the updated list to the adapter
                                        gameAdapter.setList(generatedList)
                                        scroll = if (myDataModel.totalPages != currentPage) 1 else 0
                                    }
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }


                        "getAllPlayer"  ->{
                            val myDataModel : GetAllPlayersApiResponse ?= ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                try {
                                    binding.playerCount.text = "Results: ${myDataModel.totalUsers} Players"
                                    if (myDataModel.users != null){
                                        if (playerCurrentPage <= myDataModel.pagination?.totalPages!!){
                                            pagination?.isLoading = false
                                        }
                                        if (playerCurrentPage == 1){
                                            playersAdapter.list = myDataModel.users
                                        }else{
                                            playersAdapter.addToList(myDataModel.users)
                                        }
//                                    playersAdapter.list = myDataModel.users
                                    }
                                }catch (e : Exception){
                                    e.printStackTrace()
                                }

                                if (playersAdapter.list.isNullOrEmpty()){
                                    binding.textNoData.visibility = View.VISIBLE
                                }
                                else{
                                    binding.textNoData.visibility = View.GONE
                                }


                            }
                        }
                    }
                }


//                            val myDataModel: GetAllGameApiResponse? =
//                                ImageUtils.parseJson(it.data.toString())
//                            try {
//                                if (myDataModel != null) {
//                                    if (!myDataModel.games.isNullOrEmpty()) {
//                                        //Log.i("wegt234g", "initObserver: " + gameAdapter.list)
//                                        // Set the eventGames list from the parsed data to your bean's game list
//                                        gameList =
//                                            myDataModel.games as ArrayList<GetAllGameApiResponse.Game>
//
//                                        val oldList = gameAdapter.list
//                                        val staticDates =
//                                            ImageUtils.generateStaticGameDates(gameList, oldList)
//                                         Log.i("fdsf", "initObserver: $staticDates")
//
//                                        if (page <= myDataModel.totalPages!!){
//                                            pagination?.isLoading = false
//                                        }
//                                        if (page == 1){
//                                                gameAdapter.setList(staticDates)
//                                            }else{
//                                                gameAdapter.addToLis1t(staticDates)
//                                            }
//
//                                        //  gameAdapter.setList(staticDates)
//
//
//                                    } else {
//                                        // Handle case when no games are available
//                                    }
//                                }
//                            } catch (e: Exception) {
//                                // Handle any parsing errors
//                                e.printStackTrace()
//                                // Set gameList to empty in case of error
//
//                            }
//                            try {
//                                if (myDataModel != null) {
//                                    if (!myDataModel.games.isNullOrEmpty()){
//
//
//
//
////                                        try {
////                                            val groupedList = ImageUtils.groupAllGamesByDate(myDataModel.games)
////
////                                            Log.i("groupedList", "initObserver:${groupedList.size}")
////                                            if (page <= myDataModel.totalPages!!){
////                                                pagination?.isLoading = false
////                                            }
////                                            if (page == 1){
////                                                gameAdapter.list = groupedList
////                                            }else{
////                                                gameAdapter.addToLis1t(groupedList)
////                                            }
////                                        }catch (e: Exception){
////                                            e.printStackTrace()
////                                        }
//                                    }
////                                    else {
////                                        gameAdapter.list = emptyList()
////                                        gameAdapter.notifyDataSetChanged()
////                                    }
//
//
////                                    if (!myDataModel.games.isNullOrEmpty()) {
////
////                                        try {
////                                            val groupedList =
////                                                ImageUtils.groupAllGamesByDate(myDataModel.games)
////                                            gameAdapter.list = groupedList
////                                            gameAdapter.notifyDataSetChanged()
////                                        } catch (e: Exception) {
////                                            e.printStackTrace()
////                                        }
////                                    } else {
////                                        gameAdapter.list = emptyList()
////                                        gameAdapter.notifyDataSetChanged()
////                                    }
//                                }
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            }

//                            if (gameAdapter.list.isNullOrEmpty()) {
//                                binding.textNoData.visibility = View.VISIBLE
//                            } else {
//                                binding.textNoData.visibility = View.GONE
//                            }

                Status.ERROR -> {
                    hideLoading()
                    showToast(it.message.toString())
                }

                else -> {

                }
            }
        })


    }

    val pageMap = mutableMapOf<String, Int>()
    var isLoading = false


    private fun initAdapter() {
//        gameAdapter =
//            SimpleRecyclerViewAdapter(R.layout.item_layout_event_date, BR.bean) { v, m, pos ->
//
//                val parentView = v.rootView // this is the full item layout view
//
//                val textNoData = parentView.findViewById<TextView>(R.id.textNoData)
//
//                // Example: Show it if this date has no games
//                if (m.gameList.isNullOrEmpty()) {
//                    Log.i("asdasdasd", "initAdapter: visible ")
//                    textNoData.visibility = View.VISIBLE
//                } else {
//                    Log.i("asdasdasd", "initAdapter: visible ")
//
//                    textNoData.visibility = View.GONE
//                }
//
//                when (v.id) {
//                    R.id.dropDown, R.id.tvDate -> {
//                        position = pos
//                        positionData = pos
//                        for (i in gameAdapter.list){
//                            i.isVisible = i.date == m.date
//                        }
//
//                        gameAdapter.notifyItemChanged(pos)
//
//
//                        if (m.isVisible){
//                            selectedDate = try {
//                                when (m.date) {
//                                    "Today" -> {
//                                        val cal = Calendar.getInstance()
//                                        outputFormatter.format(cal.time)
//                                    }
//
//                                    "Tomorrow" -> {
//                                        val cal = Calendar.getInstance()
//                                        cal.add(Calendar.DAY_OF_YEAR, 1)
//                                        outputFormatter.format(cal.time)
//                                    }
//
//                                    else -> {
//                                        m.date?.let { dateLabel ->
//                                            val year = Calendar.getInstance().get(Calendar.YEAR)
//                                            val fullDate =
//                                                "$dateLabel, $year" // e.g., "May 7, Wednesday, 2025"
//                                            inputFormatter.parse(fullDate)?.let { parsed ->
//                                                outputFormatter.format(parsed)
//                                            }
//                                        }
//                                    }
//                                }
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                                null
//                            }
//
//                            Log.d("SelectedDate", "Selected: $selectedDate")
//                            getData()
//                        }
//
//                    }
//
//                }
//            }
//
//        binding.rvEventDate.adapter = gameAdapter
//        val staticDates = ImageUtils.generateStaticGameDates(gameList, dummy)
//        gameAdapter.setList(staticDates)
//        gameAdapter.notifyDataSetChanged()


        gameAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_event_date, BR.bean) { v, m, pos ->
                when (v.id) {

                    R.id.dropDown, R.id.tvDate -> {



                        position = pos
                        positionData = pos
                        m.isVisible = !m.isVisible
                        gameAdapter.notifyDataSetChanged()

                        if (m.isVisible) {


                            selectedDate = try {
                                when (m.date) {
                                    "Today" -> {
                                        val cal = Calendar.getInstance()
                                        outputFormatter.format(cal.time)
                                    }

                                    "Tomorrow" -> {
                                        val cal = Calendar.getInstance()
                                        cal.add(Calendar.DAY_OF_YEAR, 1)
                                        outputFormatter.format(cal.time)
                                    }

                                    else -> {
                                        m.date?.let { dateLabel ->
                                            val year = Calendar.getInstance().get(Calendar.YEAR)
                                            val fullDate =
                                                "$dateLabel, $year" // e.g., "May 7, Wednesday, 2025"
                                            inputFormatter.parse(fullDate)?.let { parsed ->
                                                outputFormatter.format(parsed)
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                            getData()
                        }
                    }
                }
            }

        binding.rvEventDate.adapter = gameAdapter
        val staticDates = ImageUtils.generateStaticGameDates(gameList, dummy, emptyList())
        gameAdapter.setList(staticDates)
        binding.rvEventDate.setItemAnimator(null)
        gameAdapter.notifyDataSetChanged()


        playersAdapter =  SimpleRecyclerViewAdapter(R.layout.item_layout_player_list,BR.bean){v,m,pos ->
            when(v.id){
                R.id.consMain ->{
                    if (sharedPrefManager.getCurrentUser() != null) {
                        mSocket.on(Socket.EVENT_CONNECT) {

                        }
                        try {
                            Log.i("SocketHandler", "Socket connected. Joining room...")
                            val jsonData = JSONObject().apply {
                                put("type", 1)
                                put("to", m._id)

                            }

                            // Emit the joinRoom event with the prepared data
                            mSocket.emit("joinRoom", jsonData)
                            Log.i("SocketHandler", "Emitted joinRoom event: $jsonData")

                            // Listen for the response
                            mSocket.on("roomId") { args ->
                                Log.i("SocketHandler", "Received joinRoom response: $args")

                                if (args.isNotEmpty()) {
                                    try {
                                        val jsonString = args[0].toString()  // Convert object to String
                                        Log.i("RawJson", jsonString)
                                        val jsonObject = JSONObject(jsonString) // Parse it as JSON
                                        Log.i("SocketHandler", "Parsed JSON: $jsonObject")

                                        // Access specific values if needed
                                        val roomId = jsonObject.optString("roomId", "Unknown")
                                        val isBlocked = jsonObject.optBoolean("isBlock",false)
                                        Log.i("SocketHandler", "room id: $roomId   $isBlocked", )
                                        if (roomId != null){
                                            val firstName = m.firstName.orEmpty()
                                            val lastName = m.lastName

                                            val fullName = if (!lastName.isNullOrBlank()) {
                                                "$firstName $lastName"
                                            } else {
                                                firstName
                                            }
                                            val intent = Intent(requireContext(), ChatActivity::class.java)
                                            intent.putExtra("roomId",roomId)
                                            intent.putExtra("isBlocked", isBlocked)
                                            intent.putExtra("chatType","anyPlayer")
                                            intent.putExtra("playerName", fullName)
                                            intent.putExtra("side","anyPlayer")

                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            startActivity(intent)
//                                            val intent = Intent(this, ChatActivity::class.java)
//                                            intent.putExtra("data", gameDetail)
//                                            intent.putExtra("roomId",roomId)
//                                            intent.putExtra("side","single")
//                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                                            startActivity(intent)
                                        }
                                        Log.i("SocketHandler", "Extracted Room ID: $roomId")
                                    } catch (e: Exception) {
                                        Log.i("SocketHandler", "Error parsing JSON: ${e.message}")
                                    }
                                }
                            }
                        }catch (e : Exception){
                            e.printStackTrace()
                        }
                    } else {
                        loginObr.postValue(Resource.success("loginObr", true))
                    }
                }
            }

        }
        binding.rvPlayers.adapter = playersAdapter
        val lm = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        pagination = VerticalPagination(lm, 2)
        pagination?.setListener(object : VerticalPagination.VerticalScrollListener {
            override fun onLoadMore() {
                playerCurrentPage++
                Log.i("dsadasd", "onLoadMore: $playerCurrentPage")
                val data = HashMap<String, Any>()
                data["page"] = playerCurrentPage.toString()
                if (lat != 0.0 && long != 0.0){
                    data["latitude"] = if (searchLat != 0.0) searchLat else lat
                    data["longitude"] = if (searchLong != 0.0) searchLong else long
                }
                if (!gameId.isNullOrEmpty()) {
                    data["sportId"] = gameId.toString()
                }
                if (radius != null) {
                    data["radius"] = radius!!
                }
                if (sharedPrefManager.getCurrentUser() != null) {
                    data["userId"] = sharedPrefManager.getCurrentUser()?.userId.toString()
                }
                viewModel.getAllPlayer(data, Constants.GET_ALL_PLAYER)

            }
        })
        pagination?.let {
            binding.rvPlayers.addOnScrollListener(it)
        }
        playersAdapter.notifyDataSetChanged()


//        val lm = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
//        pagination = VerticalPagination(lm, 2)
//        pagination?.setListener(object : VerticalPagination.VerticalScrollListener {
//            override fun onLoadMore() {
//                page++
//                Log.i("dsadasd", "onLoadMore: $page")
//                val data = HashMap<String, Any>()
//                if (!gameId.isNullOrEmpty()){
//                    data["sportId"] = gameId.toString()
//                }
//                if (radius != null){
//                    data["radius"] = radius!!
//                }
//                if (sharedPrefManager.getCurrentUser() != null){
//                    data["userId"] = sharedPrefManager.getCurrentUser()?.userId.toString()
//                    data["latitude"] = if (searchLat != 0.0) searchLat else lat
//                    data["longitude"] = if (searchLong != 0.0) searchLong else long
//                }else{
//                    if (searchLat != 0.0 && searchLong != 0.0){
//                        data["latitude"] =  searchLat
//                        data["longitude"] = searchLong
//                    }
//
//                }
//                data["page"] = page
//                viewModel.getGames(data, Constants.GET_ALL_GAMES)
//            }
//        })
//        pagination?.let {
//            binding.rvEventDate.addOnScrollListener(it)
//        }



        categoryAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_list, BR.bean){v,m,pos  ->
            when(v.id){
                R.id.consMain, R.id.title ->{
                    binding.selectList.setText(m.title)
                    binding.rvCategory.visibility = View.GONE
                    if (m.title == "Player List"){
                       getPlayerData()
                        binding.rvPlayers.visibility = View.VISIBLE
                        binding.rvEventDate.visibility = View.GONE
                        binding.playerCount.visibility = View.VISIBLE

                    }
                    else{
                        binding.rvPlayers.visibility = View.GONE
                        binding.rvEventDate.visibility = View.VISIBLE
                        binding.playerCount.visibility =View.GONE
                    }
                }
            }

        }
        binding.rvCategory.adapter = categoryAdapter
        categoryAdapter.list = categoryList
        categoryAdapter.notifyDataSetChanged()


    }


    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when (it?.id) {
                R.id.selectSports, R.id.ivDropSports -> {
                    if (sharedPrefManager.getCurrentUser() != null) {
                        val intent = Intent(requireContext(), SelectSportsActivity::class.java)
                        intent.putExtra("side", "Home")
                        startActivity(intent)
                    } else {
                        loginObr.postValue(Resource.success("loginObr", true))
                    }

                }

                R.id.selectLocation, R.id.ivDropLocation -> {
                    val intent = Intent(requireContext(), LocationActivity::class.java)
                    startActivity(intent)
                }
                R.id.selectList ->{
                    binding.rvCategory.visibility = View.VISIBLE
                }


            }
        })
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_home
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private fun getAddressFromLatLong(lat: String?, long: String?) {
        Log.i("dfsfsdffdsf", "getAddressFromLatLong: $lat  $long")
        if (lat != null && long != null) {
            val latitude = lat.toDoubleOrNull()
            val longitude = long.toDoubleOrNull()

            if (latitude != null && longitude != null) {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                try {
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        address = addresses[0].getAddressLine(0) // Full address
                        if (sharedPrefManager.getCurrentUser() != null){
                            binding.selectLocation.setText(address)
                        }
                        else{
                            binding.selectLocation.setText("Select location")
                        }


                        Log.i("Address", "Address: $address")
                    } else {
                        Log.d("Address", "No address found.")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("AddressError", "Error getting address: ${e.message}")
                }
            } else {
                Log.e("InputError", "Invalid latitude or longitude values.")
            }
        } else {
            Log.e("InputError", "Latitude or longitude is null.")
        }
    }

    override fun onResume() {

        super.onResume()


        HomeDashBoardActivity.allGamesRefresh.observe(this) {
            Log.i("fdfdsfs", "onResume:${HomeDashBoardActivity.allGamesRefresh} ")
            when (it?.status) {
                Status.LOADING -> {

                }

                Status.SUCCESS -> {
                    Log.i("fdfdsfs", "onResume: ${HomeDashBoardActivity.allGamesRefresh}")
                    socketHandler()
                    //getData()
                    // Reset adapter data
                    gameList.clear() // <-- clear your existing list (this should be the list backing your adapter)
                    gameAdapter.notifyDataSetChanged() // notify adapter the data is cleared

                    initAdapter() // if this method resets or rebinds your adapter, keep it; else remove if unnecessary
                   // initAdapter()
                    getPlayerData()
                }

                Status.ERROR -> {

                }

                else -> {

                }
            }
        }

        var shouldCallGetData = true // Flag to control getData() execution


        if (side == "selectSports") {
          currentPage = 1
            playerCurrentPage = 1
            if (sportName != null && gameId != null) {

                // Reset adapter data
                gameList.clear() // <-- clear your existing list (this should be the list backing your adapter)
                gameAdapter.notifyDataSetChanged() // notify adapter the data is cleared

                initAdapter() // if this method resets or rebinds your adapter, keep it; else remove if unnecessary


                Log.i("fdsfdsf", "onResume: $sportName")
                binding.selectSports.setText(sportName)
                val data = HashMap<String, Any>()
                if (!gameId.isNullOrEmpty()) {
                    data["sportId"] = gameId.toString()
                }

                data["userId"] = sharedPrefManager.getCurrentUser()?.userId.toString()
                if (searchLat != 0.0 && searchLong != 0.0) {
                    data["latitude"] = searchLat
                    data["longitude"] = searchLong
                }
                if (radius != null) {
                    data["radius"] = radius!!
                }
                   data["page"] = 1
             //   data["date"] = selectedDate.toString()
            //    data["limit"] = "10"
        //        viewModel.getGames(data, Constants.GET_ALL_GAMES)
                viewModel.getAllPlayer(data, Constants.GET_ALL_PLAYER)
                side = ""
                shouldCallGetData = false // Skip getData()

            }

        }

        if (side == "selectLocation") {
          currentPage = 1
            playerCurrentPage = 1
            if (locationAddress != null && radius != null) {

                // Reset adapter data
                gameList.clear() // <-- clear your existing list (this should be the list backing your adapter)
                gameAdapter.notifyDataSetChanged() // notify adapter the data is cleared

                initAdapter() // if this method resets or rebinds your adapter, keep it; else remove if unnecessary

                gameAdapter.notifyDataSetChanged()
                binding.selectLocation.setText(locationAddress)
                val data = HashMap<String, Any>()
                data["radius"] = radius!!
                data["longitude"] = searchLong
                data["latitude"] = searchLat
                data["page"] = 1
              //  data["date"] = selectedDate.toString()
                if (!gameId.isNullOrEmpty()) {
                    data["sportId"] = gameId.toString()
                }
                if (sharedPrefManager.getCurrentUser() != null) {
                    data["userId"] = sharedPrefManager.getCurrentUser()?.userId.toString()
                }
             //   data["limit"] = "10"
             //   viewModel.getGames(data, Constants.GET_ALL_GAMES)
                viewModel.getAllPlayer(data, Constants.GET_ALL_PLAYER)
                side = ""
                shouldCallGetData = false // Skip getData()

            }
        }

        if (sharedPrefManager.getCurrentUser() != null) {
            ImageUtils.userId = sharedPrefManager.getCurrentUser()!!.userId
        }

        if (shouldCallGetData) {
            getPlayerData()
            gameList.clear() // <-- clear your existing list (this should be the list backing your adapter)
            gameAdapter.notifyDataSetChanged() // notify adapter the data is cleared

            initAdapter() // if this method resets or rebinds your adapter, keep it; else remove if unnecessary
        }

        if (binding.selectLocation.text?.isEmpty() == true) {
            binding.selectLocation.setText(address)
        }

        Log.i("dfdf", "onResume: ")
    }

}