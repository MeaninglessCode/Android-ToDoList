package com.github.meaninglesscode.todolistapp.todolist

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.github.meaninglesscode.todolistapp.R

/**
 * Class implementing [AppCompatActivity] representing the entry point activity for this
 * application.
 */
class ToDoListActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    /**
     * Override of [AppCompatActivity.onCreate].
     *
     * @param [savedInstanceState] [Bundle]? automatically passed into the [onCreate] method
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.to_do_list_act)
        setSupportActionBar(findViewById(R.id.toolbar))

        val navController: NavController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration =
            AppBarConfiguration.Builder(R.id.toDoListFragment).build()
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /**
     * Override of [AppCompatActivity.onSupportNavigateUp]
     *
     * @returns [Boolean]
     */
    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}

// Navigation keys
const val ADD_EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val DELETE_RESULT_OK = Activity.RESULT_FIRST_USER + 2
const val EDIT_RESULT_OK = Activity.RESULT_FIRST_USER + 3