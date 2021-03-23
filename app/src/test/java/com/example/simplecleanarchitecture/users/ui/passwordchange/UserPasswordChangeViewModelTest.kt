package com.example.simplecleanarchitecture.users.ui.passwordchange

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.core.lib.DefaultTestHelper
import com.example.simplecleanarchitecture.core.lib.TestException
import com.example.simplecleanarchitecture.core.lib.TestHelper
import com.example.simplecleanarchitecture.core.lib.extensions.notNullOrEmpty
import com.example.simplecleanarchitecture.core.lib.extensions.nullOrEmpty
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.core.lib.schedulers.TestAppSchedulers
import com.example.simplecleanarchitecture.users.usecase.user.UserPasswordUpdateUseCase
import com.github.terrakok.cicerone.Back
import com.github.terrakok.cicerone.Command
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.rxjava3.core.Completable
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

class UserPasswordChangeViewModelTest : TestHelper by DefaultTestHelper() {

    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: UserPasswordChangeViewModel
    private lateinit var appResources: AppResources
    private lateinit var passwordUpdateUseCase: UserPasswordUpdateUseCase

    private lateinit var passwordObserver: Observer<String>
    private lateinit var passwordConfirmedObserver: Observer<String>
    private lateinit var passwordValidationObserver: Observer<String>
    private lateinit var passwordConfirmedValidationObserver: Observer<String>
    private lateinit var isSubmitEnabledObserver: Observer<Boolean>
    private lateinit var preloaderObserver: Observer<Boolean>
    private lateinit var messageObserver: Observer<String>
    private lateinit var routingObserver: Observer<Command>

    @Before
    fun setUp() {
        setupViewModel()
        setupObservers()
    }

    @After
    fun tearDown() {
        cleanupObservers()
        invokeViewModelOnCleared(viewModel)
    }


    @Test
    fun `password update hides validation message when password is valid`() {
        viewModel.password.value = VALID_PASSWORD

        verify(passwordValidationObserver, only()).onChanged(nullOrEmpty())
    }

    @Test
    fun `password update shows validation message when password is invalid`() {
        viewModel.password.value = INVALID_PASSWORD

        verify(passwordValidationObserver, only()).onChanged(notNullOrEmpty())
    }

    @Test
    fun `password update shows validation message when confirmPassword differs`() {
        viewModel.passwordConfirmed.value = VALID_PASSWORD
        clearInvocations(passwordConfirmedValidationObserver)

        viewModel.password.value = VALID_PASSWORD + "test"

        verify(passwordConfirmedValidationObserver, only()).onChanged(notNullOrEmpty())
    }

    @Test
    fun `password update hides validation message when confirmPassword equals`() {
        viewModel.passwordConfirmed.value = VALID_PASSWORD
        clearInvocations(passwordConfirmedValidationObserver)

        viewModel.password.value = VALID_PASSWORD

        verify(passwordConfirmedValidationObserver, only()).onChanged(nullOrEmpty())
    }

    @Test
    fun `passwordConfirmed update shows validation message when password differs`() {
        viewModel.password.value = VALID_PASSWORD
        clearInvocations(passwordConfirmedValidationObserver)

        viewModel.passwordConfirmed.value = VALID_PASSWORD + "test"

        verify(passwordConfirmedValidationObserver, only()).onChanged(notNullOrEmpty())
    }

    @Test
    fun `passwordConfirmed update hides validation message when confirmPassword equals`() {
        viewModel.password.value = VALID_PASSWORD
        clearInvocations(passwordConfirmedValidationObserver)

        viewModel.passwordConfirmed.value = VALID_PASSWORD

        verify(passwordConfirmedValidationObserver, only()).onChanged(nullOrEmpty())
    }

    @Test
    fun `password update enables submit button when both password are correct`() {
        viewModel.passwordConfirmed.value = VALID_PASSWORD
        clearInvocations(isSubmitEnabledObserver)

        viewModel.password.value = VALID_PASSWORD

        verify(isSubmitEnabledObserver, only()).onChanged(true)
    }

    @Test
    fun `password update disables submit button when password or password confirmed is not correct`() {
        viewModel.password.value = VALID_PASSWORD
        viewModel.passwordConfirmed.value = VALID_PASSWORD
        clearInvocations(isSubmitEnabledObserver)

        viewModel.password.value = INVALID_PASSWORD

        verify(isSubmitEnabledObserver, only()).onChanged(false)
    }

    @Test
    fun `passwordConfirm update enables submit button when both passwords are correct`() {
        viewModel.password.value = VALID_PASSWORD
        clearInvocations(isSubmitEnabledObserver)

        viewModel.passwordConfirmed.value = VALID_PASSWORD

        verify(isSubmitEnabledObserver, only()).onChanged(true)
    }

