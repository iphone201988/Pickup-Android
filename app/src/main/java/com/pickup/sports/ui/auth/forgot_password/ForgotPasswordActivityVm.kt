package com.pickup.sports.ui.auth.forgot_password

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
class ForgotPasswordActivityVm @Inject constructor(private val apiHelper: ApiHelper) : BaseViewModel() {

    val obrCommon = SingleRequestEvent<JsonObject>()

    fun forgotPassword(data : HashMap<String,Any>, url : String){
        CoroutineScope(Dispatchers.IO).launch {
            obrCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiForRawBody(data,url)
                if (response.isSuccessful && response.body() != null){
                    obrCommon.postValue(Resource.success("forgotPassword", response.body()))
                }else{
                    obrCommon.postValue(Resource.error(handleErrorResponse(response.errorBody()),null))
                }
            }catch (e : Exception){
                obrCommon.postValue(Resource.error(e.message.toString(),null))
            }
        }
    }
}