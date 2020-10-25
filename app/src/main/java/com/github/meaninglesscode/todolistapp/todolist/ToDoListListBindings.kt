package com.github.meaninglesscode.todolistapp.todolist

import android.graphics.Paint
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.meaninglesscode.todolistapp.data.ToDoItem

/**
 * Data binding [BindingAdapter] used to bind [ToDoItem]s into the [RecyclerView].
 *
 * @param [listView] [RecyclerView] to populate
 * @param [items] [List] of [ToDoItem]s to populate the [listView] with
 */
@BindingAdapter("items")
fun setItems(listView: RecyclerView, items: List<ToDoItem>) {
    (listView.adapter as ToDoListAdapter).submitList(items)
}

/**
 * Data binding [BindingAdapter] used to mark a [ToDoItem] as completed via a strike through.
 *
 * @param [textView] [TextView] to strike through
 * @param [enabled] [Boolean] value representing whether or not to add the strike through
 */
@BindingAdapter("completedToDoItem")
fun setStyle(textView: TextView, enabled: Boolean) {
    when {
        enabled -> textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        else -> textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }
}