    @Test
    fun `passwordConfirm update disables submit button when password or password confirm is invalid`() {
        viewModel.password.value = VALID_PASSWORD
        viewModel.passwordConfirmed.value = VALID_PASSWORD
        clearInvocations(isSubmitEnabledObserver)

        viewModel.passwordConfirmed.value = INVALID_PASSWORD

        verify(isSubmitEnabledObserver, only()).onChanged(false)
    }

    @Test
    fun `submit() shows and then doesn't hide the preloader when password successfully changed`() {
        viewModel.setParams(DEFAULT_USER_ID)
        `when`(passwordUpdateUseCase.invoke(anyString(), anyString())).thenReturn(Completable.complete())

        viewModel.preloader.observeForever({ value ->
            println("Value: $value")
        })

        viewModel.submit()

        inOrder(preloaderObserver).apply {
            verify(preloaderObserver).onChanged(true)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `submit() shows and then hides the preloader when there were errors during password change`() {
        `when`(passwordUpdateUseCase.invoke(anyString(), anyString())).thenReturn(Completable.error(TestException()))
        viewModel.setParams(DEFAULT_USER_ID)

        viewModel.submit()

        inOrder(preloaderObserver).apply {
            verify(preloaderObserver).onChanged(true)
            verify(preloaderObserver).onChanged(false)
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `submit() executes password update when password is valid`() {
        val userId = "testId"
        viewModel.setParams(userId)
        viewModel.password.value = VALID_PASSWORD
        `when`(passwordUpdateUseCase.invoke(userId, viewModel.password.value!!)).thenReturn(Completable.complete())

        viewModel.submit()

        verify(passwordUpdateUseCase, only()).invoke(userId, viewModel.password.value!!)
    }

    @Test
    fun `submit() shows error message when there were some errors`() {
        val userId = DEFAULT_USER_ID
        viewModel.setParams(userId)
        viewModel.password.value = INVALID_PASSWORD
        `when`(passwordUpdateUseCase.invoke(anyString(), anyString())).thenReturn(Completable.error(TestException()))

        viewModel.submit()

        verify(messageObserver, only()).onChanged(notNullOrEmpty())
    }

    @Test
    fun `submit() closes the screen when there were no errors`() {
        viewModel.setParams(DEFAULT_USER_ID)
        viewModel.password.value = INVALID_PASSWORD
        `when`(passwordUpdateUseCase.invoke(anyString(), anyString())).thenReturn(Completable.complete())

        viewModel.submit()

        verify(routingObserver, only()).onChanged(argThat { it is Back })
    }


    private fun setupViewModel() {
        appResources = mock()
        passwordUpdateUseCase = mock()
        viewModel = UserPasswordChangeViewModel(passwordUpdateUseCase, appResources, TestAppSchedulers())

        `when`(appResources.getStringResource(R.string.password_validation_message)).thenReturn("Invalid password")
        `when`(appResources.getStringResource(R.string.password_confirmation_validation_message)).thenReturn("Password confirmation differs")
        `when`(appResources.getStringResource(R.string.common_communication_error)).thenReturn("Common communication error")
    }

    private fun setupObservers() {
        passwordObserver = mock()
        passwordConfirmedObserver = mock()
        passwordValidationObserver = mock()
        passwordConfirmedValidationObserver = mock()
        isSubmitEnabledObserver = mock()
        preloaderObserver = mock()
        messageObserver = mock()
        routingObserver = mock()
        viewModel.apply {
            password.observeForever(passwordObserver)
            passwordConfirmed.observeForever(passwordConfirmedObserver)
            passwordValidation.observeForever(passwordValidationObserver)
            passwordConfirmedValidation.observeForever(passwordConfirmedValidationObserver)
            isSubmitEnabled.observeForever(isSubmitEnabledObserver)
            preloader.observeForever(preloaderObserver)
            message.observeForever(messageObserver)
            routing.observeForever(routingObserver)
        }
        clearInvocations(
            passwordObserver,
            passwordConfirmedObserver,
            passwordValidationObserver,
            passwordConfirmedValidationObserver,
            isSubmitEnabledObserver,
            preloaderObserver,
            messageObserver,
            routingObserver
        )
    }

    private fun cleanupObservers() {
        viewModel.password.removeObserver(passwordObserver)
        viewModel.passwordConfirmed.removeObserver(passwordConfirmedObserver)
        viewModel.passwordValidation.removeObserver(passwordValidationObserver)
        viewModel.passwordConfirmedValidation.removeObserver(passwordConfirmedValidationObserver)
        viewModel.isSubmitEnabled.removeObserver(isSubmitEnabledObserver)
        viewModel.preloader.removeObserver(preloaderObserver)
        viewModel.message.removeObserver(messageObserver)
        viewModel.routing.removeObserver(routingObserver)
    }

    companion object {

        private const val VALID_PASSWORD = "V@lid001"
        private const val INVALID_PASSWORD = "short"
        private const val DEFAULT_USER_ID = "a312b3ee-84c2-11eb-8dcd-0242ac130003"

    }
}