package com.pickup.sports.ui.home_modules.select_sports

import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.pickup.sports.BR
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.model.GetAllSportsApiResponse
import com.pickup.sports.data.model.SelectSports
import com.pickup.sports.databinding.ActivitySelectSportsBinding
import com.pickup.sports.databinding.ItemLayoutSelectSportsBinding
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.base.SimpleRecyclerViewAdapter
import com.pickup.sports.ui.home_modules.create_host_game.CreateHostGameActivity
import com.pickup.sports.ui.home_modules.home_fragment.HomeFragment
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectSportsActivity : BaseActivity<ActivitySelectSportsBinding>() {

    private val viewModel : SelectSportsActivityVm by  viewModels()

    private lateinit var sportsAdapter : SimpleRecyclerViewAdapter<GetAllSportsApiResponse.Sport,ItemLayoutSelectSportsBinding>

    private var sportList = arrayListOf<SelectSports>()
    private var side : String ? = null
    override fun getLayoutResource(): Int {
         return R.layout.activity_select_sports
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
     }

    override fun onCreateView() {
        initOnClick()
        getData()
        viewModel.getAllSports(Constants.GET_ALL_SPORTS)
        setObserver()
        initAdapter()

    }

    private fun getData() {
        side = intent.getStringExtra("side")
        Log.i("dfdf", "getData: $side")
    }

    private fun setObserver() {
        viewModel.obrCommon.observe(this , Observer {
            when(it?.status){
                Status.LOADING ->{
                    progressDialogAvl.isLoading(true)
                }
                Status.SUCCESS ->{
                    hideLoading()
                    val myDataModel : GetAllSportsApiResponse ? = ImageUtils.parseJson(it.data.toString())
//                    if (myDataModel != null){
//                        try {
//                            if (myDataModel.sports != null){
//                                sportsAdapter.list = myDataModel.sports
//                            }
//                        }catch (e : Exception){
//                            e.printStackTrace()
//                        }
//
//                    }

                    try {
                        if (!myDataModel?.sports.isNullOrEmpty()) {
                            val allSports = GetAllSportsApiResponse.Sport(
                                _id = "", // Assign a unique ID
                                name = "All Sports",
                                isSelected = false
                            )

                            val updatedList = mutableListOf(allSports) // Add "All Sports" at the beginning
                            myDataModel?.sports?.filterNotNull()
                                ?.let { it1 -> updatedList.addAll(it1) } // Add the remaining sports

                            sportsAdapter.list = updatedList
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                    if (sportsAdapter.list.isNullOrEmpty()){
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
        sportsAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_select_sports, BR.bean){v, m,pos ->
            when(v.id){
                R.id.consMain ->{
                    for (i in sportList.indices) {
                        sportList[i].isSelected = i == pos
                        sportsAdapter.notifyDataSetChanged()
                    }

                    if (side != null){
                        if (side == "Home"){
                            HomeFragment.sportName = m.name
                            HomeFragment.gameId = m._id
                            HomeFragment.side = "selectSports"
                        }
                        if (side == "CreateGame"){
                            Log.i("Fdsfdsf", "initAdapter: dfdsfdsf")
                            CreateHostGameActivity.id = m._id.toString()
                            CreateHostGameActivity.sportName  = m.name
                        }
                    }


                    onBackPressedDispatcher.onBackPressed()


                }
            }

        }
        binding.rvSelectSports.adapter = sportsAdapter
        sportsAdapter.notifyDataSetChanged()
    }



    private fun initOnClick() {
        viewModel.onClick.observe(this , Observer {
            when(it?.id){
                R.id.ivBack ->{
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

}