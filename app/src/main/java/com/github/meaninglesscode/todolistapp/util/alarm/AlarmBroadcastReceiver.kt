package com.github.meaninglesscode.todolistapp.util.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.github.meaninglesscode.todolistapp.R
import com.github.meaninglesscode.todolistapp.todolist.ToDoListActivity

/**
 * A class to receive broadcasts. [AlarmBroadcastReceiver] implements [BroadcastReceiver] and is
 * used to handle alarm broadcasts and show the associated notification.
 */
class AlarmBroadcastReceiver: BroadcastReceiver() {
    /**
     * Method overriding [BroadcastReceiver.onReceive] to allow for the handling of received
     * broadcasts.
     *
     * @param [context] [Context]? automatically passed into the [onReceive] method
     * @param [intent] [Intent]? automatically passed to the [onReceive] method
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        startAlarm(context!!, intent!!)
    }

    /**
     * Method to handle the work of starting the received alarm.
     *
     * @param [context] [Context] passed through the automatically passed [Context] from [onReceive]
     * @param [intent] [Intent] passed through the automatically passed [Intent] from [onReceive]
     */
    private fun startAlarm(context: Context, intent: Intent) {
        val itemId = intent.getStringExtra("itemId")
        val title =  intent.getStringExtra("title")

        val bundle = Bundle()
        bundle.putString("id", itemId)

        val pendingIntent = NavDeepLinkBuilder(context)
            .setComponentName(ToDoListActivity::class.java)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.toDoItemViewFragment)
            .setArguments(bundle)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("This item is due!")
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(itemId, 0, notification)
    }

    /**
     * Companion object to [AlarmBroadcastReceiver] containing channel information.
     */
    companion object {
        const val CHANNEL_ID = "ALARM_SERVICE_CHANNEL"
        const val CHANNEL_NAME = "To Do Alarms"
    }
}