package com.github.meaninglesscode.todolistapp.di

import androidx.lifecycle.ViewModel
import com.github.meaninglesscode.todolistapp.todoitemmodify.ToDoItemModifyFragment
import com.github.meaninglesscode.todolistapp.todoitemmodify.ToDoItemModifyViewModel
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

/**
 * Dagger [Module] used for injection purposes with the to do item modify section of the UI.
 */
@Module
abstract class ToDoItemModifyModule {
    /**
     * [ContributesAndroidInjector] method to generate an [AndroidInjector] implemented within the
     * [ViewModelBuilder] subclass component.
     *
     * @return [ToDoItemModifyFragment] the resultant fragment
     */
    @ContributesAndroidInjector(modules = [ViewModelBuilder::class])
    internal abstract fun toDoItemModifyFragment(): ToDoItemModifyFragment

    /**
     * [Binds] the [ToDoItemModifyViewModel] [IntoMap] for dagger injection with the [ViewModelKey]
     * being [ToDoItemModifyViewModel]::class
     *
     * @return [ViewModel] the bound [ViewModel]
     */
    @Binds
    @IntoMap
    @ViewModelKey(ToDoItemModifyViewModel::class)
    internal abstract fun bindViewModel(viewModel: ToDoItemModifyViewModel): ViewModel
}