package com.pickup.sports.ui.home_modules.host_games_module

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter

class HostGameAdapter(fragmentManager: FragmentManager, lifecycle: androidx.lifecycle.Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle)  {

    private val arrayList = ArrayList<Fragment>()


    fun addFragment(fragment: Fragment) {
        arrayList.add(fragment)
        notifyDataSetChanged() // Notify adapter that data set has changed
    }
    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun createFragment(position: Int): Fragment {
        return arrayList[position]
    }

    // Ensure that fragments are properly destroyed and recreated when needed
    override fun getItemId(position: Int): Long {
        return arrayList[position].hashCode().toLong()
    }

    // Ensure that fragments are properly destroyed and recreated when needed
    override fun containsItem(itemId: Long): Boolean {
        return arrayList.any { it.hashCode().toLong() == itemId }
    }
}