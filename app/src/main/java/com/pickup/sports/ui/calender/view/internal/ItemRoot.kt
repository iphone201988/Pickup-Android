package com.pickup.sports.ui.calender.view.internal

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewGroup.MarginLayoutParams
import android.widget.LinearLayout
import com.pickup.sports.ui.calender.view.Binder
import com.pickup.sports.ui.calender.view.DaySize
import com.pickup.sports.ui.calender.view.MarginValues
import com.pickup.sports.ui.calender.view.ViewContainer
import java.time.LocalDate

internal data class ItemContent<Day>(
    val itemView: ViewGroup,
    val headerView: View?,
    val footerView: View?,
    val weekHolders: List<WeekHolder<Day>>,
)

internal fun <Day, Container : ViewContainer> setupItemRoot(
    itemMargins: MarginValues,
    daySize: DaySize,
    context: Context,
    dayViewResource: Int,
    itemHeaderResource: Int,
    itemFooterResource: Int,
    weekSize: Int,
    itemViewClass: String?,
    dayBinder: Binder<Day, Container>,
): ItemContent<Day> {
    val rootLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
    }

    val itemHeaderView = if (itemHeaderResource != 0) {
        rootLayout.inflate(itemHeaderResource).also { headerView ->
            rootLayout.addView(headerView)
        }
    } else {
        null
    }

    @Suppress("UNCHECKED_CAST")
    val dayConfig = DayConfig(
        daySize = daySize,
        dayViewRes = dayViewResource,
        dayBinder = dayBinder as Binder<Day, ViewContainer>,
    )

    val weekHolders = List(weekSize) {
        WeekHolder(
            daySize = dayConfig.daySize,
            dayHolders = List(size = 7) { DayHolder(dayConfig) },
        )
    }.onEach { weekHolder ->
        rootLayout.addView(weekHolder.inflateWeekView(rootLayout))
    }

    val itemFooterView = if (itemFooterResource != 0) {
        rootLayout.inflate(itemFooterResource).also { footerView ->
            rootLayout.addView(footerView)
        }
    } else {
        null
    }

    val itemView = customViewOrRoot(
        customViewClass = itemViewClass,
        rootLayout = rootLayout,
    ) { root: ViewGroup ->
        val width = if (daySize.parentDecidesWidth) MATCH_PARENT else WRAP_CONTENT
        val height = if (daySize.parentDecidesHeight) MATCH_PARENT else WRAP_CONTENT
        root.layoutParams = MarginLayoutParams(width, height).apply {
            bottomMargin = itemMargins.bottom
            topMargin = itemMargins.top
            marginStart = itemMargins.start
            marginEnd = itemMargins.end
        }
    }

    return ItemContent(
        itemView = itemView,
        headerView = itemHeaderView,
        footerView = itemFooterView,
        weekHolders = weekHolders,
    )
}

internal fun dayTag(date: LocalDate): Int = date.hashCode()
