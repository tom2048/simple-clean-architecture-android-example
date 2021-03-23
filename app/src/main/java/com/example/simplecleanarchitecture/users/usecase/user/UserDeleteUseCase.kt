package com.example.simplecleanarchitecture.users.usecase.user

import com.example.simplecleanarchitecture.core.repository.UsersRepository
import io.reactivex.rxjava3.core.Completable

interface UserDeleteUseCase : (String) -> Completable

class UserDeleteUseCaseDefault(private val usersRepository: UsersRepository) : UserDeleteUseCase {

    // No unit tests for simple getters
    override fun invoke(id: String): Completable = usersRepository.delete(id)

}