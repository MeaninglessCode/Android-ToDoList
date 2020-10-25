package com.github.meaninglesscode.todolistapp.di

import android.content.Context
import androidx.room.Room
import com.github.meaninglesscode.todolistapp.data.source.DefaultToDoItemsRepository
import com.github.meaninglesscode.todolistapp.data.source.ToDoItemsDataSource
import com.github.meaninglesscode.todolistapp.data.source.ToDoItemsRepository
import com.github.meaninglesscode.todolistapp.data.source.local.ToDoDatabase
import com.github.meaninglesscode.todolistapp.data.source.local.ToDoItemsLocalDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.annotation.AnnotationRetention.RUNTIME

/**
 * Dagger [Module] that includes [ApplicationModuleBinds]. Used for dependency injection.
 */
@Module(includes = [ApplicationModuleBinds::class])
object ApplicationModule {

    /**
     * [Qualifier] for the annotation class [ToDoItemsLocalDataSource] with a [RUNTIME] [Retention]
     * policy.
     */
    @Qualifier
    @Retention(RUNTIME)
    annotation class ToDoItemsLocalDataSource

    /**
     * [JvmStatic] method that [Provides] a [Singleton] injector instance of
     * [ToDoItemsLocalDataSource] instantiated from the given [ToDoDatabase] and
     * [CoroutineDispatcher].
     *
     * @param [database] [ToDoDatabase] Used for instantiation of the [ToDoItemsLocalDataSource]
     * @param [ioDispatcher] [CoroutineDispatcher] Used for instantiation of the
     * [ToDoItemsLocalDataSource]
     * @return [ToDoItemsDataSource] resultant [Singleton] data source
     */
    @JvmStatic
    @Singleton
    @ToDoItemsLocalDataSource
    @Provides
    fun provideToDoItemsLocalDataSource(
        database: ToDoDatabase,
        ioDispatcher: CoroutineDispatcher
    ): ToDoItemsDataSource {
        return ToDoItemsLocalDataSource(
            database.toDoItemDao(), ioDispatcher
        )
    }

    /**
     * [JvmStatic] method that [Provides] a [Singleton] instance of [ToDoDatabase] via
     * [Room.databaseBuilder] instantiated via the given [Context].
     *
     * @param [context] [Context] used for instantiation of the [ToDoDatabase]
     * @return [ToDoDatabase] resultant [Singleton] database
     */
    @JvmStatic
    @Singleton
    @Provides
    fun provideDataBase(context: Context): ToDoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ToDoDatabase::class.java,
            "to_do_items.db"
        ).build()
    }

    /**
     * [JvmStatic] method that [Provides] a [Singleton] instance of [Dispatchers.IO] for injection
     * throughout the application.
     *
     * @return [CoroutineDispatcher] [Singleton] IO dispatcher returned
     */
    @JvmStatic
    @Singleton
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO
}

/**
 * Submodule for repository binding included by the [ApplicationModule].
 */
@Module
abstract class ApplicationModuleBinds {
    /**
     * Method that [Binds] a [Singleton] instance of [DefaultToDoItemsRepository]
     *
     * @return [ToDoItemsRepository] returned injected parameter
     */
    @Singleton
    @Binds
    abstract fun bindRepository(repo: DefaultToDoItemsRepository): ToDoItemsRepository
}