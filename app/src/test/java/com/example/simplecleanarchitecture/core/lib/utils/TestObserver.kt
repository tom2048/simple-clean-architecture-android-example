package com.example.simplecleanarchitecture.core.lib.utils

class TestObserver<T> {

    fun onEach(item: T) {}

    fun onError(throwable: Throwable) {}

    fun onCompletion() {}

}