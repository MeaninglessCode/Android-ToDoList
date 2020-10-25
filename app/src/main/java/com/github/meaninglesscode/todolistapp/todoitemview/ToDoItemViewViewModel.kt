package com.github.meaninglesscode.todolistapp.todoitemview

import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.github.meaninglesscode.todolistapp.Event
import com.github.meaninglesscode.todolistapp.R
import com.github.meaninglesscode.todolistapp.data.Result
import com.github.meaninglesscode.todolistapp.data.ToDoItem
import com.github.meaninglesscode.todolistapp.data.source.ToDoItemsRepository
import com.github.meaninglesscode.todolistapp.util.wrapEspressoIdlingResource
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [ViewModel] representing the screen used to view a [ToDoItem]s. The [ToDoItemViewViewModel]
 * implements [ViewModel].
 *
 * @param [toDoItemsRepository] [ToDoItemsRepository] for interacting with [ToDoItem]s. This param
 * is automatically passed via dependency injection
 */
class ToDoItemViewViewModel @Inject constructor(
    private val toDoItemsRepository: ToDoItemsRepository
) : ViewModel() {

    private val _item = MutableLiveData<ToDoItem?>()
    val item: LiveData<ToDoItem?> = _item

    private val _isDataAvailable = MutableLiveData<Boolean>()
    val isDataAvailable: LiveData<Boolean> = _isDataAvailable

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _editToDoItemCommand = MutableLiveData<Event<Unit>>()
    val editToDoItemCommand: LiveData<Event<Unit>> = _editToDoItemCommand

    private val _deleteToDoItemCommand = MutableLiveData<Event<Unit>>()
    val deleteToDoItemCommand: LiveData<Event<Unit>> = _deleteToDoItemCommand

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarMessage: LiveData<Event<Int>> = _snackbarText

    private val itemId: String?
        get() = _item.value?.id

    /**
     * [LiveData] dependent on another, so [Transformations.map] is used to check the completed
     * status of [_item]. */
    val completed: LiveData<Boolean> = Transformations.map(_item) { input: ToDoItem? ->
        input?.completed ?: false
    }

    /**
     * Method to delete the currently displayed to do item.
     */
    fun deleteToDoItem() = viewModelScope.launch {
        itemId?.let {
            toDoItemsRepository.deleteToDoItem(it)
            _deleteToDoItemCommand.value = Event(Unit)
        }
    }

    /**
     * Method to edit the current to do item and update it's values.
     */
    fun editToDoItem() {
        _editToDoItemCommand.value = Event(Unit)
    }

    /**
     * Method to set the completion status of the current to do item.
     *
     * @param [completed] [Boolean] value to set the item's completed status to
     */
    fun setCompleted(completed: Boolean) = viewModelScope.launch {
        val item = _item.value ?: return@launch

        when {
            completed -> {
                toDoItemsRepository.completeToDoItem(item)
                showSnackbarMessage(R.string.to_do_item_marked_complete)
            }
            else -> {
                toDoItemsRepository.activateToDoItem(item)
                showSnackbarMessage(R.string.to_do_item_marked_active)
            }
        }
    }

    /**
     * Method to initialize the displayed item by retrieving it from [toDoItemsRepository] and
     * calling the [onToDoItemLoaded] method to load and display it, or to call the
     * [onDataNotAvailable] if there is a failure retrieving the item.
     *
     * @param [id] [String]? representing the ID of the item to start displaying
     * @param [forceRefresh] [Boolean]
     */
    fun start(id: String?, forceRefresh: Boolean = false) {
        if (_isDataAvailable.value == true && !forceRefresh || _dataLoading.value == true) {
            return
        }

        // Show loading indicator
        _dataLoading.value = true

        wrapEspressoIdlingResource {

            viewModelScope.launch {
                if (id != null) {
                    toDoItemsRepository.getToDoItem(id).let { result ->
                        when (result) {
                            is Result.Success -> onToDoItemLoaded(result.data)
                            else -> onDataNotAvailable(result)
                        }
                    }
                }
                _dataLoading.value = false
            }
        }
    }

    /**
     * Sets the currently displayed [ToDoItem] to the given [item].
     *
     * @param [item] [ToDoItem]? to display
     */
    private fun setToDoItem(item: ToDoItem?) {
        this._item.value = item
        _isDataAvailable.value = item != null
    }

    /**
     * Method to handle the loading of a [ToDoItem] by calling [setToDoItem] on the newly loaded
     * [item].
     *
     * @param [item] [ToDoItem]
     */
    private fun onToDoItemLoaded(item: ToDoItem) {
        setToDoItem(item)
    }

    /**
     * Method to handle the case where a [ToDoItem]s data is unavailable.
     *
     * @param [result] [Result] containing a [ToDoItem]. Currently not utilized in the method.
     */
    private fun onDataNotAvailable(result: Result<ToDoItem>) {
        _item.value = null
        _isDataAvailable.value = false
    }

    /**
     * Method to refresh the currently displayed [ToDoItem].
     */
    fun refresh() {
        itemId?.let { start(it, true) }
    }

    /**
     * Method to show the given [StringRes] message on this [ViewModel]'s associated snack bar.
     *
     * @param [message] [Int] representing the ID of the [StringRes] to show in the snack bar
     */
    private fun showSnackbarMessage(@StringRes message: Int) {
        _snackbarText.value = Event(message)
    }
}