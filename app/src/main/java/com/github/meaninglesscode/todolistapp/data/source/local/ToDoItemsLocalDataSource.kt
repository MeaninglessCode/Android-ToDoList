package com.github.meaninglesscode.todolistapp.data.source.local

import com.github.meaninglesscode.todolistapp.data.Result
import com.github.meaninglesscode.todolistapp.data.ToDoItem
import com.github.meaninglesscode.todolistapp.data.source.ToDoItemsDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [ToDoItemsLocalDataSource] class implementing the [ToDoItemsDataSource] interface for interaction
 * with the associated database.
 *
 * @param [toDoItemsDao] [ToDoItemsDao] for interaction with the associated database
 * @param [ioDispatcher] [CoroutineDispatcher] for launching methods in a non-blocking way
 */
class ToDoItemsLocalDataSource internal constructor(
    private val toDoItemsDao: ToDoItemsDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ToDoItemsDataSource {
    /**
     * Simple method exposing the [toDoItemsDao] getToDoItems method to get all [ToDoItem]s from the
     * table and return them wrapped in a [Result].
     *
     * @return [Result.Success] containing a [List] of [ToDoItem]s returned from the table. If there
     * is an error, then a [Result.Error] is returned instead.
     */
    override suspend fun getToDoItems(): Result<List<ToDoItem>> = withContext(ioDispatcher) {
        return@withContext try {
            Result.Success(toDoItemsDao.getToDoItems())
        }
        catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Simple method exposing the [toDoItemsDao] getToDoItemById method to get the [ToDoItem] with
     * an ID of [id] from the table and return it wrapped in a [Result].
     *
     * @param [id] [String] ID of the [ToDoItem] to fetch from the table
     * @return [Result.Success] containing a [ToDoItem] returned from the table. If there is an
     * error, then a [Result.Error] is returned insteand.
     */
    override suspend fun getToDoItem(id: String): Result<ToDoItem> = withContext(ioDispatcher) {
        try {
            val item = toDoItemsDao.getToDoItemById(id)

            return@withContext when {
                item != null -> Result.Success(item)
                else -> Result.Error(Exception("To do item not found!"))
            }
        }
        catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    /**
     * Simple method exposing the [toDoItemsDao] insertToDoItem method.
     *
     * @param [item] [ToDoItem] to insert into the table
     */
    override suspend fun saveToDoItem(item: ToDoItem) = withContext(ioDispatcher) {
        toDoItemsDao.insertToDoItem(item)
    }

    /**
     * Simple method exposing the [toDoItemsDao] updateCompleted method. Passes true so that the
     * completion status of [item] will always be set to true.
     *
     * @param [item] [ToDoItem] whose completed status is to be set to true
     */
    override suspend fun completeToDoItem(item: ToDoItem) = withContext(ioDispatcher) {
        toDoItemsDao.updateCompleted(item.id, true)
    }

    /**
     * Overload method of [completeToDoItem] that takes a [String] [id] instead of a [ToDoItem].
     * Passes true so that the completion status of the item with [id] will always be set to true.
     *
     * @param [id] [String] ID of item whose completed status is to be set to true
     */
    override suspend fun completeToDoItem(id: String) {
        toDoItemsDao.updateCompleted(id, true)
    }

    /**
     * Simple method exposing the [toDoItemsDao] updateCompleted method. Passes false so that the
     * completion status of [item] will always be set to true.
     *
     * @param [item] [ToDoItem] whose completed status is to be set to false
     */
    override suspend fun activateToDoItem(item: ToDoItem) = withContext(ioDispatcher) {
        toDoItemsDao.updateCompleted(item.id, false)
    }

    /**
     * Overload method of [completeToDoItem] that takes a [String] [id] instead of a [ToDoItem].
     * Passes true so that the completion status of the item with [id] will always be set to false.
     *
     * @param [id] [String] ID of item whose completed status is to be set to false
     */
    override suspend fun activateToDoItem(id: String) {
        toDoItemsDao.updateCompleted(id, false)
    }

    /**
     * Simple method exposing the [toDoItemsDao] deleteCompletedToDoItems method. Deletes all
     * [ToDoItem]s with a completed status of true from the table.
     */
    override suspend fun clearCompletedToDoItems() = withContext<Unit>(ioDispatcher) {
        toDoItemsDao.deleteCompletedToDoItems()
    }

    /**
     * Simple method exposing the [toDoItemsDao] deleteToDoItems method. Deletes all [ToDoItem]s
     * from the table.
     */
    override suspend fun deleteAllToDoItems() = withContext(ioDispatcher) {
        toDoItemsDao.deleteToDoItems()
    }

    /**
     * Simple method exposing the [toDoItemsDao] deleteToDoItemById method. Deletes the [ToDoItem]
     * with an ID of [id] from the table.
     *
     * @param [id] [String] ID of the [ToDoItem] to delete from the table
     */
    override suspend fun deleteToDoItem(id: String) = withContext<Unit>(ioDispatcher) {
        toDoItemsDao.deleteToDoItemById(id)
    }
}