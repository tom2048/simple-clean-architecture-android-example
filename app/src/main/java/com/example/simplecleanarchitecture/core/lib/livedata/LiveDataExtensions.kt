package com.example.simplecleanarchitecture.core.lib

import androidx.lifecycle.MutableLiveData


// Versions from the turn based game
fun <T> MutableLiveData<T>.defaultValue(defaultValue: T): MutableLiveData<T> = apply { value = defaultValue }
