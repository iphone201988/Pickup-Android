package com.pickup.sports.ui.home_modules.chat_module

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.pickup.sports.R
import com.pickup.sports.data.model.ChatItem
import com.pickup.sports.data.model.GetChatMessageApiResponse
import com.pickup.sports.data.model.GetChatMessageApiResponse.Message
import com.pickup.sports.utils.ImageUtils
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

//class ChatAdapter (private val context: Context, private val id : String?) : RecyclerView.Adapter<ViewHolder>(){
//
//    var messageList = ArrayList<Message>()
//
//    private val SEND_USER = 1
//    private val RECEIVE_USER = 0
//    private val DATE_HEADER = 2
//
//
//    class SendViewHolder(itemView: View) : ViewHolder(itemView) {
//        val sendMessage: TextView = itemView.findViewById(R.id.rightChat)
//        val sentTime : TextView = itemView.findViewById(R.id.sendTime)
//        val sentProfileName : TextView = itemView.findViewById(R.id.sentProfileName)
//
//    }
//
//    class ReceiveViewHolder(itemView: View) : ViewHolder(itemView) {
//        val receiveMessage: TextView = itemView.findViewById(R.id.leftChat)
//        val receiveTime  : TextView = itemView.findViewById(R.id.receiveTime)
//        val receiveProfileName : TextView = itemView.findViewById(R.id.receiveProfileNAme)
//
//    }
//
//    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val dateText: TextView = itemView.findViewById(R.id.date)
//    }
//
//
////    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
////        return if (viewType == SEND_USER) {
////            val view = LayoutInflater.from(context).inflate(R.layout.item_layout_send_chat,parent,false)
////            return SendViewHolder(view)
////        } else {
////            val view = LayoutInflater.from(context).inflate(R.layout.item_layout_receive_chat, parent, false)
////            return ReceiveViewHolder(view)
////        }
////    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        val inflater = LayoutInflater.from(context)
//        return when (viewType) {
//            SEND_USER -> {
//                val view = inflater.inflate(R.layout.item_layout_send_chat, parent, false)
//                SendViewHolder(view)
//            }
//            RECEIVE_USER -> {
//                val view = inflater.inflate(R.layout.item_layout_receive_chat, parent, false)
//                ReceiveViewHolder(view)
//            }
//            DATE_HEADER -> {
//                val view = inflater.inflate(R.layout.item_layout_date, parent, false)
//                DateViewHolder(view)
//            }
//            else -> throw IllegalArgumentException("Invalid view type: $viewType")
//        }
//    }
//
//
//    override fun getItemCount(): Int {
//         return   messageList.size
//
//    }
//
//    override fun getItemViewType(position: Int): Int {
//        return if (messageList[position].userId?._id == id){
//            SEND_USER
//        }else{
//            RECEIVE_USER
//        }
//
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//
//        val message = messageList[position]
//        return when (holder.itemViewType) {
//            SEND_USER -> {
//                val sendViewHolder = holder as SendViewHolder
//                sendViewHolder.sendMessage.text = message.message
//                sendViewHolder.sentTime.text = ImageUtils.setFormattedTime(message.createdAt)
//                sendViewHolder.sentProfileName.text = message.userId?.firstName?.firstOrNull()?.uppercase() ?: ""
//                //sendViewHolder.dateSend.text = ImageUtils.convertUtcToLocal(message.created_at.toString(),"hh:mm a")
//            }
//
//            RECEIVE_USER -> {
//                val receiveViewHolder = holder as ReceiveViewHolder
//                receiveViewHolder.receiveMessage.text = message.message
//                receiveViewHolder.receiveTime.text = ImageUtils.setFormattedTime(message.createdAt)
//                receiveViewHolder.receiveProfileName.text = message.userId?.firstName?.firstOrNull()?.uppercase() ?: ""
//            }
//            DATE_HEADER ->{
//                val dateViewHolder = holder as DateViewHolder
//
//            }
//
//            else -> {}
//        }
//    }
//
//    fun setList(newDataList: List<Message>) {
//        messageList.clear()
//        messageList.addAll(newDataList)
//        notifyDataSetChanged()
//    }
//
//    fun addToList(list: List<Message>) {
//        val newDataList: List<Message> = list
//        messageList.clear()
//        messageList.addAll(0,newDataList)
//        notifyDataSetChanged()
//    }
//
//
//    fun addData(data: Message) {
//        val positionStart: Int = messageList.size
//        messageList.add(data)
//        notifyItemInserted(positionStart)
//    }
//
//    fun clear(){
//        messageList.clear()
//    }
//
//
//    fun formatDateTo_EEE_MMM_dd(inputDate: String?): String {
//        return try {
//            if (inputDate.isNullOrEmpty()) return ""
//
//            val inputFormatWithMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
//            inputFormatWithMillis.timeZone = TimeZone.getTimeZone("UTC")
//
//            val inputFormatWithoutMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
//            inputFormatWithoutMillis.timeZone = TimeZone.getTimeZone("UTC")
//
//            val outputFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
//
//            val date = try {
//                inputFormatWithMillis.parse(inputDate)
//            } catch (e: Exception) {
//                inputFormatWithoutMillis.parse(inputDate)
//            }
//
//            if (date != null) outputFormat.format(date) else ""
//        } catch (e: Exception) {
//            ""
//        }
//    }
//
//}


