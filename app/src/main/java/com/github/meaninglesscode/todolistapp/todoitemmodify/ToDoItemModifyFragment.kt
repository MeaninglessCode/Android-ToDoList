package com.github.meaninglesscode.todolistapp.todoitemmodify

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.meaninglesscode.todolistapp.EventObserver
import com.github.meaninglesscode.todolistapp.R
import com.github.meaninglesscode.todolistapp.databinding.ToDoItemModifyFragBinding
import com.github.meaninglesscode.todolistapp.todolist.ADD_EDIT_RESULT_OK
import com.github.meaninglesscode.todolistapp.util.getDateStringFromMillis
import com.github.meaninglesscode.todolistapp.util.setupRefreshLayout
import com.github.meaninglesscode.todolistapp.util.setupSnackbar
import com.github.meaninglesscode.todolistapp.util.showSnackbar
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment
import java.util.*
import javax.inject.Inject

/**
 * Primary user interface for the modification of tasks. The [ToDoItemModifyFragment] implements
 * [DaggerFragment] for dependency injecting purposes.
 */
class ToDoItemModifyFragment : DaggerFragment() {
    // Data binding object for add_to_do_item_frag.xml
    private lateinit var viewDataBinding: ToDoItemModifyFragBinding

    /** [ToDoItemModifyFragmentArgs] automatically passed via [navArgs] */
    private val args: ToDoItemModifyFragmentArgs by navArgs()

    /**
     * [ViewModelProvider.Factory] provided via dependency injection so that the
     * [ToDoItemModifyViewModel] can be obtained via the factory.
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /**
     * View model produced via [viewModelFactory] to obtain the [ToDoItemModifyViewModel].
     */
    private val viewModel by viewModels<ToDoItemModifyViewModel> { viewModelFactory }

    /**
     * Method overriding [DaggerFragment.onCreateView] to allow for instantiation of data binding
     * and setting up the lifecycle components.
     *
     * @param [inflater] [LayoutInflater] automatically passed into the [onCreateView] method
     * @param [container] [ViewGroup]? automatically passed into the [onCreateView] method
     * @param [savedInstanceState] [Bundle]? automatically passed into the [onCreateView] method
     * @return [View]? The resultant [View] returned after the steps of [onCreateView] are completed
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.to_do_item_modify_frag, container, false)

        viewDataBinding = ToDoItemModifyFragBinding.bind(root).apply {
            this.viewmodel = viewModel
        }

        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        return viewDataBinding.root
    }

    /**
     * Method overriding [DaggerFragment.onActivityCreated] to allow for instantiation of this
     * fragment's [Snackbar] as well as navigation services. Currently, this method utilizes a
     * method marked as deprecated, but I am not going to update it for this assignment.
     *
     * @param [savedInstanceState] [Bundle]? automatically passed into the [onActivityCreated]
     * method
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupSnackbar()
        setupNavigation()
        setupDateTimeButton()
        setupClearDateButton()
        this.setupRefreshLayout(viewDataBinding.refreshLayout)
        viewModel.start(args.id)
    }

    /**
     * Method to initialize a [Snackbar] for use with this fragment.
     */
    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarMessage, Snackbar.LENGTH_SHORT)
    }

    /**
     * Method to [View.setOnClickListener] for the [R.id.add_to_do_item_due_date] [EditText].
     */
    private fun setupDateTimeButton() {
        activity?.findViewById<EditText>(R.id.add_to_do_item_due_date)?.let { editText ->
            /** Set [editText]'s on click listener */
            editText.setOnClickListener { view ->
                /** Cast [view] to an [EditText] type */
                val dateTextView = (view as EditText)
                // Store user inputted time information
                val setTime = Calendar.getInstance()

                // Lambda function for time picker dialog call
                val timePickerLambda = { _: TimePicker, hourOfDay: Int, minute: Int ->
                    setTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    setTime.set(Calendar.MINUTE, minute)
                    setTime.set(Calendar.SECOND, 0)
                    setTime.set(Calendar.MILLISECOND, 0)

                    val currentMillis = Calendar.getInstance().timeInMillis
                    val newMillis = setTime.timeInMillis

                    if ((newMillis - currentMillis) < 0) {
                        requireView().showSnackbar(
                            "A time in the future must be chosen!",
                            Snackbar.LENGTH_SHORT
                        )
                        dateTextView.setText("")
                    }
                    else {
                        dateTextView.setText(getDateStringFromMillis(newMillis))
                        viewModel.dueDate.value = newMillis
                    }
                }

                // Time picker dialog definition. The default time is set to the current time.
                val timePickerDialog = TimePickerDialog(requireContext(),
                    timePickerLambda,
                    setTime.get(Calendar.HOUR_OF_DAY),
                    setTime.get(Calendar.MINUTE),
                    false
                )

                // Lambda function for date picker dialog call
                val datePickerLambda = { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                    setTime.set(Calendar.YEAR, year)
                    setTime.set(Calendar.MONTH, month)
                    setTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    timePickerDialog.show()
                }

                // Date picker dialog definition. The default time is set to the current time.
                val datePickerDialog = DatePickerDialog(requireContext(),
                    datePickerLambda,
                    setTime.get(Calendar.YEAR),
                    setTime.get(Calendar.MONTH),
                    setTime.get(Calendar.DAY_OF_MONTH)
                )

                // Show date picker
                datePickerDialog.show()
            }
        }
    }

    private fun setupClearDateButton() {
        activity?.findViewById<Button>(R.id.clear_date_button)?.let {
            it.setOnClickListener {
                activity?.findViewById<EditText>(R.id.add_to_do_item_due_date)?.setText("")
                viewModel.dueDate.value = -1L
            }
        }
    }

    /**
     * Method to initialize navigation components for AndroidX navigation controls and the
     * directions available through nav_graph.xml.
     */
    private fun setupNavigation() {
        viewModel.toDoItemUpdatedEvent.observe(viewLifecycleOwner, EventObserver {
            val action = ToDoItemModifyFragmentDirections
                .actionToDoItemModifyFragmentToToDoListFragment(ADD_EDIT_RESULT_OK)
            findNavController().navigate(action)
        })
    }
}