package com.pickup.sports.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**************************  login & signup  api response*********************/

//data class LoginApiResponse(
//    var email: String?,
//    var firstName: String?,
//    var message: String?,
//    var success: Boolean?,
//    var token: String?,
//    var userId: String?
//)

data class LoginApiResponse(
    var email: String?,
    var firstName: String?,
    var lastName: String?,
    var message: String?,
    var success: Boolean?,
    var token: String?,
    var userId: String?
)

/**************************  get all game api response *********************/


data class GetAllGames(
    var date: String?,
    var isVisible: Boolean,
    var gameList: ArrayList<GetAllGameApiResponse.Game>,
    val dateValue: String = ""

)
data class GetAllGameApiResponse(
    var games: List<Game?>?,
    var success: Boolean?,
    var limit: Int?,
    var page: Int?,
    var totalPages: Int?
) {
    data class Game(
        var __v: Int?,
        var _id: String?,
        var address: String?,
        var allDatesRepeated: Boolean?,
        var chatId: String?,
        var createdAt: String?,
        var description: String?,
        var duration: Int?,
        var hostData: HostData?,
        var hostTimestamp: String?,
        var latitude: Double?,
        var location: Location?,
        var longitude: Double?,
        var maximumSlots: Int?,
        var minimumSlots: Int?,
        var parentId: String?,
        var photos: List<String?>?,
        var price: Double?,
        var repeatCustomDate: List<Any?>?,
        var repeatType: Int?,
        var repeatWeekDay: List<Int?>?,
        var slotIds: List<Any?>?,
        var sportsData: SportsData?,
        var state: String?,
        var status: Int?,
        var title: String?,
        var updatedAt: String?
    ) {
        data class HostData(
            var _id: String?,
            var firstName: String?,
            var lastName: String?,
            var rating: Double?,
            var ratingsCount: Int?
        )

        data class Location(
            var coordinates: List<Double?>?,
            var type: String?
        )

        data class SportsData(
            var _id: String?,
            var name: String?
        )
    }
}



/**************************  get single game details  apiresponse *********************/
//@Parcelize
//data class GetGameByIdApiResponse(
//    var game: Game?,
//    var success: Boolean?
//) : Parcelable {
//
//    @Parcelize
//    data class Game(
//        var _id: String?,
//        var address: String?,
//        var allDatesRepeated: Boolean?,
//        var description: String?,
//        var duration: Int?,
//        var hostData: HostData?,
//        var hostTimestamp: String?,
//        var isGroupBlock: Boolean?,
//        var latitude: Double?,
//        var longitude: Double?,
//        var maximumSlots: Int?,
//        var minimumSlots: Int?,
//        var parentId: String?,
//        var photos: List<String?>?,
//        var price: Int?,
//        var repeatCustomDate: List<String?>?,
//        var repeatType: Int?,
//        var repeatWeekDay: List<String?>?,
//        var slotIds: List<String?>?,
//        var sportsData: SportsData?,
//        var state: String?,
//        var status: Int?,
//        var title: String?
//    ) : Parcelable{
//        @Parcelize
//        data class HostData(
//            var _id: String?,
//            var firstName: String?,
//            var isHostBlock: Boolean?,
//            var lastName: String?,
//            var rating: Int?,
//            var ratingsCount: Int?
//        ): Parcelable
//
//        @Parcelize
//        data class SportsData(
//            var _id: String?,
//            var name: String?
//        ): Parcelable
//    }
//}

@Parcelize
data class GetGameByIdApiResponse(
    var game: Game?,
    var success: Boolean?
) : Parcelable {
    @Parcelize
    data class Game(
        var _id: String?,
        var address: String?,
        var allDatesRepeated: Boolean?,
        var chatId: String?,
        var hostChatWithLoggedInUser: String?,
        var description: String?,
        var duration: Int?,
        var hostData: HostData?,
        var hostTimestamp: String?,
        var isGroupBlock: Boolean?,
        var latitude: Double?,
        var longitude: Double?,
        var maximumSlots: Int?,
        var minimumSlots: Int?,
        var photos: List<String?>?,
        var price: Double?,
        var type : Int?,
        var repeatCustomDate: List<String?>?,
        var repeatType: Int?,
        var repeatWeekDay: List<String>?,
        var slotIds: List<SlotId?>?,
        var sportsData: SportsData?,
        var state: String?,
        var status: Int?,
        var title: String?
    ) : Parcelable {
        @Parcelize
        data class HostData(
            var _id: String?,
            var firstName: String?,
            var isHostBlock: Boolean?,
            var lastName: String?,
            var rating: Double?,
            var ratingsCount: Int?
        ): Parcelable

        @Parcelize
        data class SlotId(
            var _id: String?,
            var firstName: String?,
            var lastName: String?
        ): Parcelable

        @Parcelize
        data class SportsData(
            var _id: String?,
            var name: String?
        ): Parcelable
    }
}

