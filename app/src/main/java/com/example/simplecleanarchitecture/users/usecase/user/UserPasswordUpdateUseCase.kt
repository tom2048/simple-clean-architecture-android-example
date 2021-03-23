package com.example.simplecleanarchitecture.users.usecase.user

import com.example.simplecleanarchitecture.core.repository.UsersRepository
import io.reactivex.rxjava3.core.Completable

interface UserPasswordUpdateUseCase : (String, String) -> Completable

class UserPasswordUpdateUseCaseDefault(private val usersRepository: UsersRepository) : UserPasswordUpdateUseCase {

    // No unit tests for simple getters
    override fun invoke(userId: String, password: String): Completable = usersRepository.updatePassword(userId, password)

}