package com.github.meaninglesscode.todolistapp.data.source

import com.github.meaninglesscode.todolistapp.data.Result
import com.github.meaninglesscode.todolistapp.data.ToDoItem
import com.github.meaninglesscode.todolistapp.data.source.local.ToDoItemsLocalDataSource
import com.github.meaninglesscode.todolistapp.di.ApplicationModule
import com.github.meaninglesscode.todolistapp.util.EspressoIdlingResource
import com.github.meaninglesscode.todolistapp.util.wrapEspressoIdlingResource
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.inject.Inject

/**
 * [DefaultToDoItemsRepository] that implements the [ToDoItemsRepository] interface. The
 * [ToDoItemsLocalDataSource] is provided via dependency injection.
 *
 * @param [toDoItemsLocalDataSource] [ToDoItemsDataSource] implementing class for local data storage
 * and interaction
 * @param [ioDispatcher] [CoroutineDispatcher] for completing methods without blocking
 */
class DefaultToDoItemsRepository @Inject constructor(
    @ApplicationModule.ToDoItemsLocalDataSource private val toDoItemsLocalDataSource: ToDoItemsDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ToDoItemsRepository {

    private var cachedItems: ConcurrentMap<String, ToDoItem>? = null

    /**
     * Method to retrieve all [ToDoItem]s and return them wrapped in a [Result]. This method also
     * refreshes [cachedItems].
     *
     * @return [Result.Success] containing a [List] of [ToDoItem]s returned from the data source. If
     * there is an error, then a [Result.Error] is returned instead.
     */
    override suspend fun getToDoItems(): Result<List<ToDoItem>> {

        wrapEspressoIdlingResource {

            return withContext(ioDispatcher) {
                cachedItems?.let { cachedItems ->
                    return@withContext Result.Success(cachedItems.values.sortedBy { it.title })
                }

                val newItems = fetchItemsFromLocal()

                (newItems as? Result.Success)?.let { refreshCache(it.data) }

                cachedItems?.values?.let { items ->
                    return@withContext Result.Success(items.sortedBy { it.title })
                }

                (newItems as? Result.Success)?.let {
                    if (it.data.isEmpty()) {
                        return@withContext Result.Success(it.data)
                    }
                }

                return@withContext Result.Error(Exception("Illegal state"))
            }
        }
    }

    /**
     * Helper method to actually retrieve the [ToDoItem]s for [getToDoItems] from the appropriate
     * [ToDoItemsDataSource].
     *
     * @return [Result.Success] containing a [List] of [ToDoItem]s returned from the
     * [ToDoItemsLocalDataSource]. Returns a [Result.Error] in the event of an error
     */
    private suspend fun fetchItemsFromLocal(): Result<List<ToDoItem>> {
        val localItems = toDoItemsLocalDataSource.getToDoItems()
        if (localItems is Result.Success)
            return localItems
        return Result.Error(Exception("Error fetching local"))
    }

    /**
     * Method to retrieve a [ToDoItem] with the given [id] and return it wrapped in a [Result].
     *
     * @param [id] [String] representing the ID of the [ToDoItem] to retrieve
     * @return [Result.Success] containing a [ToDoItem] return from the data source
     */
    override suspend fun getToDoItem(id: String): Result<ToDoItem> {

        wrapEspressoIdlingResource {

            return withContext(ioDispatcher) {

                getToDoItemWithId(id)?.let {
                    EspressoIdlingResource.decrement()
                    return@withContext Result.Success(it)
                }

                val newItem = fetchItemFromLocal(id)

                (newItem as? Result.Success)?.let { cacheItem(it.data) }

                return@withContext newItem
            }
        }
    }

    /**
     * Helper method to actually retrieve the [ToDoItem] for [getToDoItem] from the appropriate
     * [ToDoItemsDataSource].
     *
     * @param [id] [String]
     * @return [Result.Success] containing [ToDoItem] returned from the [ToDoItemsLocalDataSource]
     * Returns a [Result.Error] in the event of an error
     */
    private suspend fun fetchItemFromLocal(id: String): Result<ToDoItem> {
        val localItem = toDoItemsLocalDataSource.getToDoItem(id)
        if (localItem is Result.Success)
            return localItem
        return Result.Error(Exception("Error fetching data from local"))
    }

    /**
     * Saves the given [item] into the appropriate [ToDoItemsDataSource]s.
     *
     * @param [item] [ToDoItem] to save into the [ToDoItemsDataSource]s
     */
    override suspend fun saveToDoItem(item: ToDoItem) {
        cacheAndPerform(item) {
            coroutineScope {
                launch { toDoItemsLocalDataSource.saveToDoItem(it) }
            }
        }
    }

    /**
     * Sets the completed status of the given [item] to true.
     *
     * @param [item] [ToDoItem] to set the completed status of to true
     */
    override suspend fun completeToDoItem(item: ToDoItem) {
        cacheAndPerform(item) {
            it.completed = true
            coroutineScope {
                launch { toDoItemsLocalDataSource.completeToDoItem(it) }
            }
        }
    }

    /**
     * Sets the completed status of the [ToDoItem] with the given [id] to true. Translates the [id]
     * into the [ToDoItem] associated with it and calls the [completeToDoItem] function overload
     * with [ToDoItem].
     *
     * @param [id] [String] of the [ToDoItem] to set the completed status of to true
     */
    override suspend fun completeToDoItem(id: String) {
        withContext(ioDispatcher) {
            getToDoItemWithId(id)?.let {
                completeToDoItem(it)
            }
        }
    }

    /**
     * Sets the completed status of the given [item] to false.
     *
     * @param [item] [ToDoItem] to set the completed status of to false
     */
    override suspend fun activateToDoItem(item: ToDoItem) = withContext(ioDispatcher) {
        // Do in memory cache update to keep the app UI up to date
        cacheAndPerform(item) {
            it.completed = false
            coroutineScope {
                launch { toDoItemsLocalDataSource.activateToDoItem(it) }
            }

        }
    }

    /**
     * Sets the completed status of the [ToDoItem] with the given [id] to false. Translates the [id]
     * into the [ToDoItem] associated with it and calls the [activateToDoItem] function overload
     * with [ToDoItem].
     *
     * @param [id] [String] of the [ToDoItem] to set the completed status of to false
     */
    override suspend fun activateToDoItem(id: String) {
        withContext(ioDispatcher) {
            getToDoItemWithId(id)?.let {
                activateToDoItem(it)
            }
        }
    }

    /**
     * Deletes all [ToDoItem]s that have a completed status from the [ToDoItemsDataSource]s and
     * removes these same items from [cachedItems].
     */
    override suspend fun clearCompletedToDoItems() {
        coroutineScope {
            launch { toDoItemsLocalDataSource.clearCompletedToDoItems() }
        }
        withContext(ioDispatcher) {
            cachedItems?.entries?.removeAll { it.value.completed }
        }
    }

    /**
     * Deletes all [ToDoItem]s from the [ToDoItemsDataSource]s and then clears [cachedItems].
     */
    override suspend fun deleteAllToDoItems() {
        withContext(ioDispatcher) {
            coroutineScope {
                launch { toDoItemsLocalDataSource.deleteAllToDoItems() }
            }
        }
        cachedItems?.clear()
    }

    /**
     * Deletes the [ToDoItem] with [id] from the [ToDoItemsDataSource]s and then removes it from
     * [cachedItems] as well.
     *
     * @param [id] [String] representing the ID of the [ToDoItem] you want to delete
     */
    override suspend fun deleteToDoItem(id: String) {
        coroutineScope {
            launch { toDoItemsLocalDataSource.deleteToDoItem(id) }
        }

        cachedItems?.remove(id)
    }

    /**
     * Refreshes [cachedItems] with the given [items].
     *
     * @param [items] [List] of [ToDoItem]s to use to refresh [cachedItems]
     */
    private fun refreshCache(items: List<ToDoItem>) {
        cachedItems?.clear()
        items.sortedBy { it.title }.forEach {
            cacheAndPerform(it) {}
        }
    }

    /**
     * Gets the [ToDoItem] with the given [id] from [cachedItems].
     *
     * @param [id] [String] ID of the item to retrieve from [cachedItems]
     * @return [ToDoItem] from [cachedItems] with [id]
     */
    private fun getToDoItemWithId(id: String) = cachedItems?.get(id)

    /**
     * Adds the given [item] to [cachedItems].
     *
     * @param [item] [ToDoItem] to insert into [cachedItems]
     * @return [ToDoItem] cached into [cachedItems]
     */
    private fun cacheItem(item: ToDoItem): ToDoItem {
        val cachedItem = ToDoItem(item.id, item.title, item.content, item.completed, item.dueDate)

        if (cachedItems == null)
            cachedItems = ConcurrentHashMap()

        cachedItems?.put(cachedItem.id, cachedItem)
        return cachedItem
    }

    /**
     * Function to cache the given [item] and then perform a given action [action] after the caching
     * is performed.
     *
     * @param [item] [ToDoItem] to be passed into [cacheItem]
     * @param [action] Action to be performed after [cacheItem] is completed
     */
    private inline fun cacheAndPerform(item: ToDoItem, action: (ToDoItem) -> Unit) {
        action(cacheItem(item))
    }
}