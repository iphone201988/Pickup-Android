
package com.pickup.sports.data.network
class NetworkError(val errorCode: Int, override val message: String?) : Throwable(message)
