package com.pickup.sports.ui.home_modules.repeat_module.weekly_module

import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.pickup.sports.BR
import com.pickup.sports.R
import com.pickup.sports.data.model.WeekModel
import com.pickup.sports.databinding.FragmentWeeklyBinding
import com.pickup.sports.databinding.ItemLayoutWeeklyBinding
import com.pickup.sports.ui.base.BaseFragment
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.base.SimpleRecyclerViewAdapter
import com.pickup.sports.ui.home_modules.create_host_game.CreateHostGameActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WeeklyFragment : BaseFragment<FragmentWeeklyBinding>() {



    private lateinit var  weekAdapter : SimpleRecyclerViewAdapter<WeekModel,ItemLayoutWeeklyBinding>
    private var weekList = arrayListOf<WeekModel>()
    private val selectedPositions = mutableListOf<Int>()
    private var selectedString : String ? = null
    private var selectedDays: String? = null // Store short form selected days
    private val viewModel : WeeklyFragmentVm by viewModels()

    companion object{
        var repeatDays : List<String> ? = null

    }

    override fun onCreateView(view: View) {
        initOnClick()
        getList()
        initAdapter()
    }


//    private fun getList() {
//
//
//        if (repeatDays != null){
//
//        }
//        weekList.add(WeekModel("Monday", 1))
//        weekList.add(WeekModel("Tuesday",2))
//        weekList.add(WeekModel("Wednesday",3))
//        weekList.add(WeekModel("Thursday",4 ))
//        weekList.add(WeekModel("Friday",5))
//        weekList.add(WeekModel("Saturday",6))
//        weekList.add(WeekModel("Sunday",7))
//    }

    private fun getList() {
        weekList.clear()

        val selectedDays = repeatDays?.mapNotNull { it.toIntOrNull() } ?: emptyList()

        val weekDays = listOf(
            "Monday" to 1,
            "Tuesday" to 2,
            "Wednesday" to 3,
            "Thursday" to 4,
            "Friday" to 5,
            "Saturday" to 6,
            "Sunday" to 7
        )

        weekList.addAll(weekDays.map { (name, count) ->
            WeekModel(name, count, isSelected = selectedDays.contains(count))
        })
    }

    private fun initAdapter() {
        weekAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_weekly, BR.bean) { v, m, pos ->
            if (pos in weekList.indices) { // ✅ Prevents index out of bounds crash
                when (v.id) {
                    R.id.consMain -> toggleSelection(pos)
                }
            }
        }

        binding.rvWeekly.adapter = weekAdapter
        weekAdapter.list = weekList
        weekAdapter.notifyDataSetChanged()
    }

    private fun toggleSelection(position: Int) {
        if (position !in weekList.indices) return // ✅ Prevents crashes if index is invalid

        val item = weekList[position]
        item.isSelected = !item.isSelected

        if (item.isSelected) {
            selectedPositions.add(item.count) // Add selected week number
        } else {
            selectedPositions.remove(item.count) // Remove if unselected
        }

        weekAdapter.notifyItemChanged(position) // Notify only the changed item


        // Convert selected days to short form and store in selectedDays
        val shortDayNames = mapOf(
            1 to "Mon", 2 to "Tues", 3 to "Wed", 4 to "Thurs",
            5 to "Fri", 6 to "Sat", 7 to "Sun"
        )

        selectedDays = selectedPositions
            .sorted() // Sort to maintain order
            .mapNotNull { shortDayNames[it] }
            .joinToString(",")


        // Print selected positions as a comma-separated string
        selectedString = selectedPositions.joinToString(",")
        Log.i("DFDfds", "toggleSelection: $selectedString")
        Log.i("DFDfds", "Selected Days: $selectedDays") // Logs short day names

    }


    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner , Observer {
            when(it?.id){

                R.id.tvContinue ->{
                    CreateHostGameActivity.repeatWeek = selectedString.toString()
                    CreateHostGameActivity.selectedSide = "Weekly"
                    CreateHostGameActivity.selectedDays = selectedDays
                    requireActivity().onBackPressed()
//                    val intent = Intent(requireContext(), CreateHostGameActivity::class.java)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                    startActivity(intent)
                }
            }
        })
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_weekly
    }

    override fun getViewModel(): BaseViewModel {
     return viewModel
    }


}