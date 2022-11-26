package com.example.simplecleanarchitecture.users.ui.userlist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.simplecleanarchitecture.R
import com.example.simplecleanarchitecture.RouterScreen
import com.example.simplecleanarchitecture.core.lib.DefaultTestHelper
import com.example.simplecleanarchitecture.core.lib.TestException
import com.example.simplecleanarchitecture.core.lib.TestHelper
import com.example.simplecleanarchitecture.core.lib.extensions.anyNotNull
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.core.lib.schedulers.TestAppSchedulers
import com.example.simplecleanarchitecture.core.repository.model.UserDetails
import com.example.simplecleanarchitecture.users.usecase.user.UserDeleteUseCase
import com.example.simplecleanarchitecture.users.usecase.user.UserShowListUseCase
import com.github.terrakok.cicerone.Command
import com.github.terrakok.cicerone.Forward
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

class UserListViewModelTest : TestHelper by DefaultTestHelper() {

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    //@Rule
    //@JvmField
    //var rxSchedulerRule = InstantRxSchedulerRule()

    private lateinit var viewModel: UserListViewModel

    private lateinit var userShowListUseCase: UserShowListUseCase
    private lateinit var userDeleteUseCase: UserDeleteUseCase

    private lateinit var appResources: AppResources

    private lateinit var userListObserver: Observer<List<UserListItem>>
    private lateinit var messageObserver: Observer<String>
    private lateinit var userActionConfirmationObserver: Observer<String>
    private lateinit var preloaderObserver: Observer<Boolean>
    private lateinit var routingObserver: Observer<Command>

    @Test
    fun `loadUsers() provides the list when the proper data is loaded`() {
        //fun `Given the proper result, when loadUsers(), then the list is prepared for the view`() {
        val expectedResult = DEFAULT_USER_LIST.map { UserListItem(it) }
        `when`(userShowListUseCase.invoke()).thenReturn(Single.just(DEFAULT_USER_LIST))

        viewModel.loadUsers()

        verify(userListObserver, only()).onChanged(argThat { it.equals(DEFAULT_USER_LIST) })
    }

    @Test
    fun `loadUsers() shows the error dialog when there is an error while loading the data`() {
        //fun `Given the error result, when loadUsers(), then the error screen is visible`() {
        `when`(userShowListUseCase.invoke()).thenReturn(Single.error(TestException()))

        viewModel.loadUsers()

        verify(userListObserver, never()).onChanged(anyNotNull())
        verify(messageObserver, only()).onChanged(argThat { !it.isNullOrEmpty() })
    }

    @Test
    fun `loadUsers() shows and then hides the preloader when the proper data is being loaded`() {
        //fun `Given any proper result, when loadUsers(), then preloader is set to on and then off`() {
        `when`(userShowListUseCase.invoke()).thenReturn(Single.just(DEFAULT_USER_LIST))

        viewModel.loadUsers()

        inOrder(preloaderObserver).apply {
            verify(preloaderObserver).onChanged(true)
            verify(preloaderObserver).onChanged(false)
        }
    }

    @Test
    fun `loadUsers() shows and then hides the preloader when there is an error while loading the data`() {
        //fun `Given an error result, when loadUsers(), then preloader is set to on and then off`() {
        `when`(userShowListUseCase.invoke()).thenReturn(Single.error(TestException()))

        viewModel.loadUsers()

        inOrder(preloaderObserver).apply {
            verify(preloaderObserver).onChanged(true)
            verify(preloaderObserver).onChanged(false)
        }
    }

    @Test
    fun `addNewUser() opens the user edit form`() {
        //fun `Given any setup, when addNewUser(), then edit user form is opened`() {
        viewModel.addNewUser()

        verify(routingObserver, only()).onChanged(argThat { it is Forward && it.screen is RouterScreen.UserEditScreen })
    }

    @Test
    fun `Given user id, when editUser(), then edit user form is opened`() {
        //fun `Given user id, when editUser(), then edit user form is opened`() {
        viewModel.editUser(DEFAULT_USER_LIST.first().id!!)

        verify(routingObserver, only()).onChanged(argThat {
            it is Forward &&
                    it.screen is RouterScreen.UserEditScreen &&
                    (it.screen as RouterScreen.UserEditScreen).id == DEFAULT_USER_LIST.first().id
        })
    }

    @Test
    fun `deleteUser() shows the confirmation dialog`() {
        //fun `Given user id, when deleteUser(), then confirmation dialog regarding this id is shown`() {
        viewModel.deleteUser(DEFAULT_USER_LIST.first().id!!)

        verify(userActionConfirmationObserver, only()).onChanged(DEFAULT_USER_LIST.first().id!!)
    }

