package com.github.meaninglesscode.todolistapp.util.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

/**
 * Object containing helpers for creating alarms.
 */
object AlarmHelper {
    /**
     * Helper method for scheduling an alarm. Handles getting the [AlarmManager], creating a new
     * [Intent], populating the [Intent] with necessary data, and setting the alarm through
     * [AlarmManager].
     *
     * @param [context] [Context] that the alarm is created from
     * @param [itemId] [String] representing the associated to do item's ID
     * @param [triggerAtMillis] [Long] the time at which this alarm is set to go off
     * @param [title] [String] representing the title of the alarm's resultant notification
     */
    fun schedule(context: Context, itemId: String, triggerAtMillis: Long, title: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val newIntent = Intent(context, AlarmBroadcastReceiver::class.java)

        newIntent.putExtra("itemId", itemId)
        newIntent.putExtra("triggerAtMillis", triggerAtMillis)
        newIntent.putExtra("title", "To do: $title")

        val pendingIntent = PendingIntent.getBroadcast(context, itemId.hashCode(), newIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    /**
     * Helper method for cancelling a scheduled alarm. Handles getting the [AlarmManager],
     * recreating the original [Intent], and calling [AlarmManager.cancel] with the recreated
     * [Intent] to fully cancel its activation.
     *
     * @param [context] [Context] that the [Intent] is recreated from to cancel the alarm
     * @param [itemId] [String] representing the associated to do item's ID
     */
    fun cancel(context: Context, itemId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val newIntent = Intent(context, AlarmBroadcastReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(context, itemId.hashCode(), newIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
    }
}