package com.pickup.sports.ui.calender.view.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.pickup.sports.ui.calender.view.core.CalendarDay
import com.pickup.sports.ui.calender.view.core.CalendarMonth
import com.pickup.sports.ui.calender.view.core.DayPosition
import com.pickup.sports.ui.calender.view.core.OutDateStyle
import com.pickup.sports.ui.calender.view.core.atStartOfMonth
import com.pickup.sports.ui.calender.view.core.nextMonth
import com.pickup.sports.ui.calender.view.core.previousMonth
import com.pickup.sports.ui.calender.view.core.yearMonth

import java.time.DayOfWeek
import java.time.YearMonth
import java.time.temporal.ChronoUnit
@RequiresApi(Build.VERSION_CODES.O)
public data class MonthData internal constructor(
    private val month: YearMonth,
    private val inDays: Int,
    private val outDays: Int,
) {

    private val totalDays = inDays + month.lengthOfMonth() + outDays

    private val firstDay = month.atStartOfMonth().minusDays(inDays.toLong())

    private val rows = (0 until totalDays).chunked(7)

    private val previousMonth = month.previousMonth

    private val nextMonth = month.nextMonth

    val calendarMonth: CalendarMonth =
        CalendarMonth(month, rows.map { week -> week.map { dayOffset -> getDay(dayOffset) } })

    private fun getDay(dayOffset: Int): CalendarDay {
        val date = firstDay.plusDays(dayOffset.toLong())
        val position = when (date.yearMonth) {
            month -> DayPosition.MonthDate
            previousMonth -> DayPosition.InDate
            nextMonth -> DayPosition.OutDate
            else -> throw IllegalArgumentException("Invalid date: $date in month: $month")
        }
        return CalendarDay(date, position)
    }
}
@RequiresApi(Build.VERSION_CODES.O)
public fun getCalendarMonthData(
    startMonth: YearMonth,
    offset: Int,
    firstDayOfWeek: DayOfWeek,
    outDateStyle: OutDateStyle,
): MonthData {
    val month = startMonth.plusMonths(offset.toLong())
    val firstDay = month.atStartOfMonth()
    val inDays = firstDayOfWeek.daysUntil(firstDay.dayOfWeek)
    val outDays = (inDays + month.lengthOfMonth()).let { inAndMonthDays ->
        val endOfRowDays = if (inAndMonthDays % 7 != 0) 7 - (inAndMonthDays % 7) else 0
        val endOfGridDays = if (outDateStyle == OutDateStyle.EndOfRow) {
            0
        } else {
            val weeksInMonth = (inAndMonthDays + endOfRowDays) / 7
            (6 - weeksInMonth) * 7
        }
        return@let endOfRowDays + endOfGridDays
    }
    return MonthData(month, inDays, outDays)
}
@RequiresApi(Build.VERSION_CODES.O)
public fun getHeatMapCalendarMonthData(
    startMonth: YearMonth,
    offset: Int,
    firstDayOfWeek: DayOfWeek,
): MonthData {
    val month = startMonth.plusMonths(offset.toLong())
    val firstDay = month.atStartOfMonth()
    val inDays = if (offset == 0) {
        firstDayOfWeek.daysUntil(firstDay.dayOfWeek)
    } else {
        -firstDay.dayOfWeek.daysUntil(firstDayOfWeek)
    }
    val outDays = (inDays + month.lengthOfMonth()).let { inAndMonthDays ->
        if (inAndMonthDays % 7 != 0) 7 - (inAndMonthDays % 7) else 0
    }
    return MonthData(month, inDays, outDays)
}
@RequiresApi(Build.VERSION_CODES.O)
public fun getMonthIndex(startMonth: YearMonth, targetMonth: YearMonth): Int {
    return ChronoUnit.MONTHS.between(startMonth, targetMonth).toInt()
}
@RequiresApi(Build.VERSION_CODES.O)
public fun getMonthIndicesCount(startMonth: YearMonth, endMonth: YearMonth): Int {
    // Add one to include the start month itself!
    return getMonthIndex(startMonth, endMonth) + 1
}
