package com.pickup.sports.data.model

import com.google.android.gms.maps.model.LatLng

data class Experience(
    val companyName: String, val startDate: String, val endDate: String
)

data class Education(
    val id: String, val educationName: String

)

data class JobTitle(
    val id: String, val title: String

)
data class FilterStatus(
    val id: String, val title: String

)

data class ReviewData(
    val name : String, val image : String, val location : String , val rating : String , val description : String
)
data class NotificationData(
    val image : String, val notification : String, val date : String
)
data class FavoriteModel(
    var image : String , var name : String, var workName : String, var location : String, var isSelected: Boolean = false
    )
data class SettingModel(
    val image : String, var name : String
)

data class SelectSports(
    val title: String,
    var isSelected: Boolean = false
)

data class AccountModuleList(
    val title : String,
    val icon : Int
)

data class Images(
    val image : Int
)
data class Players(
    val title : String
)

data class WeekModel(
    val title : String,
    val count : Int,
    var isSelected: Boolean = false
)

data class MessageModel(
    var text : String ,
    var image : Int,
    var type : Int,
    var time : String
)

data class  HostedGame(
    val image : Int,
    val gameName : String,
    val date : String,
    val time : String,
    val price : String,
    val player : String,
    val name : String,
    val rating : String,
    val totalRating : String
)

data class MyGames(
    val title: String,
    val dummy: List<HostedGame?>?
)

data class GetGames(
    val date : String,

    val gameList : List<GameList?>?,
    var isVisible : Boolean = false,
)

data class GameList(
    val image : Int,
    val gameName : String,
    val location : String,
    val rating : String,
    val personName : String,
    val miles : String,
    val price : String,
    val player : String,
    val time : String,
    val gameType : String
)
data class PlaceDetails(val name: String, val address: String, val location: LatLng)

data class CardDatas(
    val cardNumber: String, val expiryMonth: Int, val expiryYear: Int, val cvv: String,val zipCode : String
)

data class PaginationModel(
    val sportId : String,
    val radius : String,
    val userId : String,
    val latitude : Double,
    val longitude : Double,
    val page : String
)


data class PlayerMessage(
    val image : Int, val name :String, val time : String, val location : String
)

data class GroupMessage(val image : Int, val sportsName : String, val location : String , val rating : String, val price : String, val title : String, val name: String, val ratingCount : String
)