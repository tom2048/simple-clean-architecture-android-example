package com.example.simplecleanarchitecture.users.usecase.user

import com.example.simplecleanarchitecture.core.model.User
import com.example.simplecleanarchitecture.core.repository.UsersRepository
import com.example.simplecleanarchitecture.core.repository.model.UserDetails
import io.reactivex.rxjava3.core.Single

interface UserShowListUseCase : () -> Single<List<UserDetails>>

class UserShowListUseCaseDefault(private val usersRepository: UsersRepository) : UserShowListUseCase {

    // No unit tests for simple getters
    override fun invoke(): Single<List<UserDetails>> = usersRepository
        .getList()
        .firstOrError()

}