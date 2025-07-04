package com.pickup.sports.ui.home_modules.messages_module.player_messages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pickup.sports.BR
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.model.GetAllChatApiResponse
import com.pickup.sports.data.model.PlayerMessage
import com.pickup.sports.databinding.FragmentPlayerMessageBinding
import com.pickup.sports.databinding.ItemLayoutPlayerMessageBinding
import com.pickup.sports.ui.base.BaseFragment
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.base.SimpleRecyclerViewAdapter
import com.pickup.sports.ui.home_modules.chat_module.ChatActivity
import com.pickup.sports.ui.home_modules.home_fragment.HomeFragment
import com.pickup.sports.ui.home_modules.messages_module.game_group_messages.GroupMessageFragment
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.SocketHandler
import com.pickup.sports.utils.Status
import com.pickup.sports.utils.VerticalPagination
import com.pickup.sports.utils.event.SingleRequestEvent
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import org.json.JSONObject

@AndroidEntryPoint
class PlayerMessageFragment : BaseFragment<FragmentPlayerMessageBinding>() {

    private val viewModel : PlayerMessageFragmentVm by viewModels()
    var pagination: VerticalPagination? = null
    var currentPage = 1
    private lateinit var mSocket: Socket
    private var checkLoader : String ? = null
    private lateinit var playerAdapter : SimpleRecyclerViewAdapter<GetAllChatApiResponse.Chat, ItemLayoutPlayerMessageBinding>
    private val playerList = arrayListOf<PlayerMessage>()


    companion object{
        var refresh = SingleRequestEvent<Boolean>()
    }
    override fun onCreateView(view: View) {

        getAllChat()
        socketHandler()
        initAdapter()
        setObserver()

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
    private fun setObserver() {
        viewModel.obrCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
              Status.LOADING ->{
                  if (checkLoader == "hide"){
                      hideLoading()
                      checkLoader = ""
                  }
                  else{
                      progressDialogAvl.isLoading(true)
                  }
            }
               Status.SUCCESS ->{
                   hideLoading()
                   when(it.message){
                       "getAllChats" ->{
                           val myDataModel : GetAllChatApiResponse ?= ImageUtils.parseJson(it.data.toString())
                           if (myDataModel != null){
                               if (myDataModel.chats != null){
                                   if (currentPage <= myDataModel.pagination?.totalPages!!){
                                       pagination?.isLoading = false
                                   }
                                   if (currentPage == 1){
                                       playerAdapter.list = myDataModel.chats
                                   }else{
                                       playerAdapter.addToList(myDataModel.chats)
                                   }
//                                   playerAdapter.list = myDataModel.chats
                               }
                           }

                           if (playerAdapter.list.isNullOrEmpty()){
                               binding.textNoDataaa.visibility = View.VISIBLE
                               binding.rvPlayerMessage.visibility = View.GONE
                           }
                           else{
                               binding.textNoDataaa.visibility = View.GONE
                               binding.rvPlayerMessage.visibility = View.VISIBLE

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

    private fun getAllChat() {
        val data = HashMap<String,Any>()
        data["page"] = 1
        data["limit"] = "20"
        viewModel.getAllChats(data,Constants.GET_ALL_CHATS)
    }

    private fun initAdapter() {
        playerAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_player_message,BR.bean){v,m,pos ->
            when(v.id){
                R.id.consMain ->{
                    mSocket.on(Socket.EVENT_CONNECT) {

                    }
                    try {
                        Log.i("SocketHandler", "Socket connected. Joining room...")
                            val jsonData = JSONObject().apply {
//                                put("type", 1)
//                                put("gameId", JSONObject.NULL)
//                                put("chatId", m._id)


                                put("type", 1)
                                put("to", m.participants?.get(0)?._id)
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
                                        Log.i("SocketHandler", "Parsed JSON: $jsonObject")

                                        // Access specific values if needed
                                        val roomId = jsonObject.optString("roomId", "Unknown")
                                        val isBlocked = jsonObject.optBoolean("isBlock",false)
                                        Log.i("SocketHandler", "room id: $roomId , $isBlocked")
                                        if (roomId != null){
                                            val firstName = m.participants?.getOrNull(0)?.firstName.orEmpty()
                                            val lastName = m.participants?.getOrNull(0)?.lastName

                                            val fullName = if (!lastName.isNullOrBlank()) {
                                                "$firstName $lastName"
                                            } else {
                                                firstName
                                            }


                                            val intent = Intent(requireContext(), ChatActivity::class.java)
                                                intent.putExtra("roomId",roomId)
                                                intent.putExtra("chatType","anyPlayer")
                                                 intent.putExtra("playerName", fullName)
                                                 intent.putExtra("isBlocked", isBlocked)
                                                 intent.putExtra("side","anyPlayer")

                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            startActivity(intent)
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
//                    val intent  = Intent(requireContext(), ChatActivity::class.java)
//                    startActivity(intent)
                }
            }
        }
        binding.rvPlayerMessage.adapter = playerAdapter
        val lm = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        pagination = VerticalPagination(lm, 2)
       pagination?.setListener(object : VerticalPagination.VerticalScrollListener {
            override fun onLoadMore() {
                currentPage++
                Log.i("dsadasd", "onLoadMore: ${currentPage}")
                val data = HashMap<String, Any>()
                data["page"] = currentPage.toString()
                viewModel.getAllChats(data,Constants.GET_ALL_CHATS)

            }
        })
        pagination?.let {
            binding.rvPlayerMessage.addOnScrollListener(it)
        }
        playerAdapter.notifyDataSetChanged()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_player_message
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onResume() {
        getAllChat()
        super.onResume()

      refresh.observe(viewLifecycleOwner) {
            Log.i("fdfdsfs", "onResume:${refresh} ")
            when (it?.status) {
                Status.LOADING -> {

                }

                Status.SUCCESS -> {
                    checkLoader = "hide"
                    Log.i("fdfdsfs", "onResume: ${refresh}")
                    getAllChat()
                }

                Status.ERROR -> {

                }

                else -> {

                }
            }
        }
    }

}