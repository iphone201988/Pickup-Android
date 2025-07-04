package com.pickup.sports.ui.home_modules.host_games_module.past_games

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
import com.pickup.sports.data.model.GetHostGames
import com.pickup.sports.data.model.GetHostGamesApiResponse
import com.pickup.sports.databinding.FragmentPastGameBinding
import com.pickup.sports.databinding.ItemLayoutHostEventDateBinding
import com.pickup.sports.ui.base.BaseFragment
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.base.SimpleRecyclerViewAdapter
import com.pickup.sports.ui.host_game_details.HostGameDetailsActivity
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.Status
import com.pickup.sports.utils.VerticalPagination
import dagger.hilt.android.AndroidEntryPoint
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class PastGameFragment : BaseFragment<FragmentPastGameBinding>() {

    private val viewModel : PastGameFragmentVm by viewModels()

    private lateinit var pastGameAdapter : SimpleRecyclerViewAdapter<GetHostGames, ItemLayoutHostEventDateBinding>
    private var pagination: VerticalPagination? = null
    var page  = 1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(view: View) {
        initOnClick()
        getHostGames()
        initObserver()
        initAdapter()

        HostGameDetailsActivity.gameStatus = "Past"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initObserver() {
        viewModel.obrCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING ->{
                    progressDialogAvl.isLoading(true)
                }
                Status.SUCCESS ->{
                    hideLoading()
                    val myDataModel: GetHostGamesApiResponse? = ImageUtils.parseJson(it.data.toString())
                    try {
                        if (myDataModel != null && !myDataModel.games.isNullOrEmpty()) {

                            val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                            val currentDateTime = OffsetDateTime.now() // Gets the current time with offset


                            val pastGames = myDataModel?.games?.filter {
                                it?.hostTimestamp?.let { timestamp ->
                                    val gameDateTime = OffsetDateTime.parse(timestamp, formatter)
                                    gameDateTime.isBefore(currentDateTime) // Move to past games if it's before now
//                                val gameDateTime = LocalDateTime.parse(timestamp, formatter)
//                                gameDateTime.isBefore(currentDateTime)
                                } ?: false
                            } ?: emptyList()

                            Log.i("asdasd", "initObserver: $pastGames ")
                            val groupedList = ImageUtils.groupGamesByDate(pastGames)

                            if (page <= myDataModel.totalPages!!){
                                pagination?.isLoading = false
                            }
                            if (page == 1){
                                pastGameAdapter.list = groupedList
                            }else{
                                pastGameAdapter.addToListHost(groupedList)
                            }
//                            pastGameAdapter.list = groupedList
//                            pastGameAdapter.notifyDataSetChanged()
                        }

                    }catch (e :Exception){
                        e.printStackTrace()
                    }


                    if (pastGameAdapter.list.isNullOrEmpty()){
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
        pastGameAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_host_event_date, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.dropDown , R.id.tvDate -> {
                        m.isVisible = !m.isVisible
                        pastGameAdapter.notifyDataSetChanged()
                    }
                }
            }
        binding.rvHostGame.adapter = pastGameAdapter
        val lm = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        pagination = VerticalPagination(lm, 2)
        pagination?.setListener(object : VerticalPagination.VerticalScrollListener {
            override fun onLoadMore() {
                page++
                Log.i("dsadasd", "onLoadMore: $page")
                val data = HashMap<String, Any>()
                data["page"] = page.toString()
                data["hosted"] = true
                data["past"] = true
                viewModel.getHostGame(data, Constants.HOSTED_GAME)
            }
        })
        pagination?.let {
            binding.rvHostGame.addOnScrollListener(it)
        }
    }

    private fun initOnClick() {

    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_past_game
    }

    override fun getViewModel(): BaseViewModel {
       return viewModel
    }

    private fun getHostGames() {
        val data = HashMap<String,Any>()
        data["hosted"] = true
        data["past"] = true
        viewModel.getHostGame(data, Constants.HOSTED_GAME)
    }
}