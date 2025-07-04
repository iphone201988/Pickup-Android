package com.pickup.sports.data.api

import com.pickup.sports.data.model.User
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*


interface ApiService {

//    @Header("Authorization") token: String,



    @POST
    suspend fun apiForRawBody(@Body data: HashMap<String, Any>, @Url url: String , @Header("timezone") timeZone : String): Response<JsonObject>

    @POST
    suspend fun apiForRawBodyAuth(@Body data: HashMap<String, Any>, @Url url: String , @Header("Authorization") token: String): Response<JsonObject>

    @POST
    suspend fun apiForPostAuth(@Url url : String , @Header("Authorization") token: String ) : Response<JsonObject>
    @FormUrlEncoded
    @POST
    suspend fun apiForFormData(
        @FieldMap data: HashMap<String, Any>,
        @Url url: String
    ): Response<JsonObject>


    @GET
    suspend fun apiGetOutWithQuery(@Url url: String,@QueryMap data : HashMap<String, Any>?, @Header("timezone") timeZone : String): Response<JsonObject>

    @GET
    suspend fun apiGetWithAuth(@Url url : String,  @Header("Authorization") token: String) : Response<JsonObject>

    @GET
    suspend fun apiGetWithoutAuth(@Url url : String) : Response<JsonObject>

    @GET
    suspend fun apiGetWithQuery(@Url url: String, @QueryMap data : HashMap<String, Any>): Response<JsonObject>

    @GET
    suspend fun apiGetWithQueryAuth(@Url url: String, @QueryMap data : HashMap<String, Any>,  @Header("Authorization") token: String,@Header("timezone") timeZone : String): Response<JsonObject>

    @GET
    suspend fun apiGetWithQueryAuthData(@Url url: String, @QueryMap data : HashMap<String, Any> ,  @Header("Authorization") token: String): Response<JsonObject>


    @DELETE
    suspend fun deleteAccount(@Url url: String, @Header("Authorization") token: String) : Response<JsonObject>
    @PUT
    suspend fun apiForPutRawBodyWithAuth(@Body data: HashMap<String, Any>, @Url url: String,   @Header("Authorization") token: String): Response<JsonObject>
    @PUT
    suspend fun apiForPutAuth( @Url url: String,   @Header("Authorization") token: String): Response<JsonObject>

    @PUT
    suspend fun apiForPutAuthQuery( @Body data: HashMap<String, Any> , @Url url: String,   @Header("Authorization") token: String): Response<JsonObject>

    @PUT
    suspend fun apiForPutWithoutAuth( @Body data: HashMap<String, Any> , @Url url: String): Response<JsonObject>

    @Multipart
    @PUT
    suspend fun apiPutWithImage(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part?,
        @PartMap data: HashMap<String, RequestBody>,
        @Url url: String
    ): Response<JsonObject>

    @Multipart
    @POST
    suspend fun apiPostWithMultipart(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part?,
        @PartMap data: HashMap<String, RequestBody>,
        @Url url: String
    ): Response<JsonObject>


    @Multipart
    @PUT
    suspend fun apiPutWithMultipart(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part?,
        @PartMap data: HashMap<String, RequestBody>,
        @Url url: String
    ): Response<JsonObject>

    @Multipart
    @JvmSuppressWildcards
    @POST
    suspend fun apiForPostMultipart(
        @Url url: String,
        @Header("Authorization") token: String,
        @PartMap data: Map<String, RequestBody>,
        @Part parts:MutableList<MultipartBody.Part>
    ): Response<JsonObject>

    @GET("users")
    suspend fun getUsers(): Response<List<User>>




}