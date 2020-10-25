package com.github.meaninglesscode.todolistapp.di

import android.content.Context
import com.github.meaninglesscode.todolistapp.ToDoApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

/**
 * [ApplicationComponent] is a dagger-injected [Singleton] that serves as the hub for other Dagger
 * [Component]s. This interface holds modules for [ApplicationModule],
 * [AndroidSupportInjectionModule], [ToDoListModule], [ToDoItemViewModule], and
 * [ToDoItemModifyModule].
 */
@Singleton
@Component(
    modules = [
        ApplicationModule::class,
        AndroidSupportInjectionModule::class,
        ToDoListModule::class,
        ToDoItemViewModule::class,
        ToDoItemModifyModule::class
    ])
interface ApplicationComponent : AndroidInjector<ToDoApplication> {
    /**
     * [Factory] is an interface for [Component.Factory] to allow for the creation of an
     * [ApplicationComponent] via the [BindsInstance] passed [Context].
     */
    @Component.Factory
    interface Factory {
        /**
         * [Factory] creation method to take the given [Context] nad produce an
         * [ApplicationComponent].
         *
         * @param [applicationContext] [BindsInstance] [Context] for creation of an
         * [ApplicationComponent]
         */
        fun create(@BindsInstance applicationContext: Context): ApplicationComponent
    }
}