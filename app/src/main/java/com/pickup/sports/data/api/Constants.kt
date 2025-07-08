package com.pickup.sports.data.api

import java.util.TimeZone

object Constants {

    const val BASE_URL = "https://18.235.152.238:8000/api/"
    const val SOCKET_URL = "https://18.235.152.238:8000"





   var position = 0
    var timeZone = TimeZone.getDefault().id.replace("Asia/Calcutta", "Asia/Kolkata")






    /**************** API LIST *****************/

    const val HEADER_API = "X-API-Key:lkcMuYllSgc3jsFi1gg896mtbPxIBzYkEL"

  //  const val GET_ALL_GAMES = "game/getAllGames"
    const val GET_ALL_GAMES = "game/getAllGamesByDate"
    const val GET_ALL_SPORTS = "sport/getAllSports"
    const val GET_PROFILE = "user/getUserProfile"
    const val UPDATE_PROFILE = "user/updateProfile"
    const val CHANGE_PASSWORD = "user/changePassword"
    const val DELETE_ACCOUNT = "user/deleteAccount"
    const val LOGIN = "user/loginUser"
    const val SIGNUP = "user/registerUser"
    const val LOGOUT = "user/logoutUser"
    const val HOSTED_GAME = "game/getMyGames"
    const val FORGOT_PASSWORD = "user/forgotPassword"
    const val  VERIFY_OTP = "user/verifyOTP"
    const val SINGLE_GAME_DETAIL = "game/getSingleGameById"
    const val CREATE_GAME = "game/createGame"
    const val STRIPE_CONNECT = "payment/createStripeAccount"
    const val CANCEL_GAME = "game/cancelGame/"
    const val GET_BALANCE = "payment/getBalance"
    const val GET_PAYMENT_METHOD = "payment/getPaymentMethods"
    const val ADD_CARD  = "payment/addCustomerCard"
    const val MAKE_PAYMENT= "payment/makePayment"
    const val LEAVE_GAMES = "game/leaveGame/"
    const val GET_CHAT_MESSAGES = "chat/getChatMessages/"
    const val GET_REFERRAL_CODE = "user/getRefferalCode"
    const val UPDATE_GAME = "game/updateGame/"
    const val DELETE_CARD = "payment/deleteCustomerCard/"
    const val CHAT_BLOCK_UNBLOCK = "chat/block-unblock"
    const val GIVE_RATING  = "game/giveRatings"
    const val RESET_PASSWORD = "user/resetPassword"
    const val GET_ALL_PLAYER = "game/getAllPlayers"
    const val GET_ALL_CHATS = "chat/getAllChats"
    const val GET_GROUP_CHAT = "game/getGroupChats"




}