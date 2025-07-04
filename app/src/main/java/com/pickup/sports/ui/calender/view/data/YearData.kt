package com.pickup.sports.ui.calender.view.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.pickup.sports.ui.calender.view.core.CalendarYear
import com.pickup.sports.ui.calender.view.core.OutDateStyle
import java.time.DayOfWeek
import java.time.Month
import java.time.Year
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
public fun getCalendarYearData(
    startYear: Year,
    offset: Int,
    firstDayOfWeek: DayOfWeek,
    outDateStyle: OutDateStyle,
): CalendarYear {
    val year = startYear.plusYears(offset.toLong())

    // Manually create the list of months using Month.values() instead of Month.entries
    val months = List(Month.values().size) { index ->  // Use Month.values() here
        getCalendarMonthData(
            startMonth = year.atMonth(Month.values()[index]),  // Access months using Month.values()
            offset = index,
            firstDayOfWeek = firstDayOfWeek,
            outDateStyle = outDateStyle,
        ).calendarMonth
    }
    return CalendarYear(year, months)
}

@RequiresApi(Build.VERSION_CODES.O)
public fun getYearIndex(startYear: Year, targetYear: Year): Int {
    return ChronoUnit.YEARS.between(startYear, targetYear).toInt()
}

@RequiresApi(Build.VERSION_CODES.O)
public fun getYearIndicesCount(startYear: Year, endYear: Year): Int {
    // Add one to include the start year itself!
    return getYearIndex(startYear, endYear) + 1
}

/*
package com.light.financial.ui.main.kizitonwose.calendar.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.light_financial.light.ui.calender.view.core.CalendarYear
import com.light_financial.light.ui.calender.view.core.OutDateStyle
import java.time.DayOfWeek
import java.time.Month
import java.time.Year
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
public fun getCalendarYearData(
    startYear: Year,
    offset: Int,
    firstDayOfWeek: DayOfWeek,
    outDateStyle: OutDateStyle,
): CalendarYear {
    val year = startYear.plusYears(offset.toLong())
    val months = List(Month.entries.size) { index ->
        getCalendarMonthData(
            startMonth = year.atMonth(Month.JANUARY),
            offset = index,
            firstDayOfWeek = firstDayOfWeek,
            outDateStyle = outDateStyle,
        ).calendarMonth
    }
    return CalendarYear(year, months)
}

@RequiresApi(Build.VERSION_CODES.O)
public fun getYearIndex(startYear: Year, targetYear: Year): Int {
    return ChronoUnit.YEARS.between(startYear, targetYear).toInt()
}

@RequiresApi(Build.VERSION_CODES.O)
public fun getYearIndicesCount(startYear: Year, endYear: Year): Int {
    // Add one to include the start year itself!
    return getYearIndex(startYear, endYear) + 1
}
*/
