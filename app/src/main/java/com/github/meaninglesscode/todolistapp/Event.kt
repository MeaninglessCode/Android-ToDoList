package com.github.meaninglesscode.todolistapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * The [Event] class is used as a data wrapper exposed via a [LiveData] object that is
 * representative of the [Event].
 *
 * @param content The data to wrap into the [Event] class
 */
open class Event<out T>(private val content: T) {
    /**
     * [Boolean] indicated whether or not this [Event] has been handled. [hasBeenHandled] is coupled
     * with a private setter to allow checking of the [Event]'s status but to disallow changes to
     * said status.
     * */
    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * If [hasBeenHandled] is true, then null is returned. If [hasBeenHandled] is false, then it is
     * set to true and the [content] is returned.
     */
    fun getContentIfNotHandled(): T? {
        return when {
            hasBeenHandled ->  null
            else -> {
                hasBeenHandled = true
                content
            }
        }
    }

    /**
     * Allows for the checking of the [Event]'s content, regardless of the state of
     * [hasBeenHandled].
     */
    fun peekContent(): T = content
}

/**
 * An [EventObserver] that implements an [Observer] of [Event]. This aids in simplifying the
 * checking of whether or not an [Event] has already been handled or not. [onEventUnhandledContent]
 * is called if the [Event]'s contents haven't been handled.
 */
class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(event: Event<T>?) {
        event?.getContentIfNotHandled()?.let {
            onEventUnhandledContent(it)
        }
    }
}