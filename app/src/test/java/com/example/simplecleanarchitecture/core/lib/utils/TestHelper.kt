package com.example.simplecleanarchitecture.core.lib

import androidx.lifecycle.*
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlin.reflect.jvm.isAccessible

interface TestHelper {

    val lifecycleOwner: LifecycleOwner
    val lifecycle: LifecycleRegistry

    fun prepareLifecycle()

    fun cleanUpLifecycle()

    fun <T> LiveData<T>.mockObserver(clearInvocations: Boolean = false): Observer<T>

    fun invokeViewModelOnCleared(viewModel: ViewModel)
}

class DefaultTestHelper : TestHelper {

    override lateinit var lifecycleOwner: LifecycleOwner
        private set
    override lateinit var lifecycle: LifecycleRegistry
        private set

    override fun prepareLifecycle() {
        lifecycleOwner = mock()
        lifecycle = LifecycleRegistry(lifecycleOwner).apply {
            handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            handleLifecycleEvent(Lifecycle.Event.ON_START)
            handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            whenever(lifecycleOwner.lifecycle).thenReturn(this)
        }
    }

    override fun cleanUpLifecycle() {
        lifecycle.run {
            handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
    }

    override fun <T> LiveData<T>.mockObserver(clearInvocations: Boolean): Observer<T> =
        mock<Observer<T>>().also {
            this.observe(lifecycleOwner, it)
            if (clearInvocations) {
                clearInvocations(it)
            }
        }

    override fun invokeViewModelOnCleared(viewModel: ViewModel) {
        // Please read for further information: https://stackoverflow.com/questions/54115627/how-to-ensure-viewmodeloncleared-is-called-in-an-android-unit-test
        ViewModel::class.members
            .single { it.name == "onCleared" }
            .apply { isAccessible = true }
            .call(viewModel)
    }
}