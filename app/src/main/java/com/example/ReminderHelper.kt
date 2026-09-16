package com.example

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

object ReminderHelper {
    fun scheduleAssignmentReminder(
        context: Context,
        assignmentId: String,
        title: String,
        subject: String,
        dueDate: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, AssignmentReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("subject", subject)
            putExtra("dueDate", dueDate)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            assignmentId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // For demonstration, schedule it 5 seconds from now.
        // In a real app, you'd parse dueDate and schedule it a day before.
        val triggerTime = System.currentTimeMillis() + 5000L
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerTime, 60000, pendingIntent)
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
            Log.d("ReminderHelper", "Scheduled reminder for $title")
        } catch (e: SecurityException) {
            // Fallback for Exact Alarm permission denial
            alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerTime, 60000, pendingIntent)
        }
    }
}
