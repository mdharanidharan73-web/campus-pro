package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import android.app.PendingIntent

class AssignmentReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val assignmentTitle = intent.getStringExtra("title") ?: "Assignment"
        val subjectName = intent.getStringExtra("subject") ?: "Subject"
        val dueDate = intent.getStringExtra("dueDate") ?: "Soon"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "assignment_reminders",
                "Assignment Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for upcoming assignments"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap action opens the app
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "assignment_reminders")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Assignment Due Soon!")
            .setContentText("$subjectName: $assignmentTitle is due on $dueDate.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(assignmentTitle.hashCode(), notification)
    }
}
