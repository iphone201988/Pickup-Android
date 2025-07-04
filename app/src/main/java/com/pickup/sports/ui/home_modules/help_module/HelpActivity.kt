package com.pickup.sports.ui.home_modules.help_module

import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.pickup.sports.R
import com.pickup.sports.databinding.ActivityHelpBinding
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HelpActivity : BaseActivity<ActivityHelpBinding>() {

    private val viewModel : HelpActivityVm by viewModels()

    override fun getLayoutResource(): Int {
        return R.layout.activity_help
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        initOnClick()
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