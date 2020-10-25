package com.github.meaninglesscode.todolistapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.github.meaninglesscode.todolistapp.di.DaggerApplicationComponent
import com.github.meaninglesscode.todolistapp.util.alarm.AlarmBroadcastReceiver
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

/**
 * Base application that is the entry point for Dagger dependency injection. [ToDoApplication]
 * implements [DaggerApplication] to this end.
 */
open class ToDoApplication : DaggerApplication() {
    /**
     * Override of the [DaggerApplication] onCreate method in case I need to add something here
     * at a later point.
     */
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * This function creates the [DaggerApplicationComponent] via it's associated factory and
     * passes the current [Context] (applicationContext) into the creation method. The
     * [DaggerApplicationComponent] object is an automatically generated Dagger component.
     */
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.factory().create(applicationContext)
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            AlarmBroadcastReceiver.CHANNEL_ID,
            AlarmBroadcastReceiver.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(serviceChannel)
    }
}