package com.pickup.sports.ui.home_modules.chat_module

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.model.BlockChatApiResponse
import com.pickup.sports.data.model.GetChatMessageApiResponse
import com.pickup.sports.data.model.GetGameByIdApiResponse
import com.pickup.sports.data.model.MessageData
import com.pickup.sports.data.model.MessageModel
import com.pickup.sports.data.model.PushNotificationModel
import com.pickup.sports.databinding.ActivityChatBinding
import com.pickup.sports.databinding.BottomSheetBlockUserBinding
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.utils.BaseCustomBottomSheet
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.SocketHandler
import com.pickup.sports.utils.Status
import com.pickup.sports.utils.VerticalPagination
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Ack
import io.socket.client.Socket
import org.json.JSONObject

@AndroidEntryPoint
class ChatActivity : BaseActivity<ActivityChatBinding>(), BaseCustomBottomSheet.Listener {

    private var gameDetail: GetGameByIdApiResponse.Game? = null
    private lateinit var blockBottomSheet: BaseCustomBottomSheet<BottomSheetBlockUserBinding>

    private val viewModel: ChatActivityVm by viewModels()
    private lateinit var chatAdapter: ChatAdapter
    private var chaList = arrayListOf<MessageModel>()
    private var roomId: String? = null
    private var side: String? = null
    private var id: String? = null
    private var chatDataType : String ? = null
    private var gameId : String ? = null
    private lateinit var mSocket: Socket
    private var isLoading = false
    private var pagination: VerticalPagination? = null
    private var notificationData : PushNotificationModel ? = null
    private var isBlocked : Boolean ? = null
    var page = 1
    override fun getLayoutResource(): Int {
        return R.layout.activity_chat
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        initOnClick()
        id = sharedPrefManager.getCurrentUser()?.userId
        initBottomSheet()
        getData()

        receivedMessage()
        initAdapter()
        setObserver()


        val lm = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rvChat.layoutManager = lm
        binding.rvChat.adapter = chatAdapter

//        binding.rvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            private var previousFirstVisibleItem = 0
//            private var previousDy = 0
//
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
//                val totalItemCount = layoutManager.itemCount
//
//                // Track scroll direction more reliably
//                val isScrollingUp = dy < 0 || (dy == 0 && previousDy < 0)
//                previousDy = dy
//
//                // Check if we've reached the top threshold and are scrolling up
//                if (isScrollingUp && firstVisibleItem <= 3 && firstVisibleItem < previousFirstVisibleItem && !isLoading) {
//                    isLoading = true
//                    page++
//                    loadPreviousMessages()
//                }
//
//                previousFirstVisibleItem = firstVisibleItem
//
//                // Additional check for initial load when at top
//                if (firstVisibleItem <= 3 && totalItemCount > 0 && !isLoading && !recyclerView.canScrollVertically(-1)) {
//                    isLoading = true
//                    page++
//                    loadPreviousMessages()
//                }
//            }
//        })

        binding.rvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var previousFirstVisibleItem = -1
            private var previousDy = 0

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                val isScrollingUp = dy < 0 || (dy == 0 && previousDy < 0)
                previousDy = dy

                Log.d("ScrollCheck", "dy=$dy, isScrollingUp=$isScrollingUp")
                Log.d("ScrollCheck", "firstVisibleItem=$firstVisibleItem, previousFirstVisibleItem=$previousFirstVisibleItem")
                Log.d("ScrollCheck", "isLoading=$isLoading")

                if (previousFirstVisibleItem == -1) {
                    previousFirstVisibleItem = firstVisibleItem
                }

                if (isScrollingUp && firstVisibleItem <= 3 && firstVisibleItem < previousFirstVisibleItem && !isLoading) {
                    Log.d("ScrollTrigger", "Pagination triggered at page=$page")
                    isLoading = true
                    page++
                    loadPreviousMessages()
                }

                previousFirstVisibleItem = firstVisibleItem
            }
        })











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
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun initBottomSheet() {
        blockBottomSheet = BaseCustomBottomSheet(this, R.layout.bottom_sheet_block_user, this)
        blockBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        blockBottomSheet.behavior.isDraggable = true
    }

    private fun receivedMessage() {
        mSocket.on("receiveMessage") { data ->
            val jsonData = data[0] as JSONObject
            Log.i("newMessage", "Received: $jsonData")

            try {

                val messageData: MessageData? = ImageUtils.parseJson(jsonData.toString())
                Log.i("newMessage", "receivedMessage: $messageData ")
                if (messageData != null && messageData.message != null) {
                    //val chatData = convertToChatHistoryApiResponseData(messageData.data)
                    // Add the new message to the chat adapter and refresh the RecyclerView
                    Log.i("dfdfd", "receivedMessage: ${messageData.message} ")

                    val chatData = convertToChatHistoryApiResponseData(messageData.message!!)
                    runOnUiThread {
                        chatAdapter.addData(chatData)
                        chatAdapter.notifyDataSetChanged()
                        binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
                    }


                } else {
                    Log.e("newMessage", "Failed to parse message data")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        mSocket.on("receiveMessageByAnonymousPlayer") { data ->
            val jsonData = data[0] as JSONObject
            Log.i("newMessage", "Received: $jsonData")

            try {

                val messageData: MessageData? = ImageUtils.parseJson(jsonData.toString())
                Log.i("newMessage", "receiveMessageByAnonymousPlayer: $messageData ")
                if (messageData != null && messageData.message != null) {
                    //val chatData = convertToChatHistoryApiResponseData(messageData.data)
                    // Add the new message to the chat adapter and refresh the RecyclerView
                    Log.i("dfdfd", "receiveMessageByAnonymousPlayer: ${messageData.message} ")

                    val chatData = convertToChatHistoryApiResponseData(messageData.message!!)
                    runOnUiThread {
                        chatAdapter.addData(chatData)
                        chatAdapter.notifyDataSetChanged()
                        binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
                    }


                } else {
                    Log.e("newMessage", "Failed to parse message data")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    private fun convertToChatHistoryApiResponseData(messageData: MessageData.MessageResponse): GetChatMessageApiResponse.Message {
        return GetChatMessageApiResponse.Message(
            __v = messageData.__v,
            _id = messageData._id,
            chatId = messageData.chatId,
            createdAt = messageData.createdAt,
            gameId = messageData.gameId,
            isSeen = messageData.isSeen,
            message = messageData.message,
            updatedAt = messageData.updatedAt,
            userId = messageData.userId?.let { user ->
                GetChatMessageApiResponse.Message.UserId(
                    _id = user._id,
                    firstName = user.firstName,
                    lastName = user.lastName
                )
            }
        )
    }


    private fun setObserver() {
        viewModel.obrCommon.observe(this, Observer {
            when (it?.status) {
                Status.LOADING -> {
                    progressDialogAvl.isLoading(true)
                }

                Status.SUCCESS -> {
                    hideLoading()
                    when (it.message) {
//                        "getChatMessage" -> {
//                            val myDataModel: GetChatMessageApiResponse? =
//                                ImageUtils.parseJson(it.data.toString())
//                            if (myDataModel != null) {
//                                if (myDataModel.messages != null) {
//                                    if (myDataModel.messages!!.isNotEmpty())
//                                        chatAdapter.setList(myDataModel.messages!!)
//                                    chatAdapter.notifyDataSetChanged()
//                                    binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
//                                }
//                            }
//                        }



//                        "getChatMessage" -> {
//                            val myDataModel: GetChatMessageApiResponse? =
//                                ImageUtils.parseJson(it.data.toString())
//                            if (myDataModel != null) {
//                                if (myDataModel.messages != null) {
//                                    try {
//                                        if (page <= myDataModel.totalPages!!){
//                                            pagination?.isLoading = false
//                                        }
//                                        if (page == 1){
//                                            chatAdapter.setList(myDataModel.messages!!)
//                                            binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
//                                        }
//                                        else{
//                                            chatAdapter.addToList(myDataModel.messages!!)
//                                        }
//                                    }catch (e : Exception){
//                                        e.printStackTrace()
//                                    }
//                                }
//                            }
//                        }


//                        "getChatMessage" -> {
//                            val myDataModel: GetChatMessageApiResponse? = ImageUtils.parseJson(it.data.toString())
//                            if (myDataModel?.messages != null) {
//                                try {
//                                    if (page <= myDataModel.totalPages!!) {
//                                        pagination?.isLoading = false
//                                    }
//
//                                    val groupedMessages = chatAdapter.groupMessagesWithDateHeaders(myDataModel.messages!!)
//
//                                    if (page == 1) {
//                                        chatAdapter.setList(groupedMessages.reversed())
//                                        binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
//                                    } else {
//                                        chatAdapter.addToList(groupedMessages.reversed())
//                                    }
//                                } catch (e: Exception) {
//                                    e.printStackTrace()
//                                }
//                            }
//                        }


//                        "getChatMessage" -> {
//                            val myDataModel: GetChatMessageApiResponse? = ImageUtils.parseJson(it.data.toString())
//                            if (myDataModel?.messages != null) {
//                                try {
//                                    Log.i("dasdadaa", "setObserver: ${myDataModel.messages}")
//                                    if (page <= myDataModel.totalPages!!) {
//                                        pagination?.isLoading = false
//                                        isLoading = false
//                                    }
//
//                                    // 🔄 Reverse only for pages beyond 1
//                                    val orderedMessages = if (page != 1) {
//                                        myDataModel.messages!!.reversed()
//                                    } else {
//                                        myDataModel.messages!!
//                                    }
//
//                                    val prevLastDate = chatAdapter.lastInsertedDateHeader
//
//                                    // ✅ Determine the first relevant date of this page's data
//                                    val firstRelevantDate = if (page != 1 && orderedMessages.isNotEmpty()) {
//                                        chatAdapter.extractOnlyDate(orderedMessages.last().createdAt)
//                                    } else if (orderedMessages.isNotEmpty()) {
//                                        chatAdapter.extractOnlyDate(orderedMessages.first().createdAt)
//                                    } else {
//                                        null
//                                    }
//
//                                    // 🧠 If it’s the same as previous page, treat it as known
//                                    val adjustedLastDate = if (firstRelevantDate == prevLastDate) firstRelevantDate else null
//
//                                    val (groupedMessages, newLastDate) =
//                                        chatAdapter.groupMessagesWithDateHeaders(orderedMessages, adjustedLastDate)
//
//                                    chatAdapter.lastInsertedDateHeader = newLastDate
//
//                                    if (page == 1) {
//                                        chatAdapter.setList(groupedMessages)  // 🔧 Handles duplicate date header inside
//                                        binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
//                                    } else {
//                                        chatAdapter.addToList(groupedMessages)
//                                    }
//
//                                } catch (e: Exception) {
//                                    e.printStackTrace()
//                                }
//                            }
//                        }


                        "getChatMessage" -> {
                            val myDataModel: GetChatMessageApiResponse? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel?.messages != null) {
                                try {
                                    Log.i("dasdadaa", "setObserver: ${myDataModel.messages}")
                                    if (page <= myDataModel.totalPages!!) {
                                        pagination?.isLoading = false
                                        isLoading = false
                                    }

                                    // 🔄 Reverse only for pages beyond 1
                                    val orderedMessages = if (page != 1) {
                                        myDataModel.messages!!.reversed()
                                    } else {
                                        myDataModel.messages!!
                                    }

                                    val prevLastDate = chatAdapter.lastInsertedDateHeader

                                    // ✅ Determine the first relevant date of this page's data
                                    val firstRelevantDate = if (page != 1 && orderedMessages.isNotEmpty()) {
                                        chatAdapter.extractOnlyDate(orderedMessages.last().createdAt)
                                    } else if (page == 1 && orderedMessages.isNotEmpty()) {
                                        chatAdapter.extractOnlyDate(orderedMessages.first().createdAt)
                                    } else {
                                        null
                                    }

                                    // 🧠 If it’s the same as previous page, treat it as known
                                    val adjustedLastDate = if (firstRelevantDate == prevLastDate) firstRelevantDate else null


                                    Log.i("dsdsdsfds", "erere: $firstRelevantDate , $prevLastDate")

                                    Log.i("dsdsdsfds", "setObserver: $adjustedLastDate")

                                    val (groupedMessages, newLastDate) =
                                        chatAdapter.groupMessagesWithDateHeaders(orderedMessages, adjustedLastDate)

                                    Log.i("Dsadasd", "setObserver:  $newLastDate")
                                    chatAdapter.lastInsertedDateHeader = newLastDate

                                    Log.d("asfasasfa", "setObserver: ${groupedMessages}")
                                    if (page == 1) {
                                        chatAdapter.setList(groupedMessages)  // 🔧 Handles duplicate date header inside
                                        binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
                                    }
                                    else {
                                        chatAdapter.addToTop(groupedMessages)
                                    }

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }



                        "block" -> {
                            val myDataModel: BlockChatApiResponse? =
                                ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null) {
                                isBlocked = myDataModel.isBlocked
                                if (isBlocked == true){
                                    blockBottomSheet.binding.tvBlock.text = "Unblock"
                                    binding.sendChat.isFocusable = false
                                    binding.sendChat.isEnabled = false
                                    binding.sendChat.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grey))

                                }
                                else{
                                    blockBottomSheet.binding.tvBlock.text = "Block"
                                    binding.sendChat.isFocusable = true
                                    binding.sendChat.isEnabled = true
                                    binding.sendChat.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.heading_color))
                                }
                                blockBottomSheet.dismiss()
                            }
                        }
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

    private fun getData() {
        chatDataType = intent.getStringExtra("chatType")
         notificationData = intent.getParcelableExtra<PushNotificationModel>("chatDAta")
        Log.i("fdfdsf", "getData: $notificationData")
        if (notificationData != null) {
            socketHandler()
            roomId = notificationData?.ChatId
            binding.heading.text = notificationData?.name



            if (notificationData?.chatType == "1") {
                if (notificationData?.ChatId!= null && notificationData?.chatType != null && notificationData?.to != null) {
                    try {
                        Log.i("SocketHandler", "Socket connected. Joining room...")

                        val jsonData = JSONObject().apply {
                            if (notificationData?.ChatId != null){
                                put("chatId", notificationData?.ChatId)
                            }
                            else{
                                put("type",1)
                                put("to", notificationData?.to)
                            }


                        }

                        // Emit the joinRoom event with the prepared data
                        mSocket.emit("joinRoom", jsonData)
                        Log.i("SocketHandler", "Emitted joinRoom event: $jsonData")

                        mSocket.on("roomId") { args ->
                            Log.i("SocketHandler", "Received join host Room response: $args")
                        }

                        if (notificationData?.ChatId != null) {
                            val data = HashMap<String,Any>()
                            data["page"] = 1
                            data["limit"] = 40
//                            viewModel.getChatMessage(Constants.GET_CHAT_MESSAGES + notificationData?.ChatId + "/" + notificationData?.gameId,data)
                            viewModel.getChatMessage(Constants.GET_CHAT_MESSAGES + notificationData?.ChatId + "/" + notificationData?.gameId,data)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            } else if (notificationData?.chatType == "3") {
                try {
                    Log.i("SocketHandler", "Socket connected. Joining host room...")

                    val jsonData = JSONObject().apply {
                        if (notificationData?.ChatId != null){
                            put("chatId", notificationData?.ChatId)
                        }
                        else{
                            put("type", 2)
                            put("gameId", notificationData?.gameId)
                        }
                    }
                    // Emit the joinRoom event with the prepared data
                    mSocket.emit("joinRoom", jsonData)
                    Log.i("SocketHandler", "Emitted joinRoom group event: $jsonData")

                    mSocket.on("roomId") { args ->
                        Log.i("SocketHandler", "Received join host Room response: $args")
                    }
                    if (notificationData?.gameId != null && notificationData?.ChatId != null) {
                        val data = HashMap<String,Any>()
                        data["page"] = 1
                        data["limit"] = 40
                        viewModel.getChatMessage(Constants.GET_CHAT_MESSAGES + notificationData?.ChatId + "/" + notificationData?.gameId,data)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()

                }
            }
            else {
                if (notificationData?.gameId != null && notificationData?.chatType != null && notificationData?.to != null) {
                    try {
                        Log.i("SocketHandler", "Socket connected. Joining room...")

                        val jsonData = JSONObject().apply {
                            if (notificationData?.ChatId != null){
                                put("chatId", notificationData?.ChatId)

                            }
                            else{
                                put("type",1)
                                put("gameId", notificationData?.gameId)
                                put("to", notificationData?.to)
                            }


                        }

                        // Emit the joinRoom event with the prepared data
                        mSocket.emit("joinRoom", jsonData)
                        Log.i("SocketHandler", "Emitted joinRoom event: $jsonData")

                        mSocket.on("roomId") { args ->
                            Log.i("SocketHandler", "Received join host Room response: $args")
                        }

                        if (notificationData?.gameId != null && notificationData?.ChatId != null) {
                            val data = HashMap<String,Any>()
                            data["page"] = 1
                            data["limit"] = 40
                            viewModel.getChatMessage(Constants.GET_CHAT_MESSAGES + notificationData?.ChatId + "/" + notificationData?.gameId,data)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
            }
            }


        } else {
            if (chatDataType == "anyPlayer"){
                mSocket = SocketHandler.getSocket()!!
                roomId = intent.getStringExtra("roomId")
                side = intent.getStringExtra("side")
                isBlocked = intent.getBooleanExtra("isBlocked",false)
                if (isBlocked == true){
                    blockBottomSheet.binding.tvBlock.text = "Unblock"
                    binding.sendChat.isFocusable = false
                    binding.sendChat.isEnabled = false
                    binding.sendChat.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grey))

                }

                val name = intent.getStringExtra("playerName")
                if (name != null){
                    binding.heading.text = name
                }
                Log.i("fsfsd", "getData: player messages")

                if (roomId != null) {
                    val data = HashMap<String,Any>()
                    data["page"] = 1
                    data["limit"] = 40
                    viewModel.getChatMessage(Constants.GET_CHAT_MESSAGES+roomId,data)
                }
            }
            else if (chatDataType == "groupPlayer"){
                mSocket = SocketHandler.getSocket()!!
                roomId = intent.getStringExtra("roomId")
                side = intent.getStringExtra("side")
                 gameId =  intent.getStringExtra("gameId")
                val name = intent.getStringExtra("gameName")
                 binding.heading.text = name
                if (roomId != null && gameId != null) {
                    val data = HashMap<String,Any>()
                    data["page"] = 1
                    data["limit"] = 40
                    viewModel.getChatMessage(Constants.GET_CHAT_MESSAGES + roomId + "/" + gameId,data)
                }
            }
            else{
                mSocket = SocketHandler.getSocket()!!
                gameDetail = intent.getParcelableExtra<GetGameByIdApiResponse.Game>("data")
                roomId = intent.getStringExtra("roomId")
                side = intent.getStringExtra("side")
                isBlocked = intent.getBooleanExtra("isBlocked",false)
                val playerName = intent.getStringExtra("player")

                if (gameDetail != null) {
                    if (side == "single") {
                        if (playerName != null){
                            binding.heading.text = playerName
                        }else{
                            val firstName = gameDetail?.hostData?.firstName
                                ?: ""  // Fallback to empty string if firstName is null
                            val lastName = gameDetail?.hostData?.lastName
                                ?: ""    // Fallback to empty string if lastName is null

                            binding.heading.text = "$firstName $lastName"
                        }

                    } else {
                        binding.heading.text = gameDetail?.sportsData?.name
                    }
                }

                if (isBlocked == true){
                    blockBottomSheet.binding.tvBlock.text = "Unblock"
                    binding.sendChat.isFocusable = false
                    binding.sendChat.isEnabled = false
                    binding.sendChat.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.grey))

                }

                if (roomId != null && gameDetail?._id != null) {
                    val data = HashMap<String,Any>()
                    data["page"] = 1
                    data["limit"] = 40
                    viewModel.getChatMessage(Constants.GET_CHAT_MESSAGES + roomId + "/" + gameDetail?._id,data)
                }
            }


        }


    }


    private fun initAdapter() {
        chatAdapter = ChatAdapter(this, id)
        binding.rvChat.adapter = chatAdapter
//        val lm = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
//        pagination = VerticalPagination(lm, 2)
//        pagination?.setListener(object : VerticalPagination.VerticalScrollListener {
//            override fun onLoadMore() {
//                page++
//                if (notificationData != null){
//                    if (notificationData?.gameId != null && notificationData?.ChatId != null) {
//                        val data = HashMap<String,Any>()
//                        data["page"] = page
//                        data["limit"] = 40
//                        viewModel.getChatMessage(Constants.GET_CHAT_MESSAGES + notificationData?.ChatId + "/" + notificationData?.gameId,data)
//                    }
//                } else{
//                    if (chatDataType == "anyPlayer"){
//                        if (roomId != null) {
//                            val data = HashMap<String,Any>()
//                            data["page"] = page
//                            data["limit"] = 10
//                            viewModel.getChatMessage(Constants.GET_CHAT_MESSAGES+roomId,data)
//                        }
//                    }
//                    else if (chatDataType == "groupPlayer"){
//                        if (roomId != null && gameId != null) {
//                            val data = HashMap<String,Any>()
//                            data["page"] = 1
//                            data["limit"] = 40
//                            viewModel.getChatMessage(Constants.GET_CHAT_MESSAGES + roomId + "/" + gameId,data)
//                        }
//                    }
//                    else{
//                        if (roomId != null && gameDetail?._id  != null){
//                            val data = HashMap<String,Any>()
//                            data["page"] = page
//                            data["limit"] = 40
//                            viewModel.getChatMessage(Constants.GET_CHAT_MESSAGES + roomId + "/" + gameDetail?._id,data)
//                        }
//                    }
//
//                }
//            }
//        })
//        pagination?.let {
//            binding.rvChat.addOnScrollListener(it)
//        }
    }


    private fun loadPreviousMessages() {
        val data = HashMap<String, Any>()
        data["page"] = page
        data["limit"] = 40

        if (notificationData != null) {
            if (notificationData?.gameId != null && notificationData?.ChatId != null) {
                viewModel.getChatMessage(
                    Constants.GET_CHAT_MESSAGES + notificationData?.ChatId + "/" + notificationData?.gameId,
                    data
                )
            }
        } else {
            if (chatDataType == "anyPlayer") {
                if (roomId != null) {
                    data["limit"]  = 40
                    viewModel.getChatMessage(Constants.GET_CHAT_MESSAGES + roomId, data)
                }
            } else if (chatDataType == "groupPlayer") {
                if (roomId != null && gameId != null) {
                    viewModel.getChatMessage(
                        Constants.GET_CHAT_MESSAGES + roomId + "/" + gameId,
                        data
                    )
                }
            } else {
                if (roomId != null && gameDetail?._id != null) {
                    viewModel.getChatMessage(
                        Constants.GET_CHAT_MESSAGES + roomId + "/" + gameDetail?._id,
                        data
                    )
                }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun initOnClick() {
        viewModel.onClick.observe(this, Observer {
            when (it?.id) {
                R.id.ivBack -> {
                    onBackPressedDispatcher.onBackPressed()

                }

                R.id.sendChat -> {
                    if (binding.etChat.text.toString().trim().isNotEmpty()) {
                        val data = binding.etChat.text.toString().trim()

                        roomId?.let { sendMessage(data, it) }
                        binding.etChat.setText("")

                    } else {
                        showToast("Please enter message")
                    }
                }

                R.id.iv_threeDots -> {
                    blockBottomSheet.show()
                }
            }
        })

    }

    fun sendMessage(message: String, roomId: String) {
        try {
            if (mSocket == null || !mSocket.connected()) {
                Log.e("SocketHandler", "Socket is not connected. Cannot send message.")
                return
            }


            val jsonData = JSONObject().apply {
                put("message", message)
                put("roomId", roomId)
            }
            val anonymousJsonData = JSONObject().apply {
                put("message",message)
                put("chatId", roomId)
            }

            if (chatDataType == "anyPlayer"){
                Log.i("SocketHandler", "Sending anonymousData message: $anonymousJsonData") // Debug log
                mSocket.emit("sendMessageToAnonymousPlayer",anonymousJsonData,Ack{ args ->
                    Log.i(
                        "SocketHandler",
                        "single player sent successfully, server response: ${args.joinToString()}"
                    )
                })
            }
            else{
                Log.i("SocketHandler", "Sending message: $jsonData") // Debug log
                mSocket.emit("sendMessage", jsonData, Ack { args ->
                    Log.i(
                        "SocketHandler",
                        "Message sent successfully, server response: ${args.joinToString()}"
                    )
                })
            }



        } catch (e: Exception) {
            Log.e("SocketHandler", "Error sending message: ${e.message}")
        }
    }

    override fun onViewClick(view: View?) {
        when (view?.id) {
            R.id.tvBlock -> {
                if (side == "single") {
                    if (gameDetail?.hostChatWithLoggedInUser != null){
                        val data = HashMap<String, Any>()
                        data["chatId"] = gameDetail?.hostChatWithLoggedInUser.toString()
                        data["type"] = 1
                        viewModel.block(data, Constants.CHAT_BLOCK_UNBLOCK)
                    }
                    else{
                        val data = HashMap<String, Any>()
                        data["chatId"] = roomId.toString()
                        data["type"] = 1
                        viewModel.block(data, Constants.CHAT_BLOCK_UNBLOCK)
                    }

                }
                else if (side == "anyPlayer"){
                    val data = HashMap<String, Any>()
                    data["chatId"] = roomId.toString()
                    data["type"] = 1
                    viewModel.block(data, Constants.CHAT_BLOCK_UNBLOCK)
                }
                else {
                    val data = HashMap<String, Any>()
                    data["chatId"] = gameDetail?.chatId.toString()
                    data["type"] = 2
                    viewModel.block(data, Constants.CHAT_BLOCK_UNBLOCK)
                }

            }

            R.id.tvCancel -> {
                blockBottomSheet.dismiss()
            }
        }
    }

    override fun onDestroy() {
     //   mSocket.disconnect()
        super.onDestroy()
    }
}