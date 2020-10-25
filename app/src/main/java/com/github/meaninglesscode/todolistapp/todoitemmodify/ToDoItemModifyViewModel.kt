package com.github.meaninglesscode.todolistapp.todoitemmodify

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.meaninglesscode.todolistapp.Event
import com.github.meaninglesscode.todolistapp.R
import com.github.meaninglesscode.todolistapp.data.Result
import com.github.meaninglesscode.todolistapp.data.ToDoItem
import com.github.meaninglesscode.todolistapp.data.source.ToDoItemsRepository
import com.github.meaninglesscode.todolistapp.util.alarm.AlarmHelper
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [ViewModel] representing the screen used to modify [ToDoItem]s. The [ToDoItemModifyViewModel]
 * implements [ViewModel].
 *
 * @param [toDoItemsRepository] [ToDoItemsRepository] for interacting with [ToDoItem]s. This param
 * is automatically passed via dependency injection
 */
class ToDoItemModifyViewModel @Inject constructor(
    private val toDoItemsRepository: ToDoItemsRepository,
    private val context: Context
) : ViewModel() {

    /** [MutableLiveData] of [String] exposed for two-way data binding */
    val title = MutableLiveData<String>()

    /** [MutableLiveData] of [String] exposed for two-way data binding */
    val content = MutableLiveData<String>()

    /** [MutableLiveData] of [Long] exposed for one-way data binding */
    val dueDate = MutableLiveData<Long>()

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarMessage: LiveData<Event<Int>> = _snackbarText

    private val _toDoItemUpdated = MutableLiveData<Event<Unit>>()
    val toDoItemUpdatedEvent: LiveData<Event<Unit>> = _toDoItemUpdated

    // Variables maintaining the state of the currently selected to do item
    private var id: String? = null

    private var isNewToDoItem: Boolean = false

    private var isDataLoaded = false

    private var itemCompleted = false

    /**
     * Method called to initialize the [ToDoItemModifyViewModel]. If necessary, loads [ToDoItem]
     * data via [toDoItemsRepository] and [onToDoItemLoaded]. If the result is an error, then
     * [onDataNotAvailable] is called instead.
     *
     * @param [id] [String]? if the ID is null, then a new [ToDoItem] is being added
     */
    fun start(id: String?) {
        if (_dataLoading.value == true)
            return

        this.id = id
        this.dueDate.value = -1L

        // No need to populate; item is new
        if (id == null) {
            isNewToDoItem = true
            return
        }

        // Nothing to populate; data is already loaded
        if (isDataLoaded)
            return

        isNewToDoItem = false
        _dataLoading.value = true

        viewModelScope.launch {
            toDoItemsRepository.getToDoItem(id).let { result ->
                when (result) {
                    is Result.Success -> onToDoItemLoaded(result.data)
                    else -> onDataNotAvailable()
                }
            }
        }
    }

    /**
     * Method to update the two-way data binding values stored by the class when a [ToDoItem] is
     * loaded so that the display is updated accordingly.
     *
     * @param [item] [ToDoItem] whose values are to be displayed
     */
    private fun onToDoItemLoaded(item: ToDoItem) {
        title.value = item.title
        content.value = item.content
        itemCompleted = item.completed
        dueDate.value = item.dueDate
        _dataLoading.value = false
        isDataLoaded = true
    }

    /**
     * Ensures the [_dataLoading] is set to false in the event of data being unavailable.
     */
    private fun onDataNotAvailable() {
        _dataLoading.value = false
    }

    /**
     * This method is called when clicking on the fab_save_to_do_item floating button. Displays
     * appropriate text to the snack bar based on the content of the [ToDoItem] as well as calling
     * either [createToDoItem] or [updateToDoItem] appropriately based on the state of the loaded
     * [ToDoItem].
     */
    fun saveToDoItem() {
        val currentTitle = title.value
        val currentContent = content.value
        var currentDueDate = dueDate.value

        if (currentTitle == null || currentContent == null) {
            _snackbarText.value = Event(R.string.empty_item_message)
            return
        }
        if (ToDoItem(title = currentTitle, content = currentContent).isEmpty) {
            _snackbarText.value = Event(R.string.empty_item_message)
            return
        }

        val currentId = id

        if (currentDueDate == null)
            currentDueDate = -1L

        if (isNewToDoItem || currentId == null) {
            createToDoItem(ToDoItem(title = currentTitle, content = currentContent, dueDate = currentDueDate))
        }
        else {
            val item = ToDoItem(currentId, currentTitle, currentContent, itemCompleted, currentDueDate)
            updateToDoItem(item)
        }
    }

    /**
     * Method to create a new to do item, if that is what the user has chosen to do.
     *
     * @param [newItem] [ToDoItem] to add into the [toDoItemsRepository]
     */
    private fun createToDoItem(newItem: ToDoItem) = viewModelScope.launch {
        toDoItemsRepository.saveToDoItem(newItem)

        // If item has a due date set, schedule the alarm
        if (newItem.dueDate != -1L)
            AlarmHelper.schedule(context, newItem.id, newItem.dueDate, newItem.title)

        _toDoItemUpdated.value = Event(Unit)
    }

    /**
     * Method to update an existing [ToDoItem], if that is what the user has chosen to do. This
     * method throws a [RuntimeException] if [updateToDoItem] is called on an item that has not yet
     * been saved.
     *
     * @param [item] [ToDoItem] whose values are to be updated in [toDoItemsRepository]
     */
    private fun updateToDoItem(item: ToDoItem) {
        if (isNewToDoItem)
            throw RuntimeException("updateToDoItem was called on a new to do item")

        viewModelScope.launch {
            toDoItemsRepository.saveToDoItem(item)

            // If item dueDate is set, schedule an alarm. If item dueDate is unset, cancel the alarm
            when {
                item.dueDate != -1L -> AlarmHelper.schedule(context, item.id, item.dueDate, item.title)
                else -> AlarmHelper.cancel(context, item.id)
            }

            _toDoItemUpdated.value = Event(Unit)
        }
    }
}