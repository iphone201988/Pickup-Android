package com.pickup.sports.ui.home_modules.my_games.my_upcoming_games

import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pickup.sports.BR
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.model.GetMyGames
import com.pickup.sports.data.model.GetMyGamesApiResponse
import com.pickup.sports.databinding.FragmentMyUpcomingGameBinding
import com.pickup.sports.databinding.ItemLayoutMyGameDatesBinding
import com.pickup.sports.ui.base.BaseFragment
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.base.SimpleRecyclerViewAdapter
import com.pickup.sports.ui.home_modules.game_details.GameDetailsActivity
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.Status
import com.pickup.sports.utils.VerticalPagination
import dagger.hilt.android.AndroidEntryPoint
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


@AndroidEntryPoint
class MyUpcomingGameFragment : BaseFragment<FragmentMyUpcomingGameBinding>() {

    private val viewModel :  MyUpcomingGameVm by viewModels()
    private lateinit var upcomingGameAdapter : SimpleRecyclerViewAdapter<GetMyGames, ItemLayoutMyGameDatesBinding>
    private var pagination: VerticalPagination? = null
    var page  = 1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(view: View) {
        initOnClick()
        getMyGames()
        initAdapter()
        setObserver()


        GameDetailsActivity.gameStatus = "Upcoming"

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setObserver() {
        viewModel.obrCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    progressDialogAvl.isLoading(true)
                }
                Status.SUCCESS ->{
                    hideLoading()
                    val myDataModel: GetMyGamesApiResponse? = ImageUtils.parseJson(it.data.toString())

                    if (myDataModel != null && !myDataModel.games.isNullOrEmpty()) {
                        try {
                            val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                            val currentDateTime = OffsetDateTime.now() // Gets the current time with offset


                            val upcomingGames = myDataModel?.games?.filter {
                                it?.hostTimestamp?.let { timestamp ->
                                    val gameDateTime = OffsetDateTime.parse(timestamp, formatter)
                                    gameDateTime.isAfter(currentDateTime) // Move to upcoming games if it's after now
//                                    val gameDateTime = LocalDateTime.parse(timestamp, formatter)
//                                    gameDateTime.isAfter(currentDateTime)
                                } ?: false
                            } ?: emptyList()

                            Log.i("dfdsfsf", "upcoming: $upcomingGames")

                            val groupedList = ImageUtils.groupMyGamesByDate(upcomingGames)
                            if (page <= myDataModel.totalPages!!){
                                pagination?.isLoading = false
                            }
                            if (page == 1){
                                upcomingGameAdapter.list = groupedList
                            }else{
                                upcomingGameAdapter.addToListPast(groupedList)
                            }

//                            upcomingGameAdapter.list = groupedList
//                            upcomingGameAdapter.notifyDataSetChanged()
                        }catch (e : Exception){
                            e.printStackTrace()
                        }


                    }
                    if (upcomingGameAdapter.list.isNullOrEmpty()){
                        binding.textNoData.visibility = View.VISIBLE
                    }
                    else{
                        binding.textNoData.visibility = View.GONE
                    }

                }
                Status.ERROR ->{
                    hideLoading()
                    showToast(it.message.toString())
                }
                else ->{

                }
            }
        })
    }

    private fun initAdapter() {
        upcomingGameAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_my_game_dates, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.dropDown , R.id.tvDate -> {
                        m.isVisible = !m.isVisible
                        upcomingGameAdapter.notifyDataSetChanged()
                    }
                }
            }
        binding.rvMyGames.adapter = upcomingGameAdapter

        val lm = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        pagination = VerticalPagination(lm, 2)
        pagination?.setListener(object : VerticalPagination.VerticalScrollListener {
            override fun onLoadMore() {
                page++
                Log.i("dsadasd", "onLoadMore: $page")
                val data = HashMap<String, Any>()
                data["page"] = page.toString()
                data["hosted"] = false
                data["past"] = false
                viewModel.getMyGames(data, Constants.HOSTED_GAME)

            }
        })
        pagination?.let {
            binding.rvMyGames.addOnScrollListener(it)
        }
    }

    private fun initOnClick() {

    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_my_upcoming_game
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }
    private fun getMyGames() {
        val data = HashMap<String,Any>()
        data["hosted"] = false
        data["past"] = false
        viewModel.getMyGames(data, Constants.HOSTED_GAME)
    }

    override fun onResume() {
        getMyGames()
        ImageUtils.side = "Upcoming"
        super.onResume()
    }

}