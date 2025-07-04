package com.pickup.sports.ui.calender.view.core

import java.io.Serializable
import java.time.LocalDate
import javax.annotation.concurrent.Immutable

/**
 * Represents a day on the calendar.
 *
 * @param date the date for this day.
 * @param position the [DayPosition] for this day.
 */
@Immutable
public data class CalendarDay(val date: LocalDate, val position: DayPosition) : Serializable
