package com.pickup.sports.ui.home_modules

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pickup.sports.BR
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.model.GetAllGameApiResponse
import com.pickup.sports.databinding.ActivityDummyBinding
import com.pickup.sports.databinding.ItemLayoutEventsBinding
import com.pickup.sports.ui.auth.login_signup_module.MainViewModel
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.base.SimpleRecyclerViewAdapter
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.Status
import com.pickup.sports.utils.VerticalPagination
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DummyActivity : BaseActivity<ActivityDummyBinding>() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit  var simpleAdapter : SimpleRecyclerViewAdapter<GetAllGameApiResponse.Game, ItemLayoutEventsBinding>

    private var pagination: VerticalPagination? = null
    var page  = 1
    override fun getLayoutResource(): Int {
        return R.layout.activity_dummy
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {

        val data = HashMap<String, Any>()
        data["date"] = "2025-05-12"
        data["page"] = 1
        viewModel.getGames(data, Constants.GET_ALL_GAMES)
        initAdapter()
        setObserver()


    }

    private fun setObserver() {
        viewModel.obrCommon.observe(this , Observer {
            when(it?.status){
                Status.LOADING ->{
                    progressDialogAvl.isLoading(false)
                }
                Status.SUCCESS ->{
                    hideLoading()
                    val myDataModel : GetAllGameApiResponse ? = ImageUtils.parseJson(it.data.toString())
                    if (myDataModel != null){
                        if (myDataModel.games != null){
                            if (page <= myDataModel.totalPages!!){
                                pagination?.isLoading = false
                            }
                            if (page == 1) {
                                simpleAdapter.list = myDataModel.games
                            }else{
                                simpleAdapter.addToList(myDataModel.games)
                            }
                        }
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
         simpleAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_events,BR.bean){v,m,pos ->

         }
        binding.eventGame.adapter = simpleAdapter
        val lm = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        pagination = VerticalPagination(lm, 2)
        pagination?.setListener(object : VerticalPagination.VerticalScrollListener {
            override fun onLoadMore() {
                page++
                Log.i("dsadasd", "onLoadMore: $page")
                val data = HashMap<String, Any>()
                data["page"] = page.toString()
                data["date"] = "2025-05-12"
                viewModel.getGames(data, Constants.GET_ALL_GAMES)

            }
        })
        pagination?.let {
            binding.eventGame.addOnScrollListener(it)
        }
    }
}