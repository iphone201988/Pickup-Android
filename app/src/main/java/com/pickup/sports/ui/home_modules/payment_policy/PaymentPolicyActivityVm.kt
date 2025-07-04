package com.pickup.sports.ui.home_modules.payment_policy

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
class PaymentPolicyActivityVm @Inject constructor(private val apiHelper: ApiHelper): BaseViewModel() {

    val obrCommon = SingleRequestEvent<JsonObject>()

    fun getPaymentMethod(url : String){
        CoroutineScope(Dispatchers.IO).launch {
            obrCommon.postValue(Resource.loading( null))
            try {
                val response = apiHelper.apiGetWithAuth(url)
                if (response.isSuccessful && response.body() != null){
                    obrCommon.postValue(Resource.success("getPaymentMethod", response.body()))
                }
                else{
                    obrCommon.postValue(Resource.error(handleErrorResponse(response.errorBody(),response.code()),null))
                }
            }catch (e : java.lang.Exception){
                obrCommon.postValue(Resource.error(e.message.toString(),null))
            }
        }
    }


    fun joinGame(data : HashMap<String,Any>, url: String){
        CoroutineScope(Dispatchers.IO).launch {
            obrCommon.postValue(Resource.loading( null))
            try {
                val response = apiHelper.apiForRawBodyAuth(data,url)
                if (response.isSuccessful && response.body() != null){
                    obrCommon.postValue(Resource.success("joinGame", response.body()))
                }
                else{
                    obrCommon.postValue(Resource.error(handleErrorResponse(response.errorBody(),response.code()),null))
                }
            }catch (e : java.lang.Exception){
                obrCommon.postValue(Resource.error(e.message.toString(),null))
            }
        }
    }


    fun deleteCard( url: String){
        CoroutineScope(Dispatchers.IO).launch {
            obrCommon.postValue(Resource.loading( null))
            try {
                val response = apiHelper.deleteAccount(url)
                if (response.isSuccessful && response.body() != null){
                    obrCommon.postValue(Resource.success("deleteCard", response.body()))
                }
                else{
                    obrCommon.postValue(Resource.error(handleErrorResponse(response.errorBody(),response.code()),null))
                }
            }catch (e : java.lang.Exception){
                obrCommon.postValue(Resource.error(e.message.toString(),null))
            }
        }
    }
}