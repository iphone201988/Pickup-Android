package com.pickup.sports.data.api

import com.pickup.sports.data.local.SharedPrefManager
import com.pickup.sports.data.model.User
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

class ApiHelperImpl @Inject constructor(private val apiService: ApiService , val sharedPreferences: SharedPrefManager) : ApiHelper {

    override suspend fun getUsers(): Response<List<User>> = apiService.getUsers()
    override suspend fun apiForRawBody(request: HashMap<String, Any>,url:String): Response<JsonObject> {
        return apiService.apiForRawBody(request,url , Constants.timeZone)
    }
    override suspend fun apiForFormData(data: HashMap<String, Any>,url: String): Response<JsonObject> {
        return apiService.apiForFormData(data,url)
    }
    override suspend fun apiGetOutWithQuery(data: HashMap<String, Any>?, url:String): Response<JsonObject> {
        return apiService.apiGetOutWithQuery( url, data, Constants.timeZone)
    }
    override suspend fun apiGetWithQuery( data: HashMap<String, Any>,url: String): Response<JsonObject> {
        return apiService.apiGetWithQuery(url,data)
    }
    override suspend fun apiForPostMultipart(url: String,map: HashMap<String, RequestBody>,
                                          part: MutableList<MultipartBody.Part>): Response<JsonObject> {
        return apiService.apiForPostMultipart(url,getTokenFromSPref(), map, part)
    }

    override suspend fun apiGetWithAuth(url: String): Response<JsonObject> {
        return apiService.apiGetWithAuth(url,getTokenFromSPref())
    }

    override suspend fun apiGetWithoutAuth(url: String): Response<JsonObject> {
        return apiService.apiGetWithoutAuth(url)
    }

    override suspend fun apiPutWithImage(
        image: MultipartBody.Part?,
        data: HashMap<String, RequestBody>,
        url: String
    ): Response<JsonObject> {
        return  apiService.apiPutWithImage(getTokenFromSPref(),image,data, url)
    }

    override suspend fun apiPostWithMultipart(
        image: MultipartBody.Part?,
        data: HashMap<String, RequestBody>,
        url: String
    ): Response<JsonObject> {
        return apiService.apiPostWithMultipart(getTokenFromSPref(),image,data,url)
    }

    override suspend fun apiPutWithMultipart(
        image: MultipartBody.Part?,
        data: HashMap<String, RequestBody>,
        url: String
    ): Response<JsonObject> {
        return  apiService.apiPutWithMultipart(getTokenFromSPref(),image,data,url)
    }

    override suspend fun apiForPutRawBodyWithAuth(
        data: HashMap<String, Any>,
        url: String
    ): Response<JsonObject> {
        return apiService.apiForPutRawBodyWithAuth(data,url,getTokenFromSPref())
    }

    override suspend fun apiGetWithQueryAuth(
        data: HashMap<String, Any>,
        url: String
    ): Response<JsonObject> {
        return apiService.apiGetWithQueryAuth(url,data,getTokenFromSPref() ,Constants.timeZone)
    }

    override suspend fun deleteAccount(url: String): Response<JsonObject> {
        return apiService.deleteAccount(url,getTokenFromSPref())
    }

    override suspend fun apiForPostAuth(url: String): Response<JsonObject> {
        return apiService.apiForPostAuth(url,getTokenFromSPref())
    }

    override suspend fun apiForPutAuth(url: String): Response<JsonObject> {
        return apiService.apiForPutAuth(url,getTokenFromSPref())
    }



    override suspend fun apiForRawBodyAuth(
        data: HashMap<String, Any>,
        url: String
    ): Response<JsonObject> {
        return apiService.apiForRawBodyAuth(data,url,getTokenFromSPref())
    }

    override suspend fun apiForPutAuthQuery(
        data: HashMap<String, Any>,
        url: String
    ): Response<JsonObject> {
        return apiService.apiForPutAuthQuery(data, url, getTokenFromSPref())
    }

    override suspend fun apiForPutWithoutAuth(
        data: HashMap<String, Any>,
        url: String
    ): Response<JsonObject> {
        return apiService.apiForPutWithoutAuth(data,url)
    }

    override suspend fun apiGetWithQueryAuthData(
        data: HashMap<String, Any>,
        url: String
    ): Response<JsonObject> {
        return apiService.apiGetWithQueryAuthData(url,data,getTokenFromSPref())
    }


    private fun getTokenFromSPref(): String {
        return "Bearer ${
            sharedPreferences.getCurrentUser()?.token   
        }"
    }
}