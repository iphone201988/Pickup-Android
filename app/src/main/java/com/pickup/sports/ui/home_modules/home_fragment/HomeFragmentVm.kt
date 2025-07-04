package com.pickup.sports.ui.home_modules.home_fragment

import com.pickup.sports.data.api.ApiHelper
import com.pickup.sports.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeFragmentVm @Inject constructor(private val apiHelper: ApiHelper) : BaseViewModel() {



}