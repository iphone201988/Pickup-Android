package com.pickup.sports.ui.home_modules.messages_module.game_group_messages

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
import com.pickup.sports.data.model.GetGroupChatApiResponse
import com.pickup.sports.data.model.GroupMessage
import com.pickup.sports.databinding.FragmentGroupMessageBinding
import com.pickup.sports.databinding.ItemLayoutGroupMessagesBinding
import com.pickup.sports.ui.base.BaseFragment
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.base.SimpleRecyclerViewAdapter
import com.pickup.sports.ui.home_modules.HomeDashBoardActivity
import com.pickup.sports.ui.home_modules.chat_module.ChatActivity
import com.pickup.sports.ui.home_modules.game_details.GameDetailsActivity
import com.pickup.sports.ui.home_modules.home_fragment.HomeFragment
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.NotificationClass
import com.pickup.sports.utils.SocketHandler
import com.pickup.sports.utils.Status
import com.pickup.sports.utils.VerticalPagination
import com.pickup.sports.utils.event.SingleRequestEvent
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import org.json.JSONObject
import kotlin.math.log


@AndroidEntryPoint
class GroupMessageFragment : BaseFragment<FragmentGroupMessageBinding>() {
    private val viewModel: GroupMessageFragmentVm by viewModels()
    private lateinit var groupMessageAdapter: SimpleRecyclerViewAdapter<GetGroupChatApiResponse.Game, ItemLayoutGroupMessagesBinding>
    var pagination: VerticalPagination? = null
    var currentPage = 1
    private var checkLoader : String ? = null
    private lateinit var mSocket: Socket

    companion object{
       // var callApi : Boolean = false
        var refresh = SingleRequestEvent<Boolean>()
    }
    override fun onCreateView(view: View) {
        getGroupMessageData()
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
            when (it?.status) {
                Status.LOADING -> {
                    if (checkLoader == "hide"){
                        hideLoading()
                        checkLoader = ""
                    }
                    else{
                        progressDialogAvl.isLoading(true)
                    }

                }

                Status.SUCCESS -> {
                    hideLoading()
                    val myDataModel: GetGroupChatApiResponse? =
                        ImageUtils.parseJson(it.data.toString())
                    if (myDataModel != null) {
                        if (myDataModel.games != null) {
                            if (currentPage <= myDataModel.totalPages!!){
                                pagination?.isLoading = false
                            }
                            if (currentPage == 1){
                                groupMessageAdapter.list = myDataModel.games
                            }else{
                                groupMessageAdapter.addToList(myDataModel.games)
                            }
//                            groupMessageAdapter.list = myDataModel.games
                        }
                    }
                    if (groupMessageAdapter.list.isNullOrEmpty()) {
                        binding.textNoDataaa.visibility = View.VISIBLE
                        binding.rvGroupMessage.visibility = View.GONE

                    } else {
                        binding.textNoDataaa.visibility = View.GONE
                        binding.rvGroupMessage.visibility = View.VISIBLE
                    }
                }

                Status.ERROR -> {
                    hideLoading()
                    showToast(it.message.toString())
                }

                else -> {

                }
            }
        })
    }

    private fun getGroupMessageData() {
        val data = HashMap<String, Any>()
        data["page"] = 1
//        data["limit"] = "10"
        viewModel.getGroupChat(data, Constants.GET_GROUP_CHAT)
    }

    private fun initAdapter() {
        groupMessageAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_group_messages, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.consMain -> {
                        mSocket.on(Socket.EVENT_CONNECT) {

                        }
                        if (m.chatId != null) {
                            try {
                                Log.i("SocketHandler", "Socket connected. Joining host room...")

                                val jsonData = JSONObject().apply {
                                    put("type", 2)
                                    put("chatId", m.chatId)

                                }
                                // Emit the joinRoom event with the prepared data
                                mSocket.emit("joinRoom", jsonData)
                                Log.i("SocketHandler", "Emitted joinRoom event: $jsonData")

                                // Listen for the response
                                mSocket.on("roomId") { args ->
                                    Log.i(
                                        "SocketHandler",
                                        "Received join host Room response: $args"
                                    )

                                    if (args.isNotEmpty()) {
                                        try {
                                            val jsonString =
                                                args[0].toString()  // Convert object to String
                                            val jsonObject =
                                                JSONObject(jsonString) // Parse it as JSON
                                            Log.i("SocketHandler", "Parsed JSON: $jsonObject")

                                            // Access specific values if needed
                                            val roomId = jsonObject.optString("roomId", "Unknown")
                                            if (roomId != null) {
                                                val intent = Intent(
                                                    requireContext(),
                                                    ChatActivity::class.java
                                                )
                                                intent.putExtra("roomId", roomId)
                                                intent.putExtra("gameId",m._id)
                                                intent.putExtra("gameName",m.sportsData?.name)
                                                intent.putExtra("side", "Group")
                                                intent.putExtra("chatType","groupPlayer")

                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                startActivity(intent)
                                            }
                                            Log.i("SocketHandler", "Extracted Room ID: $roomId")
                                        } catch (e: Exception) {
                                            Log.e(
                                                "SocketHandler",
                                                "Error parsing JSON: ${e.message}"
                                            )
                                        }
                                    }
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
                binding.rvGroupMessage.adapter = groupMessageAdapter
                val lm = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                pagination = VerticalPagination(lm, 2)
                pagination?.setListener(object : VerticalPagination.VerticalScrollListener {
                    override fun onLoadMore() {
                        currentPage++
                        Log.i("dsadasd", "onLoadMore: ${currentPage}")
                        val data = HashMap<String, Any>()
                        data["page"] = currentPage.toString()
                        viewModel.getGroupChat(data, Constants.GET_GROUP_CHAT)

                    }
                })
                pagination?.let {
                    binding.rvGroupMessage.addOnScrollListener(it)
                }
                groupMessageAdapter.notifyDataSetChanged()



    }
    override fun getLayoutResource(): Int {
        return R.layout.fragment_group_message
    }

    override fun getViewModel(): BaseViewModel {
       return viewModel
    }

    override fun onResume() {
        super.onResume()
        getGroupMessageData()

      refresh.observe(viewLifecycleOwner) {
            Log.i("fdfdsfs", "onResume:${refresh} ")
            when (it?.status) {
                Status.LOADING -> {

                }

                Status.SUCCESS -> {
                    checkLoader = "hide"
                    Log.i("fdfdsfs", "onResume: ${refresh}")

                  getGroupMessageData()
                }

                Status.ERROR -> {

                }

                else -> {

                }
            }
        }

    }

}