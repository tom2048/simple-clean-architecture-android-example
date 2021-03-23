package com.example.simplecleanarchitecture.core.lib.di

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers

interface AppSchedulers {
    fun io(): Scheduler
    fun computation(): Scheduler
    fun mainThread(): Scheduler
}

class AppSchedulersDefault: AppSchedulers {
    override fun io() = Schedulers.io()

    override fun computation() = Schedulers.computation()

    override fun mainThread() = AndroidSchedulers.mainThread()
}