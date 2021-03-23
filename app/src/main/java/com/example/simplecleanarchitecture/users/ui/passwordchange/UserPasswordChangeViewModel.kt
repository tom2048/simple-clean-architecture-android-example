package com.example.simplecleanarchitecture.users.ui.passwordchange

import androidx.lifecycle.*
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.core.lib.const.Patterns
import com.example.simplecleanarchitecture.core.lib.defaultValue
import com.example.simplecleanarchitecture.core.lib.di.AppSchedulers
import com.example.simplecleanarchitecture.core.lib.livedata.LiveEvent
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.users.usecase.user.UserPasswordUpdateUseCase
import com.github.terrakok.cicerone.Back
import com.github.terrakok.cicerone.Command

class UserPasswordChangeViewModel(
    private val passwordUpdateUseCase: UserPasswordUpdateUseCase,
    private val appResources: AppResources,
    private val appSchedulers: AppSchedulers
) : ViewModel() {

    private var userId: String? = null

    val password = MutableLiveData<String>()

    val passwordConfirmed = MutableLiveData<String>()

    val passwordValidation: LiveData<String> = password.map {
        if (!it.isNullOrEmpty() && !Patterns.PASSWORD.matcher(it).matches()) {
            appResources.getStringResource(R.string.password_validation_message)
        } else {
            ""
        }
    }

    val passwordConfirmedValidation: LiveData<String> = MediatorLiveData<String>().apply {
        val observer = Observer<String> {
            this.value = if (!password.value.isNullOrEmpty() && !passwordConfirmed.value.isNullOrEmpty() && !passwordConfirmed.value.equals(password.value)) {
                appResources.getStringResource(R.string.password_confirmation_validation_message)
            } else {
                ""
            }
        }
        addSource(password, observer)
        addSource(passwordConfirmed, observer)
    }

    private val _preloader = MutableLiveData<Boolean>()
    val preloader: LiveData<Boolean> = _preloader

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    val isSubmitEnabled: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        val observer = Observer<Boolean> {
            this.value = passwordValidation.value.isNullOrEmpty() && passwordConfirmedValidation.value.isNullOrEmpty()
                    && !password.value.isNullOrEmpty() && !passwordConfirmed.value.isNullOrEmpty()
        }
        addSource(passwordValidation.map { !it.isNullOrEmpty() }, observer)
        addSource(passwordConfirmedValidation.map { !it.isNullOrEmpty() }, observer)
    }.defaultValue(false).distinctUntilChanged()

    private val _routing = LiveEvent<Command>()
    val routing: LiveData<Command> = _routing

    fun setParams(userId: String) {
        this.userId = userId
    }

    fun submit() {
        userId?.let { userId ->
            passwordUpdateUseCase(userId, password.value ?: "")
                .doOnSubscribe { _preloader.value = true }
                .observeOn(appSchedulers.mainThread())
                .subscribe({
                    _routing.value = Back()
                }, {
                    _preloader.value = false
                    _message.value = appResources.getStringResource(R.string.common_communication_error)
                })
        } ?: run {
            _routing.value = Back()
        }
    }


}