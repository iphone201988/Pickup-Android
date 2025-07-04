package com.pickup.sports.ui.home_modules.my_games.my_past_games

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
import com.pickup.sports.databinding.FragmentMyPastGameBinding
import com.pickup.sports.databinding.ItemLayoutMyGameDatesBinding
import com.pickup.sports.ui.base.BaseFragment
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.base.SimpleRecyclerViewAdapter
import com.pickup.sports.ui.home_modules.HomeDashBoardActivity
import com.pickup.sports.ui.home_modules.game_details.GameDetailsActivity
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.Status
import com.pickup.sports.utils.VerticalPagination
import dagger.hilt.android.AndroidEntryPoint
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


@AndroidEntryPoint
class MyPastGameFragment : BaseFragment<FragmentMyPastGameBinding>() {

    private val viewModel : MyPastGameFragmentVm by viewModels()

    private lateinit var myPastGameAdapter : SimpleRecyclerViewAdapter<GetMyGames, ItemLayoutMyGameDatesBinding>


    private var pagination: VerticalPagination? = null
    var page  = 1



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(view: View) {
       initOnClick()
        getMyGames()
        initAdapter()
        setObserver()

        GameDetailsActivity.gameStatus = "Past"
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
                            val currentDateTime = OffsetDateTime.now()


                            val pastGames = myDataModel?.games?.filter {
                                it?.hostTimestamp?.let { timestamp ->
                                    val gameDateTime = OffsetDateTime.parse(timestamp, formatter)
                                    gameDateTime.isBefore(currentDateTime) // Move to past games if it's before now
//                                    val gameDateTime = LocalDateTime.parse(timestamp, formatter)
//                                    gameDateTime.isBefore(currentDateTime)
                                } ?: false
                            } ?: emptyList()

                            Log.i("dfdsfsf", "upcoming: $pastGames")

                            val groupedList = ImageUtils.groupMyGamesByDate(pastGames)
                            if (page <= myDataModel.totalPages!!){
                                pagination?.isLoading = false
                            }
                            if (page == 1){
                                myPastGameAdapter.list = groupedList
                            }else{
                                myPastGameAdapter.addToListPast(groupedList)
                            }

//                            myPastGameAdapter.list = groupedList
//                            myPastGameAdapter .notifyDataSetChanged()
                        }catch (e : Exception){
                            e.printStackTrace()
                        }


                    }

                    if (myPastGameAdapter.list.isNullOrEmpty()){
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
        myPastGameAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_my_game_dates, BR.bean) { v, m, pos ->
                when (v.id) {
                    R.id.dropDown , R.id.tvDate-> {
                        m.isVisible = !m.isVisible
                        myPastGameAdapter.notifyDataSetChanged()
                    }
                }
            }
        binding.rvMyGames.adapter = myPastGameAdapter

        val lm = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        pagination = VerticalPagination(lm, 2)
        pagination?.setListener(object : VerticalPagination.VerticalScrollListener {
            override fun onLoadMore() {
                page++
                Log.i("dsadasd", "onLoadMore: $page")
                val data = HashMap<String, Any>()
                data["page"] = page.toString()
                data["hosted"] = false
                data["past"] = true
                viewModel.getMyGames(data, Constants.HOSTED_GAME)
            }
        })
        pagination?.let {
            binding.rvMyGames.addOnScrollListener(it)
        }
    }

    private fun initOnClick() {

    }

    override fun onResume() {
        ImageUtils.side = "past"
        getMyGames()
        HomeDashBoardActivity.refresh.observe(this){
            Log.i("fdfdsfs", "onResume:${HomeDashBoardActivity.refresh} ")
            when(it?.status){
                Status.LOADING ->{

                }Status.SUCCESS ->{
                Log.i("fdfdsfs", "onResume: ${HomeDashBoardActivity.refresh}")
                getMyGames()

            }Status.ERROR ->{

            }
                else ->{

                }
            }
        }
        super.onResume()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_my_past_game
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private fun getMyGames() {
        val data = HashMap<String,Any>()
        data["hosted"] = false
        data["past"] = true
        viewModel.getMyGames(data, Constants.HOSTED_GAME)



    }


}