package com.github.meaninglesscode.todolistapp.todolist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.meaninglesscode.todolistapp.data.ToDoItem
import com.github.meaninglesscode.todolistapp.databinding.ToDoItemBinding

/**
 * [ToDoListAdapter] implementing a [ListAdapter] utilizing a [TaskDiffCallback] for displaying
 * [ToDoItem]s in a [RecyclerView].
 *
 * @param [viewModel] [ToDoListViewModel]
 */
class ToDoListAdapter(private val viewModel: ToDoListViewModel) :
    ListAdapter<ToDoItem, ToDoListAdapter.ViewHolder>(TaskDiffCallback()) {
    /**
     * Method overriding [ListAdapter.onBindViewHolder] to handle binding of [ToDoItem]s.
     *
     * @param [holder] [ViewHolder] to bind from
     * @param [position] [Int] position of the item to bind
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(viewModel, item)
    }

    /**
     * Method overriding [ListAdapter.onCreateViewHolder] to return a [ViewHolder.from] the passed
     * [parent] [ViewGroup].
     *
     * @param [parent] [ViewGroup] automatically passed into the [onCreateViewHolder] method
     * @param [viewType] [Int] automatically passed into the [onCreateViewHolder] method
     * @return [ViewHolder] created from [parent] and returned
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    /**
     * [ViewHolder] class implementing [RecyclerView.ViewHolder].
     *
     * @param [binding] [ToDoItemBinding] item binding for binding a [ToDoItem]
     */
    class ViewHolder private constructor(val binding: ToDoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Function to set the values of [ToDoItemBinding].
         *
         * @param [viewModel] [ToDoListViewModel] to bind into [ToDoItemBinding]
         * @param [item] [ToDoItem] to bind into [ToDoItemBinding]
         */
        fun bind(viewModel: ToDoListViewModel, item: ToDoItem) {
            binding.viewmodel = viewModel
            binding.toDoItem = item
            binding.executePendingBindings()
        }

        /**
         * [Companion] object containing the [from] method to create a [ViewHolder] from a
         * [ViewGroup].
         */
        companion object {
            /**
             * Method to create a [ViewHolder] from the given [parent] [ViewGroup].
             *
             * @param [parent] [ViewGroup]
             * @return [ViewHolder]
             */
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ToDoItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

/**
 * [TaskDiffCallback] that implements [DiffUtil.ItemCallback] to compare [ToDoItem]s.
 */
class TaskDiffCallback : DiffUtil.ItemCallback<ToDoItem>() {
    /**
     * Method overriding [DiffUtil.ItemCallback.areItemsTheSame] to determine whether or not the ID
     * of [oldItem] and the ID of [newItem] are the same.
     *
     * @param [oldItem] [ToDoItem] whose ID is to be compared to the ID of [newItem]
     * @param [newItem] [ToDoItem] whose ID is to be compared to the ID of [oldItem]
     * @return [Boolean] value representing whether or not the ID of [oldItem] is equal to the ID of
     * [newItem]
     */
    override fun areItemsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean {
        return oldItem.id == newItem.id
    }

    /**
     * Method overriding [DiffUtil.ItemCallback.areContentsTheSame] to determine whether or not
     * [newItem] is equal to [oldItem].
     *
     * @param [oldItem] [ToDoItem] to be compared to [newItem]
     * @param [newItem] [ToDoItem] to be compared to [oldItem]
     * @return [Boolean] value representing whether or not [newItem] is equal to [oldItem]
     */
    override fun areContentsTheSame(oldItem: ToDoItem, newItem: ToDoItem): Boolean {
        return oldItem == newItem
    }
}