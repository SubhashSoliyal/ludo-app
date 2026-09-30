package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

object NotificationHelper {

    private const val CHANNEL_TURNS = "ludo_turn_alerts"
    private const val CHANNEL_EVENTS = "ludo_tournaments"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val turnChannel = NotificationChannel(
                CHANNEL_TURNS,
                "Match Turn Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when it's your turn in multiplayer Ludo matches"
                enableVibration(true)
            }

            val eventChannel = NotificationChannel(
                CHANNEL_EVENTS,
                "Tournaments & Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily tournament brackets and bonus chest reminders"
            }

            notificationManager.createNotificationChannels(listOf(turnChannel, eventChannel))
        }
    }

    fun showTurnAlertNotification(context: Context, playerName: String, roomCode: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_TURNS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🎲 It's Your Turn! - Room $roomCode")
            .setContentText("Roll the dice before the 15-second timer expires!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(101, notification)
    }

    fun showTournamentNotification(context: Context, tournamentTitle: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_EVENTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🏆 $tournamentTitle is Live!")
            .setContentText("Quarterfinal match is ready. Win up to 50,000 Coins!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(102, notification)
    }
}
