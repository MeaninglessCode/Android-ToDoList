package com.github.meaninglesscode.todolistapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.MapKey
import dagger.Module
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

/**
 * [TodoViewModelFactory] utilizing Dagger to create the instances. Implements
 * [ViewModelProvider.Factory].
 *
 * @param [creators] [JvmSuppressWildcards] [Map] of [ViewModel] classes to their [Provider]
 */
class TodoViewModelFactory @Inject constructor(
    private val creators: @JvmSuppressWildcards Map<Class<out ViewModel>, Provider<ViewModel>>
) : ViewModelProvider.Factory {
    /**
     * Override method to create a new [ViewModel]. If there is no [Provider] for the desired
     * [ViewModel], then an [IllegalArgumentException] is thrown.
     *
     * @return [T] [ViewModel] type to return from the associated [Provider]
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        var creator: Provider<out ViewModel>? = creators[modelClass]

        if (creator == null) {
            for ((key, value) in creators) {
                if (modelClass.isAssignableFrom(key)) {
                    creator = value
                    break
                }
            }
        }

        if (creator == null)
            throw IllegalArgumentException("Unknown model class: $modelClass")

        try {
            @Suppress("UNCHECKED_CAST")
            return creator.get() as T
        }
        catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}

/**
 * Dagger [Module] for binding a [TodoViewModelFactory].
 */
@Module
internal abstract class ViewModelBuilder {
    /**
     * [Binds] method to bind a [TodoViewModelFactory] for Dagger injection.
     *
     * @param [factory] [TodoViewModelFactory] to bind
     * @return [ViewModelProvider.Factory] bound [TodoViewModelFactory] to return
     */
    @Binds
    internal abstract fun bindViewModelFactory(
        factory: TodoViewModelFactory
    ): ViewModelProvider.Factory
}

/**
 * [Target] annotation class used as [MapKey] for accessing [ViewModel]s. Additionally, this class
 * is annotated with [AnnotationRetention.RUNTIME].
 *
 * @param [value] [KClass] to get [ViewModel] for
 */
@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)