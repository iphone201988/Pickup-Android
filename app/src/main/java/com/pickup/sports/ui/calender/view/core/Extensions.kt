package com.pickup.sports.ui.calender.view.core


import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Returns the days of week values such that the desired
 * [firstDayOfWeek] property is at the start position.
 *
 * @see [firstDayOfWeekFromLocale]
 */
@RequiresApi(Build.VERSION_CODES.O)
@JvmOverloads
fun daysOfWeek(firstDayOfWeek: DayOfWeek = firstDayOfWeekFromLocale()): List<DayOfWeek> {
    val pivot = 7 - firstDayOfWeek.ordinal
    val daysOfWeek = DayOfWeek.values()
    // Order `daysOfWeek` array so that firstDayOfWeek is at the start position.
    return daysOfWeek.takeLast(pivot) + daysOfWeek.dropLast(pivot)
}

/**
 * Returns the first day of the week from the provided locale.
 */
@RequiresApi(Build.VERSION_CODES.O)
@JvmOverloads
fun firstDayOfWeekFromLocale(locale: Locale = Locale.getDefault()): DayOfWeek =
    WeekFields.of(locale).firstDayOfWeek

/**
 * Returns a [LocalDate] at the start of the month.
 *
 * Complements [YearMonth.atEndOfMonth].
 */
@RequiresApi(Build.VERSION_CODES.O)
fun YearMonth.atStartOfMonth(): LocalDate = this.atDay(1)
val LocalDate.yearMonth: YearMonth
    @RequiresApi(Build.VERSION_CODES.O)
    get() = YearMonth.of(year, monthValue)

val YearMonth.nextMonth: YearMonth
    @RequiresApi(Build.VERSION_CODES.O)
    get() = this.plusMonths(1)

val YearMonth.previousMonth: YearMonth
    @RequiresApi(Build.VERSION_CODES.O)
    get() = this.minusMonths(1)
