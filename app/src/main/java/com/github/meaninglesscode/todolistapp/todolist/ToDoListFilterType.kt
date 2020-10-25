package com.github.meaninglesscode.todolistapp.todolist

/**
 * [Enum] class used to specify valid filtering types for the to do item list.
 */
enum class ToDoListFilterType {
    /** No filtering is applied and all tasks are shown. */
    ALL_ITEMS,

    /** Filtering is applied to show only items that are not yet completed. */
    INCOMPLETE_ITEMS,

    /** Filtering is applied to show only items that are completed. */
    COMPLETE_ITEMS,

    /** Filtering is applied to show only items that have due dates set. */
    ITEMS_WITH_DUE_DATES,

    /** Filtering is applied to show only items that are past due/ */
    ITEMS_PAST_DUE,

    /** Filtering is applied to show only items that are due today. */
    ITEMS_DUE_TODAY,

    /** Filtering is applied to show only items that are due this week. */
    ITEMS_DUE_THIS_WEEK
}