    @Test
    fun `deleteUserConfirmed() shows the confirmation message when the user is deleted`() {
        //fun `Given correct result, when deleteUserConfirmed(), then confirmation message is displayed`() {
        `when`(userDeleteUseCase.invoke(anyNotNull())).thenReturn(Completable.complete())
        `when`(userShowListUseCase.invoke()).thenReturn(Single.just(DEFAULT_USER_LIST))

        viewModel.deleteUserConfirmed(DEFAULT_USER_LIST.first().id!!)

        verify(messageObserver, only()).onChanged(argThat { !it.isNullOrEmpty() })
    }

    @Test
    fun `deleteUserConfirmed() updates the list when user is deleted`() {
        //fun `Given correct result, when deleteUserConfirmed(), then user list is updated`() {
        `when`(userDeleteUseCase.invoke(anyNotNull())).thenReturn(Completable.complete())
        `when`(userShowListUseCase.invoke()).thenReturn(Single.just(DEFAULT_USER_LIST.subList(1, 2)))

        viewModel.deleteUserConfirmed(DEFAULT_USER_LIST.first().id!!)

        verify(userListObserver, only()).onChanged(argThat { it.equals(DEFAULT_USER_LIST.subList(1, 2)) })
    }

    @Test
    fun `deleteUserConfirmed() shows and then hides the preloader when user is properly deleted`() {
        //fun `Given correct result, when deleteUserConfirmed(), then preloader is set to on and then off`() {
        `when`(userDeleteUseCase.invoke(anyNotNull())).thenReturn(Completable.complete())
        `when`(userShowListUseCase.invoke()).thenReturn(Single.just(DEFAULT_USER_LIST))

        viewModel.deleteUserConfirmed(DEFAULT_USER_LIST.first().id!!)

        inOrder(preloaderObserver).apply {
            verify(preloaderObserver).onChanged(true)
            verify(preloaderObserver).onChanged(false)
        }
    }

    @Test
    fun `deleteUserConfirmed() shows and then hides the preloader when there was an error while deleting the user`() {
        //fun `Given error result, when deleteUserConfirmed(), then preloader is set to on and then off`() {
        `when`(userDeleteUseCase.invoke(anyNotNull())).thenReturn(Completable.error(TestException()))
        `when`(userShowListUseCase.invoke()).thenReturn(Single.just(DEFAULT_USER_LIST))

        viewModel.deleteUserConfirmed(DEFAULT_USER_LIST.first().id!!)

        inOrder(preloaderObserver).apply {
            verify(preloaderObserver).onChanged(true)
            verify(preloaderObserver).onChanged(false)
        }
    }

    @Test
    fun `deleteUserConfirmed() displays an error message when there was an error while deleting the user`() {
        //fun `Given error result, when deleteUserConfirmed(), then error message is displayed`() {
        `when`(userDeleteUseCase.invoke(anyNotNull())).thenReturn(Completable.error(TestException()))
        `when`(userShowListUseCase.invoke()).thenReturn(Single.just(DEFAULT_USER_LIST))

        viewModel.deleteUserConfirmed(DEFAULT_USER_LIST.first().id!!)

        verify(messageObserver, only()).onChanged(argThat { !it.isNullOrEmpty() })
    }

    @Test
    fun `changeUserPassword() opens change password screen`() {
        viewModel.changeUserPassword(DEFAULT_USER_LIST.first().id!!)

        verify(routingObserver, times(1)).onChanged(Forward(RouterScreen.UserPasswordChangeScreen(DEFAULT_USER_LIST.first().id!!), true))
    }

    @Before
    fun setUp() {
        prepareLifecycle()
        userShowListUseCase = mock()
        userDeleteUseCase = mock()
        appResources = mock()

        viewModel = UserListViewModel(userShowListUseCase, userDeleteUseCase, appResources, TestAppSchedulers())

        `when`(appResources.getStringResource(R.string.common_communication_error)).thenReturn("Test error message.")
        `when`(appResources.getStringResource(R.string.user_delete_success_message)).thenReturn("User deleted.")

        userListObserver = viewModel.userList.mockObserver(true)
        messageObserver = viewModel.message.mockObserver(true)
        userActionConfirmationObserver = viewModel.userActionConfirmation.mockObserver(true)
        preloaderObserver = viewModel.preloader.mockObserver(true)
        routingObserver = viewModel.routing.mockObserver(true)
    }

    @After
    fun tearDown() {
        cleanUpLifecycle()
        invokeViewModelOnCleared(viewModel)
    }

    companion object {
        private val DEFAULT_USER_LIST = listOf(
            UserDetails("a312b3ee-84c2-11eb-8dcd-0242ac130003", "Nickname1", "nickname1@test.com", "Test description 1"),
            UserDetails("3b04aacf-4320-48bb-8171-af512aae0894", "Nickname2", "nickname2@test.com", "Test description 1"),
            UserDetails("52408bc4-4cdf-49ef-ac54-364bfde3fbf0", "Nickname3", "nickname3@test.com", "Test description 1")
        )
    }
}