package com.pickup.sports.ui.calender.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.withStyledAttributes
import androidx.recyclerview.widget.RecyclerView
import com.pickup.sports.R
import com.pickup.sports.ui.calender.view.core.Week
import com.pickup.sports.ui.calender.view.internal.weekcalendar.WeekCalendarAdapter
import com.pickup.sports.ui.calender.view.internal.weekcalendar.WeekCalendarLayoutManager
import com.pickup.sports.ui.calender.view.core.WeekDay
import com.pickup.sports.ui.calender.view.data.checkRange
import com.pickup.sports.ui.calender.view.internal.CalendarPageSnapHelperLegacy
import com.pickup.sports.ui.calender.view.internal.missingField
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * A week-based calendar view.
 *
 * @see CalendarView
 * @see YearCalendarView
 */
@RequiresApi(Build.VERSION_CODES.O)
public open class WeekCalendarView : RecyclerView {
    /**
     * The [WeekDayBinder] instance used for managing day
     * cell view creation and reuse.
     */
    public var dayBinder: WeekDayBinder<*>? = null
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * The [WeekHeaderFooterBinder] instance used for managing the
     * header views shown above each week on the calendar.
     */
    public var weekHeaderBinder: WeekHeaderFooterBinder<*>? = null
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * The [WeekHeaderFooterBinder] instance used for managing the
     * footer views shown below each week on the calendar.
     */
    public var weekFooterBinder: WeekHeaderFooterBinder<*>? = null
        set(value) {
            field = value
            invalidateViewHolders()
        }

    /**
     * Called when the calendar scrolls to a new week.
     * Mostly beneficial if [scrollPaged] is `true`.
     */
    public var weekScrollListener: WeekScrollListener? = null

    /**
     * The xml resource that is inflated and used as the day cell view.
     * This must be provided.
     */
    public var dayViewResource: Int = 0
        set(value) {
            if (field != value) {
                check(value != 0) { "Invalid 'dayViewResource' value." }
                field = value
                invalidateViewHolders()
            }
        }

