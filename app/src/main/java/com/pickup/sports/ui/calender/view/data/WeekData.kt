package com.pickup.sports.ui.calender.view.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.pickup.sports.ui.calender.view.core.Week
import com.pickup.sports.ui.calender.view.core.WeekDay
import com.pickup.sports.ui.calender.view.core.WeekDayPosition
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

public data class WeekDateRange(
    val startDateAdjusted: LocalDate,
    val endDateAdjusted: LocalDate,
)

@RequiresApi(Build.VERSION_CODES.O)
public fun getWeekCalendarAdjustedRange(
    startDate: LocalDate,
    endDate: LocalDate,
    firstDayOfWeek: DayOfWeek,
): WeekDateRange {
    val inDays = firstDayOfWeek.daysUntil(startDate.dayOfWeek)
    val startDateAdjusted = startDate.minusDays(inDays.toLong())
    val weeksBetween =
        ChronoUnit.WEEKS.between(startDateAdjusted, endDate).toInt()
    val endDateAdjusted = startDateAdjusted.plusWeeks(weeksBetween.toLong()).plusDays(6)
    return WeekDateRange(startDateAdjusted = startDateAdjusted, endDateAdjusted = endDateAdjusted)
}

@RequiresApi(Build.VERSION_CODES.O)
public fun getWeekCalendarData(
    startDateAdjusted: LocalDate,
    offset: Int,
    desiredStartDate: LocalDate,
    desiredEndDate: LocalDate,
): WeekData {
    val firstDayInWeek = startDateAdjusted.plusWeeks(offset.toLong())
    return WeekData(firstDayInWeek, desiredStartDate, desiredEndDate)
}

public data class WeekData internal constructor(
    private val firstDayInWeek: LocalDate,
    private val desiredStartDate: LocalDate,
    private val desiredEndDate: LocalDate,
) {
    @RequiresApi(Build.VERSION_CODES.O)
    val week: Week = Week((0 until 7).map { dayOffset -> getDay(dayOffset) })

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDay(dayOffset: Int): WeekDay {
        val date = firstDayInWeek.plusDays(dayOffset.toLong())
        val position = when {
            date < desiredStartDate -> WeekDayPosition.InDate
            date > desiredEndDate -> WeekDayPosition.OutDate
            else -> WeekDayPosition.RangeDate
        }
        return WeekDay(date, position)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
public fun getWeekIndex(startDateAdjusted: LocalDate, date: LocalDate): Int {
    return ChronoUnit.WEEKS.between(startDateAdjusted, date).toInt()
}

@RequiresApi(Build.VERSION_CODES.O)
public fun getWeekIndicesCount(startDateAdjusted: LocalDate, endDateAdjusted: LocalDate): Int {
    // Add one to include the start week itself!
    return getWeekIndex(startDateAdjusted, endDateAdjusted) + 1
}
