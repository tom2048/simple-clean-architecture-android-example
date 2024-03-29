package com.example.simplecleanarchitecture.users.ui.useredit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.core.lib.DefaultTestHelper
import com.example.simplecleanarchitecture.core.lib.TestException
import com.example.simplecleanarchitecture.core.lib.TestHelper
import com.example.simplecleanarchitecture.core.lib.exception.ValidationException
import com.example.simplecleanarchitecture.core.lib.extensions.anyNotNull
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.core.lib.schedulers.TestAppSchedulers
import com.example.simplecleanarchitecture.core.model.User
import com.example.simplecleanarchitecture.users.usecase.user.UserAddAttachmentUseCase
import com.example.simplecleanarchitecture.users.usecase.user.UserGetAttachmentUseCase
import com.example.simplecleanarchitecture.users.usecase.user.UserShowDetailsUseCase
import com.example.simplecleanarchitecture.users.usecase.user.UserUpdateUseCase
import com.github.terrakok.cicerone.Back
import com.github.terrakok.cicerone.Command
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import java.nio.charset.Charset

class UserEditViewModelTest : TestHelper by DefaultTestHelper() {

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: UserEditViewModel

    private lateinit var nicknameObserver: Observer<String>
    private lateinit var nicknameValidationErrorObserver: Observer<String>
    private lateinit var emailObserver: Observer<String>
    private lateinit var emailValidationErrorObserver: Observer<String>
    private lateinit var descriptionValidationErrorObserver: Observer<String>
    private lateinit var avatarObserver: Observer<ByteArray?>
    private lateinit var idScanObserver: Observer<ByteArray?>
    private lateinit var preloaderObserver: Observer<Boolean>
    private lateinit var errorMessageObserver: Observer<String>
    private lateinit var isSubmitEnabledObserver: Observer<Boolean>
    private lateinit var screenRoutingObserver: Observer<Command>

    private lateinit var userShowDetailsUseCase: UserShowDetailsUseCase
    private lateinit var userAddAttachmentUseCase: UserAddAttachmentUseCase
    private lateinit var userGetAttachmentUseCase: UserGetAttachmentUseCase
    private lateinit var userUpdateUseCase: UserUpdateUseCase
    private lateinit var appResources: AppResources

    @Test
    fun `submit() closes the form when there are no errors during save`() {
        //fun `Given a correct result, when submit(), then the form is closed`() {
        `when`(userUpdateUseCase.invoke(anyNotNull())).thenReturn(Completable.complete())

        viewModel.submit()

        verify(screenRoutingObserver, only()).onChanged(isA(Back::class.java))
    }

    @Test
    fun `submit() doesn't close the form when there is an error during save`() {
        //fun `Given an error result, when submit(), then the form is not closed`() {
        `when`(userUpdateUseCase.invoke(anyNotNull())).thenReturn(
            Completable.error(
                ValidationException(listOf(Pair("test", "test")))
            )
        )

        viewModel.submit()

        verify(screenRoutingObserver, never()).onChanged(anyNotNull())
    }

    @Test
    fun `submit() displays an error message when there is an error during save`() {
        //fun `Given an error result, when submit(), then the error message is displayed`() {
        `when`(userUpdateUseCase.invoke(anyNotNull())).thenReturn(
            Completable.error(
                ValidationException(listOf(Pair("test", "test")))
            )
        )

        viewModel.submit()

        verify(errorMessageObserver, only()).onChanged(argThat { !it.isNullOrEmpty() })
    }

    @Test
    fun `submit() invokes update use case when there are correct user details set`() {
        //fun `Given an correct user details, when submit(), then user object is properly created`() {
        `when`(userUpdateUseCase.invoke(anyNotNull())).thenReturn(Completable.complete())
        viewModel.email.value = DEFAULT_USER.email
        viewModel.nickname.value = DEFAULT_USER.nickname

        viewModel.submit()

        verify(userUpdateUseCase, only()).invoke(anyNotNull())
    }

    @Test
    fun `setting nickname shows validation message when the nickname is invalid`() {
        //fun `Given an invalid nickname, when nickname is set, then nickname validation message is displayed`() {
        viewModel.nickname.value = INVALID_NICKNAME

        verify(nicknameValidationErrorObserver, only()).onChanged(argThat { !it.isNullOrEmpty() })
    }

    @Test
    fun `setting email shows validation message when the email is invalid`() {
        //fun `Given an invalid email, when email is set, then email validation message is displayed`() {
        viewModel.email.value = INVALID_EMAIL

        verify(emailValidationErrorObserver, only()).onChanged(argThat { !it.isNullOrEmpty() })
    }

    @Test
    fun `setting description shows validation error message when the description is invalid`() {
        //fun `Given an invalid description, when description is set, then description validation message is displayed`() {
        viewModel.description.value = INVALID_DESCRIPTION

        verify(
            descriptionValidationErrorObserver,
            only()
        ).onChanged(argThat { !it.isNullOrEmpty() })
    }

    @Test
    fun `setting all values enables the submit button when values are valid`() {
        //fun `Given valid nickname, email and description, when values are set, then submit button is enabled`() {
        viewModel.nickname.value = VALID_NICKNAME
        viewModel.email.value = VALID_EMAIL
        viewModel.description.value = VALID_DESCRIPTION

        inOrder(isSubmitEnabledObserver).apply {
            verify(isSubmitEnabledObserver, times(2)).onChanged(false)
            verify(isSubmitEnabledObserver, times(1)).onChanged(true)
        }
    }

    @Test
    fun `loadDetails() loads user data when the data is properly loaded`() {
        //fun `Given non empty user id and correct result, when loadDetails(), then user data is passed to form`() {
        `when`(userShowDetailsUseCase.invoke(anyNotNull())).thenReturn(Single.just(DEFAULT_USER))

        viewModel.loadDetails()

        verify(nicknameObserver).onChanged(DEFAULT_USER.nickname)
        verify(emailObserver).onChanged(DEFAULT_USER.email)
    }