    /**
     * The xml resource that is inflated and used as a header for each week.
     * Set zero to disable.
     */
    public var weekHeaderResource: Int = 0
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    /**
     * The xml resource that is inflated and used as a footer for each week.
     * Set zero to disable.
     */
    public var weekFooterResource: Int = 0
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    /**
     * The fully qualified class name of a [ViewGroup] that is instantiated
     * and used as the container for each week. This class must have a
     * constructor which takes only a [Context].
     *
     * **You should exclude the name and constructor of this class from code
     * obfuscation if enabled**.
     */
    public var weekViewClass: String? = null
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    /**
     * The scrolling behavior of the calendar. If `true`, the calendar will
     * snap to the nearest week after a scroll or swipe action.
     * If `false`, the calendar scrolls normally.
     */
    public var scrollPaged: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                pagerSnapHelper.attachToRecyclerView(if (scrollPaged) this else null)
            }
        }

    /**
     * Determines how the size of each day on the calendar is calculated.
     * See the [DaySize] class documentation to understand each value.
     */
    public var daySize: DaySize = DaySize.Square
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }

    /**
     * The margins, in pixels to be applied each week view.
     * this can be used to add a space between two weeks.
     */
    public var weekMargins: MarginValues = MarginValues.ZERO
        set(value) {
            if (field != value) {
                field = value
                invalidateViewHolders()
            }
        }
    @RequiresApi(Build.VERSION_CODES.O)
    private val scrollListenerInternal = object : OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {}
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == SCROLL_STATE_IDLE) {
                calendarAdapter.notifyWeekScrollListenerIfNeeded()
            }
        }
    }

    private val pagerSnapHelper = CalendarPageSnapHelperLegacy()

    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null
    private var firstDayOfWeek: DayOfWeek? = null

    public constructor(context: Context) : super(context)

    public constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0, 0)
    }

    public constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) :
        super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr, defStyleAttr)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun init(attributeSet: AttributeSet, defStyleAttr: Int, defStyleRes: Int) {
        if (isInEditMode) return
        itemAnimator = null
        setHasFixedSize(true)
        context.withStyledAttributes(
            attributeSet,
            R.styleable.WeekCalendarView,
            defStyleAttr,
            defStyleRes,
        ) {
            dayViewResource = getResourceId(
                R.styleable.WeekCalendarView_cv_dayViewResource,
                dayViewResource,
            )
            weekHeaderResource = getResourceId(
                R.styleable.WeekCalendarView_cv_weekHeaderResource,
                weekHeaderResource,
            )
            weekFooterResource = getResourceId(
                R.styleable.WeekCalendarView_cv_weekFooterResource,
                weekFooterResource,
            )
            scrollPaged = getBoolean(R.styleable.WeekCalendarView_cv_scrollPaged, scrollPaged)
            daySize = DaySize.entries[
                getInt(R.styleable.WeekCalendarView_cv_daySize, daySize.ordinal),
            ]
            weekViewClass = getString(R.styleable.WeekCalendarView_cv_weekViewClass)
        }
        // Initial scroll setup since we check field when assigning and default value is `true`
        if (scrollPaged) pagerSnapHelper.attachToRecyclerView(this)
        check(dayViewResource != 0) { "No value set for `cv_dayViewResource` attribute." }
    }

    private val calendarLayoutManager: WeekCalendarLayoutManager
        get() = layoutManager as WeekCalendarLayoutManager

    private val calendarAdapter: WeekCalendarAdapter
        get() = adapter as WeekCalendarAdapter
    @RequiresApi(Build.VERSION_CODES.O)
    private fun invalidateViewHolders() {
        // This does not remove visible views.
        // recycledViewPool.clear()

        // This removes all views but is internal.
        // removeAndRecycleViews()

        if (adapter == null || layoutManager == null) return
        val state = layoutManager?.onSaveInstanceState()
        adapter = adapter
        layoutManager?.onRestoreInstanceState(state)
        post { calendarAdapter.notifyWeekScrollListenerIfNeeded() }
    }

    /**
     * Scroll to the week containing this [date] on the calendar.
     * This instantly shows the view for the week without any animations.
     * For a smooth scrolling effect, use [smoothScrollToWeek]
     */
    public fun scrollToWeek(date: LocalDate) {
        calendarLayoutManager.scrollToIndex(date)
    }

    /**
     * Scroll to the week containing this [date] on the calendar
     * using a smooth scrolling animation.
     * Just like [scrollToWeek], but with a smooth scrolling animation.
     */
    public fun smoothScrollToWeek(date: LocalDate) {
        calendarLayoutManager.smoothScrollToIndex(date)
    }

    /**
     * Scroll to a specific date. This brings the date cell
     * view's left edge to the left edge of the WeekCalendarView.
     * No animation is performed.
     * For a smooth scrolling effect, use [smoothScrollToDate].
     */
    public fun scrollToDate(date: LocalDate) {
        calendarLayoutManager.scrollToDay(date)
    }

    /**
     * Scroll to a specific date using a smooth scrolling animation.
     * Just like [scrollToDate], but with a smooth scrolling animation.
     */
    public fun smoothScrollToDate(date: LocalDate) {
        calendarLayoutManager.smoothScrollToDay(date)
    }

    /**
     * Scroll to the week containing this [WeekDay] on the calendar.
     * This instantly shows the view for the week without any animations.
     * For a smooth scrolling effect, use [smoothScrollToWeek]
     */
    public fun scrollToWeek(day: WeekDay) {
        scrollToWeek(day.date)
    }

    /**
     * Scroll to the week containing this [WeekDay] on the calendar
     * using a smooth scrolling animation.
     * Just like [scrollToWeek], but with a smooth scrolling animation.
     */
    public fun smoothScrollToWeek(day: WeekDay) {
        smoothScrollToWeek(day.date)
    }

    /**
     * Scroll to a specific [WeekDay]. This brings the date cell
     * view's left edge to the left edge of the WeekCalendarView.
     * No animation is performed.
     * For a smooth scrolling effect, use [smoothScrollToDay].
     */
    public fun scrollToDay(day: WeekDay) {
        scrollToDate(day.date)
    }

    /**
     * Scroll to a specific [WeekDay] using a smooth scrolling animation.
     * Just like [scrollToDay], but with a smooth scrolling animation.
     */
    public fun smoothScrollToDay(day: WeekDay) {
        smoothScrollToDate(day.date)
    }

    /**
     * Notify the calendar to reload the cell for this [date].
     * This causes [WeekDayBinder.bind] to be called with the [ViewContainer]
     * at this position. Use this to reload a date cell on the Calendar.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public fun notifyDateChanged(date: LocalDate) {
        calendarAdapter.reloadDay(date)
    }

    /**
     * Notify the calendar to reload the cell for this [WeekDay].
     * This causes [WeekDayBinder.bind] to be called with the [ViewContainer]
     * at this position. Use this to reload a date cell on the Calendar.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public fun notifyDayChanged(day: WeekDay) {
        notifyDateChanged(day.date)
    }

    /**
     * Notify the calendar to reload the view for the week containing this [date].
     *
     * This causes the following sequence of events:
     * - [WeekHeaderFooterBinder.bind] will be called for this week's header view if available.
     * - [WeekDayBinder.bind] will be called for all dates in the week.
     * - [WeekHeaderFooterBinder.bind] will be called for this week's footer view if available.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public fun notifyWeekChanged(date: LocalDate) {
        calendarAdapter.reloadWeek(date)
    }

    /**
     * Notify the calendar to reload the view for the week containing this [WeekDay].
     *
     * This causes the following sequence of events:
     * - [WeekHeaderFooterBinder.bind] will be called for this week's header view if available.
     * - [WeekDayBinder.bind] will be called for all dates in the week.
     * - [WeekHeaderFooterBinder.bind] will be called for this week's footer view if available.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public fun notifyWeekChanged(day: WeekDay) {
        notifyWeekChanged(day.date)
    }

    /**
     * Notify the calendar to reload all weeks.
     *
     * @see [notifyWeekChanged]
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public fun notifyCalendarChanged() {
        calendarAdapter.reloadCalendar()
    }

    /**
     * Find the first visible week on the calendar.
     *
     * @return The first visible week or null if not found.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public fun findFirstVisibleWeek(): Week? {
        return calendarAdapter.findFirstVisibleWeek()
    }

    /**
     * Find the last visible week on the calendar.
     *
     * @return The last visible week or null if not found.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public fun findLastVisibleWeek(): Week? {
        return calendarAdapter.findLastVisibleWeek()
    }

    /**
     * Find the first visible day on the calendar.
     * This is the day at the top-left corner of the calendar.
     *
     * @return The first visible day or null if not found.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public fun findFirstVisibleDay(): WeekDay? {
        return calendarAdapter.findFirstVisibleDay()
    }

    /**
     * Find the last visible day on the calendar.
     * This is the day at the bottom-right corner of the calendar.
     *
     * @return The last visible day or null if not found.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public fun findLastVisibleDay(): WeekDay? {
        return calendarAdapter.findLastVisibleDay()
    }

    /**
     * Setup the calendar.
     * See [updateWeekData] to update these values.
     *
     * @param startDate A date in the first week on the calendar.
     * @param endDate A date in the last week on the calendar.
     * @param firstDayOfWeek A [DayOfWeek] to be the first day of week.
     *
     * @see [daysOfWeek]
     * @see [firstDayOfWeekFromLocale]
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public fun setup(startDate: LocalDate, endDate: LocalDate, firstDayOfWeek: DayOfWeek) {
        checkRange(start = startDate, end = endDate)
        this.startDate = startDate
        this.endDate = endDate
        this.firstDayOfWeek = firstDayOfWeek

        removeOnScrollListener(scrollListenerInternal)
        addOnScrollListener(scrollListenerInternal)

        layoutManager = WeekCalendarLayoutManager(this)
        adapter = WeekCalendarAdapter(
            calView = this,
            startDate = startDate,
            endDate = endDate,
            firstDayOfWeek = firstDayOfWeek,
        )
    }

    /**
     * Update the calendar's start date or end date or the first day of week.
     * This can be called only if you have previously called [setup].
     * The calendar can handle really large date ranges so you may want to setup
     * the calendar with a large date range instead of updating the range frequently.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @JvmOverloads
    public fun updateWeekData(
        startDate: LocalDate = requireStartDate(),
        endDate: LocalDate = requireEndDate(),
        firstDayOfWeek: DayOfWeek = requireFirstDayOfWeek(),
    ) {
        checkRange(start = startDate, end = endDate)
        this.startDate = startDate
        this.endDate = endDate
        this.firstDayOfWeek = firstDayOfWeek
        updateAdapter()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateAdapter() {
        calendarAdapter.updateData(
            startDate = requireStartDate(),
            endDate = requireEndDate(),
            firstDayOfWeek = requireFirstDayOfWeek(),
        )
    }

    private fun requireStartDate(): LocalDate = checkNotNull(startDate) { missingField("startDate") }

    private fun requireEndDate(): LocalDate = checkNotNull(endDate) { missingField("endDate") }

    private fun requireFirstDayOfWeek(): DayOfWeek = checkNotNull(firstDayOfWeek) { missingField("firstDayOfWeek") }
}
