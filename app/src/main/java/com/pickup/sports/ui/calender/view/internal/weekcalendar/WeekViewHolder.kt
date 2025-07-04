package com.pickup.sports.ui.calender.view.internal.weekcalendar

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pickup.sports.ui.calender.view.core.WeekDay
import com.pickup.sports.ui.calender.view.ViewContainer
import com.pickup.sports.ui.calender.view.core.Week
import com.pickup.sports.ui.calender.view.WeekHeaderFooterBinder
import com.pickup.sports.ui.calender.view.internal.WeekHolder


internal class WeekViewHolder(
    rootLayout: ViewGroup,
    private val headerView: View?,
    private val footerView: View?,
    private val weekHolder: WeekHolder<WeekDay>,
    private val weekHeaderBinder: WeekHeaderFooterBinder<ViewContainer>?,
    private val weekFooterBinder: WeekHeaderFooterBinder<ViewContainer>?,
) : RecyclerView.ViewHolder(rootLayout) {
    private var headerContainer: ViewContainer? = null
    private var footerContainer: ViewContainer? = null

    lateinit var week: Week

    fun bindWeek(week: Week) {
        this.week = week
        headerView?.let { view ->
            val headerContainer = headerContainer ?: weekHeaderBinder!!.create(view).also {
                headerContainer = it
            }
            weekHeaderBinder?.bind(headerContainer, week)
        }
        weekHolder.bindWeekView(week.days)
        footerView?.let { view ->
            val footerContainer = footerContainer ?: weekFooterBinder!!.create(view).also {
                footerContainer = it
            }
            weekFooterBinder?.bind(footerContainer, week)
        }
    }

    fun reloadDay(day: WeekDay) {
        weekHolder.reloadDay(day)
    }
}
