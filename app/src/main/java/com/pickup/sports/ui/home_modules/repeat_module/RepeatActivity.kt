package com.pickup.sports.ui.home_modules.repeat_module

import android.os.Build
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import com.pickup.sports.R
import com.pickup.sports.databinding.ActivityRepeatBinding
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.home_modules.repeat_module.custom_module.CustomFragment
import com.pickup.sports.ui.home_modules.repeat_module.weekly_module.WeeklyFragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RepeatActivity : BaseActivity<ActivityRepeatBinding>() {

    private val viewModel : RepeatActivityVm by viewModels()
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun getLayoutResource(): Int {
        return R.layout.activity_repeat
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView() {
        initOnClick()
        initAdapter()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initAdapter() {
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager,lifecycle)
        viewPagerAdapter.addFragment(WeeklyFragment())
        viewPagerAdapter.addFragment(CustomFragment())
        binding.viewPager.adapter =  viewPagerAdapter
        binding.viewPager.isUserInputEnabled = false

        TabLayoutMediator(binding.tabLayout,binding.viewPager){ tab,position ->

            when(position){
                0 -> tab.text = "Weekly"

                1 -> tab.text = "Custom"

            }

        }.attach()
    }
}