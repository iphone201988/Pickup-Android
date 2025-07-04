package com.pickup.sports.ui.home_modules.messages_module

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.pickup.sports.R
import com.pickup.sports.databinding.FragmentMessageBinding
import com.pickup.sports.ui.base.BaseFragment
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.home_modules.messages_module.game_group_messages.GroupMessageFragment
import com.pickup.sports.ui.home_modules.messages_module.player_messages.PlayerMessageFragment
import com.pickup.sports.ui.home_modules.my_games.my_past_games.MyPastGameFragment
import com.pickup.sports.ui.home_modules.my_games.my_upcoming_games.MyUpcomingGameFragment
import com.pickup.sports.ui.home_modules.repeat_module.ViewPagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import org.checkerframework.checker.units.qual.A

@AndroidEntryPoint
class MessageFragment : BaseFragment<FragmentMessageBinding>() {

    private val viewModel : MessageFragmentVm by viewModels()
    private lateinit var viewPagerAdapter: MessageAdapter

    override fun onCreateView(view: View) {
        initOnClick()
        initAdapter()
    }

    private fun initAdapter() {
        viewPagerAdapter = MessageAdapter(childFragmentManager,lifecycle)
        viewPagerAdapter.addFragment(GroupMessageFragment())
        viewPagerAdapter.addFragment(PlayerMessageFragment())
        binding.viewPager.adapter =  viewPagerAdapter
        binding.viewPager.isUserInputEnabled = false


        TabLayoutMediator(binding.tabLayout,binding.viewPager){ tab,position ->

            when(position){
                0 -> {
                    tab.text = "Game Group Messages"

                }

                1 -> {
                    tab.text = "Player Messages"

                }

            }

        }.attach()
    }

    private fun initOnClick() {

    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_message
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

}