/**************************  get all sports api response *********************/
data class GetAllSportsApiResponse(
    var sports: List<Sport?>?,
    var success: Boolean?

) {
    data class Sport(
        var _id: String?,
        var name: String?,
        var isSelected: Boolean = false
    )
}

/**************************  get profile api response *********************/

//data class GetProfileApiResponse(
//    var success: Boolean?,
//    var user: User?
//) {
//    data class User(
//        var credits: Int?,
//        var email: String?,
//        var firstName: String?,
//        var isPaymentMethodAdded: Boolean?,
//        var isStripeAccountConnected: Boolean?,
//        var lastName: String?,
//        var location: Location?,
//        var profileImage: String?,
//        var rating: Double?,
//        var ratingsCount: Int?,
//        var token: String?,
//        var userId: String?
//    ) {
//        data class Location(
//            var coordinates: List<Double?>?,
//            var type: String?
//        )
//    }
//}


data class GetProfileApiResponse(
    var success: Boolean?,
    var user: User?
) {
    data class User(
        var city: String?,
        var countryCode: String?,
        var phone: Long?,
        var credits: Double?,
        var email: String?,
        var firstName: String?,
        var isPaymentMethodAdded: Boolean?,
        var isStripeAccountConnected: Boolean?,
        var lastName: String?,
        var location: Location?,
        var profileImage: String?,
        var rating: Double?,
        var ratingsCount: Int?,
        var referralCode: String?,
        var referredBy: String?,
        var sportId: List<SportId?>?,
        var stripeConnectId: String?,
        var stripeCustomerId: String?,
        var timezone: String?,
        var token: String?,
        var userId: String?
    ) {
        data class Location(
            var coordinates: List<Double?>?,
            var type: String?
        )

        data class SportId(
            var _id: String?,
            var name: String?
        )
    }
}




/**************************  get Host game api response *********************/

data class GetHostGames(
    var date : String?,
    var isVisible : Boolean = false,
    var gameList : List<GetHostGamesApiResponse.Game>
)
data class GetHostGamesApiResponse(
    var games: List<Game?>?,
    var success: Boolean?,
    var limit: Int?,
    var page: Int?,
    var totalPages: Int?

) {
    data class Game(

        var __v: Int?,
        var _id: String?,
        var address: String?,
        var allDatesRepeated: Boolean?,
        var chatId: String?,
        var createdAt: String?,
        var description: String?,
        var duration: Int?,
        var hostData: HostData?,
        var hostTimestamp: String?,
        var isGroupBlock: Boolean?,
        var latitude: Double?,
        var location: Location?,
        var longitude: Double?,
        var maximumSlots: Int?,
        var minimumSlots: Int?,
        var parentId: String?,
        var photos: List<String?>?,
        var price: Double?,
        var repeatCustomDate: List<String?>?,
        var repeatType: Int?,
        var repeatWeekDay: List<Any?>?,
        var slotIds: List<Any?>?,
        var sportsData: SportsData?,
        var state: String?,
        var status: Int?,
        var title: String?,
        var updatedAt: String?
    ) {
        data class HostData(
            var _id: String?,
            var firstName: String?,
            var lastName: String?,
            var rating: Double?,
            var ratingsCount: Int?
        )

        data class Location(
            var coordinates: List<Double?>?,
            var type: String?
        )

        data class SportsData(
            var _id: String?,
            var name: String?
        )
    }
}

/**************************  get my games game api response *********************/

data class GetMyGames(
    var date : String?,
    var isVisible : Boolean = false,
    var gameList : List<GetMyGamesApiResponse.Game>
)

