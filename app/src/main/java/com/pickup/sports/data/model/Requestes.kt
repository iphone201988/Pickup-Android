package com.pickup.sports.data.model

data class LoginRequestApi(
    val email: String, val password: String, val device_type: String, val device_token: String
)