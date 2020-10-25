package com.github.meaninglesscode.todolistapp.data

/**
 * Wrapper class used for data source interactions. The [Result] becomes a [Result.Success]
 * containing data when there is a successful data transaction and the [Result] becomes a
 * [Result.Error] when there is an error retrieving the data. Additionally holds a [Loading]
 * object to represent the state of a data transaction in progress.
 */
sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            Loading -> "Loading"
        }
    }
}

/**
 * [Result] extension to determine whether or not the [Result] has succeeded. This entails that the
 * [Result] is a [Result.Success] and that the [Result]'s data is not null.
 */
val Result<*>.succeeded
    get() = this is Result.Success && data != null