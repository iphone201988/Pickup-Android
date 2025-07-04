package com.pickup.sports.ui.calender.view.internal.weekcalendar

import android.os.Build
import androidx.annotation.RequiresApi
import com.pickup.sports.ui.calender.view.internal.CalendarLayoutManager
import com.pickup.sports.ui.calender.view.MarginValues
import com.pickup.sports.ui.calender.view.WeekCalendarView
import com.pickup.sports.ui.calender.view.internal.dayTag

import java.time.LocalDate

internal class WeekCalendarLayoutManager(private val calView: WeekCalendarView) :
    CalendarLayoutManager<LocalDate, LocalDate>(calView, HORIZONTAL) {
    private val adapter: WeekCalendarAdapter
        @RequiresApi(Build.VERSION_CODES.O)
        get() = calView.adapter as WeekCalendarAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getaItemAdapterPosition(data: LocalDate): Int = adapter.getAdapterPosition(data)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getaDayAdapterPosition(data: LocalDate): Int = adapter.getAdapterPosition(data)
    override fun getDayTag(data: LocalDate): Int = dayTag(data)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getItemMargins(): MarginValues = calView.weekMargins
    @RequiresApi(Build.VERSION_CODES.O)
    override fun scrollPaged(): Boolean = calView.scrollPaged
    @RequiresApi(Build.VERSION_CODES.O)
    override fun notifyScrollListenerIfNeeded() = adapter.notifyWeekScrollListenerIfNeeded()
}
