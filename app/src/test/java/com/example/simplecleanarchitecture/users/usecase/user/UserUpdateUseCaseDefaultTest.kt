package com.example.simplecleanarchitecture.users.usecase.user

import com.example.simplecleanarchitecture.core.lib.extensions.anyNotNull
import com.example.simplecleanarchitecture.core.lib.resources.AppResources
import com.example.simplecleanarchitecture.core.model.User
import com.example.simplecleanarchitecture.core.repository.AssetsRepository
import com.example.simplecleanarchitecture.core.repository.UsersRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.only
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`

class UserUpdateUseCaseDefaultTest {

    private lateinit var userUpdateUseCase: UserUpdateUseCase

    private lateinit var usersRepository: UsersRepository
    private lateinit var assetsRepository: AssetsRepository
    private lateinit var appResources: AppResources

    @Test
    fun `invoke() executes insert when empty user id`() {
        `when`(usersRepository.insert(anyNotNull())).thenReturn(Single.just(DEFAULT_USER.id))
        `when`(usersRepository.update(anyNotNull())).thenReturn(Completable.complete())
        val newUser = DEFAULT_USER.copy(id = null)

        val testObserver = userUpdateUseCase(newUser).test()

        testObserver.assertComplete()
            .dispose()
        verify(usersRepository, only()).insert(anyNotNull())
        verify(usersRepository, never()).update(anyNotNull())
    }

    @Test
    fun `invoke() executes update when empty user id`() {
        val existingUser = DEFAULT_USER

        val testObserver = userUpdateUseCase(existingUser).test()

        testObserver.assertComplete()
            .dispose()
        verify(usersRepository, never()).insert(anyNotNull())
        verify(usersRepository, only()).update(anyNotNull())
    }

    @Test
    fun `invoke() saves photo when avatar not null and new user added`() {
        val newUser = DEFAULT_USER.copy(id = null, photo = byteArrayOf())

        val testObserver = userUpdateUseCase(newUser).test()

        testObserver.assertComplete()
            .dispose()
        verify(assetsRepository, only()).saveImage(anyNotNull(), anyNotNull())
    }

    @Test
    fun `invoke() saves photo when avatar not null and existing user edited`() {
        val existingUser = DEFAULT_USER.copy(photo = byteArrayOf())

        val testObserver = userUpdateUseCase(existingUser).test()

        testObserver.assertComplete()
            .dispose()
        verify(assetsRepository, only()).saveImage(anyNotNull(), anyNotNull())
    }


    @Before
    fun setUp() {
        setupUseCase()
    }

    @After
    fun tearDown() {
        cleanupUseCase()
    }

    private fun setupUseCase() {
        usersRepository = mock()
        assetsRepository = mock()
        appResources = mock()
        userUpdateUseCase = UserUpdateUseCaseDefault(usersRepository, assetsRepository, appResources)

        `when`(usersRepository.insert(anyNotNull())).thenReturn(Single.just(DEFAULT_USER.id))
        `when`(usersRepository.update(anyNotNull())).thenReturn(Completable.complete())
        `when`(assetsRepository.saveImage(anyNotNull(), anyNotNull())).thenReturn(Completable.complete())
    }

    private fun cleanupUseCase() {
        // TODO:
    }

    companion object {
        private val DEFAULT_USER = User("a312b3ee-84c2-11eb-8dcd-0242ac130003", "Testnick", "test@test.com", "Test description")
    }
}