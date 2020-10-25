package com.github.meaninglesscode.todolistapp.di

import androidx.lifecycle.ViewModel
import com.github.meaninglesscode.todolistapp.todoitemview.ToDoItemViewFragment
import com.github.meaninglesscode.todolistapp.todoitemview.ToDoItemViewViewModel
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

/**
 * Dagger [Module] used for injection purposes with the to do item view section of the UI.
 */
@Module
abstract class ToDoItemViewModule {
    /**
     * [ContributesAndroidInjector] method to generate an [AndroidInjector] implemented within the
     * [ViewModelBuilder] subclass component.
     *
     * @return [ToDoItemViewFragment] the resultant fragment
     */
    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun toDoItemViewFragment(): ToDoItemViewFragment

    /**
     * [Binds] the [ToDoItemViewViewModel] [IntoMap] for dagger injection with the [ViewModelKey]
     * being [ToDoItemViewViewModel]::class
     *
     * @return [ViewModel] the bound [ViewModel]
     */
    @Binds
    @IntoMap
    @ViewModelKey(ToDoItemViewViewModel::class)
    abstract fun bindViewModel(viewModel: ToDoItemViewViewModel): ViewModel
}