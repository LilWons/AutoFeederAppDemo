package com.example.autofeederapp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

const val channelID = "notification_channel"
const val channelName = "com.example.autofeederapp"

class MyFirebaseMessagingService : FirebaseMessagingService() {
    //genraete the notification
    // attach the notifciation created with the curstom layout
    // show the notifiaiton

    override fun onMessageReceived(remoteMessage: RemoteMessage) {


        if (remoteMessage.notification != null) {
            generateNotification(
                remoteMessage.notification!!.title!!,
                remoteMessage.notification!!.body!!
            )
        }
        Log.d(TAG, "From: ${remoteMessage.from}")

        if(remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

        }
    }
    @SuppressLint("RemoteViewLayout")
    fun getRemoteView(tvTitle: String, tvMessage: String): RemoteViews{
        val remoteView = RemoteViews("com.example.autofeederapp",R.layout.notification)

        remoteView.setTextViewText(R.id.tvTitle,tvTitle)
        remoteView.setTextViewText(R.id.tvMessage,tvMessage)
        remoteView.setImageViewResource(R.id.ivLogo,R.drawable.aflogo)

        return remoteView
    }

    private fun generateNotification(tvTitle: String, tvMessage: String){
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this,0,intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        var builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext,channelID)
            .setSmallIcon(R.drawable.aflogo)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000,1000,1000,1000))
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)

        builder = builder.setContent(getRemoteView(tvTitle,tvMessage))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(notificationChannel)

        notificationManager.notify(0,builder.build())

    }
}