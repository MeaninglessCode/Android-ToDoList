package com.github.meaninglesscode.todolistapp.todolist

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.meaninglesscode.todolistapp.EventObserver
import com.github.meaninglesscode.todolistapp.R
import com.github.meaninglesscode.todolistapp.databinding.ToDoListFragBinding
import com.github.meaninglesscode.todolistapp.util.setupRefreshLayout
import com.github.meaninglesscode.todolistapp.util.setupSnackbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.DaggerFragment
import timber.log.Timber
import javax.inject.Inject

/**
 * Primary user interface for viewing and interacting with tasks. The [ToDoListFragment] implements
 * [DaggerFragment] for dependency injection purposes.
 */
class ToDoListFragment : DaggerFragment() {
    /**
     * [ViewModelProvider.Factory] provided via dependency injection so that the [ToDoListViewModel]
     * can be obtained via the factory.
     */
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    /**
     * View model produced via [viewModelFactory] to obtain the [ToDoListViewModel].
     */
    private val viewModel by viewModels<ToDoListViewModel> { viewModelFactory }

    /** [ToDoListFragmentArgs] automatically passed via [navArgs] */
    private val args: ToDoListFragmentArgs by navArgs()

    // Data binding object for to_do_list_frag.xml
    private lateinit var viewDataBinding: ToDoListFragBinding

    // List adapter for display of to do items
    private lateinit var listAdapter: ToDoListAdapter

    /**
     * Method overriding [DaggerFragment.onCreateView] to allow for instantiation of data binding
     * and setting up additional fragment components.
     *
     * @param [inflater] [LayoutInflater] automatically passed into the [onCreateView] method
     * @param [container] [ViewGroup]? automatically passed into the [onCreateView] method
     * @param [savedInstanceState] [Bundle]? automatically passed into the [onCreateView] method
     * @return [View]?
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = ToDoListFragBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        setHasOptionsMenu(true)
        return viewDataBinding.root
    }

    /**
     * Method overriding [DaggerFragment.onOptionsItemSelected] to handle menu bar interactions.
     *
     * @param [item] [MenuItem] interacted with by the user and automatically passed into the method
     * @return [Boolean] value returned based on whether or not an action was performed
     */
    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.menu_clear -> {
                viewModel.clearCompletedToDoItems()
                true
            }
            R.id.menu_filter -> {
                showFilteringPopUpMenu()
                true
            }
            R.id.menu_refresh -> {
                viewModel.loadToDoItems()
                true
            }
            else -> false
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.to_do_list_fragment_menu, menu)
    }

    /**
     * Method overriding [DaggerFragment.onActivityCreated] to allow for instantiation of lifecycle
     * components as well as various other fragment components. Currently, this method utilizes a
     * method marked as deprecated, but I will not be updating it for this assignment.
     *
     * @param [savedInstanceState] [Bundle]? automatically passed into the [onActivityCreated]
     * method
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewDataBinding.lifecycleOwner = this.viewLifecycleOwner
        setupSnackbar()
        setupListAdapter()
        setupRefreshLayout(viewDataBinding.refreshLayout, viewDataBinding.toDoItemsList)
        setupNavigation()
        setupFab()

        // In a real app, this should only be done on the first load or when navigating back to this
        // location. However, for the purposes of this app, data is always reloaded for simplicity.
        viewModel.loadToDoItems()
    }

    /**
     * Method to initialize navigation components for AndroidX navigation controls and the
     * directions available through nav_graph.xml.
     */
    private fun setupNavigation() {
        viewModel.openToDoItemEvent.observe(viewLifecycleOwner, EventObserver {
            openToDoItemDetails(it)
        })
        viewModel.newToDoItemEvent.observe(viewLifecycleOwner, EventObserver {
            navigateToAddNewTask()
        })
    }

    /**
     * Method to initialize a [Snackbar] for use with this fragment.
     */
    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarMessage, Snackbar.LENGTH_SHORT)
        arguments?.let {
            viewModel.showEditResultMessage(args.userMessage)
        }
    }

    /**
     * Method to handle display of the menu that allows for filtering of displayed to do items
     * based on the filtering types available in [ToDoListFilterType].
     */
    private fun showFilteringPopUpMenu() {
        val view = activity?.findViewById<View>(R.id.menu_filter) ?: return
        PopupMenu(requireContext(), view).run {
            menuInflater.inflate(R.menu.filter_to_do_items, menu)

            setOnMenuItemClickListener {
                viewModel.setFiltering(
                    when (it.itemId) {
                        R.id.active -> ToDoListFilterType.INCOMPLETE_ITEMS
                        R.id.completed -> ToDoListFilterType.COMPLETE_ITEMS
                        R.id.has_due_date -> ToDoListFilterType.ITEMS_WITH_DUE_DATES
                        R.id.past_due -> ToDoListFilterType.ITEMS_PAST_DUE
                        R.id.due_today -> ToDoListFilterType.ITEMS_DUE_TODAY
                        R.id.due_this_week -> ToDoListFilterType.ITEMS_DUE_THIS_WEEK
                        else -> ToDoListFilterType.ALL_ITEMS
                    }
                )
                viewModel.loadToDoItems()
                true
            }
            show()
        }
    }

    /**
     * Method to initialize the [R.id.fab_add_to_do_item] button and set it's on click listener
     * event.
     */
    private fun setupFab() {
        activity?.findViewById<FloatingActionButton>(R.id.fab_add_to_do_item)?.let {
            it.setOnClickListener {
                navigateToAddNewTask()
            }
        }
    }

    /**
     * Method to navigate to the [ToDoItemModifyFragment] based on the directions available in the
     * nav controller available from [findNavController].
     */
    private fun navigateToAddNewTask() {
        val action = ToDoListFragmentDirections
            .actionToDoListFragmentToToDoItemModifyFragment(
                null,
                resources.getString(R.string.add_to_do_item)
            )
        findNavController().navigate(action)
    }

    /**
     * Method to navigate to [ToDoItemViewFragment] based on the directions available in the nav
     * controller available from [findNavController].
     */
    private fun openToDoItemDetails(id: String) {
        val action = ToDoListFragmentDirections.actionToDoListFragmentToToDoItemViewFragment(id)
        findNavController().navigate(action)
    }

    /**
     * Method to use data binding in conjunction with [ToDoListAdapter] to initialize the to do item
     * list.
     */
    private fun setupListAdapter() {
        val viewModel = viewDataBinding.viewmodel

        when {
            viewModel != null -> {
                listAdapter = ToDoListAdapter(viewModel)
                viewDataBinding.toDoItemsList.adapter = listAdapter
            }
            else -> Timber.w("ViewModel not initialized when attempting to set up adapter.")
        }
    }
}