    @Test
    fun `loadDetails() shows and then hides the preloader when the data is properly loaded`() {
        //fun `Given non empty user id and correct result, when loadDetails(), then preloader is set to visible and then to gone`() {
        `when`(userShowDetailsUseCase.invoke(anyNotNull())).thenReturn(Single.just(DEFAULT_USER))

        viewModel.loadDetails()

        inOrder(preloaderObserver).apply {
            verify(preloaderObserver).onChanged(true)
            verify(preloaderObserver).onChanged(false)
        }
    }

    @Test
    fun `loadDetails() shows and then hides the preloader when there is an error while loading data`() {
        //fun `Given non empty user id and error result, when loadDetails(), then preloader is set to visible and then to gone`() {
        `when`(userShowDetailsUseCase.invoke(anyNotNull())).thenReturn(Single.error(TestException()))

        viewModel.loadDetails()

        inOrder(preloaderObserver).apply {
            verify(preloaderObserver).onChanged(true)
            verify(preloaderObserver).onChanged(false)
        }
    }

    @Test
    fun `addAvatar() adds the image to the assets when correct data provided`() {
        val imageBytes = "data".toByteArray(Charset.defaultCharset())
        `when`(
            userAddAttachmentUseCase.invoke(
                anyNotNull(),
                anyNotNull(),
                anyNotNull()
            )
        ).thenReturn(Single.just("KEY"))
        `when`(userGetAttachmentUseCase.invoke(anyNotNull())).thenReturn(Single.just(imageBytes))

        viewModel.addAvatar("test")

        verify(avatarObserver).onChanged(imageBytes)
    }

    @Test
    fun `addIdScan() adds the image to the assets when correct data provided`() {
        val imageBytes = "data".toByteArray(Charset.defaultCharset())
        `when`(
            userAddAttachmentUseCase.invoke(
                anyNotNull(),
                anyNotNull(),
                anyNotNull()
            )
        ).thenReturn(Single.just("KEY"))
        `when`(userGetAttachmentUseCase.invoke(anyNotNull())).thenReturn(Single.just(imageBytes))

        viewModel.addIdScan("test")

        verify(idScanObserver).onChanged(imageBytes)
    }

    @Test
    fun `submit() shows the preloader and doesn't hide it when the data is correctly saved`() {
        //fun `Given correct result, when submit(), then preloader is set to visible only`() {
        `when`(userUpdateUseCase.invoke(anyNotNull())).thenReturn(Completable.complete())

        viewModel.submit()

        verify(preloaderObserver, only()).onChanged(true)
    }

    @Test
    fun `submit() shows and then hides preloader when there is an error while loading the data`() {
        //fun `Given error result, when submit(), then preloader is set to visible and then to gone`() {
        `when`(userUpdateUseCase.invoke(anyNotNull())).thenReturn(Completable.error(TestException()))

        viewModel.submit()

        inOrder(preloaderObserver).apply {
            verify(preloaderObserver).onChanged(true)
            verify(preloaderObserver).onChanged(false)
        }
    }

    @Test
    fun `cancel() closes screen`() {
        viewModel.cancel()

        verify(screenRoutingObserver).onChanged(isA(Back::class.java))
    }

    @Before
    fun setUp() {
        prepareLifecycle()

        userShowDetailsUseCase = mock()
        userAddAttachmentUseCase = mock()
        userGetAttachmentUseCase = mock()
        userUpdateUseCase = mock()
        appResources = mock()

        viewModel = UserEditViewModel(
            DEFAULT_USER.id!!,
            SavedStateHandle(),
            userShowDetailsUseCase,
            userAddAttachmentUseCase,
            userGetAttachmentUseCase,
            userUpdateUseCase,
            appResources,
            TestAppSchedulers()
        )

        `when`(appResources.getStringResource(R.string.nickname_validation_message)).thenReturn("Validation error")
        `when`(appResources.getStringResource(R.string.email_validation_message)).thenReturn("Validation error")
        `when`(appResources.getStringResource(R.string.description_validation_message)).thenReturn("Validation error")

        nicknameObserver = viewModel.nickname.mockObserver(true)
        nicknameValidationErrorObserver = viewModel.nicknameValidationError.mockObserver(true)
        emailObserver = viewModel.email.mockObserver(true)
        emailValidationErrorObserver = viewModel.emailValidationError.mockObserver(true)
        descriptionValidationErrorObserver = viewModel.descriptionValidationError.mockObserver(true)
        avatarObserver = viewModel.avatar.mockObserver(true)
        idScanObserver = viewModel.idScan.mockObserver(true)

        preloaderObserver = viewModel.preloader.mockObserver(true)
        errorMessageObserver = viewModel.errorMessage.mockObserver(true)
        isSubmitEnabledObserver = viewModel.isSubmitEnabled.mockObserver(true)
        screenRoutingObserver = viewModel.screenRouting.mockObserver(true)
    }

    @After
    fun tearDown() {
        cleanUpLifecycle()

        invokeViewModelOnCleared(viewModel)
    }

    companion object {
        private val DEFAULT_USER =
            User("a312b3ee-84c2-11eb-8dcd-0242ac130003", "Testnick", "test@test.com", "")
        private const val VALID_NICKNAME = "Nick1"
        private const val INVALID_NICKNAME = "TooLongNicknameOfTheUser"
        private const val VALID_EMAIL = "test@test.com"
        private const val INVALID_EMAIL = "test@test"
        private const val VALID_DESCRIPTION = "Test description"
        private const val INVALID_DESCRIPTION = "@test@"
    }
}