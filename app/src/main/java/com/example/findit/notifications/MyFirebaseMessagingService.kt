package com.example.findit.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.findit.MainActivity
import com.example.findit.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM_SERVICE"
        private const val CHANNEL_ID = "findit_notifications"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * Override onMessageReceived to handle notification when app is in foreground
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        // Extract notification title and body
        val title = remoteMessage.notification?.title ?: "FindIt"
        val body = remoteMessage.notification?.body ?: "New notification"

        Log.d(TAG, "Title: $title, Body: $body")

        // Show notification even when app is in foreground
        showNotification(title, body)
    }

    /**
     * Override onNewToken to handle FCM token updates
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token: $token")
        // Optional: Store token in SharedPreferences or send to your server
    }

    /**
     * Create notification channel for Android 8+ (required for notifications to show)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FindIt Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications from FindIt app"
                enableLights(true)
                enableVibration(true)
            }

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    /**
     * Show notification with PendingIntent to open MainActivity
     */
    private fun showNotification(title: String, body: String) {
        try {
            // Create intent to open MainActivity when notification is clicked
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            // Create PendingIntent with unique request code
            val pendingIntent = PendingIntent.getActivity(
                this,
                System.currentTimeMillis().toInt(), // Unique request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Generate unique notification ID based on current time
            val notificationId = System.currentTimeMillis().toInt()

            // Build notification
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body)) // Show full text
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true) // Dismiss notification on click
                .setContentIntent(pendingIntent) // Open app on click
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Show heads-up notification
                .build()

            // Show notification
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificationId, notification)

            Log.d(TAG, "Notification shown with ID: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }
}