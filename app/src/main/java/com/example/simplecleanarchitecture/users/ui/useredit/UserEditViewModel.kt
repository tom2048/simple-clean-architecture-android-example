package com.example.simplecleanarchitecture.users.ui.useredit

import androidx.lifecycle.*
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.core.lib.const.Patterns
import com.example.simplecleanarchitecture.core.lib.defaultValue
import com.example.simplecleanarchitecture.core.lib.di.AppSchedulers
import com.example.simplecleanarchitecture.core.lib.exception.ValidationException
import com.example.simplecleanarchitecture.core.lib.livedata.LiveEvent
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.core.model.User
import com.example.simplecleanarchitecture.users.usecase.user.UserAddAttachmentUseCase
import com.example.simplecleanarchitecture.users.usecase.user.UserAddAttachmentUseCaseDefault.Type.Avatar
import com.example.simplecleanarchitecture.users.usecase.user.UserAddAttachmentUseCaseDefault.Type.IdScan
import com.example.simplecleanarchitecture.users.usecase.user.UserGetAttachmentUseCase
import com.example.simplecleanarchitecture.users.usecase.user.UserShowDetailsUseCase
import com.example.simplecleanarchitecture.users.usecase.user.UserUpdateUseCase
import com.github.terrakok.cicerone.Back
import com.github.terrakok.cicerone.Command
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable

class UserEditViewModel(
    private val userId: String,
    private val state: SavedStateHandle,
    private val userShowDetailsUseCase: UserShowDetailsUseCase,
    private val userAddAttachmentUseCase: UserAddAttachmentUseCase,
    private val userGetAttachmentUseCase: UserGetAttachmentUseCase,
    private val userUpdateUseCase: UserUpdateUseCase,
    private val appResources: AppResources,
    private val appSchedulers: AppSchedulers
) : ViewModel() {

    private val disposables = CompositeDisposable()

    private val isInitialized: MutableLiveData<Boolean> =
        state.getLiveData(STATE_IS_INITIALIZED, false)

    val header: LiveData<String> = MutableLiveData(
        if (!userId.isNullOrEmpty()) {
            appResources.getStringResource(R.string.user_edit_header)
        } else {
            appResources.getStringResource(R.string.user_add_header)
        }
    )

    val nickname: MutableLiveData<String> = state.getLiveData(STATE_NICKNAME, "")

    val email: MutableLiveData<String> = state.getLiveData(STATE_EMAIL, "")

    val description: MutableLiveData<String> = state.getLiveData(STATE_DESCRIPTION, "")

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

    val avatar = MutableLiveData<ByteArray?>()

    private val avatarNewAssetKey: MutableLiveData<String> = state.getLiveData(STATE_AVATAR)

    val idScan = MutableLiveData<ByteArray?>()

    private val idScanNewAssetKey: MutableLiveData<String> = state.getLiveData(STATE_ID_SCAN)

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

    fun loadDetails() {
        if (isInitialized.value == false) {
            userId.takeIf { it.isNotEmpty() }?.let { userId ->
                disposables.add(userShowDetailsUseCase(userId)
                    .doOnSubscribe { _preloader.value = true }
                    .observeOn(appSchedulers.mainThread())
                    .subscribe({ user ->
                        isInitialized.value = true
                        nickname.value = user.nickname
                        email.value = user.email
                        description.value = user.description
                        avatar.value = user.photo
                        idScan.value = user.idScan
                        _preloader.value = false
                    }, {
                        _preloader.value = false
                        _screenRouting.value = Back()
                    })
                )
            }
        } else {
            Single.zip(
                avatarNewAssetKey.value
                    ?.takeIf { !it.isNullOrEmpty() }
                    ?.let { userGetAttachmentUseCase(it) }
                    ?: Single.just(byteArrayOf()),
                idScanNewAssetKey.value
                    ?.takeIf { !it.isNullOrEmpty() }
                    ?.let { userGetAttachmentUseCase(it) }
                    ?: Single.just(byteArrayOf())
            ) { avatar, idScan ->
                Pair(
                    avatar.takeIf { it.isNotEmpty() },
                    idScan.takeIf { it.isNotEmpty() }
                )
            }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.first?.let { avatar.value = it }
                    it.second?.let { idScan.value = it }
                }, {
                    // Nothing for now
                })
        }
    }

    fun addAvatar(url: String) {
        disposables.add(userAddAttachmentUseCase(userId, url, Avatar)
            .flatMap { key -> userGetAttachmentUseCase(key).map { key to it } }
            .observeOn(appSchedulers.mainThread())
            .subscribe({
                avatarNewAssetKey.value = it.first
                avatar.value = it.second
            }, {
                it.printStackTrace()
                // Nothing for now
            })
        )
    }

    fun addIdScan(url: String) {
        disposables.add(userAddAttachmentUseCase(userId, url, IdScan)
            .flatMap { key -> userGetAttachmentUseCase(key).map { key to it } }
            .observeOn(appSchedulers.mainThread())
            .subscribe({
                idScanNewAssetKey.value = it.first
                idScan.value = it.second
            }, {
                // Nothing for now
            })
        )
    }

    fun submit() {
        disposables.add(
            userUpdateUseCase(
                User(
                    userId,
                    nickname.value ?: "",
                    email.value ?: "",
                    description.value ?: "",
                    photo = avatar.value,
                    idScan = idScan.value
                )
            )
                .doOnSubscribe { _preloader.value = true }
                .observeOn(appSchedulers.mainThread())
                .subscribe({
                    _screenRouting.value = Back()
                }, {
                    _preloader.value = false
                    if (it is ValidationException) {
                        _errorMessage.value =
                            it.validationMessages.map { it.second }.joinToString(separator = "\n")
                    } else {
                        _errorMessage.value =
                            appResources.getStringResource(R.string.common_communication_error)
                    }
                })
        )
    }

    fun cancel() {
        _screenRouting.value = Back()
    }

    companion object {
        private val STATE_IS_INITIALIZED = "STATE_IS_INITIALIZED"
        private val STATE_NICKNAME = "STATE_NICKNAME"
        private val STATE_EMAIL = "STATE_EMAIL"
        private val STATE_DESCRIPTION = "STATE_DESCRIPTION"
        private val STATE_AVATAR = "STATE_AVATAR"
        private val STATE_ID_SCAN = "STATE_ID_SCAN"
    }
}