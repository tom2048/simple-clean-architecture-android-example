package com.example.simplecleanarchitecture.core.lib.utils

import kotlinx.coroutines.Job

class FlowObserver<T> {

    private var job: Job? = null

    fun onEach(item: T) {}

    fun onError(throwable: Throwable) {}

    fun onCompletion() {}

    fun setup(job: Job) {
        this.job = job
    }

    fun cancel() {
        job!!.cancel()
    }
}