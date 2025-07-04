package com.pickup.sports.ui.host_game_details

import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import com.pickup.sports.BR
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.api.SimpleApiResponse
import com.pickup.sports.data.model.GetGameByIdApiResponse
import com.pickup.sports.databinding.ActivityHostGameDetailsBinding
import com.pickup.sports.databinding.BottomSheetCancelGameBinding
import com.pickup.sports.databinding.BottomsheetPlayersListBinding
import com.pickup.sports.databinding.ItemLayoutCancelGamePopupBinding
import com.pickup.sports.databinding.ItemLayoutDeleteGamePopupBinding
import com.pickup.sports.databinding.ItemLayoutPlayersBinding
import com.pickup.sports.ui.auth.login_signup_module.MainViewModel
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.base.SimpleRecyclerViewAdapter
import com.pickup.sports.ui.home_modules.HomeDashBoardActivity
import com.pickup.sports.ui.home_modules.chat_module.ChatActivity
import com.pickup.sports.ui.home_modules.create_host_game.CreateHostGameActivity
import com.pickup.sports.utils.BaseCustomBottomSheet
import com.pickup.sports.utils.BaseCustomDialog
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.SocketHandler
import com.pickup.sports.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class HostGameDetailsActivity : BaseActivity<ActivityHostGameDetailsBinding>() , BaseCustomBottomSheet.Listener , BaseCustomDialog.Listener {
    private val viewModel : MainViewModel by viewModels()
    private lateinit var cancelGameBottomSheet : BaseCustomBottomSheet<BottomSheetCancelGameBinding>
    private lateinit var deleteGamePopup : BaseCustomDialog<ItemLayoutDeleteGamePopupBinding>
    private lateinit var cancelPopup : BaseCustomDialog<ItemLayoutCancelGamePopupBinding>
    private lateinit var playerAdapter : SimpleRecyclerViewAdapter<GetGameByIdApiResponse.Game.SlotId, ItemLayoutPlayersBinding>
    private lateinit var playerBottomSheet : BaseCustomBottomSheet<BottomsheetPlayersListBinding>
    private var gameDate : String ? = null
    private lateinit var mSocket: Socket
    private  var gameData : GetGameByIdApiResponse.Game ? = null

    private var id : String ? = null


    companion object{
        var gameStatus : String ? =null
    }

    override fun getLayoutResource(): Int {
        return R.layout.activity_host_game_details
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        getData()
        setData()
        initOnClick()
        initBottomSheet()
        initPopup()
        socketHandler()
        initAdapter()
        initObserver()

    }

    private fun setData() {
        var status = intent.getIntExtra("status",0)
        if (gameStatus == "Past"){
            if (status == 4){
                binding.tvGroupChat.visibility = View.GONE
                binding.tvEditGame.visibility = View.GONE
                binding.tvCancelGame.visibility = View.GONE
            }else{
                binding.tvGroupChat.visibility = View.VISIBLE
                binding.tvEditGame.visibility = View.VISIBLE
                binding.tvCancelGame.visibility = View.GONE
            }

        }else{
            binding.tvGroupChat.visibility = View.VISIBLE
            binding.tvEditGame.visibility = View.VISIBLE
            binding.tvCancelGame.visibility = View.VISIBLE
        }
    }

    private fun initPopup() {
        cancelPopup = BaseCustomDialog(this , R.layout.item_layout_cancel_game_popup, this )
        deleteGamePopup = BaseCustomDialog(this , R.layout.item_layout_delete_game_popup, this)
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
    private fun initBottomSheet() {
        playerBottomSheet  = BaseCustomBottomSheet(this, R.layout.bottomsheet_players_list,this)
        cancelGameBottomSheet  = BaseCustomBottomSheet(this , R.layout.bottom_sheet_cancel_game,this)

    }

    private fun initObserver() {
        viewModel.obrCommon.observe(this , Observer {
            when(it?.status){
                Status.LOADING ->{
                    progressDialogAvl.isLoading(true)
                }
                Status.SUCCESS -> {
                    hideLoading()
                    when(it.message){
                        "singleGameDetail" ->{
                            val myDataModel: GetGameByIdApiResponse? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.game != null){

                                    gameData = myDataModel.game
                                    binding.bean  = myDataModel.game
                                    gameDate = myDataModel.game!!.hostTimestamp

                                    if (myDataModel.game!!.slotIds?.isNotEmpty() == true){
                                        binding.tvPlayer.visibility = View.VISIBLE
                                        playerAdapter.list = myDataModel.game!!.slotIds
                                    }else{
                                        binding.tvPlayer.visibility = View.INVISIBLE
                                    }

                                }
                            }
                        }
                        "cancelGame" ->{
                            val myDataModel : SimpleApiResponse ? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                val intent = Intent(this, HomeDashBoardActivity::class.java)
                                startActivity(intent)
                                cancelPopup.dismiss()
                                deleteGamePopup.dismiss()
                            }
                        }
                    }
                }
                Status.ERROR ->{
                    hideLoading()
                    showToast(it.message.toString())
                }
                else ->{

                }

            }
        })
    }

    private fun getData() {
        id = intent.getStringExtra("id")
        Log.i("dfsdfsd", "getData: $id")

        if (id != null){

            val data = HashMap<String,Any>()
            data["gameId"]  = id.toString()
            viewModel.singleGameDetail(data , Constants.SINGLE_GAME_DETAIL)
        }
    }

    private fun initOnClick() {
        viewModel.onClick.observe(this , Observer {
            when(it?.id){
                R.id.ivBack ->{
                    onBackPressedDispatcher.onBackPressed()
                }
                R.id.tvCancelGame ->{
                    cancelGameBottomSheet.show()
                }
                R.id.tvGroupChat ->{

                    if (sharedPrefManager.getCurrentUser() != null){
                        mSocket.on(Socket.EVENT_CONNECT) {

                        }
                        if (gameData?.chatId != null){
                            try {
                                Log.i("SocketHandler", "Socket connected. Joining host room...")

                                val jsonData = JSONObject().apply {
                                    put("type", 2)
                                    put("chatId", gameData!!.chatId)

                                }
                                // Emit the joinRoom event with the prepared data
                                mSocket.emit("joinRoom", jsonData)
                                Log.i("SocketHandler", "Emitted joinRoom event: $jsonData")

                                // Listen for the response
                                mSocket.on("roomId") { args ->
                                    Log.i("SocketHandler", "Received join host Room response: $args")

                                    if (args.isNotEmpty()) {
                                        try {
                                            val jsonString = args[0].toString()  // Convert object to String
                                            val jsonObject = JSONObject(jsonString) // Parse it as JSON
                                            Log.i("SocketHandler", "Parsed JSON: $jsonObject")

                                            // Access specific values if needed
                                            val roomId = jsonObject.optString("roomId", "Unknown")
                                            if (roomId != null){
                                                val intent = Intent(this, ChatActivity::class.java)
                                                intent.putExtra("data", gameData)
                                                intent.putExtra("roomId",roomId)
                                                intent.putExtra("side","Group")
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                startActivity(intent)
                                            }
                                            Log.i("SocketHandler", "Extracted Room ID: $roomId")
                                        } catch (e: Exception) {
                                            Log.e("SocketHandler", "Error parsing JSON: ${e.message}")
                                        }
                                    }
                                }

                            }catch (e : Exception){
                                e.printStackTrace()
                            }
                        }
                    }

                }
                R.id.tvPlayer ->{
                    playerBottomSheet.show()
                }
                R.id.tvEditGame ->{
                    val intent = Intent(this , CreateHostGameActivity :: class.java)
                    intent.putExtra("gameData", gameData)
                    intent.putExtra("isFromEditGame", true) // Pass flag
                    startActivity(intent)
                }
            }
        })

    }

    private fun initAdapter() {
        playerAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_players, BR.bean){ v, m, pos ->

            val name = m.firstName + (m.lastName ?: "")
            when(v.id){
                R.id.playerCons->{
                    if (sharedPrefManager.getCurrentUser() != null){
                        if (gameData?.hostData?._id == sharedPrefManager.getCurrentUser()?.userId){

                            mSocket.on(Socket.EVENT_CONNECT) {

                            }

                            if (gameData?._id != null && m._id != null) {
                                try {
                                    Log.i("SocketHandler", "Socket connected. Joining room...")

                                    val jsonData = JSONObject().apply {
                                        put("type", 1)
                                        put("gameId", gameData?._id.toString())
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
                                                val jsonObject = JSONObject(jsonString) // Parse it as JSON
                                                val isBlocked = jsonObject.optBoolean("isBlock",false)
                                                Log.i("SocketHandler", "Parsed JSON: $jsonObject")

                                                // Access specific values if needed
                                                val roomId = jsonObject.optString("roomId", "Unknown")
                                                if (roomId != null){
                                                    val intent = Intent(this, ChatActivity::class.java)
                                                    intent.putExtra("data", gameData)
                                                    intent.putExtra("roomId",roomId)
                                                    intent.putExtra("isBlocked", isBlocked)
                                                    intent.putExtra("side","single")
                                                    intent.putExtra("player",name )
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                    startActivity(intent)
                                                }
                                                Log.i("SocketHandler", "Extracted Room ID: $roomId")
                                            } catch (e: Exception) {
                                                Log.e("SocketHandler", "Error parsing JSON: ${e.message}")
                                            }
                                        }
                                    }
                                }catch (e : Exception){
                                    e.printStackTrace()
                                }
                            }

                        }
                    }else{
                      //  loginBottomSheet.show()
                    }


                }
            }

        }
        playerBottomSheet.binding.rvPlayers.adapter =playerAdapter
        playerAdapter.notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewClick(view: View?) {
        when(view?.id){
            R.id.cancelGame ->{
//                val data = HashMap<String,Any>()
//                data["deletePost"] = false
//                viewModel.cancelGame(data,Constants.CANCEL_GAME+id)
//                cancelGameBottomSheet.dismiss()

                cancelPopup.show()




            }
            R.id.deletePost -> {
                deleteGamePopup.show()

            }
            R.id.cancel ->{
                cancelGameBottomSheet.dismiss()
            }
            R.id.tvYes ->{
                try {
                    // Define formatter to parse the game date
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")

                    // Convert gameDate to LocalDate
                    val parsedGameDate = ZonedDateTime.parse(gameDate, formatter).toLocalDate()

                    // Get the current date
                    val currentDate = LocalDate.now()

                    if (parsedGameDate.isEqual(currentDate)) {
                        // If game date matches current date, call API
                        val data = HashMap<String, Any>()
                        data["deletePost"] = false
                        viewModel.cancelGame(data, Constants.CANCEL_GAME + id)
                        cancelGameBottomSheet.dismiss()
                        cancelPopup.dismiss()
                    } else {
                        showToast("Game can only be canceled on the scheduled date.")
                        cancelGameBottomSheet.dismiss()
                        cancelPopup.dismiss()
                    }
                }catch ( e : Exception){
                    e.printStackTrace()
                }
            }
            R.id.tvNo ->{
                cancelPopup.dismiss()
                cancelGameBottomSheet.dismiss()
            }
            R.id.tvDeleteYes ->{
                val data = HashMap<String,Any>()
                data["deletePost"] = true
                viewModel.cancelGame(data,Constants.CANCEL_GAME+id)
                cancelGameBottomSheet.dismiss()
                deleteGamePopup.dismiss()
            }
            R.id.tvDeleteNo ->{
                deleteGamePopup.dismiss()
                cancelGameBottomSheet.dismiss()
            }

        }
    }
}