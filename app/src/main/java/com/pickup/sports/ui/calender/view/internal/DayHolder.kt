package com.pickup.sports.ui.calender.view.internal

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import com.pickup.sports.ui.calender.view.Binder
import com.pickup.sports.ui.calender.view.DaySize
import com.pickup.sports.ui.calender.view.ViewContainer
import com.pickup.sports.ui.calender.view.core.CalendarDay
import com.pickup.sports.ui.calender.view.core.WeekDay
import java.time.LocalDate

internal data class DayConfig<Day>(
    val daySize: DaySize,
    @LayoutRes val dayViewRes: Int,
    val dayBinder: Binder<Day, ViewContainer>,
)

internal class DayHolder<Day>(private val config: DayConfig<Day>) {
    private lateinit var dayView: View
    private lateinit var viewContainer: ViewContainer
    private var day: Day? = null

    fun inflateDayView(parent: LinearLayout): View {
        return parent.inflate(config.dayViewRes).apply {
            dayView = this
            layoutParams = DayLinearLayoutParams(layoutParams).apply {
                weight = 1f // The parent's wightSum is set to 7.
                when (config.daySize) {
                    DaySize.Square -> {
                        width = MATCH_PARENT
                        height = MATCH_PARENT
                    }

                    DaySize.Rectangle -> {
                        width = MATCH_PARENT
                        height = MATCH_PARENT
                    }

                    DaySize.SeventhWidth -> {
                        width = MATCH_PARENT
                    }

                    DaySize.FreeForm -> {}
                }
            }
        }
    }

    fun bindDayView(currentDay: Day) {
        this.day = currentDay
        if (!::viewContainer.isInitialized) {
            viewContainer = config.dayBinder.create(dayView)
        }
        dayView.tag = dayTag(findDate(currentDay))
        config.dayBinder.bind(viewContainer, currentDay)
    }

    fun reloadViewIfNecessary(day: Day): Boolean {
        return if (day == this.day) {
            bindDayView(day)
            true
        } else {
            false
        }
    }
}

private fun findDate(day: Any?): LocalDate {
    return when (day) {
        is CalendarDay -> day.date
        is WeekDay -> day.date
        else -> throw IllegalArgumentException("Invalid day type: $day")
    }
}

@Suppress("FunctionName")
internal fun DayLinearLayoutParams(layoutParams: ViewGroup.LayoutParams): LinearLayout.LayoutParams =
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        // Ensure we call the correct constructor.
        LinearLayout.LayoutParams(layoutParams)
    } else {
        LinearLayout.LayoutParams(layoutParams)
    }
