package com.pickup.sports.ui.calender.view.internal

import android.graphics.Rect
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import com.pickup.sports.ui.calender.view.core.DayPosition
import com.pickup.sports.ui.calender.view.core.CalendarDay
import com.pickup.sports.ui.calender.view.core.nextMonth
import com.pickup.sports.ui.calender.view.core.previousMonth
import com.pickup.sports.ui.calender.view.core.yearMonth

import java.time.YearMonth

internal fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

internal const val NO_INDEX = -1

// Find the actual month on the calendar where this date is shown.
internal val CalendarDay.positionYearMonth: YearMonth
    @RequiresApi(Build.VERSION_CODES.O)
    get() = when (position) {
        DayPosition.InDate -> date.yearMonth.nextMonth
        DayPosition.MonthDate -> date.yearMonth
        DayPosition.OutDate -> date.yearMonth.previousMonth
    }

internal fun Rect.intersects(other: Rect): Boolean {
    return if (this.isEmpty || other.isEmpty) {
        false
    } else {
        Rect.intersects(this, other)
    }
}

internal fun missingField(field: String) = "`$field` is not set. Have you called `setup()`?"