data class GetMyGamesApiResponse(
    var games: List<Game?>?,
    var success: Boolean?,
    var limit: Int?,
    var page: Int?,
    var totalPages: Int?
) {
    data class Game(
        var __v: Int?,
        var _id: String?,
        var address: String?,
        var allDatesRepeated: Boolean?,
        var chatId: String?,
        var createdAt: String?,
        var description: String?,
        var duration: Int?,
        var hostData: HostData?,
        var hostTimestamp: String?,
        var isGroupBlock: Boolean?,
        var isRatingGiven: Boolean?,
        var latitude: Double?,
        var location: Location?,
        var longitude: Double?,
        var maximumSlots: Int?,
        var minimumSlots: Int?,
        var parentId: String?,
        var photos: List<String?>?,
        var price: Double?,
        var repeatCustomDate: List<String?>?,
        var repeatType: Int?,
        var repeatWeekDay: List<Any?>?,
        var slotIds: List<SlotId?>?,
        var sportsData: SportsData?,
        var state: String?,
        var status: Int?,
        var title: String?,
        var updatedAt: String?
    ) {
        data class HostData(
            var _id: String?,
            var firstName: String?,
            var lastName: String?,
            var rating: Double?,
            var ratingsCount: Int?
        )

        data class Location(
            var coordinates: List<Double?>?,
            var type: String?
        )

        data class SlotId(
            var _id: String?,
            var firstName: String?,
            var lastName: String?
        )

        data class SportsData(
            var _id: String?,
            var name: String?
        )
    }
}



/**************************  stripe connect  api response *********************/
data class StripeConnectApiResponse(
    var accountLink: AccountLink?,
    var success: Boolean?
) {
    data class AccountLink(
        var created: Int?,
        var expires_at: Int?,
        var `object`: String?,
        var url: String?
    )
}

/**************************  get Balance  api response *********************/
data class GetBalanceApiResponse(
    var host: Double?,
    var message: String?,
    var nextPayoutDate: String?,
    var payout: Double?,
    var referrals: Double?,
    var success: Boolean?,
    var total: Double?
)

/**************************  get payment method  api response *********************/
//data class GetPaymentMethodApiResponse(
//    var credits: Int?,
//    var paymentMethods: PaymentMethods?,
//    var success: Boolean?
//) {
//    data class PaymentMethods(
//        var externalAccounts: List<ExternalAccount?>?,
//        var paymentMethods: List<PaymentMethod?>?
//    ) {
//        data class ExternalAccount(
//            var bank_name: String?,
//            var id: String?,
//            var last4: String?
//        )
//
//        data class PaymentMethod(
//            var brand: String?,
//            var id: String?,
//            var last4: String?
//        )
//    }
//}


data class GetPaymentMethodApiResponse(
    var credits: Double?,
    var paymentMethods: PaymentMethods?,
    var success: Boolean?
) {
    data class PaymentMethods(
        var externalAccounts: List<ExternalAccount?>?,
        var paymentMethods: List<PaymentMethod?>?
    ) {
        data class ExternalAccount(
            var bank_name: String?,
            var id: String?,
            var last4: String?
        )

        data class PaymentMethod(
            var brand: String?,
            var id: String?,
            var last4: String?
        )
    }
}
/**************************  add  customer card  api response *********************/
data class AddCustomerCardResponse(
    var customerId: String?,
    var message: String?,
    var success: Boolean?
)


/**************************  get referral code  api response *********************/
data class GetReferralCodeApiResponse(
    var referralBalance: Double?,
    var referralCode: String?,
    var referralsCount: Int?,
    var success: Boolean?
)

/**************************  get  chat message  api response *********************/
data class GetChatMessageApiResponse(
    var messages: List<Message>?,
    var success: Boolean?,
    var total: Int?,
    var totalPages: Int?
) {
    data class Message(
        var __v: Int?,
        var _id: String?,
        var chatId: String?,
        var createdAt: String?,
        var gameId: String?,
        var isSeen: Boolean?,
        var message: String?,
        var updatedAt: String?,
        var userId: UserId?
    ) {
        data class UserId(
            var _id: String?,
            var firstName: String?,
            var lastName: String?
        )
    }
}



data class ChatItem(
    val type: Int,
    val message: GetChatMessageApiResponse.Message? = null,
    val dateHeader: String? = null
)

/**************************  received message socket  api response *********************/
data class MessageData(
    var message: MessageResponse?
) {
    data class MessageResponse(
        var __v: Int?,
        var _id: String?,
        var chatId: String?,
        var createdAt: String?,
        var gameId: String?,
        var isSeen: Boolean?,
        var message: String?,
        var updatedAt: String?,
        var userId: UserId?
    ) {
        data class UserId(
            var _id: String?,
            var firstName: String?,
            var lastName: String?
        )
    }
}

/**************************  Block unblock   api response *********************/
data class BlockChatApiResponse(
    var isBlocked: Boolean?,
    var message: String?
)


/**************************  push notification   api response *********************/


//@Parcelize
//data class PushNotificationModel(
//    val aps: Aps?,
//    val notificationTypes: Int?,
//    val chatType: Int?,
//    val gameId: String?,
//    val chatId: String?,
//    val to: String?,
//    val name: String?,
//    val slotIds: List<String>?
//): Parcelable
//
//@Parcelize
//
//data class Aps(
//    val alert: Alert?,
//    val badge: Int?,
//    val sound: String?
//) : Parcelable
//
//@Parcelize
//data class Alert(
//    val title: String?,
//    val body: String?
//) : Parcelable


