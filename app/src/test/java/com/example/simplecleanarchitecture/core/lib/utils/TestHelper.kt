package com.example.simplecleanarchitecture.core.lib

import androidx.lifecycle.*
import com.example.simplecleanarchitecture.core.lib.utils.FlowObserver
import com.example.simplecleanarchitecture.core.lib.utils.TestObserver
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.mockito.Mockito
import org.mockito.Mockito.spy
import kotlin.reflect.jvm.isAccessible

interface TestHelper {

    val lifecycleOwner: LifecycleOwner
    val lifecycle: LifecycleRegistry

    fun prepareLifecycle()

    fun cleanUpLifecycle()

    fun <T> LiveData<T>.mockObserver(clearInvocations: Boolean = false): Observer<T>

    fun <T> Flow<T>.mockObserver(coroutineScope: CoroutineScope, clearInvocations: Boolean = false): FlowObserver<T>

    fun <T> Flow<T>.test(coroutineScope: CoroutineScope): TestObserver<T>

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

    override fun <T> Flow<T>.mockObserver(
        coroutineScope: CoroutineScope,
        clearInvocations: Boolean
    ): FlowObserver<T> {
        val observer = spy(FlowObserver<T>())
        val job = coroutineScope.launch {
            this@mockObserver
                .catch { observer.onError(it) }
                .onEach { observer.onEach(it) }
                .onCompletion { observer.onCompletion() }
                .collect()
        }
        observer.setup(job)
        if (clearInvocations) {
            clearInvocations(observer)
        }
        return observer
    }

    override fun <T> Flow<T>.test(coroutineScope: CoroutineScope): TestObserver<T> {
        val observer = spy(TestObserver<T>())
        coroutineScope.launch {
            this@test
                .catch { observer.onError(it) }
                .onEach { observer.onEach(it) }
                .onCompletion { observer.onCompletion() }
                .collect()
        }
        return observer
    }

    override fun invokeViewModelOnCleared(viewModel: ViewModel) {
        // Please read for further information: https://stackoverflow.com/questions/54115627/how-to-ensure-viewmodeloncleared-is-called-in-an-android-unit-test
        ViewModel::class.members
            .single { it.name == "onCleared" }
            .apply { isAccessible = true }
            .call(viewModel)
    }
}