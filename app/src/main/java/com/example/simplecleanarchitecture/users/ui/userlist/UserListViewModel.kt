package com.example.simplecleanarchitecture.users.ui.userlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.RouterScreen
import com.example.simplecleanarchitecture.core.lib.di.AppSchedulers
import com.example.simplecleanarchitecture.core.lib.livedata.LiveEvent
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.core.repository.model.UserDetails
import com.example.simplecleanarchitecture.users.usecase.user.UserDeleteUseCase
import com.example.simplecleanarchitecture.users.usecase.user.UserShowListUseCase
import com.github.terrakok.cicerone.Command
import com.github.terrakok.cicerone.Forward
import io.reactivex.rxjava3.disposables.CompositeDisposable

class UserListViewModel(
    private val showListUseCase: UserShowListUseCase,
    private val userDeleteUseCase: UserDeleteUseCase,
    private val appResources: AppResources,
    private val appSchedulers: AppSchedulers
) : ViewModel() {

    private val disposables = CompositeDisposable()

    private val _userList = MutableLiveData<List<UserListItem>>()
    val userList: LiveData<List<UserListItem>> = _userList

    private val _preloader = MutableLiveData<Boolean>()
    val preloader: LiveData<Boolean> = _preloader

    private val _message = LiveEvent<String>()
    val message: LiveData<String> = _message

    private val _actionConfirmation = LiveEvent<String>()
    val userActionConfirmation: LiveData<String> = _actionConfirmation

    private val _routing = LiveEvent<Command>()
    val routing: LiveData<Command> = _routing

    fun loadUsers() {
        disposables.add(
            showListUseCase()
                .doOnSubscribe { _preloader.value = true }
                .observeOn(appSchedulers.mainThread())
                .subscribe({
                    _preloader.value = false
                    _userList.value = prepareUserItems(it)
                }, {
                    _preloader.value = false
                    _message.value = appResources.getStringResource(R.string.common_communication_error)
                })
        )
    }

    fun editUser(id: String) {
        _routing.value = Forward(RouterScreen.UserEditScreen(id), true)
    }

    fun addNewUser() {
        _routing.value = Forward(RouterScreen.UserEditScreen(null), true)
    }

    fun deleteUser(id: String) {
        _actionConfirmation.value = id
    }

    fun changeUserPassword(id: String) {
        _routing.value = Forward(RouterScreen.UserPasswordChangeScreen(id), true)
    }

    fun deleteUserConfirmed(id: String) {
        disposables.add(
            userDeleteUseCase(id)
                .andThen(showListUseCase())
                .doOnSubscribe { _preloader.value = true }
                .observeOn(appSchedulers.mainThread())
                .subscribe({
                    _preloader.value = false
                    _userList.value = prepareUserItems(it)
                    _message.value = appResources.getStringResource(R.string.user_delete_success_message)
                }, {
                    _preloader.value = false
                    _message.value = appResources.getStringResource(R.string.common_communication_error)
                })
        )
    }

    private fun prepareUserItems(users: List<UserDetails>): List<UserListItem> = users.map {
        UserListItem(it,
            { id -> editUser(id) },
            { id -> deleteUser(id) },
            { id -> changeUserPassword(id) }
        )
    }
}