package com.github.meaninglesscode.todolistapp.util

import java.text.SimpleDateFormat
import java.util.*

const val DEFAULT_FORMAT = "dd/MM/yyyy, hh:mm a"

/**
 * Helper method to get a datetime string in the given format from the given milliseconds value.
 *
 * @param [millis] [Long] representing the number of milliseconds since January 1st, 1970 that the
 * time refers to
 * @param [format] [String] representing the format of the resultant datetime [String]. The default
 * value is [DEFAULT_FORMAT]
 * @return [String] representing the datetime of the given [millis] values in the [format] given
 */
fun getDateStringFromMillis(millis: Long, format: String = DEFAULT_FORMAT): String {

    if (millis < 0L)
        return ""

    val formatter = SimpleDateFormat(format)
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = millis

    return formatter.format(calendar.time)
}