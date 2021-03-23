package com.example.simplecleanarchitecture.users.ui.useredit

import androidx.lifecycle.*
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.RouterScreen
import com.example.simplecleanarchitecture.core.lib.const.Patterns
import com.example.simplecleanarchitecture.core.lib.defaultValue
import com.example.simplecleanarchitecture.core.lib.di.AppSchedulers
import com.example.simplecleanarchitecture.core.lib.exception.ValidationException
import com.example.simplecleanarchitecture.core.lib.livedata.LiveEvent
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.core.model.User
import com.example.simplecleanarchitecture.users.usecase.user.UserShowDetailsUseCase
import com.example.simplecleanarchitecture.users.usecase.user.UserUpdateUseCase
import com.github.terrakok.cicerone.Back
import com.github.terrakok.cicerone.Command
import com.github.terrakok.cicerone.Forward
import io.reactivex.rxjava3.disposables.CompositeDisposable

class UserEditViewModel(
    private val userShowDetailsUseCase: UserShowDetailsUseCase,
    private val userUpdateUseCase: UserUpdateUseCase,
    private val appResources: AppResources,
    private val appSchedulers: AppSchedulers
) : ViewModel() {

    private val disposables = CompositeDisposable()

    private var userId: String? = null
        set(value) {
            field = value
            _header.value = if (value.isNullOrEmpty()) {
                appResources.getStringResource(R.string.user_edit_header)
            } else {
                appResources.getStringResource(R.string.user_add_header)
            }
        }

    private val _header = MutableLiveData<String>()
    val header: LiveData<String> = _header

    val nickname = MutableLiveData<String>().defaultValue("")

    val email = MutableLiveData<String>().defaultValue("")

    val description = MutableLiveData<String>().defaultValue("")

    val nicknameValidationError: LiveData<String> =
        nickname.map {
            if (!it.isNullOrEmpty() && it.length > 10) {
                appResources.getStringResource(R.string.nickname_validation_message)
            } else {
                ""
            }
        }

    val emailValidationError: LiveData<String> =
        email.map {
            if (!it.isNullOrEmpty() && !Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                appResources.getStringResource(R.string.email_validation_message)
            } else {
                ""
            }
        }

    val descriptionValidationError: LiveData<String> =
        description.map {
            if (!it.isNullOrEmpty() && !Patterns.ALPHANUMERIC.matcher(it).matches()) {
                appResources.getStringResource(R.string.description_validation_message)
            } else {
                ""
            }
        }

    val avatar = MutableLiveData<ByteArray>()

    val isSubmitEnabled: LiveData<Boolean> =
        MediatorLiveData<Boolean>().apply {
            addSource(nickname.map { it.isNullOrEmpty() }) { nicknameEmpty ->
                this.value =
                    !nicknameEmpty && !email.value.isNullOrEmpty() && !description.value.isNullOrEmpty() && nicknameValidationError.value.isNullOrEmpty()
                            && emailValidationError.value.isNullOrEmpty() && descriptionValidationError.value.isNullOrEmpty()
            }
            addSource(email.map { it.isNullOrEmpty() }) { emailEmpty ->
                this.value =
                    !emailEmpty && !nickname.value.isNullOrEmpty() && !description.value.isNullOrEmpty() && nicknameValidationError.value.isNullOrEmpty()
                            && emailValidationError.value.isNullOrEmpty() && descriptionValidationError.value.isNullOrEmpty()
            }
            addSource(description.map { it.isNullOrEmpty() }) { descriptionEmpty ->
                this.value =
                    !descriptionEmpty && !nickname.value.isNullOrEmpty() && !email.value.isNullOrEmpty() && nicknameValidationError.value.isNullOrEmpty()
                            && emailValidationError.value.isNullOrEmpty() && descriptionValidationError.value.isNullOrEmpty()
            }
        }

    private val _preloader = MutableLiveData<Boolean>().defaultValue(false)
    val preloader: LiveData<Boolean> = _preloader

    private val _errorMessage = LiveEvent<String>().defaultValue("")
    val errorMessage: LiveData<String> = _errorMessage

    private val _screenRouting = LiveEvent<Command>()
    val screenRouting: LiveData<Command> = _screenRouting

    fun setParams(userId: String?) {
        this.userId = userId
    }

    fun loadDetails() {
        userId?.let { userId ->
            disposables.add(userShowDetailsUseCase(userId)
                .doOnSubscribe { _preloader.value = true }
                .observeOn(appSchedulers.mainThread())
                .subscribe({ user ->
                    nickname.value = user.nickname
                    email.value = user.email
                    description.value = user.description
                    avatar.value = user.photo
                    _preloader.value = false
                }, {
                    _preloader.value = false
                    _screenRouting.value = Back()
                })
            )
        }
    }

    fun submit() {
        disposables.add(
            userUpdateUseCase(User(userId, nickname.value ?: "", email.value ?: "", description.value ?: "", photo = avatar.value))
                .doOnSubscribe { _preloader.value = true }
                .observeOn(appSchedulers.mainThread())
                .subscribe({
                    _screenRouting.value = Back()
                }, {
                    _preloader.value = false
                    if (it is ValidationException) {
                        _errorMessage.value = it.validationMessages.map { it.second }.joinToString(separator = "\n")
                    } else {
                        _errorMessage.value = appResources.getStringResource(R.string.common_communication_error)
                    }
                })
        )
    }

    fun cancel() {
        _screenRouting.value = Back()
    }

}