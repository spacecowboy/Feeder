package com.nononsenseapps.feeder.base

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.direct
import org.kodein.di.generic.instance
import java.lang.reflect.InvocationTargetException

/**
 * A view model which is also kodein aware. Construct any deriving class by using the getViewModel()
 * extension function.
 */
open class KodeinAwareViewModel(override val kodein: Kodein) : AndroidViewModel(kodein.direct.instance()), KodeinAware

class KodeinAwareViewModelFactory(override val kodein: Kodein)
    : ViewModelProvider.AndroidViewModelFactory(kodein.direct.instance()), KodeinAware {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (KodeinAwareViewModel::class.java.isAssignableFrom(modelClass)) {
            try {
                modelClass.getConstructor(Kodein::class.java).newInstance(kodein)
            } catch (e: NoSuchMethodException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
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

inline fun<reified T: KodeinAwareViewModel> CoroutineScopedKodeinAwareFragment.getViewModel(): T {
    val factory: KodeinAwareViewModelFactory by instance()
    return ViewModelProviders.of(this, factory).get(T::class.java)
}

inline fun<reified T: KodeinAwareViewModel> CoroutineScopedKodeinAwareActivity.getViewModel(): T {
    val factory: KodeinAwareViewModelFactory by instance()
    return ViewModelProviders.of(this, factory).get(T::class.java)
}
