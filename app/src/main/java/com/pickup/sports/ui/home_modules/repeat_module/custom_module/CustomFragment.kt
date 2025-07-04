package com.pickup.sports.ui.home_modules.repeat_module.custom_module

import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.pickup.sports.R
import com.pickup.sports.databinding.Example2CalendarDayBinding
import com.pickup.sports.databinding.Example2CalendarHeaderBinding
import com.pickup.sports.databinding.FragmentCustomBinding
import com.pickup.sports.ui.base.BaseFragment
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.calender.view.MonthDayBinder
import com.pickup.sports.ui.calender.view.MonthHeaderFooterBinder
import com.pickup.sports.ui.calender.view.ViewContainer
import com.pickup.sports.ui.calender.view.core.CalendarDay
import com.pickup.sports.ui.calender.view.core.CalendarMonth
import com.pickup.sports.ui.calender.view.core.DayPosition
import com.pickup.sports.ui.calender.view.core.daysOfWeek
import com.pickup.sports.ui.calender.view.makeInVisible
import com.pickup.sports.ui.calender.view.makeVisible
import com.pickup.sports.ui.calender.view.setTextColorRes
import com.pickup.sports.ui.calender.view.shared.DateSelection
import com.pickup.sports.ui.calender.view.shared.displayText
import com.pickup.sports.ui.home_modules.create_host_game.CreateHostGameActivity
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class CustomFragment : BaseFragment<FragmentCustomBinding>() {

    private val viewModel : CustomFragmentVm by viewModels()
    private val daysOfWeek = daysOfWeek()
    private val currentMonth: YearMonth = YearMonth.now()

    private var selection = DateSelection()
    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()
    private var selectedDatesFormatted : String ? = null

    override fun onCreateView(view: View) {
        initOnClick()
        customCalender()
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner , Observer {
            when(it?.id){
                R.id.tvContinue ->{
                    if (selectedDatesFormatted != null){
                        Log.i("sdfsddsf", "initOnClick: $selectedDatesFormatted")
                        CreateHostGameActivity.selectedSide = "Custom"
                        CreateHostGameActivity.selectedCustomDate = selectedDatesFormatted.toString()
                        requireActivity().onBackPressed()

//                        val intent = Intent(requireContext(), CreateHostGameActivity::class.java)
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                        startActivity(intent)
                    }else{
                        showToast("Please select date")
                    }


                }
            }
        })

    }

    private fun customCalender() {
        binding.legendLayout?.root?.children?.forEachIndexed { index, child ->
            (child as TextView).apply {
                text = daysOfWeek[index].displayText()
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColorRes(R.color.grey_500)
            }
        }
        binding.calenderView.setup(
            currentMonth,
            currentMonth.plusMonths(100),
            daysOfWeek.first(),
        )
        binding.calenderView.scrollToMonth(currentMonth)
        configureBinder()
    }
//    private fun configureBinder() {
//        val calendarView = binding.calenderView
//
//        class DayViewContainer(view: View) : ViewContainer(view) {
//            // Will be set when this container is bound. See the dayBinder.
//            lateinit var day: CalendarDay
//            val textView = Example2CalendarDayBinding.bind(view).exTwoDayText
//
//            init {
//                textView.setOnClickListener {
//                    if (day.position == DayPosition.MonthDate) {
//                        if (selectedDate == day.date) {
//                            selectedDate = null
//                            calendarView.notifyDayChanged(day)
//                        } else {
//                            val oldDate = selectedDate
//                            selectedDate = day.date
//                            calendarView.notifyDateChanged(day.date)
//                            oldDate?.let { calendarView.notifyDateChanged(oldDate) }
//                        }
////                        menuItem.isVisible = selectedDate != null
//                    }
//                }
//            }
//        }
//
//        var selectedDate: LocalDate = today // Default selection set to today
//
//        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
//            override fun create(view: View) = DayViewContainer(view)
//
//            override fun bind(container: DayViewContainer, data: CalendarDay) {
//                container.day = data
//                val textView = container.textView
//                textView.text = data.date.dayOfMonth.toString()
//
//                if (data.position == DayPosition.MonthDate) {
//                    textView.makeVisible()
//                    when {
//                        data.date == selectedDate -> {
//                            textView.setTextColorRes(R.color.white)
//                            textView.setBackgroundResource(R.drawable.bg_dark_btn)
//                        }
//                        else -> {
//                            textView.setTextColorRes(R.color.heading_color)
//                            textView.background = null
//                        }
//                    }
//
//                    // Set click listener to update selected date
//                    textView.setOnClickListener {
//                        selectedDate = data.date
//                        calendarView.notifyCalendarChanged() // Refresh calendar to reflect new selection
//                    }
//                } else {
//                    textView.makeInVisible()
//                }
//            }
//        }
//
//
//
//        class MonthViewContainer(view: View) : ViewContainer(view) {
//            val textView = Example2CalendarHeaderBinding.bind(view).exTwoHeaderText
//        }
//        calendarView.monthHeaderBinder =
//            object : MonthHeaderFooterBinder<MonthViewContainer> {
//                override fun create(view: View) = MonthViewContainer(view)
//                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
//                    container.textView.text = data.yearMonth.displayText()
//                }
//            }
//    }




    private fun configureBinder() {
        val calendarView = binding.calenderView
        val selectedDates = mutableSetOf<LocalDate>() // Store selected dates
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")

        // Function to update selectedDatesFormatted
        fun storeSelectedDates() {
            selectedDatesFormatted = selectedDates.joinToString(",") { date ->
                LocalDateTime.of(date, java.time.LocalTime.of(21, 37)) // Set time to 21:37
                    .atOffset(ZoneOffset.UTC) // Set UTC offset
                    .format(dateFormatter) // Format date
            }
        }

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val textView = Example2CalendarDayBinding.bind(view).exTwoDayText

            init {
                textView.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        if (selectedDates.contains(day.date)) {
                            selectedDates.remove(day.date)
                        } else {
                            selectedDates.add(day.date)
                        }
                        calendarView.notifyDayChanged(day)
                        storeSelectedDates() // Update formatted string
                    }
                }
            }
        }

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.textView
                textView.text = data.date.dayOfMonth.toString()

                if (data.position == DayPosition.MonthDate) {
                    textView.makeVisible()
                    if (selectedDates.contains(data.date)) {
                        textView.setTextColorRes(R.color.white)
                        textView.setBackgroundResource(R.drawable.bg_dark_btn)
                    } else {
                        textView.setTextColorRes(R.color.heading_color)
                        textView.background = null
                    }
                } else {
                    textView.makeInVisible()
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val textView = Example2CalendarHeaderBinding.bind(view).exTwoHeaderText
        }

        calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    container.textView.text = data.yearMonth.displayText()
                }
            }
    }






    override fun getLayoutResource(): Int {
      return R.layout.fragment_custom
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }


}