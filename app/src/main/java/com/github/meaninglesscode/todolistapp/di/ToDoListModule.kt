package com.github.meaninglesscode.todolistapp.di

import androidx.lifecycle.ViewModel
import com.github.meaninglesscode.todolistapp.todolist.ToDoListFragment
import com.github.meaninglesscode.todolistapp.todolist.ToDoListViewModel
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

/**
 * Dagger [Module] used for injection purposes with the to do list section of the UI.
 */
@Module
abstract class ToDoListModule {
    /**
     * [ContributesAndroidInjector] method to generate an [AndroidInjector] implemented within the
     * [ViewModelBuilder] subclass component.
     *
     * @return [ToDoListFragment] the resultant fragment
     */
    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun toDoListFragment(): ToDoListFragment

    /**
     * [Binds] the [ToDoListViewModel] [IntoMap] for dagger injection with the [ViewModelKey]
     * being [ToDoListViewModel]::class
     *
     * @return [ViewModel] the bound [ViewModel]
     */
    @Binds
    @IntoMap
    @ViewModelKey(ToDoListViewModel::class)
    abstract fun bindViewModel(viewModel: ToDoListViewModel): ViewModel
}