package com.example.simplecleanarchitecture.core.lib.schedulers

import com.example.simplecleanarchitecture.core.lib.di.AppSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers

class TestAppSchedulers : AppSchedulers {

    override fun io(): Scheduler = Schedulers.trampoline()

    override fun computation(): Scheduler = Schedulers.trampoline()

    override fun mainThread(): Scheduler = Schedulers.trampoline()

}