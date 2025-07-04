package com.pickup.sports.data.model

import com.squareup.moshi.Json


data class User(
    @Json(name = "id")
    val id: Int = 0,
    @Json(name = "name")
    val name: String = "",
    @Json(name = "email")
    val email: String = "",
    @Json(name = "avatar")
    val avatar: String = ""
)


data class UserSignUp(
    val data: Data,
    val message: String,
    val status: Int
)
data class Data(
    val account_verified: String,
    val age: String,
    val city_id: String,
    val city_name: String,
    val country_code: String,
    val country_id: String,
    val country_name: String,
    val created_at: String,
    val description: String?,
    val device_token: String,
    val device_type: String,
    val distance: String?,
    val document_upload: String,
    val email: String,
    val email_verified: String,
    val email_verified_at: String?,
    val gender: String,
    val id: String,
    val is_active: String,
    val lat: Double?,
    val login_type: String,
    val long: Double?,
    val name: String,
    val night_price: String,
    val night_price_type: String,
    val notification_status: String,
    val otp: String,
    val otp_expiry_time: Any?,
    val phone_no: String,
    val price: String,
    val price_type: String,
    val profile_pic: String?,
    val service_time: String,
    val signup_process: String,
    val social_id: String?,
    val state_id: String,
    val state_name: String,
    val stripe_customer_id: String,
    val token: String,
    val updated_at: String,
    val user_role: String,
    val work_experience: String,
    val zipcode: String,
    val chat_token: String,
    val connection_id: String,
    val user_status: String,
)