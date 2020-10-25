package com.github.meaninglesscode.todolistapp.data.source

import com.github.meaninglesscode.todolistapp.data.Result
import com.github.meaninglesscode.todolistapp.data.ToDoItem

/**
 * Interface for [ToDoItem] data sources to implement. Contains methods for interacting with the
 * associated table to be implemented in other data sources.
 */
interface ToDoItemsDataSource {

    suspend fun getToDoItems(): Result<List<ToDoItem>>

    suspend fun getToDoItem(id: String): Result<ToDoItem>

    suspend fun saveToDoItem(item: ToDoItem)

    suspend fun completeToDoItem(item: ToDoItem)

    suspend fun completeToDoItem(id: String)

    suspend fun activateToDoItem(item: ToDoItem)

    suspend fun activateToDoItem(id: String)

    suspend fun clearCompletedToDoItems()

    suspend fun deleteAllToDoItems()

    suspend fun deleteToDoItem(id: String)
}