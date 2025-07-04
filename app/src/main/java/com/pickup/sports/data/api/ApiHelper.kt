package com.pickup.sports.data.api

import com.pickup.sports.data.model.User
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

interface ApiHelper {
    suspend fun getUsers(): Response<List<User>>

    suspend fun apiForRawBody(request:HashMap<String, Any>,url: String): Response<JsonObject>
    suspend fun apiForFormData(data: HashMap<String, Any>,url: String): Response<JsonObject>
    suspend fun apiGetOutWithQuery(data : HashMap<String,Any>?,  url:String): Response<JsonObject>
//    suspend fun getDropDown(): Response<JsonObject>
    suspend fun apiGetWithQuery(data: HashMap<String, Any>,url: String): Response<JsonObject>
//    suspend fun getCity(id: String): Response<JsonObject>
    suspend fun apiForPostMultipart(url: String,map: HashMap<String, RequestBody>,
                                 part: MutableList<MultipartBody.Part>): Response<JsonObject>

    suspend fun apiGetWithAuth(url: String) : Response<JsonObject>

    suspend fun apiGetWithoutAuth(url: String) : Response<JsonObject>

    suspend fun apiPutWithImage(image : MultipartBody.Part?,data: HashMap<String, RequestBody>, url: String): Response<JsonObject>

    suspend fun apiPostWithMultipart(image : MultipartBody.Part?,data: HashMap<String, RequestBody>, url: String): Response<JsonObject>

    suspend fun apiPutWithMultipart(image: MultipartBody.Part?, data: HashMap<String, RequestBody>, url: String): Response<JsonObject>
    suspend fun apiForPutRawBodyWithAuth(data: HashMap<String, Any>, url: String): Response<JsonObject>

    suspend fun apiGetWithQueryAuth(data: HashMap<String, Any>, url: String) : Response<JsonObject>

    suspend fun deleteAccount(url: String) : Response<JsonObject>

    suspend fun apiForPostAuth(url: String) : Response<JsonObject>

    suspend fun apiForPutAuth(url: String) : Response<JsonObject>

    suspend fun apiForRawBodyAuth(data: HashMap<String, Any>, url: String) : Response<JsonObject>

    suspend fun apiForPutAuthQuery(data: HashMap<String, Any>, url: String) : Response<JsonObject>

    suspend fun apiForPutWithoutAuth(data: HashMap<String, Any>, url: String) : Response<JsonObject>

    suspend fun apiGetWithQueryAuthData(data: HashMap<String, Any>, url: String) : Response<JsonObject>
}