@Parcelize
data class PushNotificationModel(
    var ChatId: String?,
    var Type: String?,
    var body: String?,
    var chatType: String?,
    var gameId: String?,
    var name: String?,
    var title: String?,
    var to: String?
) : Parcelable


/**************************   get all player api response  api response *********************/

//data class GetAllPlayersApiResponse(
//    var games: List<Game?>?,
//    var success: Boolean?
//) {
//    data class Game(
//        var chatId: String?,
//        var joinedGames: Int?,
//        var totalGames: Int?,
//        var user: User?
//    ) {
//        data class User(
//            var _id: String?,
//            var city: String?,
//            var firstName: String?,
//            var lastName: String?,
//            var profileImage: String?
//        )
//    }
//}


data class GetAllPlayersApiResponse(
    var pagination: Pagination?,
    var success: Boolean?,
    var totalUsers: Int?,
    var users: List<User?>?
) {
    data class Pagination(
        var limit: Int?,
        var page: Int?,
        var totalPages: Int?
    )

    data class User(
        var joinedGames: Int?,
        var _id: String?,
        var city: String?,
        var createdAt: String?,
        var firstName: String?,
        var lastName: String?,
        var profileImage: String?

    )
}



/**************************   get all chats   api response *********************/

//data class GetAllChatApiResponse(
//    var chats: List<Chat?>?,
//    var pagination: Pagination?,
//    var success: Boolean?
//) {
//    data class Chat(
//        var _id: String?,
//        var lastMessage: LastMessage?,
//        var participants: List<Participant?>?,
//        var unSeenMessagesCount: Int?
//    ) {
//        data class LastMessage(
//            var _id: String?,
//            var isSeen: Boolean?,
//            var message: String?,
//            var updatedAt: String?
//        )
//
//        data class Participant(
//            var _id: String?,
//            var city: String?,
//            var firstName: String?,
//            var lastName: String?,
//            var profileImage: String?
//        )
//    }
//
//    data class Pagination(
//        var limit: Int?,
//        var page: Int?,
//        var totalPages: Int?
//    )
//}


data class GetAllChatApiResponse(
    var chats: List<Chat?>?,
    var pagination: Pagination?,
    var success: Boolean?
) {
    data class Chat(
        var _id: String?,
        var lastMessage: LastMessage?,
        var participants: List<Participant?>?,
        var unSeenMessagesCount: Int?
    ) {
        data class LastMessage(
            var _id: String?,
            var createdAt: String?,
            var message: String?,
            var seenBy: List<String?>?,
            var updatedAt: String?
        )

        data class Participant(
            var _id: String?,
            var city: String?,
            var firstName: String?,
            var lastName: String?,
            var profileImage: String?
        )
    }

    data class Pagination(
        var limit: Int?,
        var page: Int?,
        var totalPages: Int?
    )
}

/**************************   get group chat  api response *********************/
data class GetGroupChatApiResponse(
    var games: List<Game?>?,
    var limit: Int?,
    var page: String?,
    var success: Boolean?,
    var totalPages: Int?
) {
    data class Game(
        var __v: Int?,
        var _id: String?,
        var address: String?,
        var allDatesRepeated: Boolean?,
        var chatId: String?,
        var createdAt: String?,
        var description: String?,
        var duration: Int?,
        var hostData: HostData?,
        var hostTimestamp: String?,
        var isGroupBlock: Boolean?,
        var isRatingGiven: Boolean?,
        var latitude: Double?,
        var location: Location?,
        var longitude: Double?,
        var maximumSlots: Int?,
        var minimumSlots: Int?,
        var photos: List<String?>?,
        var price: Double?,
        var repeatCustomDate: List<Any?>?,
        var repeatType: Int?,
        var repeatWeekDay: List<Int?>?,
        var slotIds: List<SlotId?>?,
        var sportsData: SportsData?,
        var state: String?,
        var status: Int?,
        var title: String?,
        var type: Int?,
        var unSeenMessagesCount: Int?,
        var updatedAt: String?
    ) {
        data class HostData(
            var _id: String?,
            var firstName: String?,
            var lastName: String?,
            var rating: Int?,
            var ratingsCount: Int?
        )

        data class Location(
            var coordinates: List<Double?>?,
            var type: String?
        )

        data class SlotId(
            var _id: String?,
            var firstName: String?,
            var lastName: String?
        )

        data class SportsData(
            var _id: String?,
            var name: String?
        )
    }
}


