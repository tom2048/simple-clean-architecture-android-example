package com.example.simplecleanarchitecture.users.usecase.user

import com.example.simplecleanarchitecture.core.lib.TestException
import com.example.simplecleanarchitecture.core.lib.extensions.anyNotNull
import com.example.simplecleanarchitecture.core.model.User
import com.example.simplecleanarchitecture.core.repository.AssetsRepository
import com.example.simplecleanarchitecture.core.repository.UsersRepository
import com.example.simplecleanarchitecture.core.repository.model.UserDetails
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.only
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`

class UserShowDetailsUseCaseDefaultTest {

    private lateinit var userDetailsUseCase: UserShowDetailsUseCaseDefault

    private lateinit var usersRepository: UsersRepository
    private lateinit var assetsRepository: AssetsRepository

    @Test
    fun `invoke() merges user details and assets when user exists`() {
        val expected = User(DEFAULT_USER.id, DEFAULT_USER.nickname, DEFAULT_USER.email, DEFAULT_USER.description, byteArrayOf())
        `when`(usersRepository.get(anyNotNull())).thenReturn(Observable.just(DEFAULT_USER))
        `when`(assetsRepository.getImage(anyNotNull())).thenReturn(Single.just(expected.photo))

        val testObserver = userDetailsUseCase(DEFAULT_USER.id!!).test()

        testObserver.assertComplete()
            .assertValue(expected)
            .dispose()
        verify(usersRepository, only()).get(anyNotNull())
        verify(assetsRepository, only()).getImage(anyNotNull())
    }

    @Test
    fun `invoke() don't load assets when user don't exists`() {
        val error = TestException()
        `when`(usersRepository.get(anyNotNull())).thenReturn(Observable.error(error))
        `when`(assetsRepository.getImage(anyNotNull())).thenReturn(Single.just(byteArrayOf()))

        val testObserver = userDetailsUseCase(DEFAULT_USER.id!!).test()

        testObserver.assertError(error)
            .dispose()
        verify(usersRepository, only()).get(anyNotNull())
        verify(assetsRepository, never()).getImage(anyNotNull())
    }

    @Test
    fun `invoke() doesn't result with error when user exists and photo doesn't`() {
        val expected = User(DEFAULT_USER.id, DEFAULT_USER.nickname, DEFAULT_USER.email, DEFAULT_USER.description, null)
        `when`(usersRepository.get(anyNotNull())).thenReturn(Observable.just(DEFAULT_USER))
        `when`(assetsRepository.getImage(anyNotNull())).thenReturn(Single.error(TestException()))

        val testObserver = userDetailsUseCase(DEFAULT_USER.id!!).test()

        testObserver.assertComplete()
            .assertValue(expected)
            .dispose()

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
        userDetailsUseCase = UserShowDetailsUseCaseDefault(usersRepository, assetsRepository)
    }

    private fun cleanupUseCase() {
        // TODO:
    }

    companion object {
        private val DEFAULT_USER = UserDetails("a312b3ee-84c2-11eb-8dcd-0242ac130003", "Testnick", "test@test.com", "Test description")
    }
}