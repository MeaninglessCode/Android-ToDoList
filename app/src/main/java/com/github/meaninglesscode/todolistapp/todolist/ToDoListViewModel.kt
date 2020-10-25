package com.github.meaninglesscode.todolistapp.todolist

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.github.meaninglesscode.todolistapp.Event
import com.github.meaninglesscode.todolistapp.R
import com.github.meaninglesscode.todolistapp.data.Result
import com.github.meaninglesscode.todolistapp.data.ToDoItem
import com.github.meaninglesscode.todolistapp.data.source.ToDoItemsRepository
import com.github.meaninglesscode.todolistapp.util.wrapEspressoIdlingResource
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * [ViewModel] associated with the To Do List screen. Holds methods and data for displaying,
 * binding, and interacting with stored to do list items.
 *
 * @param [toDoItemsRepository] Repository for interacting with to do items. Automatically injected
 * into [ToDoListViewModel]
 */
class ToDoListViewModel @Inject constructor(private val toDoItemsRepository: ToDoItemsRepository) : ViewModel() {

    private val _items = MutableLiveData<List<ToDoItem>>().apply { value = emptyList() }
    val items: LiveData<List<ToDoItem>> = _items

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _currentFilteringLabel = MutableLiveData<Int>()
    val currentFilteringLabel: LiveData<Int> = _currentFilteringLabel

    private val _noItemsLabel = MutableLiveData<Int>()
    val noItemsLabel: LiveData<Int> = _noItemsLabel

    private val _noItemsIconRes = MutableLiveData<Int>()
    val noItemsIconRes: LiveData<Int> = _noItemsIconRes

    private val _toDoItemsAddViewVisable = MutableLiveData<Boolean>()
    val toDoItemsAddViewVisible: LiveData<Boolean> = _toDoItemsAddViewVisable

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarMessage: LiveData<Event<Int>> = _snackbarText

    private var _currentFiltering = ToDoListFilterType.ALL_ITEMS

    private val isDataLoadingError = MutableLiveData<Boolean>()

    private val _openToDoItemEvent = MutableLiveData<Event<String>>()
    val openToDoItemEvent: LiveData<Event<String>> = _openToDoItemEvent

    private val _newToDoItemEvent = MutableLiveData<Event<Unit>>()
    val newToDoItemEvent: LiveData<Event<Unit>> = _newToDoItemEvent

    // This LiveData depends on another so we can use a transformation.
    val empty: LiveData<Boolean> = Transformations.map(_items) {
        it.isEmpty()
    }

    /** Configures the initial state of the ToDoListViewModel. */
    init {
        setFiltering(ToDoListFilterType.ALL_ITEMS)
        loadToDoItems()
    }

    /**
     * Sets the currently used filtering type to one of the values of [ToDoListFilterType].
     *
     * @param [requestType] Type of filtering to use on the displayed to do items
     */
    fun setFiltering(requestType: ToDoListFilterType) {
        // Updates current filtering variable with new filtering type
        _currentFiltering = requestType

        /**
         * When block used on [requestType] to call [setFilter] with the appropriate arguments for
         * the selected filtering type.
         */
        when (requestType) {
            ToDoListFilterType.ALL_ITEMS -> {
                setFilter(
                    R.string.label_all, R.string.no_items_all,
                    R.drawable.ic_info, true
                )
            }
            ToDoListFilterType.INCOMPLETE_ITEMS -> {
                setFilter(
                    R.string.label_active, R.string.no_items_active,
                    R.drawable.ic_info, false
                )
            }
            ToDoListFilterType.COMPLETE_ITEMS -> {
                setFilter(
                    R.string.label_completed, R.string.no_items_completed,
                    R.drawable.ic_info, false
                )
            }
            ToDoListFilterType.ITEMS_WITH_DUE_DATES -> {
                setFilter(
                    R.string.label_has_due_date, R.string.no_items_due,
                    R.drawable.ic_info, false
                )
            }
            ToDoListFilterType.ITEMS_PAST_DUE -> {
                setFilter(
                    R.string.label_past_due, R.string.no_items_past_due,
                    R.drawable.ic_info, false
                )
            }
            ToDoListFilterType.ITEMS_DUE_TODAY -> {
                setFilter(
                    R.string.label_due_today, R.string.no_items_due_today,
                    R.drawable.ic_info, false
                )
            }
            ToDoListFilterType.ITEMS_DUE_THIS_WEEK -> {
                setFilter(
                    R.string.label_due_this_week, R.string.no_items_due_this_week,
                    R.drawable.ic_info, false
                )
            }
        }
    }

    /**
     * Method to set the current filtering type being used on the displayed to do items.
     *
     * @param [filteringLabelString] String resource text to display in the text area showing the
     * currently set filtering type
     * @param [noToDoItemsLabelString] String resource text to display when there are no items to
     * show for the selected filtering type
     * @param [noToDoItemsIconDrawable] Drawable resource icon to display above the text shown when
     * there are no items to display for the selected filtering type
     * @param [toDoItemsAddVisible]
     */
    private fun setFilter(
        @StringRes filteringLabelString: Int,
        @StringRes noToDoItemsLabelString: Int,
        @DrawableRes noToDoItemsIconDrawable: Int,
        toDoItemsAddVisible: Boolean
    ) {
        _currentFilteringLabel.value = filteringLabelString
        _noItemsLabel.value = noToDoItemsLabelString
        _noItemsIconRes.value = noToDoItemsIconDrawable
        _toDoItemsAddViewVisable.value = toDoItemsAddVisible
    }

