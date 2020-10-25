package com.github.meaninglesscode.todolistapp.todoitemview

import android.os.Bundle
import android.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.meaninglesscode.todolistapp.EventObserver
import com.github.meaninglesscode.todolistapp.R
import com.github.meaninglesscode.todolistapp.databinding.ToDoItemViewFragBinding
import com.github.meaninglesscode.todolistapp.todolist.DELETE_RESULT_OK
import com.github.meaninglesscode.todolistapp.util.setupRefreshLayout
import com.github.meaninglesscode.todolistapp.util.setupSnackbar
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * Primary user interface for the viewing of tasks. The [ToDoItemViewFragment] implements
 * [DaggerFragment] for dependency injection purposes.
 */
class ToDoItemViewFragment : DaggerFragment() {
    // Data binding object for to_do_item_view_frag.xml
    private lateinit var viewDataBinding: ToDoItemViewFragBinding

    /** [ToDoItemViewFragmentArgs] automatically passed by [navArgs] */
    private val args: ToDoItemViewFragmentArgs by navArgs()

    /**
     * [ViewModelProvider.Factory] provided via dependency injection so that the]
     * [ToDoItemViewViewModel] can be obtained via the factory.
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /**
     * View model produced via [viewModelFactory] to obtain the [ToDoItemViewViewModel].
     */
    private val viewModel by viewModels<ToDoItemViewViewModel> { viewModelFactory }

    /**
     * Method overriding [DaggerFragment.onActivityCreated] to allow for instantiation of various
     * additional pieces of this fragment as well as navigation services. Currently, this method
     * utilizes a method marked as deprecated, but I am not going to update it for this assignment.
     *
     * @param [savedInstanceState] [Bundle]? automatically passed into the [onActivityCreated]
     * method
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupFab()
        view?.setupSnackbar(this, viewModel.snackbarMessage, Snackbar.LENGTH_SHORT)
        setupNavigation()
        this.setupRefreshLayout(viewDataBinding.refreshLayout)
    }

    /**
     * Method to initialize navigation components for AndroidX navigation controls and the
     * directions available through nav_graph.xml.
     */
    private fun setupNavigation() {
        viewModel.deleteToDoItemCommand.observe(viewLifecycleOwner, EventObserver {
            val action = ToDoItemViewFragmentDirections
                .actionToDoItemViewFragmentToToDoListFragment(DELETE_RESULT_OK)
            findNavController().navigate(action)
        })
        viewModel.editToDoItemCommand.observe(viewLifecycleOwner, EventObserver {
            val action = ToDoItemViewFragmentDirections
                .actionToDoItemViewFragmentToToDoItemModifyFragment(
                    args.id,
                    resources.getString(R.string.edit_to_do_item)
                )
            findNavController().navigate(action)
        })
    }

    /**
     * Method to initialize the [R.id.fab_edit_to_do_item] button and set it's on click listener
     * event.
     */
    private fun setupFab() {
        activity?.findViewById<View>(R.id.fab_edit_to_do_item)?.setOnClickListener {
            viewModel.editToDoItem()
        }
    }

    /**
     * Method overriding [DaggerFragment.onCreateView] to allow for instantiation of data binding
     * and setting up lifecycle components.
     *
     * @param [inflater] [LayoutInflater] automatically passed into the [onCreateView] method
     * @param [container] [ViewGroup]? automatically passed into the [onCreateView] method
     * @param [savedInstanceState] [Bundle]? automatically passed into the [onCreateView] method
     * @return [View]? The resultant [View] returned after the steps of [onCreateView] are completed
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.to_do_item_view_frag, container, false)
        viewDataBinding = ToDoItemViewFragBinding.bind(view).apply {
            viewmodel = viewModel
        }
        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner

        viewModel.start(args.id)

        setHasOptionsMenu(true)
        return view
    }

    /**
     * Method to override [DaggerFragment.onOptionsItemSelected] to handle menu bar interactions.
     *
     * @param [item] [MenuItem] interacted with by the user and automatically passed into the method
     * @return [Boolean] value returned based on whether or not an action was performed
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> {
                viewModel.deleteToDoItem()
                true
            }
            else -> false
        }
    }

    /**
     * Method to override [DaggerFragment.onCreateOptionsMenu] to handle setting the layout of the
     * menu bar.
     *
     * @param [menu] [Menu] automatically passed into the [onCreateOptionsMenu] method
     * @param [inflater] [MenuInflater] automatically passed into the [onCreateOptionsMenu] method
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.to_do_item_view_menu, menu)
    }
}