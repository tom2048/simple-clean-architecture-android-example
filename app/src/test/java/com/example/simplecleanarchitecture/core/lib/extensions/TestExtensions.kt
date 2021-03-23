package com.example.simplecleanarchitecture.core.lib.extensions

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.internal.createInstance
import org.mockito.Mockito

inline fun <reified T : Any> anyNotNull(): T = any()

inline fun <reified T : String> nullOrEmpty(): T {
    return Mockito.argThat { arg: T? -> arg.isNullOrEmpty() } ?: createInstance(
        T::class
    )
}

inline fun <reified T : String> notNullOrEmpty(): T {
    return Mockito.argThat { arg: T? -> !arg.isNullOrEmpty() } ?: createInstance(
        T::class
    )
}

