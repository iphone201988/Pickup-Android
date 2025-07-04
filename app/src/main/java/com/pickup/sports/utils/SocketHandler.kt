package com.pickup.sports.utils

import android.util.Log
import com.pickup.sports.data.api.Constants.SOCKET_URL

import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.engineio.client.Transport
import java.net.URISyntaxException

object SocketHandler {
    private var mSocket: Socket? = null

    @Synchronized
    fun setSocket(token: String) {
        try {
            if (token.isBlank()) {
                Log.e("SocketHandler", "Token is empty! Cannot initialize socket.")
                return
            }

            val options = IO.Options().apply {
                extraHeaders = mapOf("token" to listOf(token.trim()))  // Ensure correct header format
                reconnection = true  // Enables automatic reconnection
                reconnectionAttempts = 5  // Retry 5 times before failing
                reconnectionDelay = 2000  // Wait 2 seconds before retrying
            }

            val socket = IO.socket(SOCKET_URL, options)  // Use a local variable
            mSocket = socket  // Assign to the global property

            Log.i("SocketHandler", "Socket initialized with token: $token")

            // Ensure headers are attached dynamically during handshake
            socket.io()?.on(Manager.EVENT_TRANSPORT) { args ->
                try {
                    val transport = args[0] as? Transport ?: return@on
                    transport.on(Transport.EVENT_REQUEST_HEADERS) { headerArgs ->
                        try {
                            val headers = headerArgs[0] as? MutableMap<String, List<String>> ?: return@on
                            headers["token"] = listOf(token.trim())  // Attach token dynamically
                            Log.i("SocketHandler", "Added token dynamically: $headers")
                        } catch (e: Exception) {
                            Log.e("SocketHandler", "Error modifying request headers: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SocketHandler", "Error in transport setup: ${e.message}")
                }
            }

        } catch (e: URISyntaxException) {
            Log.e("SocketHandler", "Socket initialization error: ${e.message}")
        } catch (e: Exception) {
            Log.e("SocketHandler", "Unexpected error: ${e.message}")
        }
    }




//    @Synchronized
//    fun getSocket(): Socket? {
//        return mSocket?.apply {
//            if (!connected()) {
//                connect()
//                Log.d("SocketHandler", "Socket was disconnected. Attempting to reconnect...")
//            } else {
//                Log.d("SocketHandler", "Socket is already connected.")
//            }
//        }
//    }

    @Synchronized
    fun getSocket(): Socket? {
        if (mSocket?.connected() == true) {
            Log.d("mSocket", "getSocket: Already Connected")
        } else if (mSocket?.connected() == false) {
            Log.d("mSocket", "getSocket: Socket is disconnected, attempting to reconnect.")
            mSocket?.connect()
        } else {
            Log.d("mSocket", "getSocket: Socket is neither connected nor disconnected, attempting to connect.")
            mSocket?.connect()
        }

        return mSocket
    }


    @Synchronized
    fun socketIsConnected(): Boolean {
        val isConnected = mSocket?.connected() == true
        Log.d("SocketHandler", "Socket connection status: $isConnected")
        mSocket?.emit("Connected", if (isConnected) "Connected" else "Disconnected")
        return isConnected
    }

//    @Synchronized
//    fun establishConnection() {
//        if (mSocket == null) {
//            Log.e("SocketHandler", "Socket is not initialized. Call setSocket() first.")
//            return
//        }
//        if (!mSocket!!.connected()) {
//            mSocket!!.connect()
//            Log.d("SocketHandler", "Attempting to establish socket connection...")
//        } else {
//            Log.d("SocketHandler", "Socket is already connected.")
//        }
//    }

    @Synchronized
    fun establishConnection() {
        if (mSocket == null) {
            Log.e("SocketHandler", "Socket is not initialized. Call setSocket() first.")
            return
        }

        if (!mSocket!!.connected()) {
            mSocket!!.connect()
            Log.d("SocketHandler", "Attempting to establish socket connection...")

            // Listen for successful connection
            mSocket!!.on(Socket.EVENT_CONNECT) {
                Log.i("SocketHandler", "Socket connected successfully.")
            }

            // Listen for disconnection
            mSocket!!.on(Socket.EVENT_DISCONNECT) {
                Log.e("SocketHandler", "Socket disconnected.")
            }

            // Listen for connection errors
            mSocket!!.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e("SocketHandler", "Socket connection error: ${args.joinToString()}")
            }

        } else {
            Log.d("SocketHandler", "Socket is already connected.")
        }
    }


    @Synchronized
    fun closeConnection() {
        if (mSocket != null && mSocket!!.connected()) {
            mSocket!!.disconnect()
            Log.d("SocketHandler", "Socket disconnected successfully.")
        } else {
            Log.d("SocketHandler", "Socket is already disconnected or not initialized.")
        }
    }
}