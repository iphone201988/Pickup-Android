package com.pickup.sports.ui.calender.view.internal.yearcalendar

import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearSmoothScroller
import com.pickup.sports.ui.calender.view.internal.CalendarLayoutManager
import com.pickup.sports.ui.calender.view.core.CalendarDay
import com.pickup.sports.ui.calender.view.MarginValues
import com.pickup.sports.ui.calender.view.internal.NO_INDEX
import com.pickup.sports.ui.calender.view.YearCalendarView
import com.pickup.sports.ui.calender.view.internal.dayTag
import java.time.Year
import java.time.YearMonth

@RequiresApi(Build.VERSION_CODES.O)
internal class YearCalendarLayoutManager(private val calView: YearCalendarView) :
    CalendarLayoutManager<Year, CalendarDay>(calView, calView.orientation) {
    private val adapter: YearCalendarAdapter
        get() = calView.adapter as YearCalendarAdapter

    override fun getaItemAdapterPosition(data: Year): Int = adapter.getAdapterPosition(data)
    override fun getaDayAdapterPosition(data: CalendarDay): Int = adapter.getAdapterPosition(data)
    override fun getDayTag(data: CalendarDay): Int = dayTag(data.date)
    override fun getItemMargins(): MarginValues = calView.yearMargins
    override fun scrollPaged(): Boolean = calView.scrollPaged
    override fun notifyScrollListenerIfNeeded() = adapter.notifyYearScrollListenerIfNeeded()
    fun smoothScrollToMonth(month: YearMonth) {
        val indexPosition = adapter.getAdapterPosition(month)
        if (indexPosition == NO_INDEX) return
        // Can't target a specific month in a paged calendar.
        startSmoothScroll(CalendarSmoothScroller(indexPosition, if (scrollPaged()) null else month))
    }

    fun scrollToMonth(month: YearMonth) {
        val indexPosition = adapter.getAdapterPosition(month)
        if (indexPosition == NO_INDEX) return
        scrollToPositionWithOffset(indexPosition, 0)
        // Can't target a specific day in a paged calendar.
        if (scrollPaged()) {
            calView.post { notifyScrollListenerIfNeeded() }
        } else {
            calView.post {
                val itemView = calView.findViewHolderForAdapterPosition(indexPosition)?.itemView
                    ?: return@post
                val offset = calculateDayViewOffsetInParent(month, itemView)
                scrollToPositionWithOffset(indexPosition, -offset)
                calView.post { notifyScrollListenerIfNeeded() }
            }
        }
    }

    private fun calculateDayViewOffsetInParent(month: YearMonth, itemView: View): Int {
        val dayView = itemView.findViewWithTag<View>(monthTag(month)) ?: return 0
        val rect = Rect()
        dayView.getDrawingRect(rect)
        (itemView as ViewGroup).offsetDescendantRectToMyCoords(dayView, rect)
        return if (orientation == VERTICAL) rect.top else rect.left
    }

    private inner class CalendarSmoothScroller(position: Int, val month: YearMonth?) :
        LinearSmoothScroller(calView.context) {
        init {
            targetPosition = position
        }

        override fun getVerticalSnapPreference(): Int = SNAP_TO_START

        override fun getHorizontalSnapPreference(): Int = SNAP_TO_START

        override fun calculateDyToMakeVisible(view: View, snapPreference: Int): Int {
            val dy = super.calculateDyToMakeVisible(view, snapPreference)
            if (month == null) {
                return dy
            }
            val offset = calculateDayViewOffsetInParent(month, view)
            return dy - offset
        }

        override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
            val dx = super.calculateDxToMakeVisible(view, snapPreference)
            if (month == null) {
                return dx
            }
            val offset = calculateDayViewOffsetInParent(month, view)
            return dx - offset
        }
    }
}
