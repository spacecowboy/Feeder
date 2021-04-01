package com.nononsenseapps.feeder.base

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bind
import org.kodein.di.compose.LocalDI
import org.kodein.di.compose.instance
import org.kodein.di.direct
import org.kodein.di.factory
import org.kodein.di.instance
import org.kodein.di.provider
import java.lang.reflect.InvocationTargetException

/**
 * A view model which is also kodein aware. Construct any deriving class by using the getViewModel()
 * extension function.
 */
abstract class DIAwareViewModel(override val di: DI) :
    AndroidViewModel(di.direct.instance()), DIAware

class DIAwareViewModelFactory(
    override val di: DI
) : ViewModelProvider.AndroidViewModelFactory(di.direct.instance()), DIAware {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (DIAwareViewModel::class.java.isAssignableFrom(modelClass)) {
            try {
                modelClass.getConstructor(DI::class.java).newInstance(di)
            } catch (e: NoSuchMethodException) {
                throw RuntimeException("No such constructor $modelClass", e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            } catch (e: InstantiationException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            } catch (e: InvocationTargetException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            }
        } else {
            super.create(modelClass)
        }
    }
}

inline fun <reified T : DIAwareViewModel> DI.Builder.bindWithDIAwareViewModelFactory() {
    bind<T>() with activityViewModelProvider()
    bind<T>() with factory { fragment: Fragment ->
        ViewModelProvider(fragment, instance<DIAwareViewModelFactory>()).get(T::class.java)
    }
}

inline fun <reified T : DIAwareViewModel> DI.Builder.activityViewModelProvider() = provider {
    ViewModelProvider(
        instance<ComponentActivity>(),
        instance<DIAwareViewModelFactory>()
    ).get(T::class.java)
}

@Composable
inline fun <reified T : DIAwareViewModel> DIAwareViewModel(
    key: String? = null
): T {
    val factory: DIAwareViewModelFactory = LocalDI.current.direct.instance()

    return viewModel(T::class.java, key, factory)
}
