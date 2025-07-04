package com.pickup.sports.ui.home_modules.account_module

import com.pickup.sports.data.api.ApiHelper
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.utils.Resource
import com.pickup.sports.utils.event.SingleRequestEvent
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountActivityVm @Inject constructor(private val apiHelper: ApiHelper) : BaseViewModel() {

    val obrCommon  = SingleRequestEvent<JsonObject>()

    fun getProfile(url : String){
        CoroutineScope(Dispatchers.IO).launch {
            obrCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetWithAuth(url)
                if (response.isSuccessful && response.body() != null){
                    obrCommon.postValue(Resource.success("getProfile", response.body()))
                }else{
                    obrCommon.postValue(Resource.error(handleErrorResponse(response.errorBody(),response.code()),null))
                }
            }catch (e : Exception){
                obrCommon.postValue(Resource.error(e.message.toString(),null))
            }
        }
    }

    fun logOut(url : String){
        CoroutineScope(Dispatchers.IO).launch {
            obrCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetWithAuth(url)
                if (response.isSuccessful && response.body() != null){
                    obrCommon.postValue(Resource.success("logOut", response.body()))
                }else{
                    obrCommon.postValue(Resource.error(handleErrorResponse(response.errorBody(),response.code()),null))
                }
            }catch (e : Exception){
                obrCommon.postValue(Resource.error(e.message.toString(),null))
            }
        }
    }
}