package com.pickup.sports.ui.home_modules.payment_policy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pickup.sports.R
import com.pickup.sports.data.model.GetPaymentMethodApiResponse
import com.zerobranch.layout.SwipeLayout

class CustomPaymentAdapter(private val itemClickListener: OnItemClickListener) : RecyclerView.Adapter<CustomPaymentAdapter.ViewHolder>() {

    private var itemList: MutableList<GetPaymentMethodApiResponse.PaymentMethods.PaymentMethod?> = ArrayList()
    private var currentlyOpenSwipeLayout: SwipeLayout? = null
    private var selectedPosition = 0 // Select first item by default

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardData: TextView = view.findViewById(R.id.cardExpiry)
        val deleteCard: TextView = view.findViewById(R.id.deleteCard)
        val swipeLayout: SwipeLayout = view.findViewById(R.id.swipeLayout)
        val consMain: View = view.findViewById(R.id.consMain) // Ensure this is in your layout XML

        fun bind(item: GetPaymentMethodApiResponse.PaymentMethods.PaymentMethod?, listener: OnItemClickListener, position: Int) {
            item?.let {
                cardData.text = "Card ending in ${it.last4}"

                // Handle selection background
                consMain.setBackgroundResource(
                    if (position == selectedPosition) R.drawable.bg_selected_edit_text
                    else R.drawable.bg_edit_text
                )

                deleteCard.setOnClickListener { view ->
                    listener.onItemClick(view, it, position)
                    swipeLayout.close()
                }

                consMain.setOnClickListener { view ->
                    listener.onItemClick(view, it, position)
                }

                swipeLayout.setOnActionsListener(object : SwipeLayout.SwipeActionsListener {
                    override fun onOpen(direction: Int, isContinuous: Boolean) {
                        if (direction == SwipeLayout.LEFT) {
                            closePreviouslyOpenSwipeLayout()
                            currentlyOpenSwipeLayout = swipeLayout
                        }
                    }
                    override fun onClose() {
                        if (currentlyOpenSwipeLayout == swipeLayout) {
                            currentlyOpenSwipeLayout = null
                        }
                    }
                })
            }
        }
    }

    private fun closePreviouslyOpenSwipeLayout() {
        currentlyOpenSwipeLayout?.close()
    }

    fun removeItemAt(position: Int) {
        if (position >= 0 && position < itemList.size) {
            itemList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemList.size)

            // Ensure a valid card remains selected
            if (position == selectedPosition) {
                selectedPosition = if (itemList.isNotEmpty()) 0 else -1
                notifyItemChanged(selectedPosition)
            }
        }
    }

    fun clearList() {
        itemList.clear()
        selectedPosition = 0 // Reset selection
        notifyDataSetChanged()
    }

    fun getList(): MutableList<GetPaymentMethodApiResponse.PaymentMethods.PaymentMethod?> {
        return itemList
    }

    fun setList(newDataList: List<GetPaymentMethodApiResponse.PaymentMethods.PaymentMethod?>?) {
        itemList.clear()
        if (newDataList != null) {
            itemList.addAll(newDataList)
        }
        selectedPosition = if (itemList.isNotEmpty()) 0 else -1 // Select first card
        notifyDataSetChanged()
    }

    fun addToList(list: List<GetPaymentMethodApiResponse.PaymentMethods.PaymentMethod?>?) {
        list?.let {
            val initialSize = itemList.size
            itemList.addAll(it)
            notifyItemRangeInserted(initialSize, it.size)

            // Ensure selection remains or set to first item if empty
            if (selectedPosition == -1 && itemList.isNotEmpty()) {
                updateSelection(0)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout_customer_cards, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item, itemClickListener, position)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, item: GetPaymentMethodApiResponse.PaymentMethods.PaymentMethod?, position: Int)
    }

    fun updateSelection(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position

        if (previousPosition >= 0) notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }
}
