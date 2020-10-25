package com.github.meaninglesscode.todolistapp.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.meaninglesscode.todolistapp.data.ToDoItem

/**
 * [RoomDatabase] containing the to_do_items table. The database entities are set to be represented
 * by the [ToDoItem] class. Implemented by the abstract class [ToDoDatabase] implementing
 * [RoomDatabase].
 */
@Database(entities = [ToDoItem::class], version = 1, exportSchema = false)
abstract class ToDoDatabase : RoomDatabase() {
    /**
     * Allows for the retrieval and use of the [ToDoItemsDao] from the [ToDoDatabase].
     */
    abstract fun toDoItemDao(): ToDoItemsDao
}