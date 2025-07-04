package com.pickup.sports.ui.calender.view.internal.monthcalendar

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pickup.sports.ui.calender.view.core.CalendarMonth
import com.pickup.sports.ui.calender.view.core.CalendarDay
import com.pickup.sports.ui.calender.view.MonthHeaderFooterBinder
import com.pickup.sports.ui.calender.view.ViewContainer
import com.pickup.sports.ui.calender.view.internal.WeekHolder


internal class MonthViewHolder(
    rootLayout: ViewGroup,
    private val headerView: View?,
    private val footerView: View?,
    private val weekHolders: List<WeekHolder<CalendarDay>>,
    private val monthHeaderBinder: MonthHeaderFooterBinder<ViewContainer>?,
    private val monthFooterBinder: MonthHeaderFooterBinder<ViewContainer>?,
) : RecyclerView.ViewHolder(rootLayout) {
    private var headerContainer: ViewContainer? = null
    private var footerContainer: ViewContainer? = null
    private lateinit var month: CalendarMonth

    fun bindMonth(month: CalendarMonth) {
        this.month = month
        headerView?.let { view ->
            val headerContainer = headerContainer ?: monthHeaderBinder!!.create(view).also {
                headerContainer = it
            }
            monthHeaderBinder?.bind(headerContainer, month)
        }
        weekHolders.forEachIndexed { index, week ->
            week.bindWeekView(month.weekDays.getOrNull(index).orEmpty())
        }
        footerView?.let { view ->
            val footerContainer = footerContainer ?: monthFooterBinder!!.create(view).also {
                footerContainer = it
            }
            monthFooterBinder?.bind(footerContainer, month)
        }
    }

    fun reloadDay(day: CalendarDay) {
        weekHolders.firstOrNull { it.reloadDay(day) }
    }
}