    /**
     * Method to clear all to do items that are of completed status. Launches a coroutine in the
     * [viewModelScope] to ensure that the call to the [toDoItemsRepository] does not block. Shows
     * a snack bar message to the user to notify them that completed items have been cleared and
     * then refreshes the displayed to do items.
     */
    fun clearCompletedToDoItems() {
        viewModelScope.launch {
            toDoItemsRepository.clearCompletedToDoItems()
            showSnackbarMessage(R.string.completed_items_cleared)
            loadToDoItems()
        }
    }

    /**
     * Method to update the completion status of a [ToDoItem]. Shows an appropriate snack bar
     * message based on the value of [completed] as well as calling the associated method on
     * [toDoItemsRepository] to set [item]'s state within the database.
     *
     * @param [item] [ToDoItem] whose completion state is to be set to [completed]
     * @param [completed] [Boolean] value to set [item]'s completion state to
     */
    fun completeToDoItem(item: ToDoItem, completed: Boolean) = viewModelScope.launch {
        if (completed) {
            toDoItemsRepository.completeToDoItem(item)
            showSnackbarMessage(R.string.to_do_item_marked_complete)
        }
        else {
            toDoItemsRepository.activateToDoItem(item)
            showSnackbarMessage(R.string.to_do_item_marked_active)
        }
        loadToDoItems()
    }

    /**
     * Method to set the value of [_newToDoItemEvent] to a [Unit] [Event].
     */
    fun addNewToDoItem() {
        _newToDoItemEvent.value = Event(Unit)
    }

    /**
     * Method to open and show the desired to do item.
     *
     * @param [id] [String] value for the associated ID of the to do item to open
     */
    fun openToDoItem(id: String) {
        _openToDoItemEvent.value = Event(id)
    }

    /**
     * A method to show the result of a user's edits. Shows an appropriate snack bar message based
     * on the status of their edit.
     *
     * @param [result] [Int] value of the status of the edit result
     */
    fun showEditResultMessage(result: Int) {
        when (result) {
            EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_item_message)
            ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_item_message)
            DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_item_message)
        }
    }

    /**
     * Shows a snack bar message based off of the message parameter.
     *
     * @param [message] [Int] identifier for the message to display on the snack bar
     */
    private fun showSnackbarMessage(message: Int) {
        _snackbarText.value = Event(message)
    }

    /**
     * Method to load to do items from the [toDoItemsRepository] and populate the items list.
     */
    fun loadToDoItems() {
        _dataLoading.value = true

        wrapEspressoIdlingResource {

            viewModelScope.launch {
                val itemsResult = toDoItemsRepository.getToDoItems()

                if (itemsResult is Result.Success) {
                    val items = itemsResult.data
                    val itemsToShow = ArrayList<ToDoItem>()

                    for (item in items) {
                        // Items are filtered based on the requestType
                        when (_currentFiltering) {
                            ToDoListFilterType.ALL_ITEMS -> itemsToShow.add(item)

                            ToDoListFilterType.INCOMPLETE_ITEMS -> {
                                if (item.isActive)
                                    itemsToShow.add(item)
                            }

                            ToDoListFilterType.COMPLETE_ITEMS -> {
                                if (item.completed)
                                    itemsToShow.add(item)
                            }

                            ToDoListFilterType.ITEMS_WITH_DUE_DATES -> {
                                if (item.dueDate != -1L)
                                    itemsToShow.add(item)
                            }

                            ToDoListFilterType.ITEMS_PAST_DUE -> {
                                if (item.dueDate != -1L) {
                                    if (item.dueDate - Calendar.getInstance().timeInMillis < 0)
                                        itemsToShow.add(item)
                                }
                            }

                            ToDoListFilterType.ITEMS_DUE_TODAY -> {
                                if (item.dueDate != -1L) {
                                    val currentTime = Calendar.getInstance()
                                    val itemTime = Calendar.getInstance()

                                    itemTime.timeInMillis = item.dueDate

                                    if (currentTime.get(Calendar.DAY_OF_WEEK) == itemTime.get(Calendar.DAY_OF_WEEK))
                                        itemsToShow.add(item)
                                }
                            }

                            ToDoListFilterType.ITEMS_DUE_THIS_WEEK -> {
                                if (item.dueDate != -1L) {
                                    val currentTime = Calendar.getInstance()
                                    val itemTime = Calendar.getInstance()

                                    itemTime.timeInMillis = item.dueDate

                                    if (currentTime.get(Calendar.WEEK_OF_YEAR) == itemTime.get(Calendar.WEEK_OF_YEAR))
                                        itemsToShow.add(item)
                                }
                            }
                        }
                    }

                    isDataLoadingError.value = false
                    _items.value = ArrayList(itemsToShow)
                }
                else {
                    isDataLoadingError.value = false
                    _items.value = emptyList()
                    showSnackbarMessage(R.string.loading_items_error)
                }

                _dataLoading.value = false
            }
        }
    }

    /**
     * Simple wrapper method to refresh to do items by calling the [loadToDoItems] method.
     */
    fun refresh() {
        loadToDoItems()
    }
}