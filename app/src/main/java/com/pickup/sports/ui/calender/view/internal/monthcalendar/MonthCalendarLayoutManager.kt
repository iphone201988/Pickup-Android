package com.pickup.sports.ui.calender.view.internal.monthcalendar

import android.os.Build
import androidx.annotation.RequiresApi
import com.pickup.sports.ui.calender.view.internal.CalendarLayoutManager
import com.pickup.sports.ui.calender.view.core.CalendarDay
import com.pickup.sports.ui.calender.view.CalendarView
import com.pickup.sports.ui.calender.view.MarginValues
import com.pickup.sports.ui.calender.view.internal.dayTag
import java.time.YearMonth

@RequiresApi(Build.VERSION_CODES.O)
internal class MonthCalendarLayoutManager(private val calView: CalendarView) :
    CalendarLayoutManager<YearMonth, CalendarDay>(calView, calView.orientation) {
    private val adapter: MonthCalendarAdapter
        @RequiresApi(Build.VERSION_CODES.O)
        get() = calView.adapter as MonthCalendarAdapter

    override fun getaItemAdapterPosition(data: YearMonth): Int = adapter.getAdapterPosition(data)
    override fun getaDayAdapterPosition(data: CalendarDay): Int = adapter.getAdapterPosition(data)
    override fun getDayTag(data: CalendarDay): Int = dayTag(data.date)
    override fun getItemMargins(): MarginValues = calView.monthMargins
    override fun scrollPaged(): Boolean = calView.scrollPaged
    override fun notifyScrollListenerIfNeeded() = adapter.notifyMonthScrollListenerIfNeeded()
}
