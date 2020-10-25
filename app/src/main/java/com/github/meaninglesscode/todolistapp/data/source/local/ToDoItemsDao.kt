package com.github.meaninglesscode.todolistapp.data.source.local

import androidx.room.*
import com.github.meaninglesscode.todolistapp.data.ToDoItem

/**
 * [Dao] (Data Access Object) for [ToDoItem]s. Used for interaction with the [Room] database.
 */
@Dao
interface ToDoItemsDao {
    /**
     * Method to select and return all [ToDoItem]s from the table.
     *
     * @return [List] of all [ToDoItem]s in the table
     */
    @Query("SELECT * FROM to_do_items")
    suspend fun getToDoItems(): List<ToDoItem>

    /**
     * Method to select and return the [ToDoItem] from the table with [id].
     *
     * @param [id] [String] representing the ID of the item to retrieve from the table
     * @return [ToDoItem] returned from the table if there was an item matching [id]. If not, then
     * the returned item is null.
     */
    @Query("SELECT * FROM to_do_items WHERE id = :id")
    suspend fun getToDoItemById(id: String): ToDoItem?

    /**
     * Insert a new [ToDoItem] into the table. If there is a conflict, then the conflicting item
     * is replaced by the new item.
     *
     * @param [item] The [ToDoItem] to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToDoItem(item: ToDoItem)

    /**
     * Update the to do item in the table matching [item] to the new values contained within the
     * [ToDoItem] object.
     *
     * @param [item] [ToDoItem] to update with new values
     * @return [Int] representing the number of items updated (should always be 1)
     */
    @Update
    suspend fun updateToDoItem(item: ToDoItem): Int

    /**
     * Update the completed status of a [ToDoItem] with [id] in the table to the value of
     * [completed].
     *
     * @param [id] [String] ID of to do item whose status to update
     * @param [completed] [Boolean] value to set status of [ToDoItem] to
     */
    @Query("UPDATE to_do_items SET completed = :completed WHERE id = :id")
    suspend fun updateCompleted(id: String, completed: Boolean)

    /**
     * Delete all items from the table with an ID matching [id]. Should always delete a single item
     * because [id] is the primary key and must be unique for an item to be added to the table.
     *
     * @param [id] [String] ID of item to delete from table
     * @return [Int] representing the number of items deleted (should always be 1)
     */
    @Query("DELETE FROM to_do_items WHERE id = :id")
    suspend fun deleteToDoItemById(id: String): Int

    /**
     * Delete all [ToDoItem]s from the table.
     */
    @Query("DELETE FROM to_do_items")
    suspend fun deleteToDoItems()

    /**
     * Deletes all [ToDoItem]s from the table that have a completed status of true.
     *
     * @return [Int] representing how many items have been deleted
     */
    @Query("DELETE FROM to_do_items WHERE completed = 1")
    suspend fun deleteCompletedToDoItems(): Int
}