class ChatAdapter(private val context: Context, private val id: String?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var lastInsertedDateHeader: String? = null


    companion object {
        const val SEND_USER = 1
        const val RECEIVE_USER = 0
        const val DATE_HEADER = 2
    }

    var chatItemList = ArrayList<ChatItem>()

    class SendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sendMessage: TextView = itemView.findViewById(R.id.rightChat)
        val sentTime: TextView = itemView.findViewById(R.id.sendTime)
        val sentProfileName: TextView = itemView.findViewById(R.id.sentProfileName)
    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveMessage: TextView = itemView.findViewById(R.id.leftChat)
        val receiveTime: TextView = itemView.findViewById(R.id.receiveTime)
        val receiveProfileName: TextView = itemView.findViewById(R.id.receiveProfileNAme)
    }

    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateText: TextView = itemView.findViewById(R.id.date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            SEND_USER -> SendViewHolder(inflater.inflate(R.layout.item_layout_send_chat, parent, false))
            RECEIVE_USER -> ReceiveViewHolder(inflater.inflate(R.layout.item_layout_receive_chat, parent, false))
            DATE_HEADER -> DateViewHolder(inflater.inflate(R.layout.item_layout_date, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = chatItemList.size

    override fun getItemViewType(position: Int): Int = chatItemList[position].type

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = chatItemList[position]

        when (item.type) {
            SEND_USER -> {
                val msg = item.message ?: return
                val sendHolder = holder as SendViewHolder
                sendHolder.sendMessage.text = msg.message
                sendHolder.sentTime.text = ImageUtils.setFormattedTime(msg.createdAt)
                sendHolder.sentProfileName.text = msg.userId?.firstName?.firstOrNull()?.uppercase() ?: ""
            }

            RECEIVE_USER -> {
                val msg = item.message ?: return
                val receiveHolder = holder as ReceiveViewHolder
                receiveHolder.receiveMessage.text = msg.message
                receiveHolder.receiveTime.text = ImageUtils.setFormattedTime(msg.createdAt)
                receiveHolder.receiveProfileName.text = msg.userId?.firstName?.firstOrNull()?.uppercase() ?: ""
            }

            DATE_HEADER -> {
                val dateHolder = holder as DateViewHolder
                dateHolder.dateText.text = item.dateHeader
            }
        }
    }

    fun setList(newDataList: List<ChatItem>) {
        chatItemList.clear()
        chatItemList.addAll(newDataList)
        notifyDataSetChanged()
    }


    fun addToList(newDataList: List<ChatItem>) {
        chatItemList.addAll(0, newDataList)
        notifyDataSetChanged()
    }
    fun setList5(newDataList: List<ChatItem?>) {
        chatItemList.clear()
        chatItemList.addAll(newDataList.filterNotNull())
        notifyDataSetChanged()
    }


    fun clear() {
        chatItemList.clear()
        notifyDataSetChanged()
    }

    fun addData(data: GetChatMessageApiResponse.Message) {
        val newDate = formatDateTo_EEE_MMM_dd(data.createdAt)
        var needToAddDate = true

        // Check if the last date header already matches
        for (i in chatItemList.size - 1 downTo 0) {
            if (chatItemList[i].type == DATE_HEADER) {
                val lastDate = chatItemList[i].dateHeader
                if (lastDate == newDate) {
                    needToAddDate = false
                }
                break
            }
        }

        val newItems = mutableListOf<ChatItem>()

        if (needToAddDate) {
            newItems.add(ChatItem(type = DATE_HEADER, dateHeader = newDate))
        }

        val itemType = if (data.userId?._id == id) SEND_USER else RECEIVE_USER
        newItems.add(ChatItem(type = itemType, message = data))

        val positionStart = chatItemList.size
        chatItemList.addAll(newItems)
        notifyItemRangeInserted(positionStart, newItems.size)
    }


//    fun groupMessagesWithDateHeaders(messages: List<GetChatMessageApiResponse.Message>): List<ChatItem> {
//        val groupedList = mutableListOf<ChatItem>()
//        var lastDate = ""
//
//        for (msg in messages) {
//            val dateStr = formatDateTo_EEE_MMM_dd(msg.createdAt)
//
//            if (dateStr != lastDate) {
//                groupedList.add(ChatItem(type = DATE_HEADER, dateHeader = dateStr))
//                lastDate = dateStr
//            }
//
//            val itemType = if (msg.userId?._id == id) SEND_USER else RECEIVE_USER
//            groupedList.add(ChatItem(type = itemType, message = msg))
//        }
//
//        return groupedList
//    }

    fun groupMessagesWithDateHeaders(
        messages: List<GetChatMessageApiResponse.Message>,
        lastDate: String?
    ): Pair<List<ChatItem>, String?> {
        val groupedList = mutableListOf<ChatItem>()
        var lastUsedDate = lastDate ?: ""

        for (msg in messages) {
            val onlyDate = extractOnlyDate(msg.createdAt)

            // ✅ Prevent duplicate header if current message's date is same as lastUsedDate
            val isSameDay = onlyDate == lastUsedDate

            if (!isSameDay) {
                val formattedDateHeader = formatDateTo_EEE_MMM_dd(msg.createdAt)
                groupedList.add(ChatItem(type = DATE_HEADER, dateHeader = formattedDateHeader))
                lastUsedDate = onlyDate
            }

            val itemType = if (msg.userId?._id == id) SEND_USER else RECEIVE_USER
            groupedList.add(ChatItem(type = itemType, message = msg))
        }

        return Pair(groupedList, lastUsedDate)
    }




    fun extractOnlyDate(inputDate: String?): String {
        return try {
            if (inputDate.isNullOrEmpty()) return ""

            val inputFormatWithMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val inputFormatWithoutMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormatWithMillis.timeZone = TimeZone.getTimeZone("UTC")
            inputFormatWithoutMillis.timeZone = TimeZone.getTimeZone("UTC")

            val date = try {
                inputFormatWithMillis.parse(inputDate)
            } catch (e: Exception) {
                inputFormatWithoutMillis.parse(inputDate)
            }

            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Pure date
            date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }
    }


    fun formatDateTo_EEE_MMM_dd(inputDate: String?): String {
        return try {
            if (inputDate.isNullOrEmpty()) return ""

            val inputFormatWithMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormatWithMillis.timeZone = TimeZone.getTimeZone("UTC")

            val inputFormatWithoutMillis = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormatWithoutMillis.timeZone = TimeZone.getTimeZone("UTC")

            val outputFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())

            val date = try {
                inputFormatWithMillis.parse(inputDate)
            } catch (e: Exception) {
                inputFormatWithoutMillis.parse(inputDate)
            }

            date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
