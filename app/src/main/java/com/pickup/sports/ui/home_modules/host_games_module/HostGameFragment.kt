package com.pickup.sports.ui.home_modules.host_games_module

import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayoutMediator
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.model.GetHostGamesApiResponse
import com.pickup.sports.data.model.HostedGame
import com.pickup.sports.databinding.FragmentHostGameBinding
import com.pickup.sports.ui.base.BaseFragment
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.home_modules.create_host_game.CreateHostGameActivity
import com.pickup.sports.ui.home_modules.host_games_module.past_games.PastGameFragment
import com.pickup.sports.ui.home_modules.host_games_module.upcoming_game.UpcomingGameFragment
import com.pickup.sports.ui.home_modules.repeat_module.ViewPagerAdapter
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.Status
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class HostGameFragment : BaseFragment<FragmentHostGameBinding>() {

//    private lateinit var hostAdapter : SimpleRecyclerViewAdapter<GetHostGames, ItemLayoutHostEventDateBinding>
//    private lateinit var pastHostAdapter : SimpleRecyclerViewAdapter<GetHostGamesApiResponse.Game, ItemLayoutHostGameDataBinding>
//    private lateinit var upcomingHostAdapter : SimpleRecyclerViewAdapter<GetHostGamesApiResponse.Game, ItemLayoutHostGameDataBinding>
private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var hostGameList = arrayListOf<HostedGame>()
    private val viewModel : HostGameFragmentVm by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(view: View) {
        initOnClick()
     //   getHostGames()
        initAdapter()
      //  initObserver()
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
                    if (myDataModel != null && !myDataModel.games.isNullOrEmpty()) {
//                        val groupedList = ImageUtils.groupGamesByDate(myDataModel.games)
//                        hostAdapter.list = groupedList
//                        hostAdapter.notifyDataSetChanged()


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
//                        pastHostAdapter.list = pastGames
//                        upcomingHostAdapter.list = upcomingGames
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

    private fun getHostGames() {
        val data = HashMap<String,Any>()
        data["hosted"] = true
        viewModel.getHostGame(data,Constants.HOSTED_GAME)
    }

    private fun initAdapter() {
//        hostAdapter  = SimpleRecyclerViewAdapter(R.layout.item_layout_host_event_date, BR.bean){v,m,pos ->
//            when(v.id){
//                R.id.dropDown ->{
//                    m.isVisible  = !m.isVisible
//                    hostAdapter.notifyDataSetChanged()
//                }
//            }
//        }
//        binding.rvHostGame.adapter = hostAdapter

//        pastHostAdapter  = SimpleRecyclerViewAdapter(R.layout.item_layout_host_game_data, BR.bean){v,m,pos ->
//            when(v.id){
//                R.id.consMain ->{
//                    val intent = Intent(requireContext() , HostGameDetailsActivity::class.java)
//                    intent.putExtra("id",m._id)
//                    startActivity(intent)
//                }
//            }
//        }
//        binding.rvPastGames.adapter = pastHostAdapter
//        pastHostAdapter.notifyDataSetChanged()
//
//        //upcoming games
//        upcomingHostAdapter  = SimpleRecyclerViewAdapter(R.layout.item_layout_host_game_data, BR.bean){v,m,pos ->
//            when(v.id){
//                R.id.consMain ->{
//                    val intent = Intent(requireContext() , HostGameDetailsActivity::class.java)
//                    intent.putExtra("id",m._id)
//                    startActivity(intent)
//                }
//            }
//        }
//        binding.rvUpcoming.adapter = upcomingHostAdapter
//        upcomingHostAdapter.notifyDataSetChanged()


            viewPagerAdapter = ViewPagerAdapter(childFragmentManager,lifecycle)
            viewPagerAdapter.addFragment(UpcomingGameFragment())
            viewPagerAdapter.addFragment(PastGameFragment())
            binding.viewPager.adapter =  viewPagerAdapter
            binding.viewPager.isUserInputEnabled = false

            TabLayoutMediator(binding.tabLayout,binding.viewPager){ tab,position ->

                when(position){
                    0 -> tab.text = "Upcoming Game"

                    1 -> tab.text = "Past Game"

                }

            }.attach()

    }


    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when(it?.id){
                R.id.tvHostGame ->{
                    val intent = Intent(requireContext(), CreateHostGameActivity::class.java)
                    startActivity(intent)
                }
            }
        })
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_host_game
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }


}