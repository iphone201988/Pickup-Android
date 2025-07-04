package com.pickup.sports.ui.auth.login_signup_module

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
class MainViewModel @Inject constructor(private val apiHelper: ApiHelper) : BaseViewModel() {

    val obrCommon = SingleRequestEvent<JsonObject>()


    //get profile
    fun getProfile(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            obrCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetWithAuth(url)
                if (response.isSuccessful && response.body() != null) {
                    obrCommon.postValue(Resource.success("getProfile", response.body()))
                } else {
                    obrCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            } catch (e: Exception) {
                obrCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }

    //login
    fun login(data: HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            obrCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiForRawBody(data, url)
                if (response.isSuccessful && response.body() != null) {
                    obrCommon.postValue(Resource.success("login", response.body()))
                } else {
                    obrCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            } catch (e: Exception) {
                obrCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }


    //signUp
    fun signUp(data: HashMap<String, Any>, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            obrCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiForRawBody(data, url)
                if (response.isSuccessful && response.body() != null) {
                    obrCommon.postValue(Resource.success("signUp", response.body()))
                } else {
                    obrCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            } catch (e: Exception) {
                obrCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }

    //get Games home module
    fun getGames(data: HashMap<String, Any>?, url: String){
        obrCommon.postValue(Resource.loading(null))
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiHelper.apiGetOutWithQuery(data,url)
                if (response.isSuccessful && response.body() != null){
                    obrCommon.postValue(Resource.success("getGame", response.body()))
                }
                else{
                    obrCommon.postValue(Resource.error(handleErrorResponse(response.errorBody(),response.code()),null))
                }
            }catch (e : Exception){
                obrCommon.postValue(Resource.error(e.message.toString(),null))
            }
        }

    }

    fun singleGameDetail(data : HashMap<String, Any>, url: String){
        CoroutineScope(Dispatchers.IO).launch {
            obrCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetWithQueryAuth(data, url)
                if (response.isSuccessful && response.body() != null) {
                    obrCommon.postValue(Resource.success("singleGameDetail", response.body()))
                } else {
                    obrCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            } catch (e: Exception) {
                obrCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }
    fun leaveGame( url: String){
        CoroutineScope(Dispatchers.IO).launch {
            obrCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetWithAuth( url)
                if (response.isSuccessful && response.body() != null) {
                    obrCommon.postValue(Resource.success("leaveGame", response.body()))
                } else {
                    obrCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            } catch (e: Exception) {
                obrCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }


    fun cancelGame( data: HashMap<String, Any>,url: String){
        CoroutineScope(Dispatchers.IO).launch {
            obrCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiForPutAuthQuery( data,url)
                if (response.isSuccessful && response.body() != null) {
                    obrCommon.postValue(Resource.success("cancelGame", response.body()))
                } else {
                    obrCommon.postValue(
                        Resource.error(
                            handleErrorResponse(
                                response.errorBody(),
                                response.code()
                            ), null
                        )
                    )
                }
            } catch (e: Exception) {
                obrCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }

    fun rating(data: HashMap<String, Any>, url: String ){
        CoroutineScope(Dispatchers.IO).launch {
            obrCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiForRawBodyAuth(data, url)
                if (response.isSuccessful && response.body() != null){
                    obrCommon.postValue(Resource.success("rating", response.body()))
                }else{
                    obrCommon.postValue(Resource.error(handleErrorResponse(response.errorBody(),response.code()),null))
                }
            }catch (e : Exception) {
                obrCommon.postValue(Resource.error(e.message.toString(), null))
            }
        }
    }


    fun getAllSports(url : String){
        CoroutineScope(Dispatchers.IO).launch {
            obrCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetWithAuth(url)
                if (response.isSuccessful && response.body() != null){
                    obrCommon.postValue(Resource.success("getAllSports", response.body()))
                }
                else{
                    obrCommon.postValue(Resource.error(handleErrorResponse(response.errorBody(),response.code()),null))
                }
            }catch (e : Exception){
                obrCommon.postValue(Resource.error(e.message.toString(),null))
            }
        }

    }


    fun  getAllPlayer(data: HashMap<String, Any>,url : String){
        CoroutineScope(Dispatchers.IO).launch {
            obrCommon.postValue(Resource.loading(null))
            try {
                val response = apiHelper.apiGetOutWithQuery(data,url)
                if (response.isSuccessful && response.body() != null){
                    obrCommon.postValue(Resource.success("getAllPlayer", response.body()))
                }
                else{
                    obrCommon.postValue(Resource.error(handleErrorResponse(response.errorBody(),response.code()),null))
                }
            }catch (e : Exception){
                obrCommon.postValue(Resource.error(e.message.toString(),null))
            }
        }
    }

}