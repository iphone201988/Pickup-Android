package com.pickup.sports.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pickup.sports.R
import com.pickup.sports.data.model.PushNotificationModel
import com.pickup.sports.ui.home_modules.HomeDashBoardActivity
import com.pickup.sports.ui.home_modules.messages_module.game_group_messages.GroupMessageFragment
import com.pickup.sports.ui.home_modules.messages_module.player_messages.PlayerMessageFragment
import com.pickup.sports.utils.event.SingleRequestEvent

class NotificationClass : FirebaseMessagingService() {


    var title = ""
    var body = ""

    override fun onNewToken(token: String) {
        Log.d("token", "Refreshed token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)


        if (remoteMessage.notification != null) {
           // Log.i("eriotpuerioutoirto", "onMessageReceived: ${remoteMessage.notification?.body}")

            Log.d("eriotpuerioutoirto", "notification: ${remoteMessage.notification}")
            //   remoteMessage.notification!!.body?.let { sendNotification(it) }
        }
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("eriotpuerioutoirto", "onMessageReceived: ${remoteMessage.data}")


            val pushNotificationModel = convertToPushNotificationModel(remoteMessage.data)

            sendNotification(pushNotificationModel)
        } else {
            Log.d("MyFirebase", "none_data")
        }
    }
    private fun sendNotification(message: PushNotificationModel) {
        val bundle = Bundle()
        bundle.putParcelable("notificationData", message)
        Log.i("Dfd", "sendNotification: $message")
        GroupMessageFragment.refresh.postValue(Resource.success("refresh",true))
        PlayerMessageFragment.refresh.postValue(Resource.success("refresh",true))

        val intent = Intent(this, HomeDashBoardActivity::class.java)
        intent.putExtras(bundle)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val channelId = "my_channel_id"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.pickup_app_icon)
            .setContentTitle(message.body)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)


        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "app", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }


    private fun convertToPushNotificationModel(data: Map<String, String>): PushNotificationModel {
        return PushNotificationModel(
            ChatId = data["ChatId"]?.trim(),
            Type = data["Type"]?.trim(),
            body = data["body"]?.trim(),
            chatType = data["chatType"]?.trim(), // Kept as String? to match your model
            gameId = data["gameId"]?.trim(),
            name = data["name"]?.trim(),
            title = data["title"]?.trim(),
            to = data["to"]?.trim()
        )
    }

}