package com.pickup.sports.ui.calender.view

import android.view.View
import com.pickup.sports.ui.calender.view.core.CalendarDay
import com.pickup.sports.ui.calender.view.core.CalendarMonth
import com.pickup.sports.ui.calender.view.core.CalendarYear
import com.pickup.sports.ui.calender.view.core.Week

import com.pickup.sports.ui.calender.view.core.WeekDay

public open class ViewContainer(public val view: View)

/**
 * Responsible for managing a view for a given [Data].
 * [create] will be called only once but [bind] will
 * be called whenever the view needs to be used to
 * bind an instance of the associated [Data].
 */
public interface Binder<Data, Container : ViewContainer> {
    public fun create(view: View): Container
    public fun bind(container: Container, data: Data)
}

public interface YearHeaderFooterBinder<Container : ViewContainer> : Binder<CalendarYear, Container>

public typealias YearScrollListener = (CalendarYear) -> Unit

public interface MonthHeaderFooterBinder<Container : ViewContainer> :
    Binder<CalendarMonth, Container>

public interface MonthDayBinder<Container : ViewContainer> : Binder<CalendarDay, Container>

public typealias MonthScrollListener = (CalendarMonth) -> Unit

public interface WeekHeaderFooterBinder<Container : ViewContainer> : Binder<Week, Container>

public interface WeekDayBinder<Container : ViewContainer> : Binder<WeekDay, Container>

public typealias WeekScrollListener = (Week) -> Unit
