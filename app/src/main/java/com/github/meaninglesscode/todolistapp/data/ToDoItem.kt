package com.github.meaninglesscode.todolistapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.meaninglesscode.todolistapp.todolist.ToDoListActivity
import java.util.*

/**
 * Data class to contain all [ToDoItem] related to data. This class is marked as an [Entity] for use
 * with Room. The table name associated with room is "to_do_items".
 *
 * @param [id] [String] [UUID] representing the unique [id] associated with the given [ToDoItem].
 * The column name of [id] is "id"
 * @param [title] [String] representing the [title] associated with the given [ToDoItem]. The column
 * name of [title] is "title"
 * @param [content] [String] representing the [content] associated with the given [ToDoItem]. The
 * column name of [content] is "content"
 * @param [completed] [Boolean] representing the [completed] status of the [ToDoItem]. The column
 * name of [completed] is "completed"
 * @param [dueDate] [Long] representing the [dueDate] associated with the given [ToDoItem]. The
 * column name of [dueDate] is "due_date"
 */
@Entity(tableName = "to_do_items")
data class ToDoItem @JvmOverloads constructor(
    @PrimaryKey @ColumnInfo(name = "id") var id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "title") var title: String = "",
    @ColumnInfo(name = "content") var content: String = "",
    @ColumnInfo(name = "completed") var completed: Boolean = false,
    @ColumnInfo(name = "due_date") var dueDate: Long = -1
) {
    /**
     * Method to return an identifier to display for the [ToDoItem] when it is being viewed in the
     * [ToDoListActivity]. If the [title] is empty, then the [content] is used instead of the title.
     *
     * @return [String] representing the title of the [ToDoItem] to display
     */
    val titleForList: String
        get() = if (title.isNotEmpty()) title else content

    /**
     * Helper method to determine if the [ToDoItem] is in an active state or not. If the [ToDoItem]
     * is not [completed], then it is active. If the item is [completed], then it is not active.
     *
     * @return [Boolean] value representing whether the given [ToDoItem] is in an active state
     */
    val isActive
        get() = !completed

    /**
     * Helper method to determine whether or not the [title] or [content] of the [ToDoItem] are
     * empty.
     *
     * @return [Boolean] value representing whether or not the [title] is empty or the [content] is
     * empty
     */
    val isEmpty
        get() = title.isEmpty() || content.isEmpty()
}