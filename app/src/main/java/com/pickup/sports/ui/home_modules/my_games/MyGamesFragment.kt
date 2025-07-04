package com.pickup.sports.ui.home_modules.my_games

import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayoutMediator
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.model.GetMyGamesApiResponse
import com.pickup.sports.data.model.MyGames
import com.pickup.sports.databinding.FragmentMyGamesBinding

import com.pickup.sports.ui.base.BaseFragment
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.home_modules.my_games.my_past_games.MyPastGameFragment
import com.pickup.sports.ui.home_modules.my_games.my_upcoming_games.MyUpcomingGameFragment
import com.pickup.sports.ui.home_modules.repeat_module.ViewPagerAdapter
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class MyGamesFragment : BaseFragment<FragmentMyGamesBinding>() {


    private val viewModel : MyGamesFragmentVm by viewModels()
    private lateinit var viewPagerAdapter: ViewPagerAdapter
//    private lateinit var pastGameAdapter : SimpleRecyclerViewAdapter<GetMyGamesApiResponse.Game,ItemLayoutMyGamesBinding>
//    private lateinit var upcomingGameAdapter : SimpleRecyclerViewAdapter<GetMyGamesApiResponse.Game,ItemLayoutMyGamesBinding>

    val myGamesList = mutableListOf<MyGames>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(view: View) {
        initOnClick()
      //  getMyGames()
        initAdapter()
     //   setObserver()

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
                        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                        val currentDateTime = LocalDateTime.now()

                        val pastGames = myDataModel?.games?.filter {
                            it?.hostTimestamp?.let { timestamp ->
                                val gameDateTime = LocalDateTime.parse(timestamp, formatter)
                                gameDateTime.isBefore(currentDateTime)
                            } ?: false
                        } ?: emptyList()

                        val upcomingGames = myDataModel?.games?.filter {
                            it?.hostTimestamp?.let { timestamp ->
                                val gameDateTime = LocalDateTime.parse(timestamp, formatter)
                                gameDateTime.isAfter(currentDateTime)
                            } ?: false
                        } ?: emptyList()

                        Log.i("dfdsfsf", "pastgames: $pastGames")
                        Log.i("dfdsfsf", "upcoming: $upcomingGames")
//                        pastGameAdapter.list = pastGames
//                        upcomingGameAdapter.list = upcomingGames
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

    private fun getMyGames() {
        val data = HashMap<String,Any>()
        data["hosted"] = false
        viewModel.getMyGames(data, Constants.HOSTED_GAME)
    }


    private fun initAdapter() {
//        pastGameAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_my_games,BR.bean){v,m,pos ->
//            when(v.id){
//                R.id.consMain ->{
//                    GameDetailsActivity.side = "My games"
//                    val intent = Intent(requireContext(), GameDetailsActivity::class.java)
//                    intent.putExtra("id",m._id)
//                    startActivity(intent)
//                }
//            }
//
//        }
//        binding.rvPastGames.adapter = pastGameAdapter
//        pastGameAdapter.notifyDataSetChanged()
//
//
//        // upcoming game adapter
//        upcomingGameAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_my_games,BR.bean){v,m,pos ->
//            GameDetailsActivity.side = "My games"
//            val intent = Intent(requireContext(), GameDetailsActivity::class.java)
//            intent.putExtra("id",m._id)
//            startActivity(intent)
//        }
//        binding.rvUpcoming.adapter = upcomingGameAdapter
//        upcomingGameAdapter.notifyDataSetChanged()

        viewPagerAdapter = ViewPagerAdapter(childFragmentManager,lifecycle)
        viewPagerAdapter.addFragment(MyUpcomingGameFragment())
        viewPagerAdapter.addFragment(MyPastGameFragment())
        binding.viewPager.adapter =  viewPagerAdapter
        binding.viewPager.isUserInputEnabled = false


        TabLayoutMediator(binding.tabLayout,binding.viewPager){ tab,position ->

            when(position){
                0 -> {
                    tab.text = "Upcoming Game"

                }

                1 -> {
                    tab.text = "Past Game"

                }

            }

        }.attach()
    }

    private fun initOnClick() {

    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_my_games
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onResume() {
        super.onResume()
        val data = HashMap<String,Any>()
        data["hosted"] = false
        viewModel.getMyGames(data, Constants.HOSTED_GAME)
    }

}