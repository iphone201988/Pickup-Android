package com.pickup.sports.ui.home_modules.create_host_game

import com.pickup.sports.data.api.ApiHelper
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.utils.Resource
import com.pickup.sports.utils.event.SingleRequestEvent
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class CreateHostGameActivityVm @Inject constructor(private val apiHelper : ApiHelper) :  BaseViewModel() {


    val obrCommon = SingleRequestEvent<JsonObject>()

    fun  createGame(image : MultipartBody.Part ?, data : HashMap<String , RequestBody>, url :String){
        obrCommon.postValue(Resource.loading(null))
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiHelper.apiPostWithMultipart(image,data, url)
                if (response.isSuccessful && response.body() != null){
                    obrCommon.postValue(Resource.success("createGame", response.body()))
                }
                else{
                    obrCommon.postValue(Resource.error(handleErrorResponse(response.errorBody(),response.code()),null))
                }
            }catch (e : java.lang.Exception){
                obrCommon.postValue(Resource.error(e.message.toString(),null))
            }
        }
    }

    fun updateGame(image : MultipartBody.Part ?, data : HashMap<String , RequestBody>, url :String){
        obrCommon.postValue(Resource.loading(null))
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiHelper.apiPutWithMultipart(image,data, url)
                if (response.isSuccessful && response.body() != null){
                    obrCommon.postValue(Resource.success("updateGame", response